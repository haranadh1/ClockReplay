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

package com.crp.memmgr;

import org.json.JSONException;
import org.json.JSONObject;

import com.crp.common.CRPException;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadPoolManager;

/**
 * Handles all the memory operations at a block level. The callers use the
 * methods in the class to eventually get the memory they want. Note that the
 * methods inside this class are not THREAD SAFE. Ideally, only one thread
 * should deal with one MemoryBlock object. Meaning while a thread is writing
 * into the block, no other thread should be reading from it and true 
 * the other way.
 * MemoryBlock methods do not provide synchronization, however, you can use
 * MemoryBlockStatus variable to synchronization. Note that the thread, which
 * owns the MemoryBlock(which calls MemoryManager. createMemoryBlock()) is the
 * one should eventually free it.
 * @author hpoduri
 * @version $Id$
 */
public abstract class MemoryBlock
{
    /**
     * Index of the requesting service/thread in
     * {@link PROCESS_GLOBALS #PSERVICES}.
     */
    private short   thrdIndex;
    /**
     * current offset in the buffer. the offsets from the beginning of the
     * MemBlock.
     */
    private int   curOffsetInMemBlock;
    /**
     * size of MemoryBlock.
     */
    private int   blockSize;
    /**
     * index of this memoryblock in MemMgr.
     */
    private short memMgrIndex;

    /**
     * description to indicate what this mb is used for.
     */
    private String description;
    
    /**
     * variable to store the previously written data size.
     * this is useful if a thread is writes into a mb, and
     * hand over to another thread to read, the current offset
     * is reset(as it has to start reading from the beginning), then
     * we store the current offset into prevWrittenDataSize.
     */
    private int prevWrittenDataSize;
    /**
     * MemoryBlockStatus, useful in controlling the use.
     */
    public enum MemoryBlockStatus
    {
        UNINITIALIZED,
        INIT,
        READING,
        WRITING,
        READY_TO_WRITE,
        READY_TO_READ,
        READY_TO_FREE,
        FREED
    }

    /**
     * MemoryBlockStatus for this block.
     */
    private volatile MemoryBlockStatus mbs;
    
    /**
     * number of objects in this memory block.
     */
    private int numOfObjects;
    
    
    /**
     * constructor. this constructor constructs all the members in the class
     * except control bits of blockSizeControlBits, which needs to be set with a
     * setter function separately. It is to be done this way as we don't know
     * the index of MemoryBlock in MemoryManager Buffer until the MemoryManager
     * performs a compareAndSwap.
     * @param inpBlockSize size of the requested memory block
     */
    public MemoryBlock(final int inpBlockSize, String inpDesc)
    {
        blockSize = inpBlockSize;
        curOffsetInMemBlock = 0;
        mbs = MemoryBlockStatus.INIT;
        // set the indexes to invalid, force the developer to call
        // the setIndexes.
        memMgrIndex = -1;
        thrdIndex = -1;
        numOfObjects = 0;
        description = inpDesc;
    }

    /**
     * bumps the offset in the MemoryBlock(not thread safe) typically, the
     * memory block range would be in the range shown.<br/>
     * ___________________<br/>
     * |                   |<br/>
     * |___________________|<br/>
     * ^<----blockSize---->^<br/>
     * ^ ^<br/>
     * MemoryBlock.pool+curOffsetInMemBlock
     * NOTE: as a side effect, for each bumpOffset, it is considered as an
     * object.
     * @param size : size, the curOffsetInMemBlock to be bumped to.
     * @throws MemoryManagerException on buffer overflow.
     */
    private final void bumpOffset(final int size) throws MemoryManagerException
    {
        assert (size >= 0);
        if(curOffsetInMemBlock + size > blockSize)
        {
            throw new MemoryManagerException("CRP_MEMMGR_ERROR_010",
                new String[] {String.valueOf(size), 
                    String.valueOf(remainingSpace())});
        }
        curOffsetInMemBlock += size;
        numOfObjects++;
    }

    /**
     * should be called before writing a record
     * in a memory block.
     * @param size size of the record to go in.
     * @throws MemoryManagerException on out of space.
     */
    public final void startWritingRecord(
        final int size) throws MemoryManagerException
    {
        doBeforeStartWritingRecord(size);
        
    }
    
    /**
     * each memory block type can add their own implementation
     * before start writing a record in.
     * @param size size of the record.
     * @throws MemoryManagerException on out of space.
     */
    public abstract void doBeforeStartWritingRecord(
        int size) throws MemoryManagerException;
    

    /**
     * called after writing a record into MemoryBlock.
     * unless this method is called, for memory block
     * assumes that no record is written.
     * @param size size of the record written.
     * @throws MemoryManagerException on error.
     */
    public final void finishWritingRecord(
        final int size) throws MemoryManagerException
    {
        doBeforeFinishWritingRecord(size);
        //this seals the record in.
        bumpOffset(size);
    }
    /**
     * call necessary memory block specific routine.
     * should be overridden by the derived classes.
     * @param size size of the record just written.
     */
    public abstract void doBeforeFinishWritingRecord(int size);
    
    /**
     * call after reading a record.
     * @param size size of the record read.
     * @throws MemoryManagerException on error.
     */
    public final void finishReadingRecord(final int size)
        throws MemoryManagerException
    {
        doBeforeFinishReadingRecord(size);
        bumpOffset(size);
    }
    /**
     * should be overridden by the derived classes.
     * call before finish reading record in the memory block.
     * @param size size of the record.
     */
    public abstract void doBeforeFinishReadingRecord(final int size);

    /**
     * set memmgr array index, thread index.
     * @param tIndex thread index goes into top two bytes.
     * @param inpMemMgrIndex memory mgr index goes into 3,4 bytes(from left)
     */
    public final void setIndexes(final short tIndex, final short inpMemMgrIndex)
    {
        thrdIndex = tIndex;
        memMgrIndex = inpMemMgrIndex;
    }

    /**
     * Initialize memory block.
     */
    public final void initialize()
    {
        mbs = MemoryBlockStatus.INIT;
        numOfObjects = 0;
    }
    /**
     * return memory block status.
     * @return mbs.
     */
    public final MemoryBlockStatus getStatus()
    {
        return mbs;
    }

    /**
     * returns mem mgr index.
     * @return memMgrIndex.
     */
    public final short getMemMgrIndex()
    {
        return memMgrIndex;
    }

    /**
     * returns thread index.
     * @return thrIndex.
     */
    public final short getThrdIndex()
    {
        return thrdIndex;
    }

    /**
     * reset MemBlock.
     */
    public void reset()
    {
        prevWrittenDataSize = curOffsetInMemBlock;
        curOffsetInMemBlock = 0;
        numOfObjects = 0;
    }

    /**
     * get current offset of MemBlock.
     * @return curOffsetInMemblock.
     */
    public final int getCurOffset()
    {
        return curOffsetInMemBlock;
    }

    /**
     * returns size of the memory block.
     * @return size.
     */
    public final int getSize()
    {
        return blockSize;
    }

    /**
     * unused space in memory block.
     * @return unused space.
     */
    public final int remainingSpace()
    {
        return (blockSize - curOffsetInMemBlock);
    }

    /**
     * returns true if in use.
     * @return true/false based on block usage.
     */
    public final boolean inUse()
    {
        return (mbs == MemoryBlockStatus.READING
                || mbs == MemoryBlockStatus.WRITING);
    }
    /**
     * Reset the current offset.
     * useful when you switch the memory block status from read to write
     * or the other way.
     */
    public final void resetOffset()
    {
        prevWrittenDataSize = curOffsetInMemBlock;
        curOffsetInMemBlock = 0;
        numOfObjects = 0;
    }

    /**
     * set this memory block to free. NOTE: pre-conditions for freeing should
     * handle by the calling thread.
     * @throws MemoryManagerException if the block is in use.
     */
    public final void freeMe() throws MemoryManagerException
    {
        // cool thing with memory block is you are the only one
        // using at any point of time.
        if (inUse())
        {
            throw new MemoryManagerException(
                "CRP_MEMMGR_ERROR_006",
                new String[]{String.valueOf(mbs)});
        }
        assert (getMemMgrIndex() <= GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR);
        assert (getThrdIndex() <= GLOBAL_CONSTANTS.MAX_THREADS_IN_PROCESS);
        MemoryManager.reduceUsedSpaceForthisThread(getThrdIndex(), getSize(),
            getMemMgrIndex());
        // store the old value.
        short mIndex = getMemMgrIndex();
        if (GLOBAL_UTILS.memMgrDebug())
        {
            MemoryManager.MEM_LOG.debug("freeing block index: "
                + String.valueOf(memMgrIndex) + " thr index: "
                + String.valueOf(getThrdIndex()));
        }
        reset();
        mbs = MemoryBlockStatus.UNINITIALIZED;
        try 
        {
          
            MemoryManager.addToFreeList(mIndex);
        }
        catch (CRPException e)
        {
            MemoryManager.MEM_LOG
                .error("fatal error while freeing memory block: "
                    + this.zipString());
            MemoryManager.MEM_LOG.error(e.getMessage());
        }
    }
    /**
     * returns number of objects in this memory block.
     * it is considered as an object for each bumpOffset.
     * @return numbOfObjects.
     */
    public int getNumOfObjects()
    {
        return numOfObjects;
    }

    /**
     * zip representation of MemoryBlock object.
     * @return string.
     */
    public final String zipString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("line.separator"));
        sb.append("module using: ");

        CRPThread cpt = (CRPThread) Thread.currentThread();
        sb.append(cpt.getName());
        sb.append(",curOffsetInMemoryBlock : ");
        sb.append(this.curOffsetInMemBlock);
        sb.append(",block size: ");
        sb.append(this.blockSize);
        sb.append(",mem mgr idx: ");
        sb.append(this.memMgrIndex);
        sb.append(", thrd idx: ");
        sb.append(this.thrdIndex);
        sb.append(System.getProperty("line.separator"));

        return sb.toString();
    }
    
    /**
     * to json.
     * @return json object string.
     */
    public final JSONObject toJSON()
    {
        JSONObject jsonobj = new JSONObject();
        JSONObject mbJSON = new JSONObject();
        try
        {
            if(this instanceof MemoryBlockObjectPool<?>)
            {
                MemoryBlockObjectPool<?> mbo = (MemoryBlockObjectPool<?>) this;
                jsonobj.put("object_type", mbo.getObjectFactory().getMyName());
            }
            else
            {
                jsonobj.put("object_type" , "byte_array");
            }
            CRPThread crpt = CRPThreadPoolManager.getCRPThread(this.thrdIndex);
            if(!crpt.isAlive())
            {
                jsonobj.put("owning_thread_name",
                    crpt.getMyService().getServiceInfo().getName().toString() 
                    + String.valueOf(crpt.getCRPThreadID()));
            }
            else
            {
                jsonobj.put("owning_thread_name",
                    CRPThreadPoolManager.getCRPThread(this.thrdIndex).getName());
            }
            
            jsonobj.put("memory_block_size", this.blockSize);
            jsonobj.put("current_offset", this.curOffsetInMemBlock);
            jsonobj.put("mem_mgr_index", this.memMgrIndex);
            jsonobj.put("thread_index", this.thrdIndex);
            jsonobj.put("description", this.description);
            mbJSON.put("memory_block", jsonobj);
               
        }
        catch(JSONException e)
        {
            MemoryManager.MEM_LOG.error(e.getMessage());
            return null;
        }
        return mbJSON;   
    }
    /**
     * override toString for this class.
     * @return return string representation of this class.
     */
    public final String toString()
    {
        return this.zipString();
    }
    
    /**
     * returns previously written data size.
     * @return integer.
     */
    public final int getPrevWrittenDataSize()
    {
        return prevWrittenDataSize;
    }
}
