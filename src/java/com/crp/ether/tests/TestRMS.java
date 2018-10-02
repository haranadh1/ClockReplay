package com.crp.ether.tests;


import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

import org.junit.*;
import static org.junit.Assert.*;

import com.crp.common.CRPServiceInfo;
import com.crp.common.CRPTailorMadeConfigs;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.process.ProcessMain;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadPoolManager;
import com.crp.thread.CRPThread.CRPThreadStatus;

/**
 * tests remote selector.
 * @author hpoduri
 *
 */
public class TestRMS
{

    @Before
    public void setUp() throws Exception
    {
    }
    
    public static Selector remoteSelector = null;
    @Test
    public void testSelector()
    {
       
        Runnable r1 = new Runnable() {
                
            @Override
            public void run()
            {
                try
                {
                    remoteSelector = SelectorProvider.provider().openSelector();
                    remoteSelector.select();
                    System.out.println("thread just woke up");
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }      
            } 
        };
        Thread t1 = new Thread(r1);
        t1.start();
        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        remoteSelector.wakeup();
        try
        {
            t1.join();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testServiceThread()
    {
        CRPTailorMadeConfigs config = new CRPTailorMadeConfigs();
        //also starts service thread.
        ProcessMain.initServices(config.getAllInOneConfig(), true);
        
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        CRPThread serviceThread = CRPThreadPoolManager.getCRPThread(
            PROCESS_GLOBALS.SERVICE_HANDLER_THREAD_ID);
        
        Runnable r1 = new Runnable() {
            
            @Override
            public void run()
            {
                try
                {
                    CRPServiceInfo crps = new CRPServiceInfo(
                        GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
                        "localhost",
                        "localhost", 9100, 0);
                    Photon ph = PhotonPool.createPhoton(crps, true);
                    
                    assertTrue(ph.isConnected());
                    assertTrue(ph.isLocal());
                    ph.close();
                    assertTrue(!ph.isConnected());

                    CRPServiceInfo service = new CRPServiceInfo(
                        GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,
                        "localhost",
                        "localhost",
                        9100,0);
                    Message m = new Message(MESSAGE_CODE.MSG_SHUTDOWN);
                    Thread.sleep(10000);
                    Photon shutPH = PhotonPool.sendQuickMessage(service, m);
                    
                    assertTrue(shutPH.isConnected());
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }    
            } 
        };
        Thread t1 = new Thread(r1);
        t1.start();
        
        try
        {
            t1.join();
            serviceThread.join();

        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(
            CRPThreadPoolManager.getCRPThread(PROCESS_GLOBALS.SERVICE_HANDLER_THREAD_ID).getCRPThreadStatus() == CRPThreadStatus.FINISHED);
    }
}
