/*
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

/* this file implements the methods to pack the standard data types */

package com.crp.pkunpk;

import com.crp.common.CRPLogger;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.google.protobuf.CodedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Category;

/**
 * @author hpoduri
 * @version $Id$
 *
 * methods to pack the standard java data types.
 * However, the class maintains a current
 * position in the buffer, which is updated with each pack of the element.
 * So, it should only be used to pack one class at a time.
 * For now, we are going to use the google protocol buffers
 * (but only the pack/unpack methods;
 * however, we do not use anything else(like the google tag format etc.,)
 * google's protocol buffers add parsing, which is not necessary
 * for us and also it slows down quite a bit
 * NOTE: The Packer CLASS  IS NOT THREAD SAFE.
 */
public class Packer 
{

    /**
     * logger for this class.
     */
    public static final Category PCK_LOG = CRPLogger
    .initializeLogger("com.crp.pkunpk.Packer");
    /**
     *  internal buffer stores the packed data.
     */
    private byte[] buffer;
	/**
	 * protocol buffer.
	 */
    private CodedOutputStream ostr;
	/**
	 * MemoryBlock to be used for packing.
	 */
    private MemoryBlockByteBuffer mb;
    
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
     *
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
     * wire header indicates first 8 bytes of any message.
     */
    private boolean wireHeader;
	/**
	 * constructor; should specify the size of the buffer ahead of the time.
	 * knowing the size ahead would help us optimize better
	 * @param size : represents the expected size of the buffer
	 * one thing to know while specifying the size is that there is no
	 * guarantee that the final size should always be equal to the size
	 * specified here. But it is important to specify the maximum space
	 * that the packed buffer can occupy.
	 * @type size : unsigned integer
	 */
    public Packer(final int size) 
    {
		// TODO : add exception handling when done
		// we allocate the buffer with our memory manager; we pass the
		// pre allocated buffer to the protocol buffer api

        buffer = MemoryManager.allocateObject(size);
        ostr = CodedOutputStream.newInstance(buffer, 0, size);
        mb = null;
        sEvent = false;
        fEvent = true;
        wireHeader = false;
    }
	/**
	 * constructor with MemoryBlock initialization.
	 * cool and easy way to deal with memory.
	 * @param inpmb input MemoryBlock to be used for packing.
	 */
    public Packer(final MemoryBlockByteBuffer inpmb) 
    {
        buffer = inpmb.getPoolByteArray();
        ostr = CodedOutputStream.newInstance(buffer,
	        (int) inpmb.getCurOffset(), (int) inpmb.remainingSpace());
        mb  = inpmb;
        sEvent = false;
        fEvent = true;
        wireHeader = false;
    }
    /**
     * constructor with byte buffer initialization.
     * @param inpBB input byte buffer.
     */
    public Packer(final ByteBuffer inpBB)
    {
        buffer = inpBB.array();
        ostr = CodedOutputStream.newInstance(buffer, (int) 0, inpBB.capacity());
        mb = null;
        sEvent = false;
        fEvent = true;
        wireHeader = false;
    }
    /**
     * adds fixed width wire header.
     * <message length><message code>
     * <---4bytes-----><--4bytes---->
     * @param mc : message code to be packed in.
     * @throws PackUnPackException on error.
     */
    public final void addWireHeader(final int mc) throws PackUnPackException
    {
        wireHeader = true;
        try 
        {
            //as we dont know the total size yet, 
            //we set the size to -1, in the end we have to
            // update this field again.
            //NOTE: we should always have the length field first, anything else
            //should follow the length.
     
            int tempSpaceUsed = spaceUsed();
            ostr.writeFixed32NoTag((int) -1);
            ostr.writeFixed32NoTag(mc);
            if(mb != null)
            {
                mb.setLocalOffset(
                    mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
            }
        }
        catch(IOException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_008",
                null, this.toString());
        }
        
    }
	/**
	 * returns the buffer written so far.
	 * @return buffer : byte array
	 */

    public final byte[] getBuffer()
    {
        return buffer;
    }
	/**
	 * return the space used in the packed buffer.
	 * NOTE: DO NOT USE SPACEUSED after finish event.
	 * you should use mb.getCurOffset for space used.
	 * in finishEvent, we are resetting the variables
	 * to pack the real length of the message.
	 * @return space used in the buffer
	 */
    public final int spaceUsed()
    {
        if (mb == null)
        {
            return (buffer.length - ostr.spaceLeft());
        }
        else
        {
            return ((int) mb.getSize() - ostr.spaceLeft());
        }
    }
	/**
	 * pack unsigned int 32; should always use unsigned where it is needed.
	 * packing signed integers take more space bcoz of the sign bit
	 * @param value : represents input unsigned integer value to be packed
	 * @type value : unsigned integer
	 * @throws IOException for buffer overflow or any other error
	 */
    public final void packUInt32(final int value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeRawVarint32(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(tempSpaceUsed - spaceUsed()));
        }
    }

	/**
	 * pack signed int 32; note that it uses the uInt64 for signed integers.
	 * @param value : represents the input integer to be packed
	 * @type value : signed integer
	 * @throws IOException for buffer overflow or any other error
	 */
    public final void packInt32(final int value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeInt32NoTag(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
    }

    /**
     * packs fixed width int 32 value, meaning 4 bytes, no-optimization.
     * @param value input integer to be packed as 4 bytes.
     * @throws IOException on error.
     */
    public final void packFixedInt32(final int value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeFixed32NoTag(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
    }
    
    /**
     * packs fixed width 64 bit int value, meaning 8 bytes no optimization.
     * @param value input 64bit integer value.
     * @throws IOException on error.
     */
    public final void packFixedInt64(final long value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeFixed64NoTag(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
    }
    
	/**
	 * pack unsigned 64 bit integer.
	 * again should use it where ever instead of signed for better
	 * performance
	 * @param value : represents the input unsigned long to be packed
	 * @type value: unsigned long
	 * @throws  IOException for buffer overflow or any other error
	 */
    public final void packUInt64(final long value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeRawVarint64(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
    }

	/**
	 * pack signed 64 bit integers.
	 * @param value : represents the input signed long to be packed
	 * @type value : signed long
	 * @throws IOException for buffer overflow or any other error
	 */
    public final void packInt64(final long value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeInt64NoTag(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
        
    }

	/**
	 * pack string; string is packed with prefixed length.
	 * @param value : represents the input string to be packed
	 * @type value : String
	 * @throws IOException for buffer overflow or any other error
	 */
    public final void packString(final String value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeStringNoTag(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
        
    }

	/**
	 * pack a byte; byte is written as is.
	 * @param value : represents the input byte
	 * @type value : byte
	 * @throws IOException for buffer overflow or any other error
	 */
    public final void packByte(final byte value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeRawByte(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
        
    }

	/**
	 * pack array of raw bytes.
	 * @param value :bytes array
	 * @type value: byte[]
	 * @throws IOException for buffer overflow or any other error
	 */
    public final void packBytes(final byte[] value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        
        // prefix the length of byte array
        ostr.writeRawVarint32(value.length);
        ostr.writeRawBytes(value);
        
        if(mb != null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
    }
    
    /**
     * packs bytes fixed width. dont prefix with the length.
     * it is up to the calling module to handle it.
     * @param value input byte array.
     * @throws IOException on error.
     */
    public final void packBytesFixedWidth(
        final byte [] value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();       
        ostr.writeRawBytes(value);
        if(mb != null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
        
    }
    
    /**
     * pack array of raw bytes.
     * @param value :bytes array
     * @type value: byte[]
     * @param offset : offset in byte array to be considered.
     * @param length : length of byte array to be considered, starting
     *                 from offset.
     * @throws IOException for buffer overflow or any other error
     */
    public final void packBytesFixedWidth(final byte[] value,
        final int offset, final int length) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        //dont have to prefix length, as it is left to
        // the calling module to read the correct number of
        // bytes.
        ostr.writeRawBytes(value, offset, length);

        if(mb != null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }         

        
    }
    
    /**
     * pack array of raw bytes.
     * @param value :bytes array
     * @type value: byte[]
     * @param offset : offset in byte array to be considered.
     * @param length : length of byte array to be considered, starting
     *                 from offset.
     * @throws IOException for buffer overflow or any other error
     */
    public final void packBytes(final byte[] value,
        final int offset, final int length) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        
        // prefix the length of byte array
        ostr.writeFixed32NoTag(length);
        ostr.writeRawBytes(value, offset, length);

        if(mb != null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }         
    }

	/**
	 * pack a float into the buffer.
	 * @param value : represents a java float
	 * @type value : float
	 * @throws IOException for buffer overflow or any other error
	 */
    public final void packFloat(final float value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        ostr.writeFloatNoTag(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
        
    }

	/**
	 * pack a double value.
	 * @param value : represents a java double
	 * @type value: double
	 * @throws IOException for buffer overflow or any other error
	 */
    public final void packDouble(final double value) throws IOException
    {
        int tempSpaceUsed = spaceUsed();
        
        ostr.writeDoubleNoTag(value);
        if(mb !=null)
        {
            mb.setLocalOffset(
                mb.getLocalOffset()+(spaceUsed() - tempSpaceUsed));
        }
        
    }
    
    /**
     * start event.
     * @param size approx size of the record to be written.
     * @throws PackUnPackException when start event called
     * before finishing the previous one.
     */
    public final void startEvent(
        final int size) throws PackUnPackException
    {
        if(sEvent) 
        {
            throw new PackUnPackException("CRP_PKUNPK_ERROR_002",
                null,this.toString());
        }
        try 
        {
            mb.startWritingRecord(size);
        }
        catch (MemoryManagerException e)
        {
            UnPacker.UNP_LOG.error(e.getMessage());
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
                throw new PackUnPackException ("CRP_PKUNPK_ERROR_001",
                    null, this.toString());            
            }
            //when event finished without error, update the offset
            try
            {
                mb.finishWritingRecord(spaceUsed());
            }
            catch (MemoryManagerException e)
            {
                throw new PackUnPackException ("CRP_PKUNPK_ERROR_006",
                    null, this.toString());
            }
            finally 
            {
                fEvent = sEvent = false;
            }
            
            // now check if the wire header is enabled.
            // if yes, we have to update it with the length.
            if (wireHeader)
            {
                try
                {
                    int temp = spaceUsed();
                    ostr.reset();
                    ostr.writeFixed32NoTag(temp);
                }
                catch (IOException e)
                {
                    Packer.PCK_LOG.error(e.getMessage());
                    throw new PackUnPackException(
                        "CRP_PKUNPK_ERROR_008",
                        null,
                        this.toString());
                }
            }
            
            fEvent = true;
            sEvent = false;

        }
    }
    
    /**
     * returns memory block byte buffer.
     * @return mbb.
     */
    public final MemoryBlockByteBuffer getMBB()
    {
        return mb;
    }
    
    /**
     * reset the packer.
     */
    public final void reset()
    {
        if(mb != null)
        {
            mb.reset();
            mb.resetLocalOffset();
        }
        ostr.reset();
    }
    /**
     * override toString for this class.
     * @return string representation fo this class.
     */
    public final String toString() 
    {
        StringBuilder sb = new StringBuilder();
        sb.append("packed bytes so far: ");
        sb.append(this.spaceUsed());
        if (mb != null)
        {
            sb.append("memory block being used:");
            sb.append(this.mb.zipString());
        }
        
        return sb.toString();
    }
    
   
}