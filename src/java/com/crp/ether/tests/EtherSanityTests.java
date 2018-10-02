package com.crp.ether.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.crp.capture.CaptureService;
import com.crp.common.CRPTailorMadeConfigs;
import com.crp.common.CommonLogger;
import com.crp.common.CRErrorHandling;
import com.crp.common.CRPException;
import com.crp.common.CRPServiceInfo;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.db.DBService;

import com.crp.ether.EtherData;
import com.crp.ether.EtherException;
import com.crp.ether.Message;
import com.crp.ether.NetworkChannel;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;

import com.crp.memmgr.MemoryManager;

import com.crp.process.PROCESS_GLOBALS;
import com.crp.process.ProcessMain;

import com.crp.thread.CRPThread;

public class EtherSanityTests
{
    @Test
    public void testPhotonPoolSimpleCase()
    {
        CRPTailorMadeConfigs crptconf = new CRPTailorMadeConfigs();
        ProcessMain.initServices(crptconf.getAllInOneConfig(), true);
        
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        CRPServiceInfo crps = new CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
                    "localhost",
                    "capture handler service",
                    9101, 0);
        
        Photon ph = null;
        try
        {
            ph = PhotonPool.createShortCircuitedPhoton(crps);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        GLOBAL_UTILS.shutDownAndWaitForServiceHandler(true);
    }
    
    @Test
    public void testWithMultiplePhotons()
    {
        CRPTailorMadeConfigs crptconf = new CRPTailorMadeConfigs();
        ProcessMain.initServices(crptconf.getAllInOneConfig(), true);
        
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        CRPServiceInfo crps = new CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
            "localhost",
            "capture handler service",
            9101, 0);
        try
        {
            for(int i = 0; i < 100; i++)
            {
                Photon ph = null;
                // if pooling works, this should never run out of memory.
                ph = PhotonPool.createShortCircuitedPhoton(crps);
                PhotonPool.closePhoton(ph, false);
            }
            
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    @Test
    public void testWithMultipleThreads()
    {
        CRPTailorMadeConfigs crptconf = new CRPTailorMadeConfigs();
        ProcessMain.initServices(crptconf.getAllInOneConfig(), true);
        
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        final CRPServiceInfo crps = new CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
            "localhost",
            "capture handler service",
            9101, 0);
        
        final int ITERATIONS = 100000;
        
        Runnable r1 = new Runnable() {
            public void run()
            {
                for(int i = 0; i < ITERATIONS; i++)
                {
                    Photon ph = null;
                    // if pooling works, this should never run out of memory.
                    try
                    {
                        ph = PhotonPool.createShortCircuitedPhoton(crps);
                        if(i == ITERATIONS/2)
                        {
                            continue;
                        }
                        PhotonPool.closePhoton(ph, false);

                    }
                    catch (EtherException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                System.out.println(" thread finished");
            }
        };
        
        Thread t1, t2;
        
        t1 = new Thread(r1);
        t2 = new Thread(r1);
        
        try
        {
            t1.start();
            t2.start();
            t1.join();
            t2.join();
        }
        catch(Exception e)
        {
            
        }
        GLOBAL_UTILS.shutDownAndWaitForServiceHandler(true);
        assertTrue(true);
    }
}

