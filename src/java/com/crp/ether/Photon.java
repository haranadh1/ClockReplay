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

import com.crp.common.CRPServiceInfo;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE;
import com.crp.db.DBException;
import com.crp.db.DBService;
import com.crp.ether.Channel.CommunicatorType;
import com.crp.ether.Message.MessageStatus;
import com.crp.interfaces.Caplet;
import com.crp.interfaces.ConnectionInterface;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.memmgr.MemoryManager.MemoryAllocationType;
import com.crp.memmgr.MemoryManager.MemoryBlockType;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadPoolManager;

/**
 * class to represent a connection object in ether.
 * @author hpoduri
 * @version $Id$
 */
public class Photon
{
    /**
     * indicates if this photon is short circuited.
     * a photon is called short-circuited, if it is to be used for
     * non-streaming purposes; specially using it for one or more 
     * small and quick exchange of messages.
     * we need to handle this case, as in this case we are not supposed
     * to buffer the messages, like we do it in streaming.
     * also, for short-circuited photons we do not perform the initial
     * hand shake; i.e, sending MSG_OPEN_CONNECTION.
     */
    private boolean isShortCircuited;
    
    /**
     * connection object.
     */
    private ConnectionInterface ci;
    
    /**
     * communication type.
     */
    private GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE cType;
    /**
     * ether element type. listener/client
     */
    private GLOBAL_ENUMS.ETHER_ELEMENT_TYPE eType;
    
    /**
     * Memory block byte buffer for this photon(for send).
     * this is based on java bytebuffer (direct buffers),
     * used for sending data on network.
     * only used for remote connections.
     */
    private MemoryBlockByteBuffer sendMB;
    
    /**
     * memory block byte buffer for receiving buffer.
     * based on java bytebuffer(direct buffers).
     */
    private MemoryBlockByteBuffer recvMB;
    
    /**
     * send memory block,based on byte array used for packing.
     * after packing we copy the content into sendMB, which is a
     * java bytebuffer, optimal way of sending data across network.
     */
    private MemoryBlockByteBuffer sendMBForPacking;
    
    /**
     * recv memory block, based on byte array used for unpacking.
     * after receiving we copy the bytebuffer(recvMB) contents into
     * recvMBForUnPacking, and do the unpacking from this memory block.
     */
    private MemoryBlockByteBuffer recvMBForUnPacking;
    /**
     * channel to be used for communication.
     * can be local or network channel.
     */
    private Channel ch;

    /**
     * flag to indicate if this photon is connected.
     */
    private boolean isConnected;
    
    /**
     * constructor.
     * @param inpEType type of ether element(client/listener).
     * @param inpCType type of communication(remote/local).
     * @throws EtherException on error.
     */
    public Photon(final GLOBAL_ENUMS.ETHER_ELEMENT_TYPE inpEType, 
        final GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE inpCType)
        throws EtherException
    {
        isConnected = false;
        isShortCircuited = false;
        eType = inpEType;
        cType = inpCType;
        ci = new ConnectionInterface();
        ci.setSrcAndDestServices(
            GLOBAL_ENUMS.SERVICE_CATALOG.INVALID_HANDLER_SERVICE,
            GLOBAL_ENUMS.SERVICE_CATALOG.INVALID_HANDLER_SERVICE);
        ci.setSrcAndDestThreads(
            GLOBAL_CONSTANTS.INVALID_THREAD_ID,
            GLOBAL_CONSTANTS.INVALID_THREAD_ID);
        
        
        //should figure out a better way of doing it.
        if (inpCType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE)
        {
            ch = new NetworkChannel(eType);
            
            //allocate memory blocks for send and recv.
            try
            {
                // create memory block for send buffers.
                /**
                 * here is the most unfortunate thing about java.
                 * the direct buffers for now do not allow to have
                 * a base byte array. this is just a horrible limitation.
                 * we have to deal with it.
                 * this makes us(for now, until we figure out a better way)
                 * do copy multiple times before getting to byte buffer.
                 */
                sendMB = (MemoryBlockByteBuffer) MemoryManager.createMemoryBlock(
                    CRPThread.getCurrentThreadID(),
                    GLOBAL_CONSTANTS.MAX_DIRECT_BUFFER_SIZE,
                    MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                    null,
                    MemoryAllocationType.JAVA_BYTE_BUFFER,
                    " memory block byte buffer for sending");
                
                recvMB = (MemoryBlockByteBuffer) MemoryManager.createMemoryBlock(
                    CRPThread.getCurrentThreadID(),
                    GLOBAL_CONSTANTS.MAX_DIRECT_BUFFER_SIZE,
                    MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                    null,
                    MemoryAllocationType.JAVA_BYTE_BUFFER,
                    "Memory Block byte buffer for receiving");
                
                
                sendMBForPacking = (MemoryBlockByteBuffer) MemoryManager.createMemoryBlock(
                    CRPThread.getCurrentThreadID(),
                    GLOBAL_CONSTANTS.MAX_DIRECT_BUFFER_SIZE,
                    MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                    null,
                    MemoryAllocationType.JAVA_BYTE_ARRAY,
                    "Memory Block byte buffer for packing ");
                
                recvMBForUnPacking = (MemoryBlockByteBuffer) MemoryManager.createMemoryBlock(
                    CRPThread.getCurrentThreadID(),
                    GLOBAL_CONSTANTS.MAX_DIRECT_BUFFER_SIZE,
                    MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                    null,
                    MemoryAllocationType.JAVA_BYTE_ARRAY,
                    "Memory Block byte buffer for un packing ");
            
            }
            catch (MemoryManagerException e)
            {
                MessageWorld.MSG_WORLD_LOG.error(e.getMessage());
                System.out.println(MemoryManager.toJSON().toString());
                throw new EtherException("CRP_ETHERM_ERROR_007",
                    null );
            }
        }
        else
        {
            // add for local memory channel.
            ch = new LocalChannel(GLOBAL_CONSTANTS.MAX_QUEUE_SIZE, ci); 
        }
    }
    
    /**
     * returns if this photon is short circuited.
     * @return true if short circuited or false.
     */
    public final boolean isShortCircuited()
    {
        return isShortCircuited;
    }
    
    /**
     * sets this photon as short circuited.
     */
    public final void setShortCircuited()
    {
        isShortCircuited = true;
    }
    /**
     * initialize method for photon, should be called right after
     * the constructor.
     * @throws EtherException 
     */
    public final void initializeClient() throws EtherException
    {
        ch.initClient();
    }
    
    /**
     * initialize listener.
     * @param port port number listen to.
     * @throws EtherException on error.
     */
    public final void initializeListener(
        final int port ) throws EtherException
    {
        ch.initListener(port);
    }
    
    /**
     * method to send a message( message without body )
     * this should do the right thing by calling the correct channel
     * for send.
     * @param m Message to be sent.
     * @throws EtherException 
     */
    public final boolean bsendMessage(final Message m) throws EtherException
    {
        boolean retFlag = false;
        if(cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE)
        {
            //TODO : should deal with packer objects better..may be pooling.
            
            //TODO : Should deal with packed objects; if a message contains all
            // the data packed already, we dont need to pack it again.
            // we should directly pack the message header and pass it to
            // send/recv buffers. this is useful for DBPages as they are already
            // packed.
            
            sendMB.reset();
            sendMB.getPoolByteBuffer().clear();
            sendMBForPacking.reset();
            Packer p = new Packer(sendMBForPacking);
            
            try 
            {
                m.pack(p);
            }
            catch(PackUnPackException e)
            {
                throw new EtherException("CRP_ETHERM_ERROR_004",null);
            }
            //now the unavoidable copy
            sendMB.getPoolByteBuffer().put(
                sendMBForPacking.getPoolByteArray(),
                0, sendMBForPacking.getCurOffset());
            
            //for remote connections, we dont need the message anymore from this point.
            m.updateStatus(MessageStatus.AVAILABLE);
            MessageWorld.MSG_WORLD_LOG.info("before sending message bb limit" + String.valueOf(sendMB.getPoolByteBuffer().limit()));
            retFlag = ch.blockingSend(sendMB.getPoolByteBuffer());
            MessageWorld.MSG_WORLD_LOG.info("after sending message, : bblimit: " + String.valueOf(sendMB.getPoolByteBuffer().limit()));
        }
        else if (cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.LOCAL)
        {
            m.setPhoton(this);
            retFlag = ch.blockingSend(m);
            if(isShortCircuited())
            {
                ch.flushChannel();
            }
        }
        return retFlag;
    }
    /**
     * receives a message.
     * block receive('b' stands for blocking)
     * @return message.
     * @throws EtherException 
     */
    public final Message brecvMessage() throws EtherException
    {
        Message m = null;
        if(cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE)
        {
            UnPacker unp = new UnPacker(recvMBForUnPacking);
            m = bReceiveWireHeader(unp);
            int remainingLen = recvMB.getPoolByteBuffer().remaining();
            // receive rest of the message.
            while (recvMB.getPoolByteBuffer().remaining() != 0)
            {
                ch.blockingRecv(recvMB.getPoolByteBuffer());
            }
            //now that we got the whole message, lets flip the byte buffer.
            
            recvMB.getPoolByteBuffer().flip();
           
            recvMB.getPoolByteBuffer().get(recvMBForUnPacking.getPoolByteArray(),
                GLOBAL_CONSTANTS.WIRE_HEADER_SIZE, remainingLen);
            try 
            {
                m.unpack(unp);
            }
            catch(PackUnPackException e)
            {
                UnPacker.UNP_LOG.error(e.getMessage());
                throw new EtherException("CRP_ETHERM_ERROR_009",null);
            }
            m.setPhoton(this);
        }
        else if(cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.LOCAL)
        {
            LocalChannel lc = (LocalChannel) ch;
            Object o = lc.blockingRecv();
            m = (Message) o;
        }
        return m;
    }
    /**
     * receive only wire header part of the message.
     * @param unp unpacker object.
     * @return message (related to message code)
     * @throws EtherException on error.
     */
    
    private final Message bReceiveWireHeader(
        final UnPacker unp) throws EtherException
    {
        recvMB.getPoolByteBuffer().flip();
        recvMB.resetOffset();
        recvMBForUnPacking.resetOffset();
        
        //first read only the wire header.
        //this is necessary as we should know how many
        //more bytes to be read to completely recv a message.
        // we are interested to read only one message buffer.
        // wire header length should always be 8bytes 4(length)+4(mc).
        
        //we should set the limit to 8, as we are initially interested to
        // read the header only.
        
        recvMB.getPoolByteBuffer().limit(GLOBAL_CONSTANTS.WIRE_HEADER_SIZE);
        
        while (recvMB.getPoolByteBuffer().remaining() != 0)
        {
            ch.blockingRecv(recvMB.getPoolByteBuffer());
        }        
        recvMB.getPoolByteBuffer().flip();
        recvMB.getPoolByteBuffer().get(
            recvMBForUnPacking.getPoolByteArray(), 0, GLOBAL_CONSTANTS.WIRE_HEADER_SIZE);
        
        //first unpack the wire header;
        // SHOULD ALWAYS UNPACK LENGTH FIRST AND THEN MESSAGE CODE.
        int length = -1; 
            
        int mc     = MESSAGE_CODE.INVALID_MESSAGE;
        
        try
        {
            length = unp.unpackWireHeaderLength();
            mc = unp.unpackWireHeaderMessageCode();
        }
        catch(PackUnPackException e)
        {
            UnPacker.UNP_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_009",null);
        }
        //validate the wire header.
        if(mc == MESSAGE_CODE.INVALID_MESSAGE)
        {
            throw new EtherException("CRP_ETHERM_ERROR_010", null);
        }
        if(length > recvMB.getPoolByteBuffer().capacity())
        {
            throw new EtherException("CRP_ETHERM_ERROR_011",
                new String[] {
                    String.valueOf(recvMB.getPoolByteBuffer().limit()),
                    String.valueOf(length)});
            
        }
        // now prepare the byte buffer for the next number of bytes to be read.
        recvMB.getPoolByteBuffer().flip();
        recvMB.getPoolByteBuffer().limit(
            length - GLOBAL_CONSTANTS.WIRE_HEADER_SIZE);
        
        //get a free message from the message world that corresponds to the same
        // message code.
        MessageWorld mw = CRPThreadPoolManager.getCRPThread(
            CRPThread.getCurrentThreadID()).getMyMsgWorld();
        
        String msgPoolKey = MESSAGE_CODE.getMsgPoolKeyForMessageCode(mc);
        Message m = mw.getFreeMsg(msgPoolKey);
        
        if(m == null)
        {
            // create the message pool.
            if ((mc == MESSAGE_CODE.MSG_REQ_CLIENT_CAPLET_STREAM_DB) 
                    || (mc == MESSAGE_CODE.MSG_RES_DB_CAPLET_STREAM_CLIENT))
            {
                ObjectFactory ofcap = new Caplet(null);

                mw.createMsgGroupPool(
                    ofcap,
                    GLOBAL_CONSTANTS.KB,
                    GLOBAL_CONSTANTS.MAX_DB_PAGES_PER_IO
                        * GLOBAL_CONSTANTS.DB_PAGE_SIZE);
                m = mw.getFreeMsg(msgPoolKey);
            }
        }
        assert(m != null);
        m.getMessageHeader().setMessageCode(mc);
        return m;
    }
    
    /**
     * name says it all.
     */
    public final void reset()
    {
        isShortCircuited = false;
        ci.reset();
        ch.reset();
    }
    /**
     * photon wrapper to connect to a service.
     * @param crps service info object
     * @param ct communicator type, source/sink.
     * @throws EtherException on error.
     */
    public final void connect(final CRPServiceInfo crps,
        final CommunicatorType ct)
        throws EtherException
    {
        ch.connect(crps, ct);
        
        //TODO : for now set the src to invalid,
        //later should change it to the correct one.
        
        ci.setSrcAndDestServices(
            GLOBAL_ENUMS.SERVICE_CATALOG.INVALID_HANDLER_SERVICE,
            crps.getName());
        ci.setSrcAndDestThreads(
            GLOBAL_CONSTANTS.INVALID_THREAD_ID,
            PROCESS_GLOBALS.SERVICE_HANDLER_THREAD_ID);
        isConnected = true;
    }
    
    /**
     * returns true if connected.
     * @return true/false if connected or not.
     */
    public final boolean isConnected()
    {
        return isConnected;
    }
    /**
     * returns channel.
     * @return channel this photon is using.
     */
    public final Channel getChannel()
    {
        return ch;
    }
    /**
     * close communication.
     * @return 
     * @throws EtherException on error.
     */
    public final void close() throws EtherException
    {
        ch.close();
        isConnected = false;
    }
    
    /**
     * sets src and target thread id.
     * this is useful for local channel to figure out
     * the target thread id, at the end of the connection.
     * @param inpSrc input src thread id.
     * @param inpDest input dest thread id.
     */
    public final void setThrdIDs(final short inpSrc, final short inpDest)
    {
        ci.setSrcAndDestThreads(inpSrc, inpDest);
    }
    
    /**
     * returns connection object of this photon.
     * @return Photon object.
     */
    public final ConnectionInterface getConnectionObject()
    {
        return ci;
    }
    
    /**
     * convenience method to create photon.
     * and also connect to the given service.
     * @param crps crp service info object
     * @return connected photon object
     * @throws EtherException on error.
     */
    public static final Photon createPhoton(
        final CRPServiceInfo crps) throws EtherException
    {
        //check if this service exists in this process.
        Photon ph = null;
        if( PROCESS_GLOBALS.isConfiguredOnThisProcess(crps.getName()))
        {
            ph = new Photon(GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT,
                GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.LOCAL);
        }
        else
        {
            ph = new Photon(GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT,
                GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE);
        }
        
        ph.connect(crps, CommunicatorType.SOURCE);
        return ph;
    }
    /**
     * set dest thread id.
     * @param threadID thread id to be set to.
     */
    public final void setDestThrdId(final short threadID)
    {
        ci.setSrcAndDestThreads(ci.getSrcThrdID(), threadID);
    
    }
    
    /**
     * to know if this photon is used for local communication.
     * @return true if local, false if remote.
     */
    public final boolean isLocal()
    {
        if(cType == ETHER_COMMUNICATION_TYPE.LOCAL)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * makes the sender spinning as opposed to wait.
     * 
     * @param spinFlag true is for spinning, false is to wait.
     */
    public final void setReceiverSpinning(final boolean spinFlag)
    {
        ch.setReceiverSpinning(spinFlag);
    }
    /**
     * makes the receiver spinning as opposed to wait.
     * @param spinFlag true is for spinning, false is to wait.
     */
    public final void setSenderSpinning(final boolean spinFlag)
    {
        ch.setSenderSpinning(spinFlag);
    }
    
    /**
     * sends message and dont return false, when it cant.
     * keep polling until it sends the message successfully.
     * @param m Message to be sent.
     * @throws EtherException on error.
     */
    public final void sendMessageNoFail(final Message m) throws EtherException
    {
        while (!bsendMessage(m))
        {
            ch.flushChannel();
            ch.notifyReceiver();
            Thread.yield();
        }
        ch.flushChannel();
        ch.notifyReceiver();
    }
}
