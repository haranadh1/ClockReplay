package com.crp.interfaces.tests;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.ether.EtherException;
import com.crp.ether.Message;
import com.crp.interfaces.Caplet;
import com.crp.memmgr.ObjectFactory;
import com.crp.process.ProcessMain;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;

public class CapletTest
{

    @Before
    public void setUp() throws Exception
    {
        ProcessMain.createDummyService();
    }
    
    @Test
    public void testCapletObjPool()
    {
      
        Runnable t1 = new Runnable()
        {
            public void run()
            {
                CRPThread crpt = CRPThreadPoolManager.getCRPThread(
                    CRPThread.getCurrentThreadID());
                Message currentDBMessage = null;
                
                try
                {
                    crpt.initMsgPools();
                }
                catch(CRPThreadException e)
                {
                    CommonLogger.CMN_LOG.fatal("Capture Worker Initialization failed ");
                    return;
                }
                
                // create a message pool for caplet streaming from capture service
                // to db service.
                ObjectFactory ofcap = new Caplet(null);
                try
                {
                    crpt.getMyMsgWorld().createMsgGroupPool(
                        ofcap, 100, GLOBAL_CONSTANTS.CAPLET_MEMORY_BLOCK_SIZE);
                }
                catch(EtherException e)
                {
                    CommonLogger.CMN_LOG.error(e.getMessage());
                    return;
                }
                try
                {
                    currentDBMessage = crpt.getMyMsgWorld().getFreeMsg(
                        GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY);
                    
                }
                catch(EtherException e)
                {
                    CommonLogger.CMN_LOG.error(e.getMessage());
                    return;
                }
                String [] input = new String [] {
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa0",
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa1",
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa2",
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa3",
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa4",
                    "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa5"
                    
                };
                String [] output = new String [6];
                for(int i = 0; i <= 5; i++)
                {
                    Caplet c = (Caplet)currentDBMessage.getPayLoadObject();
                    String s = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + String.valueOf(i);
                    c.setAttr("rhost", 1234, "123.12.12", 1000000L, input[i].getBytes());
                }
                for(int i = 0; i < currentDBMessage.getPayloadMBO().getActiveObjects(); i++)
                {
                    Caplet c = (Caplet)currentDBMessage.getPayloadMBO().getObjectAtIndex(i);
                    output[i] = c.getPayLoadObject().toString();
                }
                assertArrayEquals(input, output);
            }
        };
        CRPThread crpt = null;
        try
        {
            crpt = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                t1, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
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

}
