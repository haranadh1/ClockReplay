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

package com.crp.db;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.db.FileIO.FileOpenFlag;

/**
 * class to represent all file operations in crp.
 * @author hpoduri
 * @version $Id$
 */
public class FileManager
{
    /**
     * flag to indicate if fm is initialized.
     */
    private boolean isInitialized;
    
    /**
     * file io object.
     */
    private FileIO fIO;
    
    /**
     * directory string.
     */
    private String filePrefix;

    /**
     * file name.
     */
    
    private String fileName;
    
    /**
     * sequence number appended for each file.
     */
    private short seqNo;
    /**
     * default constructor.
     * @param inpDir input directory.
     */
    public FileManager(final String inpDir)
    {
        seqNo = 0;
        filePrefix = inpDir + System.getProperty("file.separator")
            + GLOBAL_CONSTANTS.CRP_FILE_PREFIX;
        fIO = null;
        isInitialized = false;
    }
    
    /**
     * initialize file manager.
     * @param inpFMS file open flag.
     * @throws DBException on error.
     */
    public final void init(final FileOpenFlag inpFMS) throws DBException
    {
        assert(inpFMS != FileOpenFlag.UNINIT);
        fileName = filePrefix + String.valueOf(seqNo);
        fIO = new FileIO(
            fileName, FileIO.IOType.DIRECT_BUFFER,
            GLOBAL_CONSTANTS.MAX_DB_PAGES_PER_IO * GLOBAL_CONSTANTS.DB_PAGE_SIZE,
            GLOBAL_CONSTANTS.DB_MAX_FILE_SIZE, inpFMS);
        isInitialized = true;
    }

    /**
     * true if initialized.
     * @return true/false if init/not init.
     */
    public final boolean isInitialized()
    {
        return isInitialized;
    }
    /**
     * method to write byte array to file.
     * @param poolByteArray array to be written to file.
     * @param length length of data to be written.
     * @throws DBException on error.
     */
    public final void writeToFile(
        final byte[] poolByteArray, final int length) throws DBException
    {
        DBService.DB_SERVICE_LOG.info(" FILE IO: WROTE " + String.valueOf(length));

        fIO.write(poolByteArray, 0, length);
        if(fIO.isFull())
        {
            fileName = filePrefix + String.valueOf(++seqNo);
            fIO.reset(fileName);
        }
    }
    
    /**
     * write EOF.
     * @throws DBException on error.
     */
    public final void closeFile() throws DBException
    {
        DBService.DB_SERVICE_LOG.info(" FILE CLOSED : " + filePrefix);
        fIO.close();
        isInitialized = false;
    }
    
    /**
     * reads data from file.
     * @param poolByteArray byte array.
     * @return bytes actually read.
     * @throws DBException on error.
     */
    public final int readFromFile(
        final byte [] poolByteArray) throws DBException
    {
        return (fIO.read(poolByteArray));
    }
    /**
     * return current file name string.
     * @return string.
     */
    public final String getCurFileName()
    {
        return filePrefix;
    }
}