/**
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved. NOTICE: All
 * information contained herein is, and remains the property of ClockReplay
 * Incorporated and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from ClockReplay Incorporated.
 */

package com.crp.ether;

import java.util.concurrent.atomic.AtomicLong;

import com.crp.common.CRPException;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE;
import com.crp.common.GLOBAL_ENUMS.ETHER_ELEMENT_TYPE;
import com.crp.ether.Channel.CommunicatorType;
import com.crp.interfaces.ConnectionInterface;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;

/**
 * pool of photon objects.
 * at most 64 photon objects can be pooled.
 * all the photons are stored in a array(fixed size, 64).
 * use a atomiclong to update the index for photons in the array.
 * @author hpoduri
 * @version $Id$
 */
public final class PhotonPool
{
    /**
     * index for photons. we can maintain at most 64 photons.
     * each bit in the long variable represents index for the photon
     * in the photonList.
     */
    private static AtomicLong phPoolIndex = null;
    
    /**
     * list of photons for reuse.
     */
    private static Photon [] phPool = null;
    
    /**
     * handles all the initial handshake and spits out a photon obj.
     * @param crps service to connect to.
     * @param forTesting back door entry to photon creation, used for
     * testing purposes.
     * @return photon object, on error null.
     * @throws EtherException on error.
     */
    public static Photon createPhoton(
        final CRPServiceInfo crps, 
        final boolean forTesting) throws EtherException
    {
        Photon ph = null;
        if( PROCESS_GLOBALS.isConfiguredOnThisProcess(crps.getName())
                && !crps.isForcedRemoteConnection())
        {
            ph = createPhotonHelper(GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.LOCAL);
        }
        else
        {
            ph = createPhotonHelper(GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE);
        }
        ph.initializeClient();
        ph.connect(crps, CommunicatorType.SOURCE);
        //get a connection message from the current thread.
        Message m = null;
        if(!forTesting)
        {
            m = CRPThread.getCurrentThread().getMyMsgWorld().getFreeMsg(
                GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CONNECTION_KEY);
            m.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_LOCAL_NEW_CONNECTION);
            ConnectionInterface ci = (
                    ConnectionInterface) (m.getPayloadMBO().getObject());
            ci.setSrcAndDestServices(
                CRPThread.getCurrentThread().getServiceName(), crps.getName());
            ci.setSrcAndDestThreads(CRPThread.getCurrentThreadID(),
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
        }
        else
        {
            m = new Message(MESSAGE_CODE.MSG_LOCAL_NEW_CONNECTION);
            m.setForTesting();
            ConnectionInterface ci = new ConnectionInterface();
            ci.setSrcAndDestServices(
                //for testing.
                GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_AGENT_SERVICE,
                crps.getName());
            ci.setSrcAndDestThreads(
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID,
                GLOBAL_CONSTANTS.INVALID_THREAD_ID);
            m.setTestObject(ci);
        }
        m.setPhoton(ph);
        if(ph.isLocal())
        {
            //first send this message to service handler.
            //TODO : what if this message fails??
            try
            {    
                CRPThreadPoolManager.getCRPThread(
                    PROCESS_GLOBALS.SERVICE_HANDLER_THREAD_ID).pushCtrlMessage(
                        m);
            }
            catch (CRPException e)
            {
                CommonLogger.CMN_LOG.error(e.getMessage());
                throw new EtherException("CRP_ETHERM_ERROR_007", null);
            }
        }
        else
        {
            m.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_REMOTE_NEW_CONNECTION);
            ph.bsendMessage(m);
        }
        return ph;
    }
    
    /**
     * closes photon connection.
     * @param ph photon obj to be closed.
     * @param forTesting used for testing.
     * @throws EtherException  on error.
     */
    public static void closePhoton(final Photon ph,
        final boolean forTesting) throws EtherException
    {
        
        Message m = null;
        if(ph.isShortCircuited())
        {
            // dont have to the handshake.
            ph.close();
            returnPhotonToPool(ph);
            return;
        }
        if(!forTesting)
        {
            m = CRPThread.getCurrentThread().getMyMsgWorld().getFreeMsg(
                GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
            m.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_CLOSE_CONNECTION);
            
        }
        else
        {
            m = new Message(MESSAGE_CODE.MSG_CLOSE_CONNECTION);
            m.setForTesting();
        }
        ph.sendMessageNoFail(m);
        // as it is a connection message, we dont want it to be buffered.
        ph.getChannel().flushChannel();
        //finally close the connection.
        ph.close();
        if(ph.isLocal())
        {
            // handle the local photons as a special case; here is why:
            // if we close the local photon on one side, and the photon has still
            // some messages on its local queue, the photon should not be added to the pool
            // simply bcoz, another guy asks for a photon, the pool may return the recent
            // photon added, there by making the status reset.(so all the pending messages in
            // the photon are gone.
            LocalChannel lc= (LocalChannel)ph.getChannel();
        }
        returnPhotonToPool(ph);
    }
    
    /**
     * updates the photon pool index with status to closed.
     * this essentially makes this photon reusable.
     * @param ph input photon.
     */
    public static void returnPhotonToPool(final Photon ph)
    {
        //TODO : 
        
        // for now do the easy thing. go over the list of photons
        // sequentially.
        for(int i = 0; i < 8* GLOBAL_CONSTANTS.LONG_SIZE; i++)
        {
            if(phPool[i] == ph)
            {
                // update the index.
                long oldVal = 0;
                long newVal = 0;
                do
                {
                    oldVal = phPoolIndex.get();      
                    newVal = oldVal & ~(1 << i);
                } while (!phPoolIndex.compareAndSet(oldVal, newVal));
                return;
            }
        }
        
    }

    /**
     * short circuits the initial hand shake message.
     * creates a photon. useful for non  streaming connection
     * send/recv small number of quick messages.
     * @param crps service to which you want to connect to.
     * however, in this case we should always give the
     * service handler to connect to, not a service that
     * the service handler is hosting.
     * @return photon object.
     * @throws EtherException on error.
     */
    public static Photon createShortCircuitedPhoton(
        final CRPServiceInfo crps) throws EtherException
    {
        Photon ph = null;
        if( PROCESS_GLOBALS.isConfiguredOnThisProcess(crps.getName())
                && !crps.isForcedRemoteConnection())
        {
            ph = createPhotonHelper(GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.LOCAL);
        }
        else
        {
            ph = createPhotonHelper(GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE);
        }
        ph.initializeClient();
        // make it a sink, as it is expected to receive a message from the
        // local channel.
        ph.connect(crps, CommunicatorType.SINK);
        ph.setShortCircuited();
        return ph;
    }
    
    /**
     * sends a quick message, returns the photon.
     * @param crps service to connect to.
     * @param m message to be passed to the service.
     * @return photon object(short circuited)
     * @throws EtherException on error.
     */
    public static Photon sendQuickMessage(final CRPServiceInfo crps,
        final Message m) throws EtherException
    {
        Photon ph = null;
        ph = createShortCircuitedPhoton(crps);
        m.setPhoton(ph);
        if(ph.isLocal())
        {
            if(crps.getName() 
                    == GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE)
            {
                try
                {
                    CommonLogger.CMN_LOG.info("before sending quick msg: " );
                    CRPThreadPoolManager.getCRPThread(
                        PROCESS_GLOBALS.SERVICE_HANDLER_THREAD_ID).pushCtrlMessage(
                            m);
                }
                catch (CRPException e)
                {
                    CommonLogger.CMN_LOG.error(e.getMessage());
                    throw new EtherException("CRP_ETHERM_ERROR_007", null);
                }
            }
        }
        else
        {
            ph.sendMessageNoFail(m);
        }
        return ph;
    }
    
    /**
     * helper function to create a raw photon.
     * @param cType what type of photon you want.
     * @return Photon Object.
     * @throws EtherException on error.
     */
    public static final Photon createPhotonHelper(
        final GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE cType) throws EtherException
    {
        Photon ph = null;
        
        if(cType == ETHER_COMMUNICATION_TYPE.LOCAL)
        {
            // first check if any photon available in the pool.
            ph = getPhotonFromPool(ETHER_COMMUNICATION_TYPE.LOCAL);
            if(ph == null)
            {
                //use local connection.
                ph = new Photon(
                    ETHER_ELEMENT_TYPE.CLIENT, ETHER_COMMUNICATION_TYPE.LOCAL);
            }
            else
            {
                // dont forget to reset the photon.
                // we dont want the history troubling us.
                ph.reset();
                return ph;
            }
        }
        else
        {
            ph = getPhotonFromPool(ETHER_COMMUNICATION_TYPE.REMOTE);
            if(ph == null)
            {
                ph = new Photon(
                    ETHER_ELEMENT_TYPE.CLIENT, ETHER_COMMUNICATION_TYPE.REMOTE);
            }
            else
            {
                // dont forget to reset the photon.
                // we dont want the history troubling us.
                ph.reset();
                return ph;
            }
        }
        addPhotonToPool(ph);
        return ph;
    }

    /**
     * add the photon to the photon pool.
     * @param ph photon object to be update to the index.
     * @return true/false whether or not photon added to pool.
     */
    private static boolean addPhotonToPool(final Photon ph)
    {
        if(phPool == null)
        {
            phPool = new  Photon [GLOBAL_CONSTANTS.LONG_SIZE];
        }
        if(phPoolIndex == null)
        {
            phPoolIndex = new AtomicLong(0);
        }
        if(phPoolIndex.get() == 0xffffffffffffffffL)
        {
            return false;
        }
        long newVal = 0;
        long oldVal = 0;
        int index = 0;
        do
        {
            oldVal = phPoolIndex.get();
            index = 0;
            while(index < 8*GLOBAL_CONSTANTS.LONG_SIZE)
            {                
                if((oldVal & (1 << index)) != 0)
                {
                    index++;
                    continue;
                }
                else
                {
                    if(phPool[index] == null)
                    {
                        newVal = oldVal | (1 << index);

                        break;
                    }
                    index++;

                }
            }
            if(index == 8*GLOBAL_CONSTANTS.LONG_SIZE)
            {
                // cannot find a free slot in the photon pool.
                return false;
            }
            
        } while (!phPoolIndex.compareAndSet(oldVal, newVal));
        phPool[index] = ph;
        return true;
    }
    
    /**
     * returns a free photon from the list.
     * @param cType communication type, indicates what type of photon
     * to be retrieved from the pool.
     * @return Photon object.
     */
    private static final Photon getPhotonFromPool(
        final GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE cType)
    {
        if(phPool == null)
        {
            phPool = new  Photon [8* GLOBAL_CONSTANTS.LONG_SIZE];
        }
        if(phPoolIndex == null)
        {
            phPoolIndex = new AtomicLong(0);
        }
        if(phPoolIndex.get() == 0xffffffffffffffffL)
        {
            return null;
        }
        
        long newVal = 0;
        long oldVal = 0;
        int index = 0;
        Photon ph = null;
        do
        {
            index = 0;
            oldVal = phPoolIndex.get();
            while(index < 8*GLOBAL_CONSTANTS.LONG_SIZE)
            {                
                if((oldVal & (1 << index)) == 0)
                {
                    ph = phPool[index];
                    if(ph != null)
                    {
                        assert(!ph.isConnected());
                        if(cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.LOCAL)
                        {
                            if(ph.isLocal())
                            {
                                break;
                            }
                        }
                        if(cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE)
                        {
                            if(!ph.isLocal())
                            {
                                break;
                            }
                        }
                    }
                }
                index++;
            }
            if(index == 8 * GLOBAL_CONSTANTS.LONG_SIZE)
            {
                return null;
            }
            newVal = oldVal | (1 << index);
        } while (!phPoolIndex.compareAndSet(oldVal, newVal));
        
        return(phPool[index]);
    }
}
