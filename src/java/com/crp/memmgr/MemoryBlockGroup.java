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
/**
 * Memory Block Group class.
 * class that stores (fixed/variable) number of memory blocks.
 * memory blocks can be of different types.
 * only one thread can operate on one memory block group.
 * @author hpoduri
 * @version $Id$
 */

public class MemoryBlockGroup
{
    /**
     * memory block array.
     */
    private MemoryBlock [] mbg;
    /**
     * current index in memory block array.
     */
    private int index;
    /**
     * constructor.
     * @param size number of memory blocks in the array.
     */
    public MemoryBlockGroup(final int size)
    {
        mbg = new MemoryBlock[size];
        index = 0;
    }
    /**
     * add memory block to memory block group array.
     * not thread safe.
     * @param mb memory block to be added.
     * @return index of the memory block just added.
     */
    public final int addMemoryBlock(final MemoryBlock mb)
    {
        int lindex = index;
        mbg[index++] = mb;
        return lindex;
    }
    /**
     * returns the memory block at a given location.
     * @param index index of the memory block in MemoryBlockGroup array.
     * @return memory block at the index.
     */
    public final MemoryBlock getMemoryBlock(final int index)
    {
        return mbg[index];
    }
}
