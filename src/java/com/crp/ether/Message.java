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


import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.interfaces.AbstractInterfaceBase;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryBlockObjectPool;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManager.MemoryBlockType;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.UnPacker;
import com.crp.thread.CRPThread;

/**
 * message class for ether.
 * In ether, all the components talk to each other in form of messages.
 * @author hpoduri
 * @version $Id$
 */

public class Message extends AbstractInterfaceBase
{
    
    /**
     * test interface.
     * should only be used while unit testing.
     * this helps in creating simple messages.
     */
    private boolean forTesting;
    
    /**
     * flag to indicate if this msg is to be processed.
     */
    private boolean considerForProcessing;
    
    /**
     * test object.
     * should only be used when forTesting is true.
     */
    private Object testObject;
    
    /**
     * message header.
     */
    private MessageHeader mh;
    
    /**
     * a message can have a packed contents of its objects.
     * ex: db page.
     */
    private Packer packedObject;
    
    /**
     * payload memory block.
     */
    private MemoryBlockObjectPool<?> payloadMB;
    
    /**
     * represents the state of this message.
     */
    private volatile int mStatus;

    /**
     * this should not go while packing.
     * only to be used for temporary purposes.
     */
    private Photon ph;
    
    /**
     * used during capture.
     * number of bytes yet to come from the packet capture.
     */
    private int expectedLength;
    
    /**
     * received number of bytes during capture.
     */
    private int receivedLength;
    
    /**
     * debugger to debug the message.
     */
    private MessageDebugger mdbg;
    
    /**
     * general constructor.
     * @param inpMsgCode message code for this message.
     */
    public Message(final int inpMsgCode)
    {
        mh = new MessageHeader(inpMsgCode, 0);
        payloadMB = null;
        mStatus = MessageStatus.UNINNT;
        ph = null;
        /**
         * by default set it to true; only set to false
         * when for some reason this msg is not considered for
         * processing.
         */
        considerForProcessing = true;
        if(GLOBAL_UTILS.msgDebug())
        {
            mdbg = new MessageDebugger();
        }
        expectedLength = receivedLength = 0;
    }
    
    /**
     * returns message header.
     * @return message header.
     */
    public final MessageHeader getMessageHeader()
    {
        return mh;
    }
    /**
     * checks to see if this is ether message.
     * @return boolean , true if it is ether message or false.
     */
    public final boolean isEtherMessage()
    {
        if (mh.messageCode() >= MESSAGE_CODE.MSG_ETHER_START_MARK 
                && mh.messageCode() <= MESSAGE_CODE.MSG_ETHER_END_MARK)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    /**
     * initialize necessary stuff for this message.
     * mainly creating memory blocks etc.,
     * @param inpOF object factory instance.
     * @param numObjs number of objects to be created.
     * @param inpBlockSize memory block size for the object.
     * @throws EtherException on error
     */
    public final void initMsgElements(
        final ObjectFactory inpOF, final int numObjs,
        final int inpBlockSize) throws EtherException
    {
        try
        {
            // create memory block for the object
            payloadMB = MemoryManager.createObjectPoolMemoryBlock(
                CRPThread.getCurrentThreadID(),
                numObjs,
                inpBlockSize,
                MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL,
                inpOF, inpOF.anyVarFields());
        }
        catch (MemoryManagerException e)
        {
            System.out.println(MemoryManager.toJSON().toString());
            MessageWorld.MSG_WORLD_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_005",
                new String[] {inpOF.getMyName()} );
        }
    }
    
    /**
     * returns payload memory block object pool in this msg.
     * @return memory block object pool.
     */
    public final MemoryBlockObjectPool<?> getPayloadMBO()
    {
        return payloadMB;
    }
    /**
     * returns the current message status.
     * @return MessageStatus.
     */
    public final int getStatus()
    {
        return mStatus;
    }
    /**
     * update the message status.
     * @param inpMStatus status, updated to.
     * @throws EtherException on invalid change of status.
     */
    public final void updateStatus(final int inpMStatus)
        throws EtherException
    {
        /*if(mStatus == MessageStatus.UNINNT 
                && inpMStatus == MessageStatus.CURRENTLY_IN_USE)
        {
            throw new EtherException("CRP_ETHERM_ERROR_006",
                new String[] {mStatus.toString(), inpMStatus.toString()});
        }*/
        // only update the status if the message is processed
        if(inpMStatus == MessageStatus.AVAILABLE && considerForProcessing == false)
        {
            // do nothing.
            return;
        }
        mStatus = inpMStatus;
    }
    /**
     * sets pay load memory block.
     * @param o object to be packed.
     */
    public final void setPayloadMB( final MemoryBlockObjectPool<?> o )
    {
        payloadMB = o;
    }

    /**
     * serializes the current Message object into byte stream.
     * @param pk : Packer object where the Message is folded into
     * @throws PackUnPackException 
     */
    public final void pack(final Packer pk) throws PackUnPackException
    {
        
        try
        {
            if(forTesting)
            {
                pk.addWireHeader(mh.messageCode());
                mh.setSize(1);
                mh.pack(pk);
                AbstractInterfaceBase ibo = (AbstractInterfaceBase) testObject;
                ibo.pack(pk);
            }
            //first thing, add a wire header to the message.
            pk.startEvent(getPackedBufferSize());
            
            pk.addWireHeader(mh.messageCode());
            
            if(payloadMB != null && payloadMB.getActiveObjects() > 0)
            {
                mh.setSize(payloadMB.getActiveObjects());
            }
            else
            {
                mh.setSize(0);
            }
            
            mh.pack(pk);
            // pack pay load here.
            if( mh.size() > 0 )
            {
                //pack payload from the objects from payload memory block.
                for (int i = 0; i < payloadMB.getActiveObjects(); i++)
                {
                    AbstractInterfaceBase ibo =
                        (AbstractInterfaceBase) payloadMB.getObjectAtIndex(i);
                    ibo.pack(pk);
                }
            }
            pk.finishEvent();
        }
        
        catch (PackUnPackException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw e;
        }
    }
    /**
     * construct the current Message object from the provided unpacker.
     * @param unpk the unpacker
\     *@throws PackUnPackException on error.
     */ 
    @Override
    public final void unpack(final UnPacker unpk) throws PackUnPackException
    {
        /**
         * NOTE: for unpacking, we dont do the packing way.
         * we let the module calling(mostly photon) unpack the wire header
         * before we get here.
         * this is because, we read partial buffers, first the wire header
         * and then the payload. unless we read the wire header we have no 
         * way of knowing what type of message needs to be instantiated.
         * 
         */
       
        mh.unpack(unpk);

        // we dont know what object to be called for unpack, it should be
        // decided by the message type.

        if (mh.size() > 0)
        {
            for (int i = 0; i < mh.size(); i++)
            {
                AbstractInterfaceBase aib = (AbstractInterfaceBase) payloadMB
                    .getObject();
                // unpack pay load.
                aib.unpack(unpk);
            }
        }
          
    }

    /**
     * creates an instance of message class.
     * 
     * @return message object.
     */
    @Override
    public final Object createObjectInstance(MemoryBlockByteBuffer mbb)
    {
        return new Message(MESSAGE_CODE.INVALID_MESSAGE);
    }

    /**
     * returns approx message size.
     * @return size of this message.
     */
    @Override
    public final int getObjSize()
    {
        //first add the message header length
        int len = 4 * GLOBAL_CONSTANTS.INT_SIZE + GLOBAL_CONSTANTS.LONG_SIZE;
        //add super class size
        len = len + super.getObjSize();
        //now add all objects size
        
        if(mh.size() > 0)
        {
            for(int i = 0; i < mh.size(); i++)
            {
                AbstractInterfaceBase aib =
                    (AbstractInterfaceBase) payloadMB.getObject();
                //unpack pay load.
                len = len + aib.getObjSize();
            }
        }
        return len;
    }

    @Override
    public void resetObject(final Object obj)
    {
        if(payloadMB != null)
        {
            payloadMB.resetPool();
        }
        mStatus = MessageStatus.UNINNT;
    }
    
    @Override
    public final String getMyName()
    {
        return GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY;
    }

    /**
     * sets photon object from which this message came from.
     * @param inpPH input photon object.
     */
    public final void setPhoton(final Photon inpPH)
    {
        ph = inpPH;
    }
    /**
     * returns the photon object.
     * @return p photon.
     */
    public final Photon getPhoton()
    {
        return ph;
    }
    /**
     * returns first object in the memory block.
     * @return object.
     */
    public final Object getFirstObject()
    {
        if(!forTesting)
        {
            return (payloadMB.getObjectAtIndex(0));
        }
        else
        {
            return testObject;
        }
    }

    /**
     * returns a free object from the object pool in message.
     * @return returns a object from the pool associated to the message.
     */
    public final Object getPayLoadObject()
    {
        return (payloadMB.getObject());
    }
    @Override
    public boolean anyVarFields()
    {
        return false;
    }

    /**
     * only used while testing.
     * enables message for testing.
     * this do not use the crp mem mgr and crp threads.
     */
    public final void setForTesting()
    {
        forTesting = true;
        testObject = null;
    }
    
    /**
     * sets test object.
     * @param o any interface object.
     */
    public final void setTestObject(final Object o)
    {
        testObject = o;
    }
    
    /**
     * returns the test object. 
     * @return any interface object previously set.
     */
    public final Object getTestObject()
    {
        return testObject;
    }

    @Override
    public int getPackedBufferSize()
    {
        int payloadLen = 0;
        if (payloadMB != null)
        {
            for (int i = 0; i < payloadMB.getActiveObjects(); i++)
            {
                AbstractInterfaceBase aib = (AbstractInterfaceBase) payloadMB
                    .getObjectAtIndex(i);
                payloadLen += aib.getPackedBufferSize();
            }
        }
        return (mh.getPackedBufferSize() + payloadLen);
    }

    /**
     * set method for packer.
     * this should be used if the message contents are already packed.
     * or you construct a message with packed contents. works well for db page.
     * @param inpPacker input packer.
     */
    public final void setPackerForObjs(
        final Packer inpPacker)
    {
        packedObject = inpPacker;
    }

    /**
     * returns packer.
     * @return Packer object.
     */
    public final Packer getPackerForObjs()
    {
        return packedObject;
    }

    @Override
    public boolean anyRoomForObjOfVarLength(final int length)
    {
        int lenToBeReceived = expectedLength - receivedLength;
        
        assert(lenToBeReceived >= 0);
        
        lenToBeReceived = lenToBeReceived > 0 ? lenToBeReceived: 0;
        
        if(payloadMB.getMBB() != null )
        {
            if( (payloadMB.getMBB().remainingSpace() - lenToBeReceived) 
                    >= length)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            //message contains fixed width objects.
            return false;
        }
    }

    @Override
    public int getVarLenFieldsSize()
    {
        return -1;
    }    
    
    @Override
    public void reset()
    {
        
        
    }
    /**
     * set the message processing flag to false.
     */
    public final void dontConsiderForProcessing()
    {
        considerForProcessing = false;
    }

    /**
     * set the message as processed.
     */
    public final void considerForProcessing()
    {
        considerForProcessing = true;
        
    }
    
    /**
     * returns message debugger.
     * @return MessageDebugger object.
     */
    public final MessageDebugger getDebugger()
    {
        return mdbg;
    }
    /**
     * @param expectedLength the expectedLength to set
     */
    public final void setExpectedLength(final int expectedLength)
    {
        this.expectedLength = expectedLength;
    }

    /**
     * @return the expectedLength
     */
    public final int getExpectedLength()
    {
        return expectedLength;
    }

    /**
     * @param receivedLength the receivedLength to set
     */
    public final void setReceivedLength(final int receivedLength)
    {
        this.receivedLength = receivedLength;
    }

    /**
     * @return the receivedLength
     */
    public final int getReceivedLength()
    {
        return receivedLength;
    }
    /**
     * state of the msg.
     */
    public interface MessageStatus 
    {
        /**
         * not initialized.
         */
        public final int UNINNT = 0;
        
        /**
         * available to use.
         */
        public final int AVAILABLE=1;
        /**
         * currently being used by producer/consumer.
         */
        public final int CURRENTLY_IN_USE =2;
    }
    @Override
    public void reset(MemoryBlockByteBuffer inpMBB)
    {
        expectedLength = receivedLength = 0;
        
    }

   
}
