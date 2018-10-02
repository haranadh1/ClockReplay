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

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS.DBAccessMode;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.MessageGroup;
import com.crp.ether.Photon;
import com.crp.ether.Message.MessageStatus;
import com.crp.interfaces.Caplet;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadPoolManager;

/**
 * class that manages db pages.
 * fills the db pages with the caplets. maintains a list of
 * db pages.
 * @author hpoduri
 * @version $Id$
 */
public class DBPageManager
{
	/**
	 * db access mode.
	 */
	private DBAccessMode dbm;
	
    /**
     * total number of caplets written/read so far.
     */
    private int totalNumOfCaplets;
    
    /**
     * flag to indicate if this object is initialized.
     */
    private boolean isInitialized;
    
    /**
     * represents db pages in memory.
     */
    private int numOfDBPagesInMemory;
    /**
     * message group for db pages.
     */
    private MessageGroup mgPages;
    
    /**
     * current DB Page Message.
     */
    private Message currentPageMsg;
    
    /**
     * current Caplet Message.
     */
    private Message currentCapletMsg;
    
    /**
     * represents the current page.
     */
    private Page currentPage;

    /**
     * store pending caplet.
     * caplet spilled across multiple msg buffers
     * can be stored here.
     */
    private Caplet pendingCaplet;
    
    /**
     * constructor.
     */
    public DBPageManager()
    {
        isInitialized = false;
        currentPageMsg = null;
        currentPage = null;
        pendingCaplet = null;
        totalNumOfCaplets = 0;
        dbm = DBAccessMode.UNINIT;
        
    }
    /**
     * initialize the db pages from the pool.
     * @param inpNumOfDBPages number of db pages should be kept
     * in memory before start writing to the disk.
     * @param inpDBM db access mode; based on which we initiate necessary
     * data structures in the page manager.
     * @throws DBException on error.
     */
    public final void init(final int inpNumOfDBPages,
        final DBAccessMode inpDBM) throws DBException
    {
        isInitialized = true;
        pendingCaplet = null;

        dbm = inpDBM;
        
        ObjectFactory of = new Page(null);
        numOfDBPagesInMemory = inpNumOfDBPages;
        /** create input memory block size from the number of pages */
        try
        {
            mgPages = new MessageGroup(GLOBAL_CONSTANTS.NO_DB_PAGE_MSGS);
            mgPages.initMsgs(of, numOfDBPagesInMemory, -1);
            currentPageMsg = getNextAvailablePageMsg();
            currentPage = (Page) getNextAvailablePage();
            CRPThread crpt = CRPThread.getCurrentThread();
            if(dbm == DBAccessMode.SEQUENETAL_READ)
            {
                // do necessary initialization specific to db read.
                currentPage.getPageHeader().reset();
                ObjectFactory ofcap = new Caplet(null);
                try
                {
                    if (crpt.getMyMsgWorld().getFreeMsg(
                        GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY) == null)
                    {
                        // make sure you have enough space in the
                        // memory block that can fit in all the pages
                        // that are read from io. which is max_db_pages_per_io.
                        crpt.getMyMsgWorld().createMsgGroupPool(
                            ofcap,
                            GLOBAL_CONSTANTS.KB,
                            GLOBAL_CONSTANTS.MAX_DB_PAGES_PER_IO
                                * GLOBAL_CONSTANTS.DB_PAGE_SIZE);
                        
                    }
                }
                catch (EtherException e)
                {
                    DBService.DB_SERVICE_LOG.error(e.getMessage());
                    throw new DBException("CRP_DATABS_ERROR_005", null);
                }
                currentCapletMsg = getNextAvailableCapletMsg();
            }
            assert(currentPage != null);
            currentPage.resetObject(null);
        }
        
        catch(EtherException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DATABS_ERROR_005", null);
        }      
        of = null;
        
    }
    
    /**
     * returns if this object is initialized or not.
     * @return true/false.
     */
    public final boolean isInitialized()
    {
        return isInitialized;
    }
    /**
     * add caplet to the current db page.
     * @param c input caplet to be added to db page.
     * @param phToIO photon to connect to io.
     * used to flush channel and wake up io thread,
     * when trying to get a free msg.
     * @return returns message to be sent to io thread.
     * if the current caplet can fit in the current message,
     * then null is returned.
     * @throws DBException on error.
     */
    public final Message addCaplet(final Caplet c,
        final Photon phToIO) throws DBException
    {
        assert (currentPage != null);
        /**
         * message to be sent to IO.
         */
        Message retMsg = null;
        
        /**
         * lets setup the CapletFillTracker object.
         * and set the offset, which is the length of the caplet to be
         * packed into the db page. we again have to check this after 
         * calling addcaplet, as this method returns the number of bytes
         * successfully packed(in the offset variable).
         */
        
        //this loop continues until we fill this caplet in a/multiple pages.
        int packedDataSize = 0;
        
        c.getCFT().setPageAppenderLen(0);
        while (true)
        {
            c.getCFT().setOffset(packedDataSize);
            
            assert(currentPage != null);
            assert(currentPageMsg != null);
            assert(currentPageMsg.getPackerForObjs() != null);
            currentPage.addCaplet(c, currentPageMsg.getPackerForObjs());
            packedDataSize = c.getCFT().getOffset();
           
            if(c.getPackedBufferSize() == packedDataSize)
            {
                //caplet fit in the current page no worries. end of story.
                break;
            }
            else
            {
                if(retMsg != null)
                {
                    // meaning that, even after getting a new page, the current
                    // caplet for some reason did not go in.
                    int i = 0;
                    i++;
                }
                Page tempPage = currentPage;
                
                tempPage.getPageHeader();
                DBService.DB_SERVICE_LOG.info(" NUM CAPLETS IN THIS PAGE: " 
                    + String.valueOf(currentPage.getPageHeader().numOfCaplets));
                totalNumOfCaplets += currentPage.getPageHeader().numOfCaplets;
                
                currentPage = getNextAvailablePage();
                if(currentPage == null)
                {
                    // means we should move to the next message and
                    // we should send this msg to IO.
                    retMsg = currentPageMsg;
                    assert(retMsg.getPackerForObjs().getMBB().getCurOffset() > 0);
                    //finish writing the last bytes of the current caplet.
                    // we cannot do it inside page, as it does not have the
                    // knowledge about the next page is in a different page.
                    try
                    {
                        currentPageMsg
                            .getPackerForObjs()
                            .getMBB()
                            .finishWritingRecord(c.getCFT().getOffset()
                                + c.getCFT().getPageAppenderLen());
                    }
                    catch (MemoryManagerException e)
                    {
                        DBService.DB_SERVICE_LOG.error(e.getMessage());
                        throw new DBException("CRP_DATABS_ERROR_006",
                            new String[] {
                                currentPageMsg.getPackerForObjs().toString(),
                                currentPage.toString()});
                    }
                    while ((currentPageMsg = getNextAvailablePageMsg()) == null)
                    {
                        phToIO.getChannel().flushChannel();
                        phToIO.getChannel().notifyReceiver();
                        Thread.yield();
                    }
                    currentPage = getNextAvailablePage();
                }
            }
        }
        return retMsg;
    }

    /**
     * returns next available page message in the msg group.
     * @return message m (null, if no msg is available).
     * @throws DBException on error.
     */
    private Message getNextAvailablePageMsg() throws DBException
    {
        Message msg = null;
        try
        {
            msg = mgPages.getNextAvailableMsg();
            if(msg == null)
            {
                return null;
            }
        }
        catch(EtherException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            msg = null;
        }
        msg.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_DB_PAGE_TO_FILE_IO);
        if(msg.getPackerForObjs() == null)
        {
            try
            {
                MemoryBlockByteBuffer mbb =
                    (MemoryBlockByteBuffer) MemoryManager
                    .createMemoryBlock(
                        CRPThread.getCurrentThreadID(),
                        numOfDBPagesInMemory * GLOBAL_CONSTANTS.DB_PAGE_SIZE,
                        MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                        "create packer mbb for db pages");
                Packer pk = new Packer(mbb);
                msg.setPackerForObjs(pk);
            }
            catch(MemoryManagerException e)
            {
                DBService.DB_SERVICE_LOG.error(e.getMessage());
                throw new DBException("CRP_DATABS_ERROR_006", null);
            }
        }
        msg.getPayloadMBO().reset();
        msg.getPackerForObjs().reset();
        return msg;
    }
    
    /**
     * returns caplet message.
     * @return message object.
     */
    private Message getNextAvailableCapletMsg()
    {
        Message m = null;
        CRPThread crpt = CRPThread.getCurrentThread();
        try
        {
            m = crpt.getMyMsgWorld().getFreeMsgNoFail(
                GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY);
        }
        catch(EtherException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
        }
        m.getPayloadMBO().reset();

        return m;
    }
    
    /**
     * returns next available page in the cur msg.
     * @return msg or null if the current msg do not have
     * any available pages.
     */
    private Page getNextAvailablePage()
    {
        Page pg = (Page)currentPageMsg.getPayLoadObject();
        if(pg != null)
        {
            pg.reset();
        }
        return pg;
    }
    
    /**
     * returns next available caplet in the cur msg.
     * @return msg or null if the current msg do not have
     * any available pages.
     */
    private Caplet getNextAvailableCaplet()
    {
        assert(currentCapletMsg != null);
        Caplet c = null;
        c = (Caplet) currentCapletMsg.getPayLoadObject();
        c.reset();
        return (c);
        
    }
    /**
     * return current msg.
     * @return Message object.
     */
    public final Message getCurrentMsg()
    {
        return currentPageMsg;
    }
    
    /**
     * read caplets from the unpacker objects and return msg object.
     * @param unp unpacker object to read data from.
     * @return returns message object contains caplets.
     * @throws DBException on error.
     */
    public final Message readNextCapletsIntoMsg(
        final UnPacker unp) throws DBException
    {
        
        assert (currentPage != null);
        assert (currentCapletMsg != null);
        assert(unp.getMBB().getSize() 
            <= currentCapletMsg.getPayloadMBO().getMBB().getSize());
        
        /**
         * message to be sent to the service asking for.
         */
        Message retMsg = null;
        
        
        if(getPendingCaplet() != null)
        {
            currentPage.getPageHeader().reset();
            currentPage.extractPageHeader(unp);
            //finish extracting the remaining payload data.
            currentPage.extractCaplet(unp, getPendingCaplet(),
                Page.CAPLET_PARTIAL_UNPACK_NO_CAPLET_HEADER);
        }
        else
        {
            currentPage.extractPageHeader(unp);
            unp.getMBB().resetOffset();

            //if(unp.getMBB().getPrevWrittenDataSize() 
             //       < GLOBAL_CONSTANTS.DB_PAGE_SIZE)
            if(currentPage.getPageHeader().isThisLastPage())
            {
                unp.setPosition(0);
                readFullCapletsInLastPage(unp);

                retMsg = currentCapletMsg;
                return retMsg;
            }
            else
            {
                if(currentPage.getPageHeader().numOfCaplets == 0)
                {
                    // zero records in the page.
                    DBService.DB_SERVICE_LOG.warn(
                        "Zero records in Page, and it is not the last page");
                    retMsg = currentCapletMsg;
                    return retMsg;
                }
                currentPage.extractPageHeader(unp);
              //set the mbb buffer to the starting position.
                int startingOffset = 
                    currentPage.getPageHeader().offsetFlagIndicator &
                    PageHeader.CAPLET_SPILLED_TO_NEXT_PAGE_MASK;
                assert(startingOffset < GLOBAL_CONSTANTS.DB_PAGE_SIZE);
                unp.setPosition(startingOffset); 
            }
            
              
        }
            
        /**
         * before anything, lets setup the CapletFillTracker object.
         * and set the offset, which is the length of the caplet to be
         * packed into the db page. we again have to check this after
         * calling extractCaplet, as this method returns the number of bytes
         * successfully packed(in the offset variable).
         */
        
        // first get a caplet from the memory block.
        
        //this loop continues until we fill this caplet in a/multiple pages.
       
        /**
         * this loop iterates until we are done collecting
         * all the caplets in this unpacker object.
         */
        while (true)
        {
            // get number of caplets in this page.
            int noOfCs = -1;
            DBService.DB_SERVICE_LOG.info(" NUM CAPLETS IN THIS PAGE: " 
                + String.valueOf(currentPage.getPageHeader().numOfCaplets));
            totalNumOfCaplets += currentPage.getPageHeader().numOfCaplets;

            if (currentPage.getPageHeader().isCapletSpilledToNextPage())
            {
                noOfCs = currentPage.getPageHeader().numOfCaplets -1;
            }
            else
            {
                noOfCs = currentPage.getPageHeader().numOfCaplets;
            }        
            // fill all the caplets in the page, handle the last one separately.
            for (int i = 0; i < noOfCs; i++)
            {
                Caplet c = getNextAvailableCaplet();
                if (c == null)
                {
                    DBService.DB_SERVICE_LOG.error(
                        "Not enough caplets in the message");
                    retMsg = currentCapletMsg;
                    return retMsg;
                }
                currentPage.extractCaplet(unp, c, Page.CAPLET_FULL_UNPACK_FLAG);
            }
            
            // check again, if this is the last page.
            
            if(currentPage.getPageHeader().isThisLastPage())
            {
                retMsg = currentCapletMsg;
                return retMsg;
            }
            
            // now the hard part, retrieve the last caplet.
            Caplet c = null;
         
            if (!currentPage.getPageHeader().isCapletSpilledToNextPage())
            {
                moveToNextDBPage(unp);
                if(unp.getMBB().getCurOffset() == unp.getMBB().getSize())
                {
                    retMsg = currentCapletMsg;
                    currentCapletMsg = getNextAvailableCapletMsg();
                    // there is no pending caplet in this case;
                    setPendingCaplet(null);
                    return retMsg;
                }
                else
                {
                    currentPage.extractPageHeader(unp);
                }
            }
            else if ((unp.getMBB().getSize() - unp.getMBB().getCurOffset()) 
                    > GLOBAL_CONSTANTS.DB_PAGE_SIZE)
            {
                c = getNextAvailableCaplet();

                // another page exists; all we have to do is to read
                // the caplet across the pages.

                currentPage.extractCaplet(
                    unp,
                    c,
                    Page.CAPLET_PARTIAL_UNPACK_FLAG);
                
                moveToNextDBPage(unp);


                // go to the next page. the following statement give
                // the same effect as of moving to next page.

                currentPage.getPageHeader().reset();
                currentPage.extractPageHeader(unp);
                // fill the remaining part of the caplet,
                // by reading the bytes from the new page.

                currentPage.extractCaplet(
                    unp,
                    c,
                    Page.CAPLET_PARTIAL_UNPACK_NO_CAPLET_HEADER);
            }
            else
            {
                // this means that this is the last page in this unpacker.
                // we should move to the next caplet message and store the
                // caplet in the new message.

                retMsg = currentCapletMsg;
                currentCapletMsg = getNextAvailableCapletMsg();
                assert (currentCapletMsg != null);
                c = getNextAvailableCaplet();
                currentPage.extractCaplet(
                    unp,
                    c,
                    Page.CAPLET_PARTIAL_UNPACK_FLAG);
                
                moveToNextDBPage(unp);
                // set the pending caplet.
                setPendingCaplet(c);

                return retMsg;
            }
          
        }
    }
    
    /**
     * reads full packets from the last page.
     * as this is the last db page in a file, we should handle
     * this as a special case. we cannot assume that the page header
     * is written.
     * @param unp unpacker object.
     * @throws DBException on error.
     */
    private void readFullCapletsInLastPage(
        final UnPacker unp) throws DBException
    {
        for(int i = 0; i < currentPage.getPageHeader().numOfCaplets; i++)
        {
            Caplet c = getNextAvailableCaplet();
            currentPage.extractCaplet(unp, c, Page.CAPLET_FULL_UNPACK_FLAG);
        }
        
    }
    /**
     * increments the buffer ptr in unp(mbb) by DB_PAGE_SIZE.
     * @param unp unpacker object.
     * @throws DBException on error.
     */
    private final void moveToNextDBPage(
        final UnPacker unp) throws DBException
    {
        try
        {
            unp.getMBB().finishReadingRecord(
                GLOBAL_CONSTANTS.DB_PAGE_SIZE);
            unp.setPosition(unp.getMBB().getCurOffset());

        }
        catch (MemoryManagerException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DATABS_ERROR_011", null);
        }
    }
    
    /**
     * should be called at the end of addCaplets.
     * @param pk packer object, the caplets are being packed into.
     * @throws DBException on error.
     */
    public final void finishWritingCaplets(final Packer pk) throws DBException
    {
        DBService.DB_SERVICE_LOG.info(" left over caplets in this page: " 
            + currentPage.getPageHeader().numOfCaplets);
        
        totalNumOfCaplets += currentPage.getPageHeader().numOfCaplets;

        DBService.DB_SERVICE_LOG.info(" Finished Writing [ " 
            + totalNumOfCaplets + " ] caplets to db" );
        
        // add end of data flag.
        currentPage.getPageHeader().offsetFlagIndicator 
            = currentPage.getPageHeader().offsetFlagIndicator
            | PageHeader.END_OF_DATA_INDICATOR;
        
        currentPage.addPageHeader(currentPageMsg.getPackerForObjs(), true);
        try
        {
            pk.getMBB().finishWritingRecord(GLOBAL_CONSTANTS.DB_PAGE_SIZE 
                - pk.getMBB().getCurOffset() % GLOBAL_CONSTANTS.DB_PAGE_SIZE);
        }
        catch (MemoryManagerException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new DBException("CRP_DATABS_ERROR_006",
                new String[] {
                    currentPageMsg.getPackerForObjs().toString(),
                    currentPage.toString()});
        }
        return;
        /** dont know if we have to handle this case yet.
         * 
        if(currentPage != null)
        {
            currentPage.addPageHeader(currentPageMsg.getPackerForObjs(), false);
            int noOfPages 
                = currentPageMsg.getPackerForObjs().getMBB().getCurOffset() 
                / GLOBAL_CONSTANTS.DB_PAGE_SIZE;
            try
            {
                currentPageMsg.getPackerForObjs().getMBB().finishWritingRecord(
                    ( noOfPages + 1) * GLOBAL_CONSTANTS.DB_PAGE_SIZE
                    - currentPageMsg.getPackerForObjs(
                        ).getMBB().getCurOffset());
            }
            catch (MemoryManagerException e)
            {
                throw new DBException("CRP_DATABS_ERROR_011", null);
            }
        }*/
    }
    
    /**
     * returns pending caplet.
     * @return Caplet object.
     */
    public final Caplet getPendingCaplet()
    {
        return pendingCaplet;
    }
    
    /**
     * store the pending caplet (if any) while reading.
     * useful when a caplet's payload data is across
     * io messages.
     * @param inpCaplet input caplet.
     */
    public final void setPendingCaplet(final Caplet inpCaplet)
    {
        this.pendingCaplet = inpCaplet;
        
    }
    
    /**
     * returns total number of caplets written/read so far.
     * @return integer.
     */
    public final int getTotalNumberOfCaplets()
    {
        return totalNumOfCaplets;
    }
    
    /**
     * returns db access mode.
     * @return db access mode.
     */
    public final DBAccessMode getDBM()
    {
    	return dbm;
    }
    
    /**
     * resets the db page manager object.
     * should call the setDBAccessMode before'
     * reusing the object.
     */
    public final void reset()
    {
        dbm = DBAccessMode.UNINIT;
        currentPage.reset();
    }
    
    /**
     * sets db access mode.
     * @param inpDBM input db access mode.
     */
    public final void setDBAccessMode(final DBAccessMode inpDBM)
    {
        dbm = inpDBM;
    }
    
    /**
     * returns current page.
     * @return Page Object.
     */
    public final Page getCurrentPage()
    {
        
        return currentPage;
    }
}

