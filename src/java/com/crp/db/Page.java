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
import com.crp.interfaces.AbstractInterfaceBase;
import com.crp.interfaces.Caplet;
import com.crp.interfaces.CapletHeader;
import com.crp.interfaces.CommInterface;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManagerException;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * Page implementation. A page consists of multiple caplets, up to a maximum
 * length of GLOBAL_CONSTANTS.MAX_DB_PAGE_SIZE.
 * 
 * PAGE-CAPLET ARITHMETIC
 *  __________________________________________________________
 * | caplet         | payload |                                |
 * | header         | len(4b) |                                |
 * |--------- ----------------|                                |
 * |                          |                                |
 * |     PAYLOAD              |                                |
 * |                          |                                |
 * |                          |                                |
 * |__________________________|                                |
 * |                                                           |
 * |                                                           |
 * |                                                           |
 * |                                                           |
 * |                                                           |
 * |                                                   ________|
 * |                                                  | page   |
 * |                                                  |header  |
 * |__________________________________________________|________|
 * 
 */
public class Page extends AbstractInterfaceBase
{
    /**
     * Page = PageHeader + payload consisting of Caplets.
     */
    private PageHeader   ph;
    
    /**
     * represents current used space in this page.
     */
    private int currentUsedSpace;

    
    /**
     * filler to be filled in the page(if required).
     * this space can be used by any other field in future.
     */
    public static final String filler = "FILLERFILLERFILLERFILLERFILLERFILLERFILLERFILLERFILLERFILLERFILLERFILLERFILLER";
    
    /**
     * flags to represent unpack type of the current caplet.
     */
    /**
     * full caplet unpack.
     */
    public static int CAPLET_FULL_UNPACK_FLAG = 1;
    /**
     * partial caplet unpack.
     */
    public static int CAPLET_PARTIAL_UNPACK_FLAG = 2;
    /**
     * unpack caplet with no caplet header.
     */
    public static int CAPLET_UNPACK_NO_CAPLET_HEADER = 4;
    /**
     * unpack partial caplet with no caplet header.
     */
    public static int CAPLET_PARTIAL_UNPACK_NO_CAPLET_HEADER = CAPLET_PARTIAL_UNPACK_FLAG 
    | CAPLET_UNPACK_NO_CAPLET_HEADER;
    
    /**
     * constructor. nothing to do for now.
     * @param inpPH : PageHeader
     */
    public Page(final PageHeader inpPH)
    {
        currentUsedSpace = 0;
        if(inpPH == null)
        {
            ph = new PageHeader(GLOBAL_CONSTANTS.INVALID_TIME_STAMP,
                GLOBAL_CONSTANTS.INVALID_TIME_STAMP, -1, -1);
        }
        else
        {
            ph = inpPH;
        }
    }

    /**
     * @return the ph
     */
    public final PageHeader getPageHeader()
    {
        return ph;
    }

    /**
     * @param ph the ph to set
     */
    public final void setPageHeader(final PageHeader ph)
    {
        this.ph = ph;
    }



    /**
     * returns the size of the Caplet.
     * @return caplet size (int)
     */
    public final int getSize()
    {
        int totalSize = ph.getSize();
        
        return totalSize;
    }

    
    /**
     * tostring implementation.
     * @return ret : string representation of the class
     */
    public final String toString()
    {
        String ret = " Class : Page" + "\n" + ph.zipString(); 
        return ret;
    }

    @Override
    public void unpack(UnPacker unp) throws PackUnPackException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean anyVarFields()
    {
        //set it false, as we dont want
        //memory block byte buffer created for the db page.
        // we instead create a packer object(mbb) and copy
        //the contents.
        return false;
    }

    @Override
    public Object createObjectInstance(final MemoryBlockByteBuffer mbb)
    {
        return (new Page(null));
    }

    @Override
    public String getMyName()
    {
        return GLOBAL_CONSTANTS.CRPObjNameStrings.DB_PAGE_KEY;
    }

    @Override
    public int getObjSize()
    {
        return ph.getObjSize();
    }

    @Override
    public void resetObject(Object obj)
    {
        if(obj == null)
        {
            currentUsedSpace = 0;
        }
        
    }
    
    /**
     * space used for caplet header.
     * @param ch input caplet header object.
     * @return int represents the space needed for ch.
     */
    public final int spaceForCapletHeader(final CapletHeader ch)
    {
        //we leave some buffer, 4 bytes for prefix length of crp string
        // and another 4 bytes free for now.
        int buffer = GLOBAL_CONSTANTS.INT_SIZE * 2;
        return (ch.getPackedBufferSize() + buffer);
    }
    
    /**
     * returns size of the caplet payload data to be packed.
     * @param c input caplet.
     * @return integer represents the caplet size to be packed.
     */
    public final int currentCapletSizeToPack(final Caplet c)
    {
        if(c.getCFT().getOffset() > 0)
        {
            return (c.getPackedBufferSize() - c.getCFT().getOffset());
        }
        else
        {
            return c.getPackedBufferSize();
        }
    }
    
    /**
     * adds caplet to this db page.
     * @param c input caplet to packed into a db page.
     * @param pk packer object to pack caplet contents.
     * @throws DBException on error.
     */
    public final void addCaplet(final Caplet c,
        final Packer pk) throws DBException
    {
        assert(currentUsedSpace() + currentFreeSpace() 
            == totalAvailableSpaceInPage());
        
        int packedCapletSize = c.getPackedBufferSize();
        int tempCurrentUsedSpace = currentUsedSpace;
        // first check if this page can accommodate this caplet.
        if(currentFreeSpace() < currentCapletSizeToPack(c))
        {
            //fill as much payload as we can fit in a page.
            if (c.getCFT().getOffset() == 0) 
                // indicates caplet should be
                // packed from the beginning.
            {
                if (currentFreeSpace() < spaceForCapletHeader(c
                    .getCapletHeader()))
                {
                    // cant do much in this case, we dont want to split
                    // caplet header across multiple pages.
                    c.getCFT().setOffset(0);
                    // means we dont use the caplet.
                      
                    //call finish record for the remaining bytes in the page.
                    try
                    {
                        pk
                            .getMBB()
                            .finishWritingRecord(addPageHeader(pk, true));
                    }
                    catch (MemoryManagerException e)
                    {

                        throw new DBException(
                            "CRP_DATABS_ERROR_006",
                            new String[]{pk.toString(), this.zipString()});
                    }
                    return;
                }
            }
            // partial caplet fill case.
            try
            {
                // means we have to fill from the caplet header.
                // i.e, from the beginning of the caplet.
                int bytesWritten = 0;
                if (c.getCFT().getOffset() == 0)
                {
                    c.getCapletHeader().setLength(
                        c.getPayLoadObject().length());
                    c.getCapletHeader().pack(pk);
                    currentUsedSpace += c.getCapletHeader().getPackedBufferSize();
                    bytesWritten += c.getCapletHeader().getPackedBufferSize();
                    c.getPayLoadObject().packPartial(pk,
                        c.getCFT().getOffset(),
                        currentFreeSpace() - GLOBAL_CONSTANTS.INT_SIZE);
                    // -4 is for the prefixed length for crp string(payload) data.
                    bytesWritten += currentFreeSpace();
                    currentUsedSpace += currentFreeSpace();
                    c.getCFT().setOffset(bytesWritten);
                    ph.offsetFlagIndicator = 
                        PageHeader.CAPLET_SPILLED_TO_NEXT_PAGE_FLAG 
                        | ph.offsetFlagIndicator;
                    DBService.DB_SERVICE_LOG.info(
                        " Caplet Filled Partially : " );
                    if(ph.offsetFlagIndicator < 0)
                    {
                        //update the start offset, or ignore.
                        ph.offsetFlagIndicator = pk.getMBB().getLocalOffset();
                        ph.startTs = c.getCapletHeader().getTimestamp();
                    }
                    ph.endTs = c.getCapletHeader().getTimestamp();
                    ph.numOfCaplets++;
                    c.getCFT().setPageAppenderLen(addPageHeader(pk, true));
              
                    assert(c.getCFT().getOffset() <= packedCapletSize);
                    return;
                }
                else
                {
                    bytesWritten = c.getCFT().getOffset();
                    int tempSpace = currentFreeSpace();
                    c.getPayLoadObject().packPartial(
                        pk,
                        c.getCFT().getOffset(),
                        currentFreeSpace());
                    bytesWritten += tempSpace;
                    c.getCFT().setOffset(bytesWritten);
                    assert(c.getCFT().getOffset() <= packedCapletSize);
                    
                    //partial caplet written. update the page start offset.
                    //this indicates a new db page with prev caplet partial fill.
                    ph.offsetFlagIndicator = 
                        PageHeader.CAPLET_SPILLED_TO_NEXT_PAGE_FLAG 
                        | ph.offsetFlagIndicator;
                    c.getCFT().setPageAppenderLen(addPageHeader(pk, true));
                    return;
                }

            }
            catch(PackUnPackException e)
            {
                DBService.DB_SERVICE_LOG.error(e.getMessage());
                c.getCFT().setOffset(0);
                currentUsedSpace = tempCurrentUsedSpace;
                assert(c.getCFT().getOffset() <= packedCapletSize);

                throw new DBException("CRP_DATABS_ERROR_006", new String[] {
                    pk.toString(), this.zipString()
                });
            }    
        }
        else
        {
            // caplet pay load can fit in this page.
            // see if this caplet has been filled partially in prev page.
            // offset != 0 indicates that this caplet is previously packed
            // partially in another page.
            try
            {
                if (c.getCFT().getOffset() > 0)
                {
                    // pack the remaining bytes.
                    int remainingSpace = packedCapletSize
                        - c.getCFT().getOffset();
                    
                    assert(remainingSpace > 0);
                    c.getPayLoadObject().packPartial(
                        pk,
                        c.getCFT().getOffset(),
                        remainingSpace);
                    currentUsedSpace += remainingSpace;
                    c.getCFT().setOffset(
                        c.getCFT().getOffset() + remainingSpace);
                    assert(c.getCFT().getOffset() <= packedCapletSize);
                    ph.endTs = c.getCapletHeader().getTimestamp();

                    try
                    {
                        // we should count the exact number of bytes
                        // since the last finish writing record.
                        // hence, adding the page header + filler length.
                        if(pk.getMBB().getCurOffset() == 0)
                        {
                            // meaning, this is a packer from new message.
                            // we should only update the finishWritingRecord
                            // with remainingSpace
                            pk.getMBB().finishWritingRecord(
                                remainingSpace);
                        }
                        else
                        {
                            // same message, but a different page.
                            // then we should update for the whole record.
                            pk.getMBB().finishWritingRecord(
                                packedCapletSize 
                                + c.getCFT().getPageAppenderLen());
                        }
                    }
                    catch (MemoryManagerException e)
                    {
                        DBService.DB_SERVICE_LOG.error(e.getMessage());
                        throw new DBException("CRP_DATABS_ERROR_006",
                            new String[] {
                            pk.toString(), this.zipString()
                        });
                     
                    }
                }
                else
                {
                    if(ph.offsetFlagIndicator < 0)
                    {
                        //update the start offset, or ignore.
                        ph.offsetFlagIndicator = pk.getMBB().getLocalOffset();
                        ph.startTs = c.getCapletHeader().getTimestamp();
                    }
                    c.pack(pk);
                    currentUsedSpace += packedCapletSize;
                    try
                    {
                        pk.getMBB().finishWritingRecord(
                            packedCapletSize + c.getCFT().getPageAppenderLen());
                    }
                    catch (MemoryManagerException e)
                    {
                        DBService.DB_SERVICE_LOG.error(e.getMessage());
                        throw new DBException("CRP_DATABS_ERROR_006", new String[] {
                            pk.toString(), this.zipString()
                        });
                    }
                    ph.endTs = c.getCapletHeader().getTimestamp();
                    c.getCFT().setOffset(packedCapletSize);
                    ph.numOfCaplets++;
                    
                }
            }
            catch(PackUnPackException e)
            {
                DBService.DB_SERVICE_LOG.error(e.getMessage());
                c.getCFT().setOffset(0);
                currentUsedSpace = tempCurrentUsedSpace;
                assert(c.getCFT().getOffset() <= packedCapletSize);

                throw new DBException("CRP_DATABS_ERROR_006", new String[] {
                    pk.toString(), this.zipString()
                });
            }
            
        }
        
    }
    
    /**
     * string representation of page.
     * @return string object.
     */
    private String zipString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" page header: " + ph.zipString());
        sb.append("used space: " + this.currentUsedSpace);
        sb.append(" total available space in the page: " 
            + this.totalAvailableSpaceInPage());
        
        return sb.toString();
    }

    @Override
    public int getPackedBufferSize()
    {
        return GLOBAL_CONSTANTS.DB_PAGE_SIZE;
    }

    @Override
    public void pack(Packer pk) throws PackUnPackException
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * shows current free space in this db page.
     * @return integer.
     */
    public final int currentFreeSpace()
    {
        return(totalAvailableSpaceInPage() - currentUsedSpace());
    }
    
    /**
     * returns current used space in the page.
     * @return int.
     */
    public final int currentUsedSpace()
    {
        return currentUsedSpace;
    }

    /**
     * total available size in this page.
     * @return space available.
     */
    public final int totalAvailableSpaceInPage()
    {
        return (GLOBAL_CONSTANTS.DB_PAGE_SIZE - ph.getPackedBufferSize()); 
    }
    
    /**
     * adds page header in the end.
     * @param pk input packer object to pack the page header into.
     * @param needFiller true if filling is needed if there is space left
     * between currentUsedSpace and totalAvailableSpace.
     * @return returns page appender length;
     * page appender length = "filler_length" + "page_header_length";
     * @throws DBException on error.
     */
    public final int addPageHeader(final Packer pk,
        final boolean needFiller) throws DBException
    {
        int pageAppenderLength = 0;
        {
            pageAppenderLength = (
                    totalAvailableSpaceInPage() - currentUsedSpace());
            try
            {
                if(needFiller)
                {
                    for(int i = 0; i < pageAppenderLength; i++)
                    {
                        pk.packByte((byte)'C');
                    }
                }
            }
            catch (IOException e)
            {
                DBService.DB_SERVICE_LOG.error(e.getMessage());
                throw new DBException("CRP_DATABS_ERROR_007", null);
            }
        }
        
        
        try
        {
            ph.pack(pk);
        }
        catch(PackUnPackException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DATABS_ERROR_007", null);
        }
        pageAppenderLength += ph.getPackedBufferSize();
        if(ph.isThisLastPage())
        {
            DBService.DB_SERVICE_LOG.info(
                " adding page header with  end of data: "
                + Integer.toHexString(ph.offsetFlagIndicator));
        }
        else
        {
            DBService.DB_SERVICE_LOG.info(
                " adding page header without  end of data: "
                + Integer.toHexString(ph.offsetFlagIndicator));
        }
        return pageAppenderLength;
    }
    
    /**
     * extracts page header from the unpacker.
     * @param unp input unpacker object.
     * @throws DBException on error.
     */
    public final void extractPageHeader(final UnPacker unp) throws DBException
    {
        // read the last ph.getPackedBufferSize() bytes
        unp.setPosition(unp.getMBB().getCurOffset() 
            + GLOBAL_CONSTANTS.DB_PAGE_SIZE - ph.getPackedBufferSize());
        
        try
        {
            ph.unpack(unp);
        }
        catch (PackUnPackException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            unp.setPosition(unp.getMBB().getCurOffset());
            throw new DBException("CRP_DATABS_ERROR_008", null);
        }
        unp.setPosition(unp.getMBB().getCurOffset());
    }
    
    /**
     * extracts next caplet from the page.
     * you should call Caplet.getPayloadLength and see
     * if the payload can fit in the caplet's mbb.
     * this method assumes that the caplet has enough
     * space to fit the payload extracted.
     * @param unp unpacker.
     * @param c caplet to be updated.
     * @param extractionType how a caplet to be extracted.
     * @return returns the number of payload bytes unpacked.
     * @throws DBException on error.
     */
    public final int extractCaplet(
        final UnPacker unp,
        final Caplet c, 
        final int extractionType) throws DBException
    {
        int retPayLoadLenExtracted = -1;
        try
        {
            if (extractionType == CAPLET_FULL_UNPACK_FLAG)
            {
                int packedDataSize = c.readPayloadLength(unp, 0);
                assert(packedDataSize >= 0 
                    && packedDataSize < GLOBAL_CONSTANTS.DB_PAGE_SIZE);
                
                // set the content length, which is required
                // for the caplet object to return correct
                // packed buffer size.
                c.getPayLoadObject().setLength(packedDataSize);
                c.unpack(unp);
                // update mbb offset by adding the caplet size.
                c.getMBB().finishWritingRecord(c.getPayLoadObject().length());
                retPayLoadLenExtracted = packedDataSize;
            }
            else if (extractionType == CAPLET_PARTIAL_UNPACK_FLAG)
            {
                int capPayLoadDataInThisPage = c.readPayloadLength(unp, 0);
                
                c.getCapletHeader().unpack(unp);
                
                // set the content length, which is required
                // for the caplet object to return correct
                // packed buffer size.
                c.getPayLoadObject().setLength(c.getCapletHeader().getLength());

                // now .. do the partial unpacking until the end of page.

                c.getPayLoadObject().unpackPartial(
                    unp,
                    unp.getCurrentPosition(),
                    capPayLoadDataInThisPage);
                c.getCFT().setOffset(capPayLoadDataInThisPage);
                retPayLoadLenExtracted = capPayLoadDataInThisPage;
            }
            else if(extractionType == CAPLET_PARTIAL_UNPACK_NO_CAPLET_HEADER)
            {
                c.getPayLoadObject().unpackPartial(unp,
                    unp.getMBB().getCurOffset(),
                    c.getPayLoadObject().length() - c.getCFT().getOffset() );
                retPayLoadLenExtracted = c.getPayLoadObject().length() 
                    - c.getCFT().getOffset();
                c.getMBB().finishWritingRecord(c.getPayLoadObject().length());

            }
        }
        catch (PackUnPackException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DATABS_ERROR_009", null);
        }
        catch (MemoryManagerException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DATABS_ERROR_009", null);
        }
        return (retPayLoadLenExtracted);
    }
    @Override
    public boolean anyRoomForObjOfVarLength(int length)
    {
        return false;
    }

    @Override
    public int getVarLenFieldsSize()
    {
        return -1;
    }

    /**
     * check to see if this page is valid/contains valid data.
     * @return true/false whether the page is valid or not..
     */
    public final boolean isValid()
    {
        if(ph.getSize() == GLOBAL_CONSTANTS.INVALID_TIME_STAMP)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

   
    
    
    /**
     * resets the current page.
     */
    public final void reset()
    {
        this.currentUsedSpace = 0;
        this.ph.reset();
        
    }

    @Override
    public void reset(MemoryBlockByteBuffer inpMBB)
    {
        // TODO Auto-generated method stub
        
    }
}
