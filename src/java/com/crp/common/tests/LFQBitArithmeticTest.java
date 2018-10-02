package com.crp.common.tests;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.crp.common.LockFreeQ.QueueStatus;

public class LFQBitArithmeticTest
{

    @Before
    public void setUp() throws Exception
    {
    }

    
    @Test
    public void testIsReadyForConsumer()
    {
        String s = "0404100204040404";
        long status = Long.parseLong(s, 16);
        
        boolean ret = QueueStatus.isReadyForConsumer(6, status);
        assertEquals(true, ret);
    }

    @Test
    public void testJavaThreadInterrupts()
    {
        Runnable producer = new Runnable() {
            public void run()
            {
                while(true)
                {
                    if(Thread.currentThread().isInterrupted())
                    {
                        try
                        {
                            Thread.sleep(10000000);
                        }
                        catch(InterruptedException e)
                        {
                            System.out.println(" woke up from sleep");
                        }
                        break;
                    }
                }               
            }
        };
        Runnable consumer = new Runnable() {
            public void run()
            {
                String s = "jlt";

               
                System.out.println("DONE! consumer");
            }
        };
        Thread t1 = new Thread(producer);
        Thread t2 = new Thread(consumer);
        t1.start();
        try
        {
            Thread.sleep(1000);
            t1.interrupt();
            t1.join();
        }
        catch (InterruptedException e)
        {
            System.out.println(" main thread interrupted");
        }
       
    }
}
