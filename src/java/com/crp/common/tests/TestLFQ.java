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

package com.crp.common.tests;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.LockFreeQ;
import com.crp.process.ProcessMain;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;

/**
 * tests the lock free queue.
 * @author hpoduri
 *
 */
public class TestLFQ
{
    public static int MAX_ELEMS_IN_QUEUE = 10000000;
    /**
     * tests simple case.
     */
    @Test
    public void testSimple()
    {
        final LockFreeQ lfq = new LockFreeQ(1000, false);
        
        lfq.setConsumerSpinning(true);
        lfq.setProducerSpinning(true);
        Runnable producer = new Runnable() {
            public void run()
            {
                lfq.subscribeAsProducer((short)Thread.currentThread().getId());
                short tid = (short) Thread.currentThread().getId();
                for (int i = 0; i < MAX_ELEMS_IN_QUEUE; i++)
                {
                    String s = String.valueOf(i);
                   // System.out.println(" pstring: " + s);
                    while(!lfq.addToQ(s, tid))
                    {
                        ;
                    }
                }
                lfq.doneProducing();
                System.out.println("DONE! producer");
            }
        };
        Runnable consumer = new Runnable() {
            public void run()
            {
                lfq.subscribeAsConsumer((short)Thread.currentThread().getId());
                String s = "jlt";
                short tid = (short) Thread.currentThread().getId();

                for (int i = 0; i < MAX_ELEMS_IN_QUEUE; i++)
                {  
                    while ((s = (String)lfq.getElemFromQ(tid)) == null)
                    {
                        ;
                    }
                    
                    //System.out.println(" cstring: " + s);
                }
                System.out.println("DONE! consumer");
                assertEquals(s, String.valueOf(MAX_ELEMS_IN_QUEUE -1));
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        long sT = System.currentTimeMillis();
        threadPool.execute(producer);
        threadPool.execute(consumer);
        threadPool.shutdown();
        try
        {
            threadPool.awaitTermination(2400, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        long fT = System.currentTimeMillis();
        System.out.println(" elapsed time with crp queues: " + String.valueOf(fT-sT));
        
    }
    @Test
    public void testJavaQueues()
    {
        final ConcurrentLinkedQueue<String> clq = new ConcurrentLinkedQueue<String>();
        Runnable producer = new Runnable() {
            public void run()
            {
                for (int i = 0; i < MAX_ELEMS_IN_QUEUE; i++)
                {
                    String s = String.valueOf(i);
                    //System.out.println(" pstring: " + s);
                    while(!clq.add(s)) ;
                }
                
                System.out.println("DONE! producer");
            }
        };
        Runnable consumer = new Runnable() {
            public void run()
            {
                int i = 0;
                String s = "jlt";
                while (true)
                {
                    if (i >= MAX_ELEMS_IN_QUEUE)
                    {
                        break;
                    }
                    s = (String) clq.poll();
                    if (s == null)
                    {
                        continue;
                    }
                    i++;
                    // System.out.println(" cstring: " + s);
                }
                assertEquals(s, String.valueOf(MAX_ELEMS_IN_QUEUE -1));

                System.out.println("DONE! consumer");
            }
        };
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        long sT = System.currentTimeMillis();

        threadPool.execute(producer);
        threadPool.execute(consumer);
        threadPool.shutdown();
        
        try
        {
            threadPool.awaitTermination(2400, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        long fT = System.currentTimeMillis();
        System.out.println(" elapsed time with java queues: " + String.valueOf(fT-sT));
        assertTrue(true);
    }
    
    @Test
    public void testWithCRPThreadsWithWait()
    {
        ProcessMain.createDummyService();
        
        final LockFreeQ lfq = new LockFreeQ(1000, true);
        
        lfq.setConsumerSpinning(false);
        lfq.setProducerSpinning(false);
        Runnable thread1 = new Runnable() {
            public void run()
            {
                lfq.subscribeAsProducer(CRPThread.getCurrentThreadID());
                for (int i = 0; i < MAX_ELEMS_IN_QUEUE; i++)
                {
                    String s = String.valueOf(i);
                   // System.out.println(" pstring: " + s);
                    while (!lfq.addToQ(s))
                        ;
                }
                lfq.doneProducing();
                System.out.println("DONE! producer");
            }
        };
        
        Runnable thread2 = new Runnable() {
            public void run()
            {
                String s = "jlt";
                lfq.subscribeAsConsumer(CRPThread.getCurrentThreadID());
                for (int i = 0; i < MAX_ELEMS_IN_QUEUE; i++)
                {  
                    while ( (s = (String)lfq.getElemFromQ()) == null)
                        ;
                    
                }
                System.out.println("DONE! consumer");
                assertEquals(s, String.valueOf(MAX_ELEMS_IN_QUEUE -1));

            }
        };
        CRPThread crpt1 = null;
        CRPThread crpt2 = null;
        try
        {
            crpt1 = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                thread1, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
            crpt2 = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                thread2, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
        }
        catch(CRPThreadException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            
        }
        long sT = System.currentTimeMillis();
        crpt1.start();
        crpt2.start();
        
        try
        {
            crpt1.join();
            crpt2.join();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        long fT = System.currentTimeMillis();
        System.out.println(" elapsed time with crp thread queues with wait: " + String.valueOf(fT-sT));
        assertTrue(true);

    }
    
    @Test
    public void testFullDuplex()
    {
        final LockFreeQ lfq = new LockFreeQ(1000, true);
        ProcessMain.createDummyService();

        lfq.setConsumerSpinning(true);
        lfq.setProducerSpinning(true);
        Runnable producer = new Runnable() {
            public void run()
            {
                int counter = 1;
                lfq.subscribeAsProducer(CRPThread.getCurrentThreadID());

                for (int i = 0; i < MAX_ELEMS_IN_QUEUE; i++)
                {
                    String s = String.valueOf(i);
                   // System.out.println(" pstring: " + s);
                    while (!lfq.addToQ(s)) ;
                    
                    String s1 = (String) lfq.getElemFromQ();
                    if(s1 != null)
                        counter++;
                }
                lfq.doneProducing();

                while (true)
                {
                    if(counter == MAX_ELEMS_IN_QUEUE)
                        break;
                    if( lfq.getElemFromQ() != null)
                        counter++;
                }
               
                assertEquals(MAX_ELEMS_IN_QUEUE, counter);
                System.out.println("DONE! producer");
            }
        };
        Runnable consumer = new Runnable() {
            public void run()
            {
                String s = "jlt";
                lfq.subscribeAsConsumer(CRPThread.getCurrentThreadID());

                for (int i = 0; i < MAX_ELEMS_IN_QUEUE; i++)
                {  
                   
                    while((s = (String) lfq.getElemFromQ())== null)
                    {
                        ;
                    }
                    while ( !lfq.addToQ(s)) ;

                    //System.out.println(" cstring: " + s);
                }
                
                System.out.println("DONE! consumer");
                assertEquals(s, String.valueOf(MAX_ELEMS_IN_QUEUE -1));
            }
        };
        
        CRPThread crpt1 = null;
        CRPThread crpt2 = null;
        try
        {
            crpt1 = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                producer, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
            crpt2 = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                consumer, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
        }
        catch(CRPThreadException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            
        }
        long sT = System.currentTimeMillis();
        crpt1.start();
        crpt2.start();
        
        try
        {
            crpt1.join();
            crpt2.join();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        long fT = System.currentTimeMillis();
        System.out.println(" elapsed time with crp thread(full duplex): " + String.valueOf(fT-sT));
        assertTrue(true);
    }
    public static void main(String [] args)
    {
    	TestLFQ tlfq = new TestLFQ();
        tlfq.testSimple();
        //testJavaQueues();
        //testWithCRPThreads();
    }
    
    
}
