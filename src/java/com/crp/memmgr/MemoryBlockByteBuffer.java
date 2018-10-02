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

import java.nio.ByteBuffer;

import com.crp.memmgr.MemoryManager.MemoryAllocationType;


/**
 * MemoryBlock representing byte array(byte buffer)
 * @author hpoduri
 * @version $Id$
 *
 */
public class MemoryBlockByteBuffer extends MemoryBlock 
{
    /**
     * used for offset while writing record.
     * cycles between startWritingRecord and
     * finishWritingRecord.
     */
    private int localOffset; 
    /**
     * byte buffer for direct buffer allocation.
     */
    private ByteBuffer bb;
    /**
     * byte array of MemoryBlock.
     */
    private byte [] pool;
    /**
     * constructor.
     * @param inpBlockSize ref super class constructor.
     * @param mat memory allocation type for this mb.
     * @param inpDesc small note what this mb is used for.
     * @throws MemoryManagerException on error.
     */
    public MemoryBlockByteBuffer(final int inpBlockSize,
        final MemoryAllocationType mat, final String inpDesc)
        throws MemoryManagerException 
    {
        super(inpBlockSize, inpDesc);
        if(getStatus() != MemoryBlockStatus.INIT 
                && getStatus() != MemoryBlockStatus.WRITING)
        {
            throw new MemoryManagerException("CRP_MEMMGR_ERROR_009",
                new String [] {String.valueOf(getStatus())});
        }
        try 
        {
            if(mat == MemoryAllocationType.JAVA_BYTE_ARRAY 
                    || mat == MemoryAllocationType.NOT_VALID)
            {
                pool = new byte [inpBlockSize];
                bb = null;
            }
            else if (mat == MemoryAllocationType.JAVA_BYTE_BUFFER)
            {
                bb = ByteBuffer.allocateDirect(inpBlockSize);
                // cant do this..may be in future java versions.
                //pool = bb.array();
            }
        } 
        catch (OutOfMemoryError e) 
        {
            String [] args = new String [1];
            args[0] = String.valueOf(inpBlockSize);
            MemoryManager.MEM_LOG.error("OutofMemory Error " + e.getMessage());
            throw (new MemoryManagerException("CRP_MEMMGR_ERROR_001",
                args));
        }
        localOffset = 0;
    }
    
    /**
     * returns pool buffer.
     * @return pool.
     */
    public final byte [] getPoolByteArray()
    {
        return pool;
    }
    /**
     * returns pool byte buffer.
     * @return byte buffer.
     */
    public final ByteBuffer getPoolByteBuffer()
    {
        return bb;
    }
    /**
     * adds content to the byte array and updates offset.
     * @param b input string.
     * @return true on success, false on failure to accommodate.
     * @throws MemoryManagerException on error.
     */
    public final boolean addContent(
        final byte [] b) throws MemoryManagerException
    {
        if(b.length > remainingSpace())
        {
            return false;
        }
        System.arraycopy(b, 0,
            getPoolByteArray(), localOffset, b.length);
        localOffset += b.length;
        return true;
    }

    @Override
    public void doBeforeFinishWritingRecord(final int size)
    {
        if(bb != null)
        {
            bb.flip();
        }
        
        assert(getCurOffset() + size == localOffset);
    }

    /**
     * startrecord..
     * addcontent updates local offset..
     * finish writing record(size)
     * size = getLocalOffset()-getCurOffset()
     * returns local offset.
     * @return local offset.
     */
    public final int getLocalOffset()
    {
        return localOffset;
    }
    
    /**
     * updates the local offset.
     * @param inpOffset input offset.
     */
    public final void setLocalOffset(final int inpOffset)
    {
        localOffset = inpOffset;
    }
    
    @Override
    public void doBeforeStartWritingRecord(final int size)
        throws MemoryManagerException
    {
        localOffset = getCurOffset();
        if(size > remainingSpace())
        {
            throw new MemoryManagerException(
                "CRP_MEMMGR_ERROR_010",
                new String[]{String.valueOf(size),
                    String.valueOf(remainingSpace())});
        }
            
    }
    /**
     * resets the local offset. makes this mb ready to read.
     */
    public final void resetLocalOffset()
    {
        localOffset = 0;
    }
    
    /**
     * make the memory block byte buffer ready to read.
     */
    public final void readyToRead()
    {
        resetLocalOffset();
        resetOffset();

    }

    @Override
    public void doBeforeFinishReadingRecord(int size)
    {
        //assert(getCurOffset() + size == localOffset);
        
    }

}
