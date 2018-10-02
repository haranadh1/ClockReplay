package com.crp.db.tests;


import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.tests.TestCapletGenerator;
import com.crp.db.DBException;
import com.crp.db.DBService;
import com.crp.db.Page;
import com.crp.ether.MessageWorld;
import com.crp.interfaces.Caplet;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryBlockObjectPool;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.memmgr.MemoryManager.MemoryAllocationType;
import com.crp.memmgr.MemoryManager.MemoryBlockType;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;
import com.crp.process.ProcessMain;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;

public class TestDBPage extends TestCase
{
    public enum TEST_INDEX
    {
        ADD_CAPLET_SANITY,
        WRITE_READ_CAPLET_SANITY,
        
    }

    @Before
    public void setUp() throws Exception
    {
        ProcessMain.createDummyService();
    }
    
    @Test
    public void testAddCaplet()
    {
        startCRPThreadForPageTesting(TEST_INDEX.ADD_CAPLET_SANITY);
    }
    
    @Test
    public void testPageWriteAndRead()
    {
        startCRPThreadForPageTesting(TEST_INDEX.WRITE_READ_CAPLET_SANITY);
    }
    
    private void testReadCapletHelper(MemoryBlockByteBuffer packMBB, MemoryBlockObjectPool<?> inpCMBO)
    {
        UnPacker unp = new UnPacker(packMBB);
        packMBB.resetOffset();
        ObjectFactory of = new Caplet(null);
        int maxCaplets = 50;
        MemoryBlockObjectPool<?> capMBO = null;
        try
        {
            // create memory block for the object
            
            capMBO = MemoryManager.createObjectPoolMemoryBlock(
                CRPThread.getCurrentThreadID(),
                maxCaplets,
                20 * GLOBAL_CONSTANTS.MB,
                MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL,
                of, of.anyVarFields());
        }
        catch (MemoryManagerException e)
        {
            MessageWorld.MSG_WORLD_LOG.error(e.getMessage());
            
        }
        Page currentPage = new Page(null);
        
        try
        {
            currentPage.extractPageHeader(unp);

            while (true)
            {
                // get number of caplets in this page.
                int noOfCs = -1;
                if (currentPage.getPageHeader().isCapletSpilledToNextPage())
                {
                    noOfCs = currentPage.getPageHeader().numOfCaplets - 1;
                }
                else
                {
                    noOfCs = currentPage.getPageHeader().numOfCaplets;
                }
                // fill all the caplets in the page, handle the last one
                // separately.
                for (int i = 0; i < noOfCs; i++)
                {
                    Caplet c = (Caplet) capMBO.getObject();
                    if (c == null)
                    {
                        assertTrue(false);
                    }
                    currentPage.extractCaplet(
                        unp,
                        c,
                        Page.CAPLET_FULL_UNPACK_FLAG);
                }

                // now the hard part, retrieve the last caplet.
                Caplet c = null;

                if (!currentPage.getPageHeader().isCapletSpilledToNextPage())
                {
                    try
                    {
                        unp.getMBB().finishReadingRecord(
                            GLOBAL_CONSTANTS.DB_PAGE_SIZE);
                        unp.setPosition(unp.getMBB().getCurOffset());
                    }
                    catch (MemoryManagerException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else if ((unp.getMBB().getSize() - unp.getMBB().getCurOffset()) > GLOBAL_CONSTANTS.DB_PAGE_SIZE)
                {
                    c = (Caplet) capMBO.getObject();

                    // another page exists; all we have to do is to read
                    // the caplet across the pages.

                    currentPage.extractCaplet(
                        unp,
                        c,
                        Page.CAPLET_PARTIAL_UNPACK_FLAG);

                    try
                    {
                        unp.getMBB().finishReadingRecord(
                            GLOBAL_CONSTANTS.DB_PAGE_SIZE);
                        unp.setPosition(unp.getMBB().getCurOffset());

                    }
                    catch (MemoryManagerException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
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
                    int i = 0;
                    i++;
                    break;
                }
            }
        }
        catch(DBException e)
        {
            System.out.println(e.getMessage());
            assertTrue(false);
        }
        
        // now compare the caplets.
        for(int i = 0; i < inpCMBO.getActiveObjects()-1; i++)
        {
            assertTrue(Caplet.compare((Caplet)inpCMBO.getObjectAtIndex(i), (Caplet)capMBO.getObjectAtIndex(i)));
        }
    }
    private MemoryBlockObjectPool<?> testAddCapletHelper(MemoryBlockObjectPool<?> pageMBO,
        MemoryBlockByteBuffer packMBB)
    {
        Packer pk = new Packer(packMBB);
        TestCapletGenerator tcg = new TestCapletGenerator();
        tcg.init(50);
        Page curPage = (Page) pageMBO.getObject();
        long packedSize = 0;
        for (int i = 0; i <= 50; i++ )
        {
            Caplet c = tcg.getNextCaplet();
            if (c == null)
                break;
            try
            {
                c.getCFT().setOffset(0);
                packedSize += c.getPackedBufferSize();
                
                int packedDataSize = 0;
                while(true)
                {
                    c.getCFT().setOffset(packedDataSize);
                    curPage.addCaplet(c, pk);
                    packedDataSize = c.getCFT().getOffset();
                    if(c.getPackedBufferSize() == packedDataSize)
                    {
                        //caplet fit in the current page no worries. end of story.
                        break;
                    }
                    else
                    {
                        curPage = (Page) pageMBO.getObject();
                        if(curPage == null)
                        {
                            break;
                        }
                    }
                    
                }
                if(curPage == null)
                {
                    break;
                }
            }
            catch (DBException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        assertTrue(true);
        return tcg.getMBO();
    }
    private void startCRPThreadForPageTesting(final TEST_INDEX ti)
    {
        Runnable t = new Runnable() {
            public void run()
            {
                ObjectFactory of = new Page(null);
                
                int numObjs = GLOBAL_CONSTANTS.MAX_DB_PAGES_PER_IO;
                MemoryBlockObjectPool<?> pageMBO = null;
                MemoryBlockByteBuffer packMBB = null;
                
                CRPThread crpt = CRPThreadPoolManager.getCRPThread(CRPThread
                    .getCurrentThreadID());
                try
                {
                    crpt.initMsgPools();
                }
                catch (CRPThreadException e)
                {
                    CommonLogger.CMN_LOG.error(e.getMessage());
                    e.printStackTrace();
                    return;
                }
                try
                {
                    // create memory block for the object
                    
                    pageMBO = MemoryManager.createObjectPoolMemoryBlock(
                        CRPThread.getCurrentThreadID(),
                        numObjs,
                        0,
                        MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL,
                        of, of.anyVarFields());
                    packMBB = (MemoryBlockByteBuffer) MemoryManager.createMemoryBlock(
                        CRPThread.getCurrentThreadID(),
                        GLOBAL_CONSTANTS.MAX_DB_PAGES_PER_IO * GLOBAL_CONSTANTS.DB_PAGE_SIZE,
                        MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER, null,
                        MemoryAllocationType.JAVA_BYTE_ARRAY, null);
                }
                catch (MemoryManagerException e)
                {
                    MessageWorld.MSG_WORLD_LOG.error(e.getMessage());
                    
                }
                if (ti == TEST_INDEX.ADD_CAPLET_SANITY)
                {
                    testAddCapletHelper(pageMBO, packMBB);
                }
                else if( ti == TEST_INDEX.WRITE_READ_CAPLET_SANITY)
                {
                    MemoryBlockObjectPool<?> cMBO = testAddCapletHelper(pageMBO, packMBB);
                    testReadCapletHelper(packMBB, cMBO);
                }
            }

           
        };
        CRPThread crpt = null;
        try
        {
              crpt = CRPThreadPoolManager.createNewThread(GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE, t,
                GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
        }
        catch (CRPThreadException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        crpt.start();
        try
        {
            crpt.join();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
/*
    public static void main(String[] args) 
    {
        junit.textui.TestRunner.run(TestDBPage.class);
    }*/
}
