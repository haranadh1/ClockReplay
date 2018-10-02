/*
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains
 * the property of ClockReplay Incorporated and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or
 * copyright law.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from ClockReplay Incorporated.
 */
package com.crp.db;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import com.crp.common.GLOBAL_CONSTANTS;

/**
 * Write a file with the given data in DB Page Size blocks.
 * Gives a generic read/write interface for file handling.
 * currently memory mapped and direct buffers are supported,
 * which can be chosen at the time of this class instantiation.
 */
public class FileIO
{
 
    /**
     * reusable direct byte buffer.
     */
    private ByteBuffer       byteBuffer;

    /**
     * file name to be used.
     */
    private String           fileName;
    
    /**
     * file output stream object for sequential operations.
     */
    FileOutputStream fos;

    /**
     * random access file object.
     */
    private RandomAccessFile raf;
    
    /**
     * file channel to be used with raf.
     */
    private FileChannel      channel;

    /**
     * file manager status.
     */
    public enum FileOpenFlag
    {
        UNINIT,
        OPEN_FOR_READ,
        OPEN_FOR_WRITE,
        OPEN_FOR_READ_WRITE,
    }
    /**
     * fms status variable.
     */
    private FileOpenFlag fof;
    
    
    /**
     * memory mapped buffer.
     * cant be reused as it is created by java api.
     */
    private MappedByteBuffer mbb = null;

    /**
     * memory mapped buffer size to be used for memory mapped file.
     */
    private long memoryMappedBufferSize = GLOBAL_CONSTANTS.DB_PAGE_SIZE;

    /**
     * indicates max file size. note that this is not exact
     * file size we eventually create, we may add another buffer after
     * a file reached max size.
     */
    private long maxFileSize;
    /**
     * memory mapped buffer offset.
     */
    private long fileOffset = 0;

    /**
     * Specifies io type.
     *
     */
    public enum IOType 
    {
        INVALID_TYPE,
        /**
         * uses java nio memory mapped interface.
         * NOTE: useful only with large files.
         * do not use this if you are reading small portion
         * of the file.
         */
        MEMORY_MAPPED,
        /**
         * used java nio, with direct buffer.
         */
        DIRECT_BUFFER,
        /**
         * regular java buffered read/write interface.
         */
        FILE_BUFFERS,
    }

    /**
     * to know if a file reached its max size or not.
     */
    private boolean isFull;
    /**
     * IO type for this file.
     */
    private IOType iot;
    
    /**
     * constructor.
     * @param fileName name of the file to be used for subsequent
     * operations.
     * @param inpIOT type to used for this file operations.
     * @param fileBufferSize direct buffer size to be allocated for file write.
     * @param inpMaxFileSize max file size after which roll over should happen.
     * @param inpFOF input file open flag.
     * @throws DBException on error.
     */
    public FileIO(
        final String fileName,
        final IOType inpIOT, final int fileBufferSize,
        final long inpMaxFileSize,
        final FileOpenFlag inpFOF) throws DBException
    {
        this.fileName = fileName;
        iot = inpIOT;
        isFull = false;
        maxFileSize = inpMaxFileSize;
        fof = inpFOF;
        fos = null;
        raf = null;
        if(iot == IOType.DIRECT_BUFFER)
        {
            byteBuffer = ByteBuffer
            .allocateDirect(fileBufferSize);
            // Explicitly setting byte order
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        else if(iot == IOType.MEMORY_MAPPED)
        {
            byteBuffer = null;
            memoryMappedBufferSize = fileBufferSize;
        }
        else
        {
            assert(false);
        }
        this.open(fof);
    }

    /**
     * returns memory mapped buffer size.
     * @return long value.
     */
    public final long getMemoryMappedBufferSize()
    {
        return memoryMappedBufferSize;
    }

    /**
     * sets memory mapped buffer size.
     * @param memoryMappedBufferSize buffer size to be set.
     */
    public final void setMemoryMappedBufferSize(
        final long memoryMappedBufferSize)
    {
        // Add a check ensuring that this size is <= 1GB
        this.memoryMappedBufferSize = memoryMappedBufferSize;
    }

    /**
     * write interface to direct buffer with position.
     * @param dataBytes input data.
     * @param position from which data should be written.
     * @param length  length of data to be written.
     * @throws DBException on error.
     */
    private void writeToDirectBuffer(
        final byte[] dataBytes, final int position,
        final int length) throws DBException
    {
        assert(length > 0);
        DBService.DB_SERVICE_LOG.info(" length to be written to file : " + String.valueOf(length));
        try
        {
            byteBuffer.clear();
            byteBuffer.put(dataBytes, position, length);
            byteBuffer.flip();
            channel.write(byteBuffer);
            fileOffset += (dataBytes.length - position);
            if(fileOffset > maxFileSize)
            {
                isFull = true;
            }
        }
        catch (IOException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DB_ERROR_003", new String[]{fileName});
        }
    }
    
    /**
     * generic write interface.
     * @param dataBytes input byte buffer to be written.
     * @throws DBException on error.
     */
    public final void write(final byte[] dataBytes) throws DBException
    {
        if(iot == IOType.DIRECT_BUFFER)
        {
            writeToDirectBuffer(dataBytes, 0, dataBytes.length);
        }
        else if(iot == IOType.MEMORY_MAPPED)
        {
            writeToMemoryMappedBuffer(dataBytes);
        }
        else
        {
            // should not be here.
            assert(false);
        }
    }

    /**
     * write from a position in the buffer.
     * @param dataBytes input buffer.
     * @param position position from which the input buffer should be written.
     * @param length length of data from the position to be written.
     * @throws DBException on error.
     */
    public final void write(
        final byte[] dataBytes, final int position,
        final int length) throws DBException
    {
        if(iot == IOType.DIRECT_BUFFER)
        {
            writeToDirectBuffer(dataBytes, position, length);
        }
        else if(iot == IOType.MEMORY_MAPPED)
        {
            writeToMemoryMappedBuffer(dataBytes);
        }
        else
        {
            // should not be here.
            assert(false);
        }
    }

    /**
     * 
     * @param readBytes array to read data into.
     * @return length of bytes read.
     * @throws DBException on error.
     */
    public final int read(
        final byte[] readBytes) throws DBException
    {
        int retLen = -1;
        try
        {
            byteBuffer.clear();
            retLen = channel.read(byteBuffer);
            byteBuffer.flip();
            if(retLen > 0)
            {
                byteBuffer.get(readBytes, 0, retLen);
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            DBService.DB_SERVICE_LOG.info(" Length of the byte buffer for copy: " 
                + String.valueOf(retLen) + " byte buffer limit: " + readBytes.length);
            
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DB_ERROR_004", new String[]{fileName});
        }
        catch (IOException e)
        {
            DBService.DB_SERVICE_LOG.info(" Length of the byte buffer for copy: " + String.valueOf(retLen));
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DB_ERROR_004", new String[]{fileName});
        }
        catch(BufferUnderflowException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DB_ERROR_004", new String[]{fileName});
        }
        return retLen;
    }

    public void seek(long position) throws DBException
    {
        try
        {
            assert(raf != null);
            raf.seek(position);
        }
        catch (IOException e)
        {
            throw new DBException("CRP_DB_ERROR_005", new String[]{
                String.valueOf(position), fileName});
        }
    }

    public ByteOrder getOrder()
    {
        return byteBuffer.order();
    }

    private void writeToMemoryMappedBuffer(byte[] dataBytes) throws DBException
    {
        resetMBBIfRequired(dataBytes);
        try
        {
            mbb.put(dataBytes);
        }
        catch (BufferOverflowException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DB_ERROR_003", new String[]{fileName});
        }
    }

    public void readFromMemoryMappedBuffer(byte[] readBytes) throws DBException
    {
        resetMBBIfRequired(readBytes);
        try
        {
            mbb.get(readBytes);
        }
        catch (BufferUnderflowException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DB_ERROR_004", new String[]{fileName});
        }
    }

    private void resetMBBIfRequired(byte[] inputBytes) throws DBException
    {
        //validation.
        
        if(inputBytes.length != GLOBAL_CONSTANTS.DB_PAGE_SIZE)
        {
            throw new DBException("CRP_DATABS_ERROR_004", new String[]{
                String.valueOf(inputBytes.length),
                String.valueOf(GLOBAL_CONSTANTS.DB_PAGE_SIZE), fileName});
            
        }
        if (null == mbb || memoryMappedBufferSize == mbb.position())
        {
            long channelSize = 0;
            long maxSize = memoryMappedBufferSize;
            try
            {
                channelSize = channel.size();
                if (maxSize > channelSize)
                {
                    maxSize = channelSize;
                }
                mbb = channel.map(
                    FileChannel.MapMode.READ_WRITE,
                    fileOffset,
                    maxSize);
            }
            catch(IOException e)
            {
                DBService.DB_SERVICE_LOG.error(e.getMessage());
                throw new DBException("CRP_DATABS_ERROR_003",
                    new String[]{fileName});
            }
        }
        fileOffset += inputBytes.length;
    }

    /**
     * opens a file in read write mode.
     * if memory mapped, this creates a dummy file
     * with max size, later would be updated. this is
     * the fastest way to load data into a file in java.
     * @param inpFOF file open flag.
     * @throws DBException on error.
     */
    public final void open(final FileOpenFlag inpFOF) throws DBException
    {
        // Create a file, open for writing, and get a FileChannel
        try
        {
            if(inpFOF == FileOpenFlag.OPEN_FOR_WRITE)
            {
                raf = null;
                fos = new FileOutputStream(fileName);
                channel = fos.getChannel();

                //raf = new RandomAccessFile(fileName, "rw");
            }
            else if(inpFOF == FileOpenFlag.OPEN_FOR_READ)
            {
                raf = new RandomAccessFile(fileName, "r");
                channel = raf.getChannel();

            }
            else if(inpFOF == FileOpenFlag.OPEN_FOR_READ_WRITE)
            {
                raf = new RandomAccessFile(fileName, "rw");
                channel = raf.getChannel();
            }
            else
            {
                assert(false);
            }
            if((channel.size() == 0) && (iot == IOType.MEMORY_MAPPED))
            {
                //empty file, create a file with max file size.
                // close the file first and create it with max size.
                this.close();
                this.createFileWithMaxSize();
                raf = new RandomAccessFile(fileName, "rw");
                channel = raf.getChannel();
            } 
        }
        catch (FileNotFoundException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DATABS_ERROR_002",
                new String[]{fileName});
        }
        catch(IOException e)
        {
            DBService.DB_SERVICE_LOG.info("some thing went wrong...");
            DBService.DB_SERVICE_LOG.error(e.toString());
            throw new DBException("CRP_DATABS_ERROR_003",
                new String[]{fileName});
        }
    }

    /**
     * close the file.
     * @throws DBException on error.
     */
    public final void close() throws DBException
    {
        try
        {
            // channel.force(false);
            channel.close();
            if(raf != null)
            {
                raf.close();
            }
            if(fos != null)
            {
                fos.close();
            }
        }
        catch (IOException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DB_ERROR_002", new String[]{fileName});
        }
    }
    
    /**
     * creates a file with max size.
     * @throws DBException on error.
     */
    public final void createFileWithMaxSize() throws DBException
    {
        try
        {
            RandomAccessFile f = new RandomAccessFile(fileName, "rw");
            f.seek((long)(GLOBAL_CONSTANTS.DB_MAX_FILE_SIZE) -1 );
            f.writeByte('a');
        }
        catch(IOException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DATABS_ERROR_003",
                new String[]{fileName});
        }
    }
    /**
     * returns if the file is full.
     * @return boolean true/false if file is full or not.
     */
    public final boolean isFull()
    {
        return isFull;
    }

    /**
     * closes the current file.
     * reset the current file name to new one.
     * opens the new file.
     * @param newFileName new file name.
     */
    public final void reset(final String newFileName) throws DBException
    {
        this.close();
        fileName = newFileName;
        this.open(fof); 
    }
}
