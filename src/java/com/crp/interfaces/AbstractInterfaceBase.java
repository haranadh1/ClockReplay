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

package com.crp.interfaces;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryBlockObjectPool;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * base object for any interface in CRP.
 * for now, the main purpose of the object is to
 * store the memory blocks that this object depends on.
 * 
 * @author hpoduri
 * @version $Id$
 */
public abstract class AbstractInterfaceBase implements CommInterface
{
    /**
     * indicates if this object is valid or not.
     */
    private boolean isValid;
    
    /**
     * byte buffer memory block for the interface object.
     * all variable data should go from here.
     */
    private MemoryBlockByteBuffer mbb;
    /**
     * object pool memory block, from where this object is
     * allocated.
     */
    private MemoryBlockObjectPool<?> mbo;
    /**
     * set the memory blocks to be used by the objects later on.
     * every object should use one memory block for the object pool
     * and one for the variable length fields inside the object.
     * @param inpMBB byte buffer mb
     * @param inpMBO object pool mb
     */
    public final void setMemoryBlocks( final MemoryBlockByteBuffer inpMBB,
        final MemoryBlockObjectPool<?> inpMBO)
    {
        mbb = inpMBB;
        mbo = inpMBO;
        isValid = true;
    }
    
    /**
     * makes the obj invalid.
     */
    public final void setInvalid()
    {
        isValid = false;
    }
    
    /**
     * returns true/false if valid/invalid.
     * @return boolean.
     */
    
    public boolean isValid()
    {
        return isValid;
    }
    /**
     * returns byte buffer mb.
     * @return mbb.
     */
    public final MemoryBlockByteBuffer getMBB()
    {
        return mbb;
    }
    /**
     * returns object pool mb.
     * @return mbo.
     */
    public final MemoryBlockObjectPool<?> getMBO()
    {
        return mbo;
    }
    
    @Override
    public abstract void pack(Packer pk) throws PackUnPackException;
    
    @Override
    public abstract void unpack(UnPacker unp) throws PackUnPackException;
    
    @Override
    public abstract Object createObjectInstance(MemoryBlockByteBuffer mbb);
    
    @Override
    public abstract String getMyName();
    
    @Override
    public int getObjSize()
    {
        return (2*GLOBAL_CONSTANTS.LONG_SIZE);  
    }
    
    @Override
    public abstract void resetObject(Object obj);
    
}
