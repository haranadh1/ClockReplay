package com.crp.common.tests;


import static org.junit.Assert.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.crp.common.CRPContext;
import com.crp.common.CRPContextObjectInterface;
import com.crp.common.CRPException;
import com.crp.common.CRPTailorMadeConfigs;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.process.ProcessMain;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;

public class testCRPContext
{

    @Before
    public void setUp() throws Exception
    {
        CRPTailorMadeConfigs crpConfig = new CRPTailorMadeConfigs();
        
        ProcessMain.initServices(crpConfig.getAllInOneConfig(),true);
        
    }
    public class ctxObj implements CRPContextObjectInterface
    {
        String str;
        
        public ctxObj(String inpStr)
        {
            str = inpStr;
        }
        @Override
        public JSONObject toJSON() throws JSONException
        {
            // TODO Auto-generated method stub
            return null;
        }
        public String toString()
        {
            return str;
        }
        
    }
    private void threadRunMethod()
    {
        try
        {
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CRPThread crpt = CRPThread.getCurrentThread();
        try
        {
            CRPContext ctx = crpt.getThreadContextGroup().createContext("FOR TESTING");
            
            

            crpt.getThreadContextGroup().addObject(ctx, new ctxObj("abc"), "abc");
            crpt.getThreadContextGroup().addObject(ctx, new ctxObj("abcdef"), "abcdef");

            CRPContext ctx1 = crpt.getThreadContextGroup().createContext("FOR TESTING IIII");

            crpt.getThreadContextGroup().addObject(ctx1, new ctxObj("xyz"), "xyz");
            crpt.getThreadContextGroup().addObject(ctx1, new ctxObj("123"), "123");
            System.out.println(crpt.getThreadContextGroup().toJSON().toString());

            crpt.getThreadContextGroup().deleteContext(0);
            crpt.getThreadContextGroup().deleteContext(1);
            System.out.println(crpt.getThreadContextGroup().toJSON().toString());
        }
        catch (CRPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testWithSimpleCRPThread()
    {
        Runnable thread1 = new Runnable() {
            public void run()
            {
                threadRunMethod();
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
            
        }
        catch(CRPThreadException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            
        }
        long sT = System.currentTimeMillis();
        crpt1.start();
        
        try
        {
            crpt1.join();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    
    @Test
    public void testWithMultipleThreads()
    {
        Runnable thread1 = new Runnable() {
            public void run()
            {
                threadRunMethod();
            }
        };
        CRPThread crpt1 = null;
        CRPThread crpt2 = null;
        CRPThread crpt3 = null;
        
        try
        {
            crpt1 = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                thread1, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
            crpt2 = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                thread1, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
            crpt3 = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                thread1, GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
            
        }
        catch(CRPThreadException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            
        }
        long sT = System.currentTimeMillis();
        crpt1.start();
        crpt2.start();
        crpt3.start();
        
        try
        {
            crpt1.join();
            crpt2.join();
            crpt3.join();
            assertEquals(crpt1.getThreadContextGroup().toJSON().toString(), crpt2.getThreadContextGroup().toJSON().toString());
            assertEquals(crpt1.getThreadContextGroup().toJSON().toString(), crpt3.getThreadContextGroup().toJSON().toString());

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

}
