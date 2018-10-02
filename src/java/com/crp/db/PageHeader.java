/*
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

import java.io.IOException;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.PackUnPackInterface;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * this file implements the page header.
 */
public class PageHeader implements PackUnPackInterface
{
    /**
     * In case we want to use the java serialization, we should have a mechanism
     * of detecting the changes in the class layout that might affect the
     * deserialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * offset in the page where the first caplet starts. NOTE that this need not
     * be 0 always, as a part of the previous caplet might be packed in this
     * page.
     * mostly we use the lower order bits for the offset, as the offset starts
     * from 0. as an optimization of space, we also use the higher order bits for
     * status such as to detect if a caplet is filled partially in the page.
     */
    public int                offsetFlagIndicator;

    /**
     * flag to represent if the last caplet in this page spilled to the next page.
     */
    public static int CAPLET_SPILLED_TO_NEXT_PAGE_FLAG = 0x01000000;
    
    /**
     * mask for the caplet spilled to next page flag.
     */
    public static int CAPLET_SPILLED_TO_NEXT_PAGE_MASK = 0x00ffffff;
    
    /**
     * end of data stream indicator.
     */
    public static int END_OF_DATA_INDICATOR = 0x10000000;
    
    /**
     * mask for end of data flag.
     */
    public static int END_OF_DATA_INDICATOR_FLAG = 0x00ffffff;
    
    /**
     * long is used for timestamp. we represent the timestamp as long variable
     * and java dont have typedef :-(
     */
    public long               startTs;
    public long               endTs;
    public int                numOfRequests;
    public int                numOfResponses;
    /**
     *  this should be numOfRequests + numOfResponses.
     */
    public int                numOfCaplets;

    // private int nextRequestOffset;

    /**
     * there are always some potential risks need to be considered while adding
     * new variable here; should make sure the serialize and deserialize
     * conforming to that change. It is always better to add a variable in the
     * end as opposed to adding it in the middle;
     */

   
    /**
     * constructor.
     */
    public PageHeader(final long startTs, final long endTs,
        final int numOfRequests, final int numOfResponses)
    {
        // , final int nextRequestOffset
        this.startTs = startTs;
        this.endTs = endTs;
        this.numOfRequests = numOfRequests;
        this.numOfResponses = numOfResponses;
        this.offsetFlagIndicator = -1;
        numOfCaplets = 0;
    }

    /**
     * @return the startTs
     */
    public long getStartTs()
    {
        return startTs;
    }

    /**
     * @param startTs the startTs to set
     */
    public void setStartTs(long startTs)
    {
        this.startTs = startTs;
    }

    /**
     * @return the endTs
     */
    public long getEndTs()
    {
        return endTs;
    }

    /**
     * @param endTs the endTs to set
     */
    public void setEndTs(long endTs)
    {
        this.endTs = endTs;
    }

    /**
     * @return the numOfRequests
     */
    public int getNumOfRequests()
    {
        return numOfRequests;
    }

    /**
     * @param numOfRequests the numOfRequests to set
     */
    public void setNumOfRequests(int numOfRequests)
    {
        this.numOfRequests = numOfRequests;
    }

    /**
     * @return the numOfResponses
     */
    public int getNumOfResponses()
    {
        return numOfResponses;
    }

    /**
     * @param numOfResponses the numOfResponses to set
     */
    public void setNumOfResponses(int numOfResponses)
    {
        this.numOfResponses = numOfResponses;
    }

    // /**
    // * @return the nextRequestOffset
    // */
    // public int getNextRequestOffset() {
    // return nextRequestOffset;
    // }
    //
    // /**
    // * @param nextRequestOffset the nextRequestOffset to set
    // */
    // public void setNextRequestOffset(int nextRequestOffset) {
    // this.nextRequestOffset = nextRequestOffset;
    // }

    /**
     * get the size of the PageHeader instance.
     * @return size of the PageHeader(int)
     */
    public final int getSize()
    {
        return (com.crp.common.GLOBAL_CONSTANTS.LONG_SIZE
            + com.crp.common.GLOBAL_CONSTANTS.LONG_SIZE
            + com.crp.common.GLOBAL_CONSTANTS.INT_SIZE + com.crp.common.GLOBAL_CONSTANTS.INT_SIZE); // +
                                                                                                    // com.crp.common.GLOBAL_CONSTANTS.INT_SIZE
    }

    /**
     * serializes the page header into a byte stream and returns the same.
     * @param pk Packer object into which the page header is to be packed into.
     *        the caller of this method should make sure that the packer is set
     *        to correct size, this pack method assumes enough size exists in
     *        Packer object
     * @throws PackUnPackException on error.
     */
    public final void pack(final Packer pk) throws PackUnPackException
    {
        try
        {
            pk.packFixedInt64(startTs);
            pk.packFixedInt64(endTs);
            pk.packFixedInt32(numOfRequests);
            pk.packFixedInt32(numOfResponses);
            pk.packFixedInt32(numOfCaplets);
            pk.packFixedInt32(offsetFlagIndicator);
        }
        catch (IOException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_015", null, this
                .zipString());
        }
    }

    /**
     * tostring implementation.
     * @return ret : string representation of the class
     */
    public final String zipString()
    {

        String ret = " Class : PageHeader" + "\n start timestamp = "
            + this.startTs + "\n end timestamp = " + this.endTs
            + "\n number of requests = " + Integer.toString(this.numOfRequests)
            + "\n number of responses = "
            + Integer.toString(this.numOfResponses);
        // + "\n next request offset = "
        // + Integer.toString(this.nextRequestOffset)
        return ret;
    }

    /**
     * returns the object size.(only for the fixed width variables.
     */
    public final int getObjSize()
    {
        // this is approx size of the fixed width variables in this object.
        return (GLOBAL_CONSTANTS.LONG_SIZE * 2 + GLOBAL_CONSTANTS.INT_SIZE * 2);
    }

    @Override
    public int getPackedBufferSize()
    {
        return (GLOBAL_CONSTANTS.LONG_SIZE * 2 + GLOBAL_CONSTANTS.INT_SIZE * 4);
    }

    @Override
    public void unpack(UnPacker unp) throws PackUnPackException
    {
        try
        {
            this.startTs = unp.unpackFixedInt64();
            this.endTs = unp.unpackFixedInt64();
            this.numOfRequests = unp.unpackFixedInt32();
            this.numOfResponses = unp.unpackFixedInt32();
            this.numOfCaplets = unp.unpackFixedInt32();
            this.offsetFlagIndicator = unp.unpackFixedInt32();
        }
        catch (IOException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_016", null, this
                .zipString());
        }

    }

    /**
     * resets all the fields in page header.
     */
    public final void reset()
    {
        this.offsetFlagIndicator = -1;
        this.startTs = GLOBAL_CONSTANTS.INVALID_TIME_STAMP;
        this.endTs = GLOBAL_CONSTANTS.INVALID_TIME_STAMP;
        this.numOfCaplets = 0;
    }
    
    /**
     * returns true if a caplet in this page is spilled to
     * next page.
     * @return true/false.
     */
    public final boolean isCapletSpilledToNextPage()
    {
        assert(this.offsetFlagIndicator >= 0);
        if((offsetFlagIndicator & CAPLET_SPILLED_TO_NEXT_PAGE_FLAG)
                == CAPLET_SPILLED_TO_NEXT_PAGE_FLAG)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * returns true if this is the last page.
     * @return TRUE/FALSE if this is last page or not.
     */
    public final boolean isThisLastPage()
    {
        if((offsetFlagIndicator & END_OF_DATA_INDICATOR)
                == END_OF_DATA_INDICATOR)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
