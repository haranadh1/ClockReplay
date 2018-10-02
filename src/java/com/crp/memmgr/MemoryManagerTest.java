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

package com.crp.memmgr;

import java.io.IOException;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import com.crp.common.CRErrorHandling;
import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;

import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.process.ProcessMain;

import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;

/**
 * Tests for Memory Manager and MemoryBlock classes.
 * @author hpoduri
 */
public final class MemoryManagerTest
{

    /**
     * make constructor private.
     */
    private MemoryManagerTest()
    {
        //do nothing.
    }
    /**
     * test methods.
     */
    private static void testMemoryManagerInit()
    {
        // try init first
        try
        {
            MemoryManager.init(100);
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        // register service first.
        PROCESS_GLOBALS.addService(new CRPService(
            new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
                "localhost",
                "service for capture",
                8085,
                0)));
        PROCESS_GLOBALS.addService(new CRPService(
            new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE,
                "localhost",
                "service for db",
                8086,
                0)));
        PROCESS_GLOBALS.addService(new CRPService(
            new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.REPLAY_SERVICE,
                "localhost",
                "service for replay",
                8085,
                0)));

        try
        {
            MemoryManager.registerThread((short) 0);
            MemoryManager.init(100);
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());

        }
    }

    /**
     * test excpetions.
     */
    private static void testMemoryManagerExceptions()
    {
        try
        {
            throw (new MemoryManagerException(
                "CRP_MEMMGR_ERROR_001",
                new String[]{"1"}));
        }
        catch (Exception e)
        {
            MemoryManager.MEM_LOG.error(e.getMessage());
        }
        try
        {
            throw (new MemoryManagerException(
                "CRP_MEMMGR_ERROR_002",
                new String[]{"1", "2", "3"}));
        }
        catch (Exception e)
        {
            MemoryManager.MEM_LOG.error(e.getMessage());
        }
        try
        {
            throw (new MemoryManagerException(
                "CRP_MEMMGR_ERROR_003",
                new String[]{"INITIALIZED"}));
        }
        catch (Exception e)
        {
            MemoryManager.MEM_LOG.error(e.getMessage());
        }
        try
        {
            throw (new MemoryManagerException(
                "CRP_MEMMGR_ERROR_004",
                new String[]{"1"}));
        }
        catch (Exception e)
        {
            MemoryManager.MEM_LOG.error(e.getMessage());
        }
    }

    /**
     * checks for invalid data.
     */
    private static void testForInvalidData()
    {
        try
        {
            MemoryManager.init(0);
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        try
        {
            MemoryManager.init(100);
            MemoryBlock mb = MemoryManager.createMemoryBlock(
                (short) 0,
                1000,
                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                "for testing");
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static void testMemoryBlockCreationThrds()
    {
        /**
         * sample CaptureService thread.
         * @author hpoduri
         */
        System.out.println(GLOBAL_UTILS.getCurrentMethod() + "-------");
        try
        {
            MemoryManager.nuke();
            
            MemoryManager.init(100 * GLOBAL_CONSTANTS.MB);
         // register services.
            MemoryManager.registerThread((short) 0);
            MemoryManager.registerThread((short) 1);
            MemoryManager.registerThread((short) 2);
            
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        Runnable CaptureService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 100; i++)
                {
                    try
                    {
                        int size = ((int) ((Math.random() * 100) + 1));
                        MemoryBlock mb = MemoryManager
                            .createMemoryBlock(
                                (short) 0,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("DONE! CS ");
            }
        };
        /**
         * Sample DB Service thread.
         * @author hpoduri
         */
        Runnable DBService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 100; i++)
                {
                    try
                    {
                        int size = ((int) ((Math.random() * 10) + 1));
                        MemoryBlock mb = MemoryManager
                            .createMemoryBlock(
                                (short) 1,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("DONE! DB SERVICE");
            }
        };
        Runnable RepService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 100; i++)
                {
                    try
                    {
                        int size = ((int) ((Math.random() * 10) + 1));
                        MemoryBlock mb = MemoryManager
                            .createMemoryBlock(
                                (short) 2,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("DONE! replay SERVICE");
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(CaptureService);
        threadPool.execute(DBService);
        threadPool.execute(RepService);
        threadPool.shutdown();
        try
        {
            threadPool.awaitTermination(120, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    /*
     * mem mgr bounds check.
     */
    public static void testMemMgrBounds() {
        //test the MemoryManager index out of bounds
        //try the default 1024 limit.
        System.out.println(GLOBAL_UTILS.getCurrentMethod() + "-------");
        try
        {
            MemoryManager.nuke();
            
            MemoryManager.init(100 * GLOBAL_CONSTANTS.MB);
         // register services.
            MemoryManager.registerThread((short) 0);
            MemoryManager.registerThread((short) 1);
            MemoryManager.registerThread((short) 2);
            
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        Runnable CaptureService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 500; i++)
                {
                    try
                    {
                        int size = ((int) ((Math.random() * 300000) + 1));
                        MemoryBlock mb = MemoryManager
                            .createMemoryBlock(
                                (short) 0,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("DONE! CS ");
            }
        };
        Runnable DBService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 500; i++)
                {
                    try
                    {
                        int size = ((int) ((Math.random() * 25000) + 1));
                        MemoryBlock mb = MemoryManager
                            .createMemoryBlock(
                                (short) 1,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("DONE! DB SERVICE");
            }
        };
        Runnable RepService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 500; i++)
                {
                    try
                    {
                        int size = ((int) ((Math.random() * 150000) + 1));
                        MemoryBlock mb = MemoryManager
                            .createMemoryBlock(
                                (short) 2,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("DONE! replay SERVICE");
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(CaptureService);
        threadPool.execute(DBService);
        threadPool.execute(RepService);
        threadPool.shutdown();
        try
        {
            threadPool.awaitTermination(120, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    private static void testFreeMemBlock()
    {
        //test the MemoryManager index out of bounds
        //try the default 1024 limit.
        System.out.println(GLOBAL_UTILS.getCurrentMethod() + "-------");
        MemoryManager.MEM_LOG.info(GLOBAL_UTILS.getCurrentMethod());
        try
        {
            MemoryManager.nuke();
            
            MemoryManager.init(100 * GLOBAL_CONSTANTS.MB);
         // register services.
            MemoryManager.registerThread((short) 0);
            MemoryManager.registerThread((short) 1);
            MemoryManager.registerThread((short) 2);
            
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        Runnable CaptureService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 500; i++)
                {
                    MemoryBlock mb = null;
                    try
                    {
                        int size = ((int) ((Math.random() * 300000) + 1));
                        mb = MemoryManager
                            .createMemoryBlock(
                                (short) 0,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                        Random rnd = new Random();
                        boolean free = rnd.nextBoolean();
                        free = false;
                        if (free)
                        {
                            mb.freeMe();
                        }
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                    
                }
                System.out.println("DONE! CS ");
            }
        };
        Runnable DBService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 500; i++)
                {
                    try
                    {
                        int size = ((int) ((Math.random() * 250000) + 1));
                        MemoryBlock mb = MemoryManager
                            .createMemoryBlock(
                                (short) 1,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                        Random rnd = new Random();
                        boolean free = rnd.nextBoolean();
                        if (free)
                        {
                            mb.freeMe();
                        }
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("DONE! DB SERVICE");
            }
        };
        Runnable RepService = new Runnable() {
            public void run()
            {
                for (int i = 0; i < 500; i++)
                {
                    try
                    {
                        int size = ((int) ((Math.random() * 150000) + 1));
                        MemoryBlock mb = MemoryManager
                            .createMemoryBlock(
                                (short) 2,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                        Random rnd = new Random();
                        boolean free = rnd.nextBoolean();
                        if (free)
                        {
                            mb.freeMe();
                        }
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("DONE! replay SERVICE");
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        threadPool.execute(CaptureService);
        threadPool.execute(DBService);
        threadPool.execute(RepService);
        threadPool.shutdown();
        try
        {
            threadPool.awaitTermination(120, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    /**
     * tests performance of java memory.
     */
    private static void testJavaPerformance()
    {
        /**
         * create a 2mb buffer ahead and start packing the
         * byte buffers of random size, which is how we normally get it over the stream.
         */
        //test the MemoryManager index out of bounds
        //try the default 1024 limit.
        System.out.println(GLOBAL_UTILS.getCurrentMethod() + "-------");
        MemoryManager.MEM_LOG.info(GLOBAL_UTILS.getCurrentMethod());
        
        final long numOfRuns = 300000L;

        Runnable CaptureService = new Runnable() {
            public void run()
            {
                for (long i = 0; i < numOfRuns; i++)
                {
                    MemoryBlock mb = null;
                    try
                    {
                        int size = ((int) GLOBAL_CONSTANTS.MB);
                        byte [] b = new byte [size];
                        Packer pk = new Packer(2*GLOBAL_CONSTANTS.MB);
                        for(int j = 0 ; j <= 50; j++)
                        {
                            Random r = new Random();
                            
                            byte [] b1 = new byte[(r.nextInt(2000)+1)];
                            pk.packBytes(b1);
                            
                        }
                        b = null;              
                        
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    
                }
                System.out.println("DONE! CS ");
            }
        };
        Runnable DBService = new Runnable() {
            public void run()
            {
                for (long i = 0; i < numOfRuns; i++)
                {
                    MemoryBlock mb = null;
                    try
                    {
                        int size = ((int) GLOBAL_CONSTANTS.MB);
                        byte [] b = new byte [size];
                        Packer pk = new Packer(2*GLOBAL_CONSTANTS.MB);
                        for(int j = 0 ; j <= 50; j++)
                        {
                            Random r = new Random();
                            
                            byte [] b1 = new byte[(r.nextInt(2000)+1)];
                            pk.packBytes(b1);
                            
                        }
                        b = null;              
                        
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    
                }
                System.out.println("DONE! DB SERVICE");
            }
        };
        Runnable RepService = new Runnable() {
            public void run()
            {
                for (long i = 0; i < numOfRuns; i++)
                {
                    MemoryBlock mb = null;
                    try
                    {
                        int size = ((int) GLOBAL_CONSTANTS.MB);
                        byte [] b = new byte [size];
                        Packer pk = new Packer(2*GLOBAL_CONSTANTS.MB);
                        for(int j = 0 ; j <= 50; j++)
                        {
                            Random r = new Random();
                            
                            byte [] b1 = new byte[(r.nextInt(2000)+1)];
                            pk.packBytes(b1);
                            
                        }
                        b = null;              
                        
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    
                }
                System.out.println("DONE! replay SERVICE");
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        long sT = System.currentTimeMillis();
        threadPool.execute(CaptureService);
        threadPool.execute(DBService);
        threadPool.execute(RepService);
        threadPool.shutdown();
        try
        {
            threadPool.awaitTermination(1200000, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        long fT = System.currentTimeMillis();
        System.out.println(" elapsed time with java mem mgr: " + String.valueOf(fT-sT));
    }   
    
    /**
     * tests performance of mem mgr.
     */
    private static void testPerformance()
    {
        //test the MemoryManager index out of bounds
        //try the default 1024 limit.
        /**
         * with memory blocks, it does not matter what is the size of 
         * each caplet byte buffer, as we essentially copying it into
         * packer byte buffer. what we want to test is the memory block
         * creating and freeing.
         * ideally we dont have the free memory block overhead as you 
         * can see in this example.we hardly free the memory blocks
         * in a realistic use case.
         */
        System.out.println(GLOBAL_UTILS.getCurrentMethod() + "-------");
        MemoryManager.MEM_LOG.info(GLOBAL_UTILS.getCurrentMethod());
        try
        {            
            MemoryManager.init(100 * GLOBAL_CONSTANTS.MB);
         // register services.
            MemoryManager.registerThread((short) 0);
            MemoryManager.registerThread((short) 1);
            MemoryManager.registerThread((short) 2);
            
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        final long numOfRuns = 300000L;

        Runnable CaptureService = new Runnable() {
            public void run()
            {
                //it does not matter what size, as we are not creating
                // a byte array every time.
                byte [] value = new byte [1000];
                for (long i = 0; i < numOfRuns; i++)
                {
                    MemoryBlockByteBuffer mb = null;
                    try
                    {
                        int size = ((int) GLOBAL_CONSTANTS.MB);
                        mb = (MemoryBlockByteBuffer)MemoryManager
                            .createMemoryBlock(
                                (short) 0,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");
                        Packer pk = new Packer(mb);
                        
                        
                        for (int j = 0; j <= 50; j++)
                        {
                            try
                            {
                                pk.startEvent(1000);
                                try
                                {
                                    pk.packBytes(value);
                                }
                                catch (IOException e)
                                {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                pk.finishEvent();

                            }
                            catch (PackUnPackException e)
                            {

                            }
                        }
                        mb.freeMe();
                        
                        
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                    
                }
                System.out.println("DONE! CS ");
            }
        };
        Runnable DBService = new Runnable() {
            public void run()
            {
                //it does not matter what size, as we are not creating
                // a byte array every time.
                byte [] value = new byte [1000];
                for (long i = 0; i < numOfRuns; i++)
                {
                    MemoryBlockByteBuffer mb = null;
                    try
                    {
                        int size = ((int) GLOBAL_CONSTANTS.MB);
                        mb = (MemoryBlockByteBuffer)MemoryManager
                            .createMemoryBlock(
                                (short) 1,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");

                        Packer pk = new Packer(mb);
                        
                        
                        for (int j = 0; j <= 50; j++)
                        {
                            try
                            {
                                pk.startEvent(1000);
                                try
                                {
                                    pk.packBytes(value);
                                }
                                catch (IOException e)
                                {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                pk.finishEvent();
                            }
                            catch (PackUnPackException e)
                            {
                                 int k = 0; 
                                 k++;
                            }
                        }
                        mb.freeMe();
                        
                        
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                    
                }
                System.out.println("DONE! DB SERVICE");
            }
        };
        Runnable RepService = new Runnable() {
            public void run()
            {
                //it does not matter what size, as we are not creating
                // a byte array every time.
                byte [] value = new byte [1000];
                for (long i = 0; i < numOfRuns; i++)
                {
                    MemoryBlockByteBuffer mb = null;
                    try
                    {
                        int size = ((int) GLOBAL_CONSTANTS.MB);
                        mb = (MemoryBlockByteBuffer)MemoryManager
                            .createMemoryBlock(
                                (short) 2,
                                size,
                                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                                "for testing");

                        Packer pk = new Packer(mb);
                        
                        
                        for (int j = 0; j <= 50; j++)
                        {
                            try
                            {
                                pk.startEvent(1000);
                                try
                                {
                                    pk.packBytes(value);
                                }
                                catch (IOException e)
                                {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                pk.finishEvent();
                            }
                            catch (PackUnPackException e)
                            {

                            }
                        }
                        mb.freeMe();
                        
                        
                    }
                    catch (MemoryManagerException e)
                    {
                        System.out.println(e.getMessage());
                    }
                    
                }
                System.out.println("DONE! replay SERVICE");
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        long sT = System.currentTimeMillis();
        threadPool.execute(CaptureService);
        threadPool.execute(DBService);
        threadPool.execute(RepService);
        threadPool.shutdown();
        try
        {
            threadPool.awaitTermination(1200000, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        long fT = System.currentTimeMillis();
        System.out.println(" elapsed time with crp mem mgr: " + String.valueOf(fT-sT));
    }  
    /**
     * tests.
     * @param args : arguments to main(if any)
     */
    public static void main(final String[] args)
    {
        // Load the error codes
        CRErrorHandling.load();
        MemoryManager.MEM_LOG.info(" Memory Manager Initiated");
        MemoryManager.MEM_LOG.error(" test error stream of the log");
     
        /**
        // start tests
        testMemoryManagerExceptions();
        testMemoryManagerInit();
        // test memory block creation with threads.
        testMemoryBlockCreationThrds(); 
        MemoryManager.dumpMemoryBlockInfo();
        
        //test mem mgr bounds
//        testMemMgrBounds();
        
        System.out.println("before dump memblock info");
        MemoryManager.dumpMemoryBlockInfo();
        
        //test free memory block interface.
//        testFreeMemBlock();
        //test performance.
        testPerformance();
        MemoryManager.dumpMemoryBlockInfo();
        testJavaPerformance();
        MemoryManager.dumpMemoryBlockInfo(); */
        
        testJSON();
    }
    private static void testJSON()
    {
        ProcessMain.createDummyService();
        final Runnable r1 = new Runnable() {
            public void run()
            {
                int i = 0;
                
                try
                {
                    try
                    {
                        CRPThread.getCurrentThread().initMsgPools();
                    }
                    catch (CRPThreadException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    CommonLogger.CMN_LOG.info(MemoryManager.toJSON().toString(4));
                }
                catch (JSONException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        CRPThread crpt = null;
        try
        {
             crpt = CRPThreadPoolManager.createNewThread(GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                r1, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD, GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
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
}
