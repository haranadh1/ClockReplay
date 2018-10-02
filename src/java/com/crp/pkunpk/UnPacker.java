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

/** this file implements the methods to unpack the standard data
 *  types from the buffer*/

/**
 * @author hpoduri
 * @version $Id$
 */
package com.crp.pkunpk;

import com.crp.common.CRPLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.ether.MESSAGE_CODE;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManagerException;
import com.google.protobuf.CodedInputStream;
import java.io.IOException;

import org.apache.log4j.Category;
/**
 * this class has methods to unpack the standard datatypes.
 * these were packed by the methods in Packer class
 * NOTE: THIS CLASS IS NOT THREAD SAFE
 */
public class UnPacker 
{
    /**
     * logger for this class.
     */
    public static final Category UNP_LOG = CRPLogger
    .initializeLogger("com.crp.pkunpk.UnPacker");
    /**
     * protobuffer.
     */
    private final CodedInputStream istr;

    /**
     * Memory Block.
     */
    private final MemoryBlockByteBuffer mb;
    
    
    /**
     *              Why do we need startEvent and endEvent??
     *              ----------------------------------------
     * think of it as a transaction (like begin trans/end trans)
     * if any error happens in between, the whole changes will be lost
     * ex: lets say that you are packing a Caplet object into memory block,
     * before you start writing a first Caplet class member into packer,
     * you should call startEvent(), when you are done writing all the 
     * variables then you should again call endEvent().
     * if there any error occurs in between, all the changes you have written
     * will be lost and you have to start over.
     * NOTE that nested events are not supported. you should always consider the
     * setting the events before packing/unpacking on the highest level class.
     */
    /**
     * boolean to represent when a thread starts writing a record into mb.
     * writing a record is considered as an event.
     */
    private boolean sEvent;
    /**
     * boolean to represent the end of writing record(event).
     */
    private boolean fEvent;
	/**
	 * constructor.
	 * @param inpBuf : input buffer to read from
	 * @type inpBuf: byte array
	 * @param size : length of input buffer
	 * @type size : integer
	 */
    public UnPacker(final byte[] inpBuf, final int size)
    {
        istr = CodedInputStream.newInstance(inpBuf, 0, size);
        mb = null;
    }

    /**
     * constructor that takes MemoryBlock.
     * @param inpMb input memory block to read from.
     */
    public UnPacker(final MemoryBlockByteBuffer inpMb)
    {
        istr = CodedInputStream.newInstance(inpMb.getPoolByteArray(),
            inpMb.getCurOffset(), inpMb.remainingSpace());
        mb = inpMb;
    }
	// Unpack methods
	/**
	 * Unpacks the unsigned integer from the packed buffer.
	 * @throws IOException when error
	 * @return type: integer
	 */
    public final int unpackUInt32() throws IOException
    {
        return istr.readRawVarint32();
    }

	/**
	 * Unpacks the signed integer from the packed buffer.
	 * @throws IOException when error
	 * @return type: integer
	 */
    public final int unpackInt32() throws IOException
    {
        return istr.readRawVarint32();
    }

    /**
     * unpacks fixed width 4 byte integer.
     * @return integer 4byte width.
     * @throws IOException on error.
     */
    public final int unpackFixedInt32() throws IOException
    {
        return istr.readFixed32();
    }
    
    /**
     * unpacks 8 byte integer.
     * @return integer 8 byte width.
     * @throws IOException on error.
     */
    public final long unpackFixedInt64() throws IOException
    {
        return istr.readFixed64();
    }
	/**
	 * Unpacks the unsigned long integer from the packed buffer.
	 * @throws IOException when error
	 * @return type: long
	 */
    public final long unpackUInt64() throws IOException
    {
        return istr.readRawVarint64();
    }

	/**
	 * Unpacks the signed long integer from the packed buffer.
	 * @throws IOException when error
	 * @return type: long
	 */
    public final long unpackInt64() throws IOException
    {
        return istr.readRawVarint64();
    }

	/**
	 * Unpacks the string from the packed buffer.
	 * @throws IOException when error
	 * @return type: String
	 */
    public final String unpackString() throws IOException
    {
        return istr.readString();
    }

	/**
	 * Unpacks the byte (single byte) from the packed buffer.
	 * @throws IOException when error
	 * @return type: byte
	 */
    public final byte unpackByte() throws IOException
    {
        return istr.readRawByte();
    }

	/**
	 * Unpacks array of raw bytes from the packed buffer.
	 * @throws IOException when error
	 * @return type: byte []
	 */
    public final byte[] unpackBytes() throws IOException
    {
        // read the length of the byte array first
        final int size = istr.readRawVarint32();
        return istr.readRawBytes(size);
    }

    /**
     * reads byte array fixed width.
     * @param inpByteArray dest array to be copied into.
     * @param inputLen length to be copied from the buffer.
     * @param offset offset in the byte array from where the content
     * to be copied.
     * @throws IOException on error.
     */
    public final void unpackBytesFixedWidth(
        final byte [] inpByteArray, final int inputLen, 
        final int offset) throws IOException
    {
        istr.readRawBytes(inputLen, inpByteArray, offset);
    }
    /**
     * Unpacks array of raw bytes from the packed buffer into the given buffer.
     * @param inpBuffer byte array to be read into. 
     * @param inpOffset offset in byte array to be considered.
     * @throws IOException when error
     * @return type: size of bytes read.
     */
    public final int unpackBytes( final byte [] inpBuffer,
        final int inpOffset) throws IOException
    {
        // read the length of the byte array first
        final int size = istr.readFixed32();
        istr.readRawBytes(size, inpBuffer, inpOffset);
        return size;
    }
    
    /**
     * unpacks array of bytes from the buffer based on offset, length.
     * @param poolByteArray array to copy data from
     * @param inpOffset offset in the array to start copy data from.
     * @param inpSize size of data to be copied.
     * @return returns number of bytes read.
     * @throws IOException on error.
     */
    public final int unpackBytes(
        final byte[] poolByteArray,
        final int inpOffset, final int inpSize) throws IOException
    {
        // read the length of the byte array first
        final int size = istr.readFixed32();
        assert(size >= inpSize);
        istr.readRawBytes(inpSize, poolByteArray, inpOffset);
        return size; 
    }
    /**
     * convenience function to unpack into another memory block byte buffer.
     * @param mbb memory block byte buffer.
     * @param inpLen length of bytes to unpack;
     *  -1 indicate unpack all the bytes.
     * @return size of bytes unpacked.
     * @throws IOException on error.
     */
   
    public final int unpackBytes(final MemoryBlockByteBuffer mbb,
        int inpLen) throws IOException
    {
        // read the length of the byte array first
        final int size = istr.readFixed32();
        assert(size >= inpLen);
        if(inpLen == -1)
        {
            // read the whole thing.
            inpLen = size;
        }
        istr.readRawBytes(inpLen, mbb.getPoolByteArray(), mbb.getLocalOffset());
        mbb.setLocalOffset(mbb.getLocalOffset() + inpLen);
        return size; 
    }
    
    /**
     * convenience method to unpack fixed byte array into mbb.
     * @param mbb target mbb to unpack bytes into.
     * @param inpLen length of bytes to be unpacked.
     * @return number of bytes unpacked.
     * @throws IOException on error.
     */
    public final int unpackBytesFixedWidth(final MemoryBlockByteBuffer mbb,
        final int inpLen) throws IOException
    {
        assert(inpLen < mbb.getSize());
        istr.readRawBytes(inpLen,
            mbb.getPoolByteArray(), mbb.getLocalOffset());     
        mbb.setLocalOffset(mbb.getLocalOffset() + inpLen);
        return inpLen;
    }
	/**
	 * Unpacks float value from the packed buffer.
	 * @throws IOException when error
	 * @return type : float
	 */
    public final float unpackFloat() throws IOException
    {
        return istr.readFloat();
    }

	/**
	 * Unpacks double value from the packed buffer.
	 * @return type: double
	 * @throws IOException when error
	 */
    public final double unpackDouble() throws IOException
    {
        return istr.readDouble();
    }
    /**
     * start event.
     * @throws PackUnPackException when start event called
     * before finishing the previous one.
     */
    public final void startEvent() throws PackUnPackException
    {
        if(sEvent) 
        {
            throw new PackUnPackException("CRP_PKUNPK_ERROR_004",
                null, this.toString());
        }
        sEvent = true;
        fEvent = false;
    }
    /**
     * Finish event.
     * @throws PackUnPackException when trying to finish event,
     * which was not started to begin with.
     */
    public final void finishEvent() throws PackUnPackException
    {
        if(mb != null)
        {
            if (!sEvent)
            {
                //reset to make sure the next one writes correctly
                fEvent = true;
                sEvent = false;
                throw new PackUnPackException ("CRP_PKUNPK_ERROR_003",
                    null, this.toString());            
            }
            //when event finished without error, update the offset
            try 
            {
                mb.finishWritingRecord(istr.getTotalBytesRead());
            }
            catch (MemoryManagerException e)
            {
                fEvent = sEvent = false;
                UnPacker.UNP_LOG.error(e.getMessage());
                throw new PackUnPackException(
                    "CRP_PKUNPK_ERROR_007", null, this.toString());
            }
            fEvent = true;
            sEvent = false;

        }
    }
    /**
     * unpack wire header length.
     * NOTE: we can do length and message code at once by creating
     * a new class, but creating a new class in java is a heap operation
     * again triggered by java gc.
     * should be the first four bytes in any message buffer.
     * @return length (total length of the message returned)
     * @throws PackUnPackException on error.
     */
    public final int unpackWireHeaderLength() throws PackUnPackException
    {
        int length = -1;
        try
        {
            length = istr.readFixed32();
        }
        catch(IOException e)
        {
            UnPacker.UNP_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_009",
                null, this.toString());
        }
        return length;
    }
    /**
     * unpacks wire header message code.
     * @return mc message code.
     * @throws PackUnPackException on error.
     */
    public final int unpackWireHeaderMessageCode() throws PackUnPackException
    {
        int mc = MESSAGE_CODE.INVALID_MESSAGE;
        try
        {
            mc = istr.readFixed32();
        }
        catch(IOException e)
        {
            UnPacker.UNP_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_009",
                null, this.toString());
        }
        return mc;
    }
    
    /**
     * sets the buffer position in the memory block byte buffer.
     * useful to read data from a random position.
     * NOTE: random reads do not update the memory block local offset.
     * it is the responsibility of the caller. should not be used
     * for sequential reads in the buffer.
     * @param inpPosn pointer in the buffer.
     */
    public final void setPosition(final int inpPosn)
    {
        istr.setPosition(inpPosn);
    }
    
    /**
     * returns the underlying mbb.
     * @return memory block byte buffer used to unpack.
     */
    public final MemoryBlockByteBuffer getMBB()
    {
        return mb;
    }
    
    /**
     * returns the current position in the buffer.
     * @return integer.
     */
    public final int getCurrentPosition()
    {
        return istr.getCurrentPosition();
    }
    
    /**
     * to string representation of this unpacker object.
     * @return string rep of this obj.
     */
    public final String toString()
    {
        StringBuilder sb = new StringBuilder(
            GLOBAL_CONSTANTS.MEDIUM_STRING_SIZE);
        sb.append(" position: " + String.valueOf(istr.getCurrentPosition()));
        if(mb != null)
        {
            sb.append("\n mbb: " + mb.zipString());
        }
        return sb.toString();
    }

    /**
     * resets the unpacker object.
     */
    public final void reset()
    {
        istr.setPosition(0);
        if( mb != null)
        {
            mb.reset();
            mb.resetLocalOffset();
        }
        
    }
}
