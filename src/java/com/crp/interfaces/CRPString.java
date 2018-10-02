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

import java.io.IOException;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.ether.Message;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * A mutable string object with content represented in the form of a byte array.
 * For instance a regular string "abc" is represent as ['a','b', 'c']. The
 * reason for implementing our own string class that it is <B>mutable</B> and
 * hence reuses the same space available in the byte array for replace
 * operation. Please note that this class is {@link java.io.Serializable} since
 * we take care of (un) packing the content as needed. This class is not thread
 * safe.
 * 
 * @author subbu
 * @version $Id$
 */
public final class CRPString extends AbstractInterfaceBase
{
    /**
     * adding/modifying any variable to this might break the
     * getObjectSize method, please check. 
     */
   
    /**
     * store the initial offset in memory block.
     */
    private int offset;
    
    /**
     * stores local current offset, for partial packing crp strings.
     */
    private int localOffset;
    
    /**
     * store the size of this string.
     */
    private int size;

    /**
     * fragment offset.
     * should only be used if this string is a fragment.
     */
    private long fragmentOffset;

    /**
     * flag to indicate if the data in this crp string is partial or not.
     */
    private boolean isPartial;
    
    /**
     * constructor.
     * @param inpMBB memory block byte buffer for var data.
     */
    public CRPString(final MemoryBlockByteBuffer inpMBB)
    {
        this.setMemoryBlocks(inpMBB, null);
        localOffset = 0;
        offset = 0;
        fragmentOffset = -1;
        isPartial = false;
    }

    /**
     * set content.
     * @param inpStr string to be copied from.
     * @return false on out of space, otherwise true.
     */
    public boolean setContent(final String inpStr)
    {
        return(setContent(inpStr.getBytes()));
    }
    /**
     * sets the offset of this string in the mbb.
     * @param off offset index.
     */
    public final void setOffset(final int off)
    {
        offset = off;
    }
    
    /**
     * returns offset of this string in the mbb.
     * @return int.
     */
    public final int getOffset()
    {
        return offset;
    }
    /**
     * set content.
     * @param b byte array to be copied from.
     * @return false on out of space, otherwise true.
     */
    public boolean setContent(final byte[] b)
    {
        offset = getMBB().getLocalOffset();
        if(b.length > getMBB().remainingSpace())
        {
            offset = 0;
            return false;
        }
        try
        {
            /**
             * only call this if crp string is not part of another object created
             * from memory pool.
             */
            if(this.getMBO() != null)
            {
                getMBB().startWritingRecord(b.length);
            }
            getMBB().addContent(b);
            /**
             * we should only call the finishwritingrecord if the object
             * itself is crpstring. however, if crp string is part of another
             * object say, caplet, then ideally this is not the end of record.
             * so, we should not call the finishwritingrecord here.
             * this will only be known based on the getMBO, if we get the crp
             * object from the pool, then we will have a valid MBO.
             */
            if(this.getMBO() != null)
            {
                getMBB().finishWritingRecord(b.length);
            }
        }
        catch (MemoryManagerException e)
        {
            ObjectFactory.OF_LOG.error(e.getMessage());
            return false;
        }
        
        size = b.length;
        return true;
    }
   
    /**
     * Returns the length of the string as a number of bytes.
     * @return The number of bytes in the string.
     */
    public int length()
    {
        return size;
    }

    /**
     * set content length.
     * @param inpLen length of the content.
     */
    public final void setLength(final int inpLen)
    {
        assert(inpLen >= 0);
        size = inpLen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o)
    {
        if (o == null || !(o instanceof CRPString))
        {
            return false;
        }
        
        CRPString b = (CRPString) o;
        
        if (this == b 
                || (getMBB().getPoolByteArray() == null 
                        && b.getMBB().getPoolByteArray() == null))
        {
            return true;
        }
        else
        {
            if (size != b.length())
            {
                return false;
            }
            
            for (int i = 0; i < b.length(); i++)
            {
                if (b.getMBB().getPoolByteArray()[b.offset + i] != this
                    .getMBB()
                    .getPoolByteArray()[this.offset + i])
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * Note: Calling this method will create a new string as it has to.
     * Internally
     * it uses {@link java.lang.StringBuilder} which is not thread safe.
     */
    @Override
    public String toString()
    {
        if (this.getMBB().getPoolByteArray() == null || this.size == 0)
        {
            return this.getClass().getName();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++)
        {
            sb.append((char) this.getMBB().getPoolByteArray()[offset + i]);
        }
        
        String s = sb.toString();
        sb = null;
        
        return s;
    }

    @Override
    public void pack(final Packer pk) throws PackUnPackException
    {    
        try 
        {
            pk.packBytes(
                this.getMBB().getPoolByteArray(), this.offset, this.size);
        }
        catch (IOException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw new PackUnPackException(
                "CRP_PKUNPK_ERROR_006", null, pk.toString());
        }    
    }

    @Override
    public void unpack(final UnPacker unpk) throws PackUnPackException
    {
        offset = getMBB().getCurOffset();
        try
        {
            size = unpk.unpackBytes(this.getMBB(), -1);
               
        }
        catch (IOException e)
        {
            offset = 0;
            UnPacker.UNP_LOG.error(e.getMessage());
            throw new PackUnPackException ("CRP_PKUNPK_ERROR_005", null,
                Message.class.getName());
        }
        try 
        {
            if(getMBO() != null)
            {
                getMBB().finishWritingRecord(size);
            }
        }
        catch (MemoryManagerException e)
        {
            UnPacker.UNP_LOG.error(e.getMessage());
        }
    }

    @Override
    public Object createObjectInstance(MemoryBlockByteBuffer mbb)
    {
        // you can set it null in this case,
        // as while creating as part of the object pool,
        // setMemoryBlocks will again be called.
        return (new CRPString(mbb));
    }

    @Override
    public int getObjSize()
    {
        // for now lets do it the hard way.
        
        return ( (3*GLOBAL_CONSTANTS.INT_SIZE) 
                + GLOBAL_CONSTANTS.LONG_SIZE
                + GLOBAL_CONSTANTS.LONG_SIZE
                //adding it for reference variable.
                //add the max possible value.
                + GLOBAL_CONSTANTS.LONG_SIZE);
    }

    @Override
    public void resetObject(final Object obj)
    {
        // TODO Auto-generated method stub
        
    }
    @Override
    public String getMyName()
    {
        return GLOBAL_CONSTANTS.CRPObjNameStrings.CRP_STRING_KEY;
    }
    @Override
    public boolean anyVarFields()
    {
        return true;
    }

    @Override
    public int getPackedBufferSize()
    {
        // size + prefixed fixed (4byte)int 32 length.
        return ( GLOBAL_CONSTANTS.INT_SIZE + size );
    }
    
    /**
     * convenience method to pack partial data.
     * @param pk input packer object.
     * @param inpOffset offset in the string, from where
     * the data to be packed.
     * @param inpSize size of the data to be packed.
     * @throws PackUnPackException on error.
     */
    public final void packPartial(
        final Packer pk, final int inpOffset, 
        final int inpSize) throws PackUnPackException
    {
        if(inpOffset == 0)
        {
            // reset the local offset;
            // useful if the crp string reset object is not called
            // for any reason.
            localOffset = 0;
        }
        if(inpSize > this.size)
        {
            Packer.PCK_LOG.error(
                "pack partial: Input Size bigger than crp string size");
            throw new PackUnPackException(
                "CRP_PKUNPK_ERROR_006", null, pk.toString());
        }
        try 
        {
            if(localOffset != 0)
            {
                pk.packBytesFixedWidth(
                    this.getMBB().getPoolByteArray(),
                    offset + localOffset, inpSize);
            }
            else
            {
                pk.packBytes(
                    this.getMBB().getPoolByteArray(),
                    localOffset + offset, inpSize);
            }
        }
        catch (IOException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw new PackUnPackException(
                "CRP_PKUNPK_ERROR_006", null, pk.toString());
        }
        localOffset = localOffset + inpSize;
    }

    /**
     * convenience method to unpack partial data.
     * @param unp input unpacker object.
     * @param inpOffset offset in the string, from where
     * the data to be unpacked.
     * @param inpSize size of the data to be unpacked.
     * @throws PackUnPackException on error.
     */
    public final void unpackPartial(
        final UnPacker unp, final int inpOffset, 
        final int inpSize) throws PackUnPackException
    { 
        
        if(inpSize > this.size)
        {
            Packer.PCK_LOG.error(
                "pack partial: Input Size bigger than crp string size");
            throw new PackUnPackException(
                "CRP_PKUNPK_ERROR_005", null, unp.toString());
        }
        try 
        {
            if(localOffset != 0)
            {
                unp.unpackBytesFixedWidth(
                    this.getMBB(), inpSize);
            }
            else
            {
                unp.unpackBytes(
                    this.getMBB(),
                     inpSize);
            }
        }
        catch (IOException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw new PackUnPackException(
                "CRP_PKUNPK_ERROR_005", null, unp.toString());
        }
        localOffset = localOffset + inpSize;
        assert(localOffset <= size);
    }
    
    @Override
    public boolean anyRoomForObjOfVarLength(final int length)
    {
        if(getMBB().remainingSpace() >= length)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public int getVarLenFieldsSize()
    {
        return size;
    }

    @Override
    public void reset()
    {
        offset = 0; localOffset = 0; size = 0;
        fragmentOffset = -1;
    }

    @Override
    public void reset(MemoryBlockByteBuffer inpMBB)
    {
        this.setMemoryBlocks(inpMBB, null);
        
    }

    /**
     * sets offset for this fragment string.
     * useful only if this crp string is part of the
     * fragmented caplet.
     * @param seq (in case of tcp, this would be a tcp sequence number).
     */
    public void setFragmentOffset(final long seq)
    {
        fragmentOffset = seq;
        
    }
    /**
     * returns offset of this fragment in the network packet.
     * @return long.
     */
    public final long getFragmentOffset()
    {
        return fragmentOffset;
    }

    /**
     * set a flag to indicate that the data is partial.
     */
    public void setPartialData()
    {
        isPartial = true;
    }
    
    /**
     * set the flag as completed, meaning the crp string has full data.
     */
    public void setCompleted()
    {
        isPartial = false;
    }
    
}