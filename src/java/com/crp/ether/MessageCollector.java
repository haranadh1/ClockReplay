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

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.thread.CRPThread;

/**
 * collect all the messages from all the sources.
 * @author hpoduri
 * @version $Id$
 */
public class MessageCollector
{
    
    /**
     * counter to indicate no of photons.
     */
    private int numOfRemotePhotons;
    /**
     * remote photon list.
     */
    private Photon [] remotePhotonList;
    
    /**
     * counter to indicate num of local photons.
     */
    private int numOfLocalPhotons;
    /**
     * local photon list.
     */
    private ArrayList<Photon> localPhotonList;
    /**
     * sensor to sense messages from all the sources.
     */
    private RemoteMessageSensor mSensor;
    
    /**
     * holder to hold the messages received.
     */
    private MessageHolder mHolder;
    /**
     * constructor.
     * @throws EtherException on error.
     */
    public MessageCollector() throws EtherException
    {
        // create a selector to feed it to the sensor.
        try
        {
            mSensor = new RemoteMessageSensor();
        }
        catch (IOException e)
        {
            RemoteMessageSensor.RMS_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_018", null);
           
        }  
        
        remotePhotonList = new Photon[GLOBAL_CONSTANTS.MAX_PHOTONS_TO_SENSE];
        localPhotonList  = new ArrayList<Photon>(GLOBAL_CONSTANTS.MAX_PHOTONS_TO_SENSE);
        numOfRemotePhotons = numOfLocalPhotons = 0;
        mHolder = new MessageHolder();
    }
    
    /**
     * register photon to the collector.
     * this ensures that any messages for the photon will be
     * collected.
     * @param inpPH photon to be registered.
     * @throws EtherException on error.
     */
    public final void registerPhoton(final Photon inpPH) throws EtherException
    {            
        if( !inpPH.isLocal())
        {
            //do only for remote connections.
            NetworkChannel nc = (NetworkChannel) inpPH.getChannel();
            // now register the photon to the remote message sensor.
            
            if(!nc.registerChannelToSelector(mSensor.getSelector()))
            {
                throw new EtherException("CRP_ETHERM_ERROR_022", null);
            }
            if(nc.getElementType() == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT)
            {
                // dont have to add for listener type photons.
                mSensor.addPhotonToMap(inpPH);
            }
            
            remotePhotonList[numOfRemotePhotons++] = inpPH;
        }
        else
        {
            localPhotonList.add(inpPH);
        }
    }
    
    /**
     * unregister photon from the interest list of this thread.
     * @param inpPH photon to be unregistered
     * @throws EtherException on error.
     */
    public final void unregisterPhoton(final Photon inpPH) throws EtherException
    {
        if(!inpPH.isLocal())
        {
          //do only for remote connections.
            NetworkChannel nc = (NetworkChannel) inpPH.getChannel();
            // now register the photon to the remote message sensor.
            
            if(!nc.unregisterChannelToSelector(mSensor.getSelector()))
            {
                throw new EtherException("CRP_ETHERM_ERROR_021", null);
            }
            if(nc.getElementType() == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT)
            {
                // dont have to add for listener type photons.
                mSensor.removePhotonFromMap(inpPH);
            }
            
            numOfRemotePhotons--;
        }
        else
        {
            for(int i = 0; i < localPhotonList.size(); i++)
            {
                if(localPhotonList.get(i) == inpPH)
                {
                    localPhotonList.remove(i);
                    break;
                }
            }
        }
    }
    /**
     * wait for new incoming messages and return when any.
     * @return message holder object, contains received msgs.
     * @throws EtherException on error.
     */
    public final MessageHolder checkForNewMessages() throws EtherException
    {
        
        try
        {
            mSensor.receiveIncomingMessages(mHolder);
        }
        catch (EtherNetworkException e)
        {
            RemoteMessageSensor.RMS_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_019", null);     
        }
        
        // now deal with the local photons to see if any incoming msgs.
        
        for(int i = 0; i < localPhotonList.size(); i++)
        {
            if(localPhotonList.get(i) == null)
            {
                continue;
            }
            else
            {
                Message m = null;
                while ((m = localPhotonList.get(i).brecvMessage())!= null)
                {  
                    mHolder.addMessage(m);
                }
            }
            if(i == GLOBAL_CONSTANTS.MAX_PHOTONS_TO_SENSE)
            {
                break;
            }
        }
           
        // now deal with the control messages for this thread.
        Message m = null;
        while ( (m = CRPThread.getCurrentThread().popCtrlMessage()) != null )
        {
            mHolder.addMessage(m);
        }
//        RemoteMessageSensor.RMS_LOG.info("Number of Messages after checking: " + String.valueOf(mHolder.size()));
        return mHolder;
    }
    
    /**
     * wakes up message collector.
     * which would go on collecting the messages
     * from the registered photons.
     */
    public final void wakeup()
    {
        mSensor.wakeup();
    }
    
    /**
     * wait for incoming messages.
     * this is a blocking call.
     * @throws EtherException on error.
     */
    public final void waitForIncomingMessages() throws EtherException
    {
        try
        {
            mSensor.waitForMessages();
        }
        catch(EtherNetworkException e)
        {
            RemoteMessageSensor.RMS_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_019", null);
        }
    }
    
    /**
     * returns message holder.
     * @return MessageHolder object.
     */
    public final MessageHolder getMessageHolder()
    {
        return mHolder;
    }
}
