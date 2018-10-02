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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.crp.common.CRPServiceInfo;
import com.crp.common.CRPLogger;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_ENUMS.ETHER_ELEMENT_TYPE;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.thread.CRPThread;


import org.apache.log4j.Category;


/**
 * implements channel interface for network communication.
 * @author hpoduri
 * @version $Id$
 */
public class NetworkChannel implements Channel
{
    /**
     * store the photon, socket hash map here.
     * we should be able to go to the right photon
     * on receiving incoming data from a socket.
     */
    public static Map<Socket, Photon> photonSocketMap = 
        new HashMap<Socket, Photon>();  

    /**
     * network element type. can be listener/client.
     */
    private GLOBAL_ENUMS.ETHER_ELEMENT_TYPE eeType;
    /**
     * port to listen to, only used for network listener.
     */
    private int port;
    /**
     * represents the server channel.
     * only works with neType= LISTENER
     */
    private ServerSocketChannel ssc;
    /**
     * represents client socket channel.
     * only works with neType = CLIENT
     */
    private SocketChannel csc;
    /**
     * selector for the socket.
     */
    private Selector sel;
    /**
     * create a separate logger for NetworkChannel.
     */
    public static final Category NET_LOG = 
        CRPLogger.initializeLogger("com.crp.ether.NetworkChannel");
    /**
     * constructor.
     * @param inpNEType : Network element type; this tells
     *                 how you want to use the network channel.
     * 
     */
    public NetworkChannel( final GLOBAL_ENUMS.ETHER_ELEMENT_TYPE inpNEType)
    {
        eeType = inpNEType;
        ssc = null;
        sel = null;
        csc = null;
    }
    /**
     * constructor, used mainly by the ETHER_ELEMENT_TYPE.CLIENT.
     * @param inpNEType : Network element type; this tells
     *                 how you want to use the network channel.
     * @param inpSC Socket channel on which read/write should be done.
     */
    public NetworkChannel(
        final GLOBAL_ENUMS.ETHER_ELEMENT_TYPE inpNEType,
        final SocketChannel inpSC)
    {
        eeType = inpNEType;
        ssc = null;
        sel = null;
        csc = inpSC;
    }
    
    /**
     * returns client socket.
     * @return socket object.
     */
    public final Socket getClientSocket()
    {
        if(eeType == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT)
        {
            return csc.socket();
        }
        if(eeType == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.LISTENER)
        {
            return null;
        }
        return null;
    }
    /**
     * sets client socket channel.
     * @param inpSC input java socket channel.
     */
    public final void setClientSocketChannel(final SocketChannel inpSC)
    {
        csc = inpSC;
    }
    /**
     * initialize the channel for network server.
     * @param inpPort port on which the server should listen to.
     * @throws EtherException on error.
     */
    public final void initListener(final int inpPort)
        throws EtherException
    {
        assert(eeType == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.LISTENER);
        port = inpPort;
        try 
        {
            ssc = ServerSocketChannel.open();
            //set it to non-blocking, we can use select 
            ssc.configureBlocking(false);
            //get the socket connected to this channel and bind it to the port.
            ssc.socket().bind(new InetSocketAddress(inpPort));
            sel = SelectorProvider.provider().openSelector();
            ssc.register(sel, SelectionKey.OP_ACCEPT);
            NET_LOG.info(PROCESS_GLOBALS.getMyServiceInfoString() 
                + ": Socket Listening on port "
                + String.valueOf(inpPort));
        }
        catch (IOException e)
        {
            NET_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_001", null);
        }
    }
   
    /**
     * initialize the channel for network client.
     * @throws EtherException on error.
     */
    public final void initClient()
        throws EtherException
    {
        assert(eeType == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT);
        try 
        {
            csc = SocketChannel.open();
            csc.configureBlocking(true);
            
            //TODO : non blocking
            /*
             * set it to non-blocking, we can use select 
            csc.configureBlocking(false);
            sel = Selector.open();
            csc.register(sel, SelectionKey.OP_CONNECT 
                | SelectionKey.OP_READ | SelectionKey.OP_WRITE);*/
            NET_LOG.info(PROCESS_GLOBALS.getMyServiceInfoString() 
                + ": client Socket initialized");
        }
        catch (IOException e)
        {
            NET_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_001", null);
        }
    }
    /**
     * @param crps CRP service to connect to.
     * @throws EtherException on error.
     */
    public final void connect(final CRPServiceInfo crps,
        final CommunicatorType ct)
        throws EtherException
    {
        assert(eeType == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT);
        try 
        {
            csc.connect(new InetSocketAddress(crps.getHost(), crps.getPort()));
            NET_LOG.info(PROCESS_GLOBALS.getMyServiceInfoString()
                + ": CONNECTED TO "
                + crps.zipString());
        }
        catch (IOException e)
        {
            NET_LOG.error(e.getMessage());
            throw new EtherException(
                "CRP_ETHERM_ERROR_015", new String [] {crps.zipString()});
        }
    }
    /**
     * blocking send.
     * @param bb ByteBuffer to be sent.
     * @return number of bytes written.
     * @throws EtherException on error while sending.
     */
    public final int blockingSend(final ByteBuffer bb)
        throws EtherException
    {
        int bytesWritten = -1;
        NET_LOG.info("inside nw blocking send : bytes remaining : " + String.valueOf(bb.remaining()));
        assert(csc != null && eeType == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT);
        try
        {
            if (csc != null)
            {
                // means client blocking send
                bb.flip();
                do
                {
                    bytesWritten = csc.write(bb);
                    
                    NET_LOG.info("bytes written: " + String.valueOf(bytesWritten));
                    NET_LOG.info("bytes remaining: " + String.valueOf(bb.remaining()));
                }  while ( bb.remaining() > 0 );
           
            }
            else
            {
                NET_LOG.info("csc is null");
            }
            
        }
        catch (IOException e)
        {
            NET_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_003",
                (new String[] {String.valueOf(bb.limit())}));
        }
        return bytesWritten;
    }
    /**
     * blocking recv.
     * @param bb ByteBuffer where the received data should go.
     * @throws EtherException on error while receiving.
     * @return returns the number of bytes read into the byte buffer.
     * it only reads the bytes that can fit in the byte buffer.
     */
    public final int blockingRecv(final ByteBuffer bb)
        throws EtherException
    {
        assert(csc != null && eeType == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT);
        int bytesRead = -1;
        try
        {
            if (csc != null)
            {
                bytesRead = csc.read(bb);
                if (bytesRead == -1)
                {
                    //close the socket.
                    csc.close();
                    csc = null;
                    throw new EtherException("CRP_ETHERM_ERROR_012", null);
                }
            }
        }
        catch (IOException e)
        {
            NET_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_013",
                (new String[] {String.valueOf(bb.limit())}));
        }
        return bytesRead;
    }
    /**
     * receives incoming data(connection request/message etc.,).
     * @return photon object. the caller should check for null object.
     * @throws EtherNetworkException on error.
     */
    
    public final Message receiveIncomingData() throws EtherNetworkException
    {
        assert(eeType == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.LISTENER);
        try
        {
            Message m = null;
            NET_LOG.info("before select: " + String.valueOf(sel.keys().size()));
            
            int num = sel.select();
            NET_LOG.info("num of keys after select: " + String.valueOf(num));
            if (num == 0)
            {
                // fake alarm.
                return null;
            }
            // Get the keys corresponding to the activity
            // that has been detected, and process them
            // one by one
            Iterator it = sel.selectedKeys().iterator();
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
                    
                    NET_LOG.info("Received connection from " + sc.socket());
                    
                    Photon p;
                    try
                    {
                        p = new Photon(
                            GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT,
                            GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE);
                    }
                    catch (EtherException e)
                    {
                        NET_LOG.error(e.getMessage());
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
                    sc.register(sel, SelectionKey.OP_READ);
                      
                }
                else if ((key.readyOps() & SelectionKey.OP_READ)
                        == SelectionKey.OP_READ )
                {
                    NET_LOG.info("before reading socket");
                    SocketChannel sc = (SocketChannel) key.channel();
                    
                    //retrieve the photon associated with this socket.
                    Photon p = photonSocketMap.get(sc.socket());
                    
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
                        key.cancel();
                        
                        //before handing it over to any service
                        // change the connection type to blocking.
                        // for now we support blocking.
                        sc.configureBlocking(true);
                    }
                    catch(EtherException e)
                    {
                        key.cancel();
                        sc.close();
                        NET_LOG.error(e.getMessage());
                    }
                    
                }
            }
            
            return m;
        }
        catch (IOException e)
        {
            NET_LOG.error(e.getMessage());
            throw new EtherNetworkException("CRP_ETHERM_ERROR_002", null);
        }
        
    }
    
    @Override
    public final int blockingRecv(final Object o) throws EtherException
    {
        return (blockingRecv((ByteBuffer)o));
    }
    /**
     * wrappers of channel interface.
     * @param o object to be sent across the wire.
     * @throws EtherException on error.
     */
    @Override
    public final boolean blockingSend(final Object o) throws EtherException
    {
        if( blockingSend((ByteBuffer)o) > 0 )
        {
            return true;
        }
        else
        {
            return false;
        }
        
    }
    @Override
    public void close() throws EtherException
    {
        try
        {
            if(csc != null)
            {
                csc.close();
            }
            if(ssc != null)
            {
                ssc.close();
            }
        }
        catch(IOException e)
        {
            NET_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_014", null);
        }
    }
    @Override
    public void flushChannel()
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * register this channel to the given selector.
     * @param inpSel selector to be registered to.
     * @return true if successful.
     * @throws EtherException on error.
     */
    public final boolean registerChannelToSelector(
        final Selector inpSel) throws EtherException
    {
        try
        { 
            if (getElementType() 
                    == GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT)
            {
                getClientSocket().getChannel().register(
                    inpSel,
                    SelectionKey.OP_READ 
                    | SelectionKey.OP_WRITE);
            }
            else
            {
                getServerSocket().getChannel().register(
                    inpSel, SelectionKey.OP_ACCEPT);
                
            }
        }
        catch (Exception e)
        {
            RemoteMessageSensor.RMS_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_004", null);
        }
        return true;
    }
    
    /**
     * unregister this channel from the given selector.
     * @param inpSel selector, from which this channel is to be unregistered.
     * @return true if unregister is successful, fail otherwise.
     */
    public final boolean unregisterChannelToSelector(final Selector inpSel)
    {
        assert(getClientSocket() != null);
        
        Iterator<SelectionKey> it = inpSel.keys().iterator();
        
        while(it.hasNext())
        {
            SelectionKey sk = (SelectionKey) it.next();
            if(sk.channel() == this.csc)
            {
                // cancel only this channel.
                sk.cancel();
                return true;
            }
        }
        NET_LOG.warn(" CHANNEL IS NOT REGISTERED IN THE SELECTOR");
        return false;
    }
    /**
     * returns element type.
     * @return ether element type.
     */
    public final ETHER_ELEMENT_TYPE getElementType()
    {
        
        return eeType;
    }
    /**
     * returns server socket.
     * @return server sockety.
     */
    public final ServerSocket getServerSocket()
    {
        assert(ssc != null);
        return ssc.socket();
    }
    @Override
    public void notifyReceiver()
    {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void setReceiverSpinning(boolean spinFlag)
    {
        // TODO Auto-generated method stub
        
    }
    @Override
    public void setSenderSpinning(boolean spinFlag)
    {
        // TODO Auto-generated method stub
        
    }
    @Override
    public int blockingRecv(Object o, short tid)
    {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public boolean blockingSend(Object o, short tid)
    {
        // TODO Auto-generated method stub
        return false;
    }
    @Override
    public void reset()
    {
        
        
    }
	
   
}
