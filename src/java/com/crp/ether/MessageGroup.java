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

import com.crp.common.CommonLogger;
import com.crp.ether.Message.MessageStatus;
import com.crp.memmgr.MemoryBlockObjectPool;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.memmgr.MemoryManager.MemoryBlockType;
import com.crp.thread.CRPThread;

/**
 * Message Group class.
 * class that stores some(fixed/variable) number of messages.
 * only one thread can operate at any point of time on one
 * MessageGroup. (producer/consumer should honor this rule).
 * each MessageGroup is associated with one MemoryBlockGroup class.
 * All the data related to the messages inside a MemoryGroup class
 * should only reside on the corresponding MemoryBlockGroup, which
 * makes this class independent.
 * 
 * @author hpoduri
 * @version $Id$
 */
public class MessageGroup
{
    /**
     * memory block group corresponding to this message group.
     */
    private MemoryBlockObjectPool<Message> mbg;
    
    /**
     * constructor.
     * @param size  number of messages this message group holds.
     * @throws EtherException on failure to create memory block.
     */
    public MessageGroup(final int size) throws EtherException
    {
        ObjectFactory ofm = new Message(MESSAGE_CODE.INVALID_MESSAGE);
        
        mbg = null;
        try
        {
            mbg = (MemoryBlockObjectPool<Message>) MemoryManager
                .createObjectPoolMemoryBlock(
                    CRPThread.getCurrentThreadID(),
                    size,
                    -1, //dont need mbb here.
                    MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL,
                    ofm,
                    false);
        }
        catch (MemoryManagerException e)
        {
            MessageWorld.MSG_WORLD_LOG.error(e.getMessage());
            throw new EtherException(
                "CRP_ETHERM_ERROR_005",
                new String[]{ofm.getMyName()});
        }
        
    }
    /**
     * initialize messages.
     * @param ofs object factory instance.
     * @param numObjs number of objects.
     * @param inpBlockSize block size for the object memory block.
     * @throws EtherException on error.
     */
    public final void initMsgs(
        final ObjectFactory ofs, final int numObjs,
        final int inpBlockSize) throws EtherException
    {
        Message m = null;
        for(int i=0; i< mbg.getNumOfObjects(); i++)
        {
            m = mbg.getObjectAtIndex(i);
            m.initMsgElements(ofs, numObjs, inpBlockSize);
        }
        
    }
    
    /**
     * returns number of msgs in this group.
     * @return integer.
     */
    public final int numOfActiveMsgs()
    {
        return mbg.getActiveObjects();
    }
    
    /**
     * returns memory block object pool for messages in this mbg.
     * @return memory block object pool object.
     */
    public final MemoryBlockObjectPool<Message> getMessageMBO()
    {
        return mbg;
    }
    /**
     * returns the next available message from the group.
     * @return Message Object or null.
     * @throws EtherException on error.
     */
    public final Message getNextAvailableMsg() throws EtherException
    {
        Message m = null;
        for(int i=0; i< numOfActiveMsgs(); i++)
        {
            m = mbg.getObjectAtIndex(i);
            if(m.getStatus() != MessageStatus.CURRENTLY_IN_USE)
            {
                m.updateStatus(MessageStatus.CURRENTLY_IN_USE);
                return m; 
            }
        }

        m = mbg.getObject();
        
        if (m != null)
        {
            if(m.getStatus() != MessageStatus.CURRENTLY_IN_USE)
            {
                m.updateStatus(MessageStatus.CURRENTLY_IN_USE);
                return m;
            }
        }
        return m;
    }
}