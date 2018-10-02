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
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Category;

import com.crp.common.CRPLogger;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE;

/**
 * class to represent the message sensor.
 * this class is useful to figure out if there are any messages
 * on the channels subscribed.
 * this can simply be a selector waiting on some network I/O.
 * @author hpoduri
 * @version $Id$
 */
public class RemoteMessageSensor
{
    /**
     * store the photon, socket hash map here.
     * we should be able to go to the right photon
     * on receiving incoming data from a socket.
     */
    private static Map<Socket, Photon> photonSocketMap = 
        new HashMap<Socket, Photon>();  
    
    /**
     * create a separate logger for NetworkChannel.
     */
    public static final Category RMS_LOG = 
        CRPLogger.initializeLogger("com.crp.ether.RemoteMessageSensor");
    
    /**
     * selector java nio.
     */
    private Selector remoteSelector;
    
    /**
     * constructor.
     * @throws IOException on error.
     */
    public RemoteMessageSensor() throws IOException
    {
        remoteSelector = SelectorProvider.provider().openSelector();

    }
    
    /**
     * wake up from select call.
     */
    public final void wakeup()
    {
        if(remoteSelector != null)
        {
            remoteSelector.wakeup();
        }
    }

    /**
     * returns the remote selector.
     * @return selector.
     */
    public final Selector getSelector()
    {
        // TODO Auto-generated method stub
        return remoteSelector;
    }
    
    /**
     * sense for incoming data, process it.
     * @param inpMH input message holder.
     * @return 
     * @throws EtherNetworkException on error.
     */
    public final void receiveIncomingMessages(
        final MessageHolder inpMH) throws EtherNetworkException
    {
        try
        {
            Message m = null;
              
            /**
             * wait max 10 seconds.
             */
            int num = remoteSelector.select(10*1000);
                       
            if (num == 0)
            {
                // fake alarm.
                ;
            }
            // Get the keys corresponding to the activity
            // that has been detected, and process them
            // one by one
            Iterator it = remoteSelector.selectedKeys().iterator();
            while (it.hasNext()) 
            {
                // Get a key representing one of bits of I/O
                // activity
                SelectionKey key = (SelectionKey) it.next();
                it.remove();
                //previously canceled key might throw a surprise.
                if(!key.isValid())
                {
                    continue;
                }
                // What kind of activity is it?
                if ((key.readyOps() & SelectionKey.OP_ACCEPT) 
                        == SelectionKey.OP_ACCEPT)
                {

                    // It's an incoming connection.
                    // Register this socket with the Selector
                    // so we can read for input on it

                    ServerSocketChannel sChannel 
                        = (ServerSocketChannel)key.channel();
                    SocketChannel sc = sChannel.accept();
                    
                    RMS_LOG.info("Received Remote connection from " 
                        + sc.socket());
                    
                    Photon p = null;
                    try
                    {
                        p = PhotonPool.createPhotonHelper(
                            ETHER_COMMUNICATION_TYPE.REMOTE);
                        
                    }
                    catch (EtherException e)
                    {
                        RMS_LOG.error(e.getMessage());
                        throw new EtherNetworkException(
                            "CRP_ETHERM_ERROR_008", null);
                    }
                    
                    //set the java network client channel.
                    ((NetworkChannel)p.getChannel()).setClientSocketChannel(
                        sc);
                    //add to the photon socket map.
                    photonSocketMap.put(sc.socket(), p);
                    
                    //register the new socket for select call.
                    sc.configureBlocking(false);
                    sc.register(remoteSelector, SelectionKey.OP_READ);
                      
                }
                else if ((key.readyOps() & SelectionKey.OP_READ)
                        == SelectionKey.OP_READ )
                {
                    RMS_LOG.info("before reading socket");
                    SocketChannel sc = (SocketChannel) key.channel();
                    
                    //retrieve the photon associated with this socket.
                    Photon p = photonSocketMap.get(sc.socket());
                    
                    assert(p != null);
                    try 
                    {
                        m = p.brecvMessage();
                        
                        // associate photon with this message.
                        m.setPhoton(p);
                        
                        /**
                         * NOTE: now that we got what we wanted, i.e connection
                         *
                         * from remote host to some service hosted by this 
                         * process.
                         * it is now the responsibility of the individual
                         * service handler services to deal with this
                         * connection(photon).
                         */
                        if(m.getMessageHeader().messageCode() 
                                == MESSAGE_CODE.MSG_REMOTE_NEW_CONNECTION)
                        {
                            key.cancel();
                        }
                        
                        sc.configureBlocking(false);
                    }
                    catch(EtherException e)
                    {
                        key.cancel();
                        sc.close();
                        
                        try
                        {
                            if(p != null && p.isShortCircuited())
                            {
                                PhotonPool.closePhoton(p, false);
                            }
                        }
                        catch (EtherException e1)
                        {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        RMS_LOG.error(e.getMessage());
                    }
                    if(m != null)
                    {
                        //add to the holder.
                        inpMH.addMessage(m);
                    }
                }
            }
            
        }
        catch (IOException e)
        {
            RMS_LOG.error(e.getMessage());
            throw new EtherNetworkException("CRP_ETHERM_ERROR_002", null);
        }
    }
    
    /**
     * add a photon to socket map.
     * @param inpPH input photon object.
     */
    public final void addPhotonToMap(final Photon inpPH)
    {
        NetworkChannel nc = (NetworkChannel) inpPH.getChannel();
        photonSocketMap.put(nc.getClientSocket(), inpPH);
    }
    
    public final void removePhotonFromMap(final Photon inpPH)
    {
        NetworkChannel nc = (NetworkChannel) inpPH.getChannel();
        photonSocketMap.remove(nc.getClientSocket());
    }
    /**
     * blocking call, that waits for incoming messages.
     * @throws EtherNetworkException on error.
     */
    public final void waitForMessages() throws EtherNetworkException
    {
        try
        {
            remoteSelector.select();
        }
        catch(IOException e)
        {
            RMS_LOG.error(e.getMessage());
            throw new EtherNetworkException("CRP_ETHERM_ERROR_020", null);
        }
    }
}
