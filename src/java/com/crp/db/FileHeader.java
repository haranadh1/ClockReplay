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

import java.io.IOException;

import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * This file implements the file header.
 * 
 */
public class FileHeader {
    /**
     * In case we want to use the java serialization, we should have
     * a mechanism of detecting the changes in the class layout that
     * might affect the de-serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * long is used for timestamp.
     * we represent the timestamp as long variable and java
     * dont have typedef :-(
     */
    private long startTs;
    private long endTs;
    private int numOfSessions;

    /**
     * there are always some potential risks need to be considered
     * while adding new variable here;
     * should make sure the serialize and deserialize
     * conforming to that change.
     * It is always better to add a variable in the end as opposed to
     * adding it in the middle;
     */

    /**
     * constructor for file header.
     * @param startTs : timestamp in milli/nano seconds
     * @param endTs : timestamp in milli/nano seconds
     * @param numOfSessions : number of sessions
     */
    public FileHeader(final long startTs, final long endTs,
        final int numOfSessions) {
        //, final int nextRequestOffset
        this.startTs = startTs;
        this.endTs = endTs;
        this.numOfSessions = numOfSessions;
    }

    /**
     * @return the startTs
     */
    public long getStartTs() {
        return startTs;
    }

    /**
     * @param startTs the startTs to set
     */
    public void setStartTs(long startTs) {
        this.startTs = startTs;
    }

    /**
     * @return the endTs
     */
    public long getEndTs() {
        return endTs;
    }

    /**
     * @param endTs the endTs to set
     */
    public void setEndTs(long endTs) {
        this.endTs = endTs;
    }

    /**
     * @return the numOfSessions
     */
    public int getNumOfSessions() {
        return numOfSessions;
    }

    /**
     * @param numOfSessions the numOfSessions to set
     */
    public void setNumOfSessions(int numOfSessions) {
        this.numOfSessions = numOfSessions;
    }

    /**
     * get the size of the FileHeader instance.
     * @return size of the FileHeader(int)
     */
    public final int getSize() {
        return (com.crp.common.GLOBAL_CONSTANTS.LONG_SIZE
            + com.crp.common.GLOBAL_CONSTANTS.LONG_SIZE
            + com.crp.common.GLOBAL_CONSTANTS.INT_SIZE); // + com.crp.common.GLOBAL_CONSTANTS.INT_SIZE
    }

    /**
     * serializes the page header into a byte stream and returns the same.
     * @param pk Packer object into which the page header is to be
     *        packed into.
     *        the caller of this method should make sure that the packer is set
     *        to correct size, this pack method assumes enough size exists in
     *        Packer object
     */
    public final void pack(final Packer pk) {
        try {
            pk.packUInt64(startTs);
            pk.packUInt64(endTs);
            pk.packUInt32(numOfSessions);
        } catch(IOException e) {
            System.out.println(" exception " + e.getStackTrace());
        }
    }

    /**
     * Unpack FileHeader from given UnPacker object.
     * @param unp UnPacker object from which ch should be unpacked
     * @return ch FileHeader(if successful),if fails returns null
     */
    public static FileHeader unpack(final UnPacker unp) {
        FileHeader ch = null;
        try {
            ch =
                new FileHeader(unp.unpackUInt64(), unp.unpackUInt64(), unp
                    .unpackUInt32()); // , unp.unpackUInt32()
        } catch(IOException e) {
            System.out.println(" IOException " + e.getStackTrace());
        }
        return ch;
    }

    /**
     * tostring implementation.
     * @return ret : string representation of the class
     */
    public final String toString() {
        String ret =
            " Class : FileHeader" + "\n start timestamp = " + this.startTs
                + "\n end timestamp = " + this.endTs
                + "\n number of sessions = "
                + Integer.toString(this.numOfSessions);
        return ret;
    }
}
