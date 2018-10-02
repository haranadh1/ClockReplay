/**
 *
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of ClockReplay Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or
 * copyright law.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from ClockReplay Incorporated.
 */


package com.crp.memmgr;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONException;
import org.json.JSONObject;

import com.crp.common.CRPException;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.LockFreeStack;
import com.crp.common.LockFreeStack.Item;
import com.crp.memmgr.MemoryManager.MemoryAllocationType;
import com.crp.memmgr.MemoryManager.MemoryBlockType;

/**
 * free space manager.
 * memory blocks after being freed, added to the map.
 * key would be string, that represents the memory block.
 * value would be a lock free stack of all the memory blocks
 * for the key(string).
 *
 */
public class FreeSpaceManager
{
    /**
     * indicates free space in the mem mgr.
     * Note that this is not synchronized, so at any point of time
     * we get a rough free space, not exact.
     */
    private AtomicLong freeSpace;
    
    /**
     * map indicating all the freed memory blocks.
     * for now lets use java concurrent hash map, as freeing mb is
     * very rare in crp.
     */
    private ConcurrentHashMap<String, LockFreeStack<Short>> fsMap;
    
    /**
     * constructor.
     */
    public FreeSpaceManager()
    {
        fsMap = new ConcurrentHashMap<String, LockFreeStack<Short>>();
        freeSpace = new AtomicLong(0);
    }
    
    /**
     * returns string name for the memory block.
     * to be used as the key for the free space manager map.
     * @param mb input memory block to which name needs to be generated.
     * @return string object.
     */
    public static String generateKeyForMemoryBlock(final MemoryBlock mb)
    {
        StringBuilder sb = new StringBuilder();
        if(mb instanceof MemoryBlockObjectPool<?>)
        {
            MemoryBlockObjectPool<?> mbo = (MemoryBlockObjectPool<?>) mb;
            sb.append(mbo.getObjectName());
            //sb.append("_" + String.valueOf(mbo.getNumOfObjects()));
        }
        else
        {
            MemoryBlockByteBuffer mbb = (MemoryBlockByteBuffer) mb;
            if(mbb.getPoolByteBuffer() != null)
            {
                sb.append("mb_byte_buffer");
                
            }
            else
            {
                sb.append("mb_byte_array");
            }
            sb.append("_" + String.valueOf(mb.getSize()));
        }
        return sb.toString();
    }
    
    /**
     * add memory block to free space manager.
     * @param mb
     */
    public final void addMBToFreeSpace(final MemoryBlock mb)
    {
        String str = generateKeyForMemoryBlock(mb);

        LockFreeStack<Short> lfsVal = fsMap.get(str);
        if(lfsVal == null)
        {
            lfsVal 
                = new LockFreeStack<Short>(
                        GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR);
            fsMap.putIfAbsent(str, lfsVal);
        }
        else
        {
            try
            {
                lfsVal.push(mb.getMemMgrIndex());
            }
            catch (CRPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // now that we are done adding a entry; add to the free space now.
        long oldVal = 0;
        long newVal = 0;
        do
        {
            oldVal = freeSpace.get();
            newVal = oldVal + mb.getSize();
        }while (!freeSpace.compareAndSet(oldVal, newVal));
        MemoryManager.MEM_LOG.info("ADD:  key: " + str + " , value Index : " + mb.getMemMgrIndex());
        
    }
    
    /**
     * returns current free space.
     * @return long
     */
    public final long getFreeSpace()
    {
        return freeSpace.get();
    }
    /**
     * returns free memory block index from free space manager.
     * it tries to match all the input parameters to pick the
     * correct matching memory block needed.
     * @param mbt memory block type.
     * @param mat memory allocation type.
     * @param of object factory.
     * @param blockSize input memory block size.
     * @return memory block index.
     */
    public final short getMBFromFreeSpaceManager(
        final MemoryBlockType mbt,
        final MemoryAllocationType mat, final ObjectFactory of,
        final int blockSize)
    {
        StringBuilder sb = new StringBuilder();
        if(mbt == MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL)
        {
            sb.append(of.getMyName());
        }
        else
        {
            if(mat == MemoryAllocationType.JAVA_BYTE_BUFFER)
            {
                sb.append("mb_byte_buffer");
                
            }
            else
            {
                sb.append("mb_byte_array");
            }
            sb.append("_" + String.valueOf(blockSize));
        }
        
        LockFreeStack<Short> lfs = fsMap.get(sb.toString());
        if(lfs == null)
        {
            return -1;
        }
        
        short mIndex = -1; 
        try
        {
            mIndex = lfs.pop();
        }
        catch (CRPException e)
        {
            MemoryManager.MEM_LOG.error(e.getMessage());
            mIndex = -1;
        }
        
        if(mIndex >= 0)
        {
            // update the free space.
            long oldVal = 0;
            long newVal = 0;
            do
            {
                oldVal = freeSpace.get();
                newVal = oldVal - blockSize;
            }while (!freeSpace.compareAndSet(oldVal, newVal));
        }
        MemoryManager.MEM_LOG.info(" RETRIEVE: key: " + sb.toString() + " , value Index : " + String.valueOf(mIndex));

        return (mIndex);
    }
    
    /**
     * convert the free memory block info to json.
     * TODO : this method is not thread safe.; should make it thread safe.
     * can crash.
     * @return JSONObject.
     */
    public final JSONObject toJSON()
    {
        Iterator itr = null;
        JSONObject mblockJSON = new JSONObject();

        itr = fsMap.values().iterator();
        while(itr.hasNext())
        {
            LockFreeStack <Short> indexStack;
            indexStack = (LockFreeStack<Short>)itr.next();
            
            Item<Short> index = (Item<Short>) indexStack.getTop();
            while(index != null)
            {
                if(index.data < 0 
                        || index.data > GLOBAL_CONSTANTS.MAX_MEM_BLOCKS_IN_MEM_MGR)
                {
                    index = index.next;
                    continue;
                }
                MemoryBlock mb = MemoryManager.getMBAtIndex(index.data);
                if(mb == null)
                {
                    index = index.next;
                    continue;
                }
                try
                {
                    mblockJSON.put(String.valueOf(index), mb.toJSON());
                }
                catch (JSONException e)
                {
                    MemoryManager.MEM_LOG.error(e.getMessage());
                    return null;
                }
                index = index.next;
            }
        }
        return mblockJSON;
    }
}
