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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import com.crp.memmgr.ObjectFactory;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadPoolManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Category;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crp.common.CRPLogger;
import com.crp.common.CRPException;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.common.LockFreeStack;

// Implements the memory manager for ether ;

/**
 * Memory Manager for Ether.
 * @author hpoduri
 * @version $Id$
 */
/**
 * Implementation Notes of Memory Manager.
 * THREAD_POOL, THREAD ID AS INDEX.
 * ____________________________
 * |thr |thr |
 * |id  |id  |
 * |0___|1___|_____________________
 * \    \ 
 * \       \
 * \          \ 
 * \              \ 
 * \                 \
 *  0 1 2 3 4 5 6 7 8  
 * _______________________(MemoryIndexForThrd: per thread)
 * |_|_|_|_|_|_|_|_|_|_|_|        (per thread bit map)  
 *  |   \  \
 *  \    \   \
 *   \     \    \
 *    \      \    \       
 *     \      \     \
 *      \      \      \
 * m     \_______\______ \___________________________________
 * B    |Memory |Memory |                      |
 * l    |Block  |Block  |         .............|
 * o    |Index:0|Index:1|         .............|    
 * c    |_______|_______|______________________|_____________
 * k                          /     \
 * s                         /       \
 *             MemoryBlock        MemoryBlock
 *             ByteBuffer         Object Pool             
 */

/**
 * how Memory Manager(MM) works -Memory manager manages memory in chunks of
 * MemoryBlocks. -MM maintains a list of {@link #MemoryBlock} currently in use.
 * -MM managed memory can dynamically grow or shrink, based on requests. -MM
 * maintains the currently used space in variable offset, which is fully
 * synchronized with atomics.
 */
public final class MemoryManager
{
    /**
     * make the constructor private.
     */
    private MemoryManager()
    {
        
    }
    /**
     * create a separate logger for MemoryManager.
     */
    public static final Category MEM_LOG = CRPLogger
                                             .initializeLogger("com.crp.common.MemoryManager");


    //TODO: not memory allocation type is not necessary, we can combine it with memory block type.
    
    /**
     * Memory Allocation type, mainly used for byte buffers.
     * However, it can also be used for object pool in future.
     */
    public static enum MemoryAllocationType 
    {
        // may be fore Object Pool type.
        NOT_VALID,
        JAVA_BYTE_ARRAY,
        JAVA_BYTE_BUFFER,
        /**
         * dont know what types can be there for object.
         * may be on stack/heap etc.
         */
    }
    
    /**
     * Represent the status of Memory Manager.
     */
    private static enum MemMgrStatus
    {
        /**
         * before calling init.
         */
        UNINITIALIZED,
        /**
         * after calling init/reinit.
         */
        INITIALIZED,
        /**
         * once MemMgr is full.
         */
        FULL, COMPACT,
    }

    /**
     * usedSpaceIndex to represent the total used space in MemManager. the two
     * higher order bytes represents the total # of the MemoryBlock objects
     * {@link #mBlocks} in Memory Manager. the lower 6 bytes represent the total
     * usedSpace. we should not directly read from usedSpaceIndex, it is safe to
     * use GLOBAL_UTIL.get/incr methods.
     */
    private static AtomicLong    usedSpaceIndex;
    
    /**
     * free space manager object.
     */
    private static FreeSpaceManager fsm;
    /**
     * Memory Manager Status.
     */
    private static MemMgrStatus  mMStatus = MemMgrStatus.UNINITIALIZED;
    /**
     * total memory allocated thru MemoryManager.
     */
    private static long          totalMemory;
    /**
     * MemoryBlock list. The length of this array is stored in the first two
     * bytes of {@link #usedSpaceIndex} field.
     * we want the changes in the values of mBlocks to be reflected in all
     * the threads reading.
     */
    private volatile static MemoryBlock[] mBlocks  =
        new MemoryBlock[GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR];

    /**
     * bitset index array for each thread registered.
     */
    private static volatile MemoryIndexForThrd [] memBitMapIndexForThrds =
        new MemoryIndexForThrd[GLOBAL_CONSTANTS.MAX_THREADS_IN_PROCESS];
    
    /**
     * represents free memory block index in the {@link #mBlocks}
     */
    private static LockFreeStack<Short> freeBlockIndexStack = null;

    /**
     * memory block type.
     */
    public static enum MemoryBlockType
    {
        /**
         * represents byte buffer.
         */
        MEMORY_BLOCK_BYTE_BUFFER,
        /**
         * represents any object pool.
         */
        MEMORY_BLOCK_OBJECT_POOL,
        /**
         * represents a string memory block.
         * this is specific to store(reuse)string objects.
         */
        MEMORY_BLOCK_STRING_POOL,
    }

    /**
     * @deprecated
     * @param size : represents the size of the object to be allocated
     * @return : byte array
     */
    public static byte[] allocateObject(final int size)
    {
        return (new byte[size]);
    }

    /**
     * initialize memory.
     * @param size size of total memory allocated by the memory manager. NOTE:
     *        size should always be in bytes should throw proper error
     *        on invalid memory size.
     * @throws MemoryManagerException throws when cannot allocate memory
     */
    public static void init(final int size) throws MemoryManagerException
    {
        if(mMStatus == MemMgrStatus.INITIALIZED)
        {
            return;
        }
        if (size <= 0)
        {
            throw (new MemoryManagerException(
                "CRP_MEMMGR_ERROR_004",
                new String[]{String.valueOf(size)}));
        }

        usedSpaceIndex = new AtomicLong(0);
        fsm = new FreeSpaceManager();
        totalMemory = size;
        // initialize freeSpaceIndexStack
        freeBlockIndexStack = new LockFreeStack<Short>(
            GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR);
        
        for (int i = 0; i < memBitMapIndexForThrds.length; i++ )
        {
            memBitMapIndexForThrds[i] = null;
        }
        mMStatus = MemMgrStatus.INITIALIZED;
        MEM_LOG.info("Memory Manager Initialized with " + String.valueOf(size));
    }

    /**
     * re-inits the MemoryManager Object.
     * @param size , the size of newly formed MemoryManager buffer. size=0, is
     *        interpreted as keep the original size as is.
     * @throws MemoryManagerException on error. NOTE that this method is not
     *         thread safe. should always be called when no other thread is
     *         accessing memory blocks or MemoryManager data structures.
     */
    public static void reinit(final int size) throws MemoryManagerException
    {
        if (size < 0)
        {
            throw (new MemoryManagerException(
                "CRP_MEMMGR_ERROR_004",
                new String[]{String.valueOf(size)}));
        }
        mMStatus = MemMgrStatus.UNINITIALIZED;
        // reset the MemoryBlock array elements.
        for (int i = 0; i < GLOBAL_UTILS.getMemBlockIndex(usedSpaceIndex.get()); i++)
        {
            mBlocks[i] = null;
        }
        usedSpaceIndex.set(0);
        //initialize the free space blocks index
        freeBlockIndexStack = new LockFreeStack<Short>(
            GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR);
        
        if (size != 0)
        { 
            // size=0, indicates re-use the old buffer.
            // some redundant initialization, but should be fine.
            init(size);
        }
        mMStatus = MemMgrStatus.INITIALIZED;
    }

    /**
     * nuke the current memory manager including registered threads. the
     * difference between init and nuke is that, nuke demands all the threads to
     * re-register to the memory manager again.
     * deprecated: no need of nuke as the re-init and init will do the same.
     * removed the limitation that registerThread should happen before init.
     * Now, register thread can be called any time.
     */
    @Deprecated
    public static void nuke()
    {
        // reset everything.
        mMStatus = MemMgrStatus.UNINITIALIZED;
        usedSpaceIndex = new AtomicLong(0);
        totalMemory = 0;

        //Initialize free space blocks index.
        freeBlockIndexStack = new LockFreeStack<Short>(
            GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR);
        
        for (int i = 0; i < memBitMapIndexForThrds.length; i++)
        {
            memBitMapIndexForThrds[i] = null;
        }
        MEM_LOG.info("Memory Manager nuked. ");
    }

    /**
     * register a service. for optimization, pass the index of the service in
     * {@link PROCESS_GLOBALS.#PSERVICES}
     * this method is thread safe assuming that the index being passed
     * is thread id (which is generated uniquely)
     * @param tIndex pass index of the service in PSERVICES
     * @throws MemoryManagerException on error.
     */
    public static void registerThread(final short tIndex)
        throws MemoryManagerException
    {
        assert (mMStatus != MemMgrStatus.UNINITIALIZED);
        assert (tIndex >= 0);
        //we dont need to worry about synchronization
        //as thread is expected to be unique.
        
        memBitMapIndexForThrds[tIndex] = new MemoryManager.MemoryIndexForThrd();
        MEM_LOG.info("Thread Index " + String.valueOf(tIndex) + " registered.");
    }

    /**
     * creates memory block and adds into queue.
     * this method has been added to ensure
     * backward compatibility with the older version,
     * which does not take an object factory parameter.
     * this might be revisited soon.
     * @param thrdIndex description of calling module
     * @param inpBlockSize size of requested memory block in MBs
     * @param mbt MemoryBlockType to be created.
     * @param inpDesc why do you need this mb?
     * @return mb, MemoryBlock just created.
     * @throws MemoryManagerException when MemManager runs out of space.
     */
    public static MemoryBlock createMemoryBlock(final short thrdIndex,
        final int inpBlockSize, final MemoryBlockType mbt, final String inpDesc)
        throws MemoryManagerException
    {
    	
    	return createMemoryBlock(thrdIndex,inpBlockSize,mbt,null,
    	    MemoryAllocationType.NOT_VALID, inpDesc);
    }
    
    /**
     * creates memory block and adds into queue.
     * @param thrdIndex description of calling module
     * @param inpBlockSize size of requested memory block in bytes.
     * @param mbt MemoryBlockType to be created.
     * @param of object factory to pass to object pool.
     * @param mat memory allocation type, used for byte buffer mbs.
     * @param inpDesc small note what this mb is used for.
     * @return mb, MemoryBlock just created.
     * 
     * @throws MemoryManagerException when MemManager runs out of space.
     */
    public static MemoryBlock createMemoryBlock(final short thrdIndex,
        final int inpBlockSize,
        final MemoryBlockType mbt, final ObjectFactory of,
        final MemoryAllocationType mat,
        final String inpDesc)
        throws MemoryManagerException
    {
        assert (thrdIndex >= 0);
        assert (mbt == MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER 
            || mbt == MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL);
        
        // Check the validity of the size
        if (inpBlockSize <= 0)
        {
            throw (new MemoryManagerException(
                "CRP_MEMMGR_ERROR_004",
                new String[]{String.valueOf(inpBlockSize)}));
        }
        
        // Check the status
        if (mMStatus != MemMgrStatus.INITIALIZED)
        {
            throw new MemoryManagerException(
                "CRP_MEMMGR_ERROR_003",
                new String[]{String.valueOf(mMStatus)});
        }
        
        // validate thread index
        if (memBitMapIndexForThrds[thrdIndex] == null)
        {
            throw (new MemoryManagerException(
                "CRP_MEMMGR_ERROR_007",
                new String[]{String.valueOf(thrdIndex)}));
        }
        
        MemoryBlock mb = null;
        try
        {
            // First create a MemoryBlock Object
            if (mbt == MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER)
            {
                mb = new MemoryBlockByteBuffer(inpBlockSize, mat, inpDesc);
            }
            else
            {
            	assert (of != null);
                mb = new MemoryBlockObjectPool(inpBlockSize,of, inpDesc);
            }
        }
        catch (OutOfMemoryError e)
        {
            String[] args = new String[1];
            args[0] = String.valueOf(inpBlockSize);
            MEM_LOG.error("OutofMemory Error " + e.getMessage());
            throw (new MemoryManagerException("CRP_MEMMGR_ERROR_001", args));
        }
     
        long tempUsedSpace = usedSpaceIndex.get();
        long tempUsedSpaceWithIndex = -1;
 
        
        // check if we can find in the free list.
        /*MemoryBlock freeMB = getMemoryBlockFromFreeList(
            thrdIndex, inpBlockSize, mbt, mat);
        */
        MemoryBlock freeMB = null;
        short mIndex = fsm.getMBFromFreeSpaceManager(
            mbt, mat, of, inpBlockSize);
        if(mIndex >= 0)
        {
            freeMB = mBlocks[mIndex];
        }
        if( freeMB != null )
        {
            if(mat == MemoryAllocationType.JAVA_BYTE_BUFFER)
            {
                assert(((MemoryBlockByteBuffer)freeMB).getPoolByteBuffer() != null);
            }
            return freeMB;
        }
        
        // check if the memmgr can honor the request
        if ((GLOBAL_UTILS.getUsedSpace(tempUsedSpace) 
                + inpBlockSize > totalMemory))
        {
            MEM_LOG.error(toJSON().toString());
            // throw error if you cannot find free memory block or any error
            String[] args = new String[3];
            args[0] = String.valueOf(inpBlockSize);
            args[1] = String.valueOf(GLOBAL_UTILS.getUsedSpace(usedSpaceIndex
                .get()));
            args[2] = String.valueOf(totalMemory);
            throw (new MemoryManagerException("CRP_MEMMGR_ERROR_002", args));

        }
 
        do
        {
         // save the current value
            tempUsedSpace = usedSpaceIndex.get(); 
            // check for index out of bounds
            if ((GLOBAL_UTILS.getMemBlockIndex(tempUsedSpace) 
                    == GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR - 1))
            {
                throw new MemoryManagerException(
                    "CRP_MEMMGR_ERROR_008",
                    new String[]{String.valueOf(GLOBAL_UTILS
                        .getMemBlockIndex(tempUsedSpace))});
            }
            
            tempUsedSpaceWithIndex = tempUsedSpace + inpBlockSize;
            if (tempUsedSpace != 0)
            {
                // don't increment the index for the first memory block, as
                // the index should be zero for the first element in list.
                tempUsedSpaceWithIndex = GLOBAL_UTILS
                    .incrMemBlockIndex(tempUsedSpaceWithIndex);
            }
            // Now, Atomically update usedSpaceIndex variable.
        }
        while (!usedSpaceIndex.compareAndSet(tempUsedSpace, tempUsedSpaceWithIndex));
        
        // Don't forget to set the indexes in MemoryBlock Object;
        mb.setIndexes(thrdIndex,
            GLOBAL_UTILS.getMemBlockIndex(tempUsedSpaceWithIndex));
        // add it to the MemoryManager's MemBlocks array;
        // this would help the MemManager to keep track of block usage.
        // this is ok to do without synchronization as we already reserved
        // a slot with index.
        mBlocks[GLOBAL_UTILS.getMemBlockIndex(tempUsedSpaceWithIndex)] = mb;
        // update the bitmap index
        memBitMapIndexForThrds[thrdIndex].bsIndexForThisThrd
            .set(GLOBAL_UTILS.getMemBlockIndex(tempUsedSpaceWithIndex));
        memBitMapIndexForThrds[thrdIndex].usedSpaceForThisThrd 
            += inpBlockSize;        
        return mb;
    }

    /**
     * retrieves memory block from the free list.
     * @param thrdIndex thread id requesting for the memory block.
     * @param inpBlockSize size of requested memory block in bytes.
     * @param mbt memory block type to be retrieved from the free list.
     * @param mat memory allocation type.
     * @throws MemoryManagerException on no free blocks available error.
     * @return MemoryBlock 
     */
    
    private static MemoryBlock getMemoryBlockFromFreeList(final short thrdIndex,
        final int inpBlockSize,
        final MemoryBlockType mbt,
        final MemoryAllocationType mat) throws MemoryManagerException
    {
        // we should only look for free blocks when there is no enough room.
        try
        {
            if ( mat == MemoryAllocationType.JAVA_BYTE_BUFFER 
                    || mbt != MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER)
            {
                ;
            }
            else if (freeBlockIndexStack.getTop() != null)
            {
                short memMgrIndex = freeBlockIndexStack.pop();
                
                // Now that you got the index of the memory block,
                // you can do anything with it, as you are the only
                // one looking at it.
                
                mBlocks[memMgrIndex].initialize();
                mBlocks[memMgrIndex].setIndexes(thrdIndex, memMgrIndex);
                memBitMapIndexForThrds[thrdIndex].bsIndexForThisThrd
                    .set(memMgrIndex);
                memBitMapIndexForThrds[thrdIndex].usedSpaceForThisThrd 
                    += mBlocks[memMgrIndex].getSize();
                
                if (GLOBAL_UTILS.memMgrDebug())
                {
                    MEM_LOG.debug(" assigining memory from free block : "
                        + String.valueOf(memMgrIndex) + " thr index: "
                        + String.valueOf(thrdIndex));
                }

                return (mBlocks[memMgrIndex]);
            }

        }
        catch (CRPException e)
        {
            MEM_LOG.error(e.getMessage());
            
        }
        return null;
    }
    
    /**
     * creates object pool memory block and adds into queue.
     * this is a convenience method to directly create object pool
     * memory block along with byte buffer memory block, used for
     * the variable fields inside the object(for which the memory
     * block is being created).
     * @param thrdIndex description of calling module
     * @param numOfObjects number of objects.
     * @param inpBlockSize size of requested memory block in bytes.
     * only used for variable length data types.(for mbb).
     * @param mbt MemoryBlockType to be created.
     * @param of object factory to pass to object pool.
     * @param createMBByteBuffer tells if you want to create
     * MemoryBlockByteBuffer for this object pool. set to true/false
     * accordingly.
     * @return mbo MemoryBlockObjectPool just created.
     * @throws MemoryManagerException when MemManager runs out of space.
     */
    public static MemoryBlockObjectPool<?> createObjectPoolMemoryBlock(
        final short thrdIndex,
        final int numOfObjects,
        final int inpBlockSize, final MemoryBlockType mbt,
        final ObjectFactory of,
        final boolean createMBByteBuffer) throws MemoryManagerException
    {
        MemoryBlockByteBuffer mbb = null;
        // first create a byte buffer mb.
        if(createMBByteBuffer)
        {
            mbb = (MemoryBlockByteBuffer)
            // we have to allocate large array for byte buffer
            // when comparing to the number of objects.
                createMemoryBlock(thrdIndex, inpBlockSize,
                    MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER, null, 
                    MemoryAllocationType.JAVA_BYTE_ARRAY, of.getMyName()
                    + "_mbb");
        }
        
        // now create one for the object pool
        MemoryBlockObjectPool<?> mbo = (MemoryBlockObjectPool<?>)
            createMemoryBlock(thrdIndex, numOfObjects * of.getObjSize(),
                mbt, of, MemoryAllocationType.NOT_VALID,
                of.getMyName() + "_mbo");
        
        mbo.init(mbb);
        // now set the byte buffer memory block for var fields.
        mbo.setMBB(mbb);
        
        return mbo;
    }
    /**
     * reduce the totalUsedSpace for the thread.
     * @param tIndex thread index.
     * @param size size of the memory block.
     * @param memMgrIndex index of memory block in mem mgr.
     */
    public static void reduceUsedSpaceForthisThread(
        final short tIndex, final int size, final short memMgrIndex)
    {
        assert (tIndex >= 0 && size > 0 && memMgrIndex >= 0);
        assert (memBitMapIndexForThrds[tIndex] != null);
       
        // clear the bit for this memory block in the thread(tIndex)
        memBitMapIndexForThrds[tIndex].bsIndexForThisThrd
            .clear(memMgrIndex);
        memBitMapIndexForThrds[tIndex].usedSpaceForThisThrd -= size;
    }
    /**
     * add to the free list of memory manager.
     * @param memMgrIndex index of the memory block in memmgr.
     * @throws CRPException on stack overflow.
     */
    public static void addToFreeList( final short memMgrIndex )
        throws CRPException
    {
        assert(memMgrIndex >= 0 
            && memMgrIndex < GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR);
        // this should be the last thing to be done before freeing.
        fsm.addMBToFreeSpace(mBlocks[memMgrIndex]);
    }
    /**
     * returns the string representation of static MemoryManager class.
     * @return toString of Memory Manager.
     */
    public static String zipString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Mem Mgr Status: "); sb.append(mMStatus);
        if (mMStatus != MemMgrStatus.UNINITIALIZED)
        {
            sb.append("total memory blocks: ");
            sb.append(GLOBAL_UTILS.getMemBlockIndex(usedSpaceIndex.get()));
            sb.append(" total used space: ");
            sb.append(GLOBAL_UTILS.getUsedSpace(usedSpaceIndex.get()));
        }
        return (sb.toString());
    }

    /**
     * dumps the current memory block information in the log.
     */
    public static void dumpMemoryBlockInfo()
    {
        StringBuilder sb = new StringBuilder();
        if (mMStatus != MemMgrStatus.UNINITIALIZED)
        {
            if (GLOBAL_UTILS.memMgrDebug()) 
            {
                for (int i = 0; i < GLOBAL_UTILS.getMemBlockIndex(usedSpaceIndex
                    .get()); i++)
                {
                    sb.append(mBlocks[i].zipString());
                }
                MEM_LOG.info(sb.toString());
            }
        }
        // thread wise used space details.
        StringBuilder sbAcc = new StringBuilder();
        sbAcc.append(System.getProperty("line.separator"));
        for (int i = 0; i < memBitMapIndexForThrds.length; i++)
        {
            if (memBitMapIndexForThrds[i] == null)
            {
                break;
            }
            sbAcc.append(" thread index: ");
            sbAcc.append(i);
            sbAcc.append(" used Space :  ");
            sbAcc.append(memBitMapIndexForThrds[i].usedSpaceForThisThrd);
            sbAcc.append(System.getProperty("line.separator"));
            
            if (GLOBAL_UTILS.memMgrDebug()) 
            {
                for (int j = 0; j <= memBitMapIndexForThrds[i]
                    .bsIndexForThisThrd.size(); j++)
                {
                    sbAcc.append(System.getProperty("line.separator"));
                    if (memBitMapIndexForThrds[i]
                        .bsIndexForThisThrd.get(j))
                    {
                        sbAcc.append("MB Index: ");
                        sbAcc.append(mBlocks[j].getMemMgrIndex());
                        sbAcc.append("MB Size: ");
                        sbAcc.append(mBlocks[j].getSize());
                    }
                    sbAcc.append(System.getProperty("line.separator"));
                }
            }
        }
        //Dump Free MemoryBlocks...
        sbAcc.append(System.getProperty("line.separator"));

        sbAcc.append(freeBlockIndexStack.zipString());
        
        MEM_LOG.info(sbAcc);
    }

    /**
     * returns json representation of memory manager.
     * @return jsonobject.
     */
    public static JSONObject toJSON()
    {
        JSONObject mblockJSON = new JSONObject();
        JSONObject memMgrJSON = new JSONObject();
        try
        {
            for (int i = 0; i < GLOBAL_UTILS.getMemBlockIndex(usedSpaceIndex
                .get()); i++)
            {
                if (mBlocks[i] == null)
                {
                    continue;
                }
                mblockJSON.put(String.valueOf(i), mBlocks[i].toJSON());
            }
            JSONObject tempObj = null;
            JSONObject threadJSON = new JSONObject();
            for (int i = 0; i < memBitMapIndexForThrds.length; i++)
            {
                if (memBitMapIndexForThrds[i] == null)
                {
                    break;
                }
                tempObj = new JSONObject();
                tempObj.put("thread_index", i);
                CRPThread crpt = CRPThreadPoolManager.getCRPThread((short)i);
                if(!crpt.isAlive())
                {
                    tempObj.put("thread_name",
                        crpt.getMyService().getServiceInfo().getName().toString()
                        + String.valueOf(crpt.getCRPThreadID()));
                    tempObj.put("thread_status", crpt.getState().toString());
                }
                else
                {
                    tempObj.put("thread_name", CRPThreadPoolManager
                        .getCRPThread((short)i)
                            .getName());
                    tempObj.put("thread_status", crpt.getState().toString());
                }
                tempObj.put(
                    "used_space",
                    memBitMapIndexForThrds[i].usedSpaceForThisThrd);
                threadJSON.put(String.valueOf(i), tempObj);
                tempObj = null;
            }
            JSONArray jsonA = new JSONArray();
            jsonA.put(new JSONObject().put("thread_usage", threadJSON));
            jsonA.put(new JSONObject().put("memory_blocks", mblockJSON));
            memMgrJSON.put("memory_manager_info", jsonA);
            memMgrJSON.put("total_memory", totalMemory);
            memMgrJSON.put("free_space", String.valueOf(fsm.getFreeSpace()));
            memMgrJSON.put("free-blocks", fsm.toJSON());

        }
        catch (JSONException e)
        {
            MemoryManager.MEM_LOG.error(e.getMessage());
            return null;
        }
        return memMgrJSON;   
    }
    
    /**
     * returns number of memory blocks owned by this thread.
     * @param inpThreadID input thread id.
     * @return number of memory blocks owned by this thread.
     */
    public static final int getNumOfMBsOwnedByMe(final short inpThreadID)
    {
        assert(inpThreadID < GLOBAL_CONSTANTS.MAX_THREADS_IN_PROCESS);
        int mbs = 0;
        if(memBitMapIndexForThrds[inpThreadID] == null)
        {
            MEM_LOG.warn("Thread [" + String.valueOf(inpThreadID) 
                + "]is not registerd or do not own any memory blocks");
            return 0;
        }
        for(int i = 0; i < GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR; i++)
        {
            if(memBitMapIndexForThrds[inpThreadID].bsIndexForThisThrd.get(i))
            {
                mbs++;
            }
        }
        return mbs;
    }
    
    /**
     * returns bitmap index of memory blocks for the given thread id.
     * @param tid input thread id.
     * @return bit set.
     */
    public static final BitSet getMBBitMapStatusForTheThread(final short tid)
    {
        if(memBitMapIndexForThrds[tid] == null)
        {      
            MEM_LOG.warn("thread [" + String.valueOf(tid) + "] is not registered");
            return null;
        }
        return memBitMapIndexForThrds[tid].bsIndexForThisThrd;
    }
    
    /**
     * returns memory block at a given index.
     * @param index index at which mb is requested for.
     * @return mb or null.
     */
    public static final MemoryBlock getMBAtIndex(int index)
    {
        assert(index >= 0 && index < GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR);
        return mBlocks[index];
    }
    /**
     * get memory block at a given index, owned by this thread.
     * @param inpThreadID input thread.
     * @param index index at which memory block is requested.
     * @return memory block object if exists or null.
     */
    public static final MemoryBlock getMemoryBlockForTheThrdAtIndex(final short inpThreadID,
        final int index)
    {
        if(memBitMapIndexForThrds[inpThreadID] == null)
        {          
            return null;
        }
        MemoryBlock mb = null;
        if(memBitMapIndexForThrds[inpThreadID].bsIndexForThisThrd.get(index))
        {
            mb = mBlocks[index];
        }
        return mb;
    }
    /**
     * create a class to hold the information per thread.
     */
    private static class MemoryIndexForThrd
    {
        /**
         * cumulative total used space so far for this thread.
         */
        public long   usedSpaceForThisThrd;
        /**
         * stores all the memmgr indexes of the blocks this thread owns.
         */
        public BitSet bsIndexForThisThrd;

        /**
         * constructor.
         */
        public MemoryIndexForThrd()
        {
            usedSpaceForThisThrd = 0;
            bsIndexForThisThrd = new BitSet(
                GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR);
            bsIndexForThisThrd.clear();
        }
    }
    


}
