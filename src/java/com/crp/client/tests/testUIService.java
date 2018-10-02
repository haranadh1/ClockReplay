package com.crp.client.tests;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.crp.client.UIService;
import com.crp.common.CRPException;
import com.crp.common.CRPServiceInfo;
import com.crp.common.GLOBAL_ENUMS;

public class testUIService
{

    private UIService uis;
    
    @Before
    public void setUp() throws Exception
    {
        uis = new UIService(new CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.UI_CLIENT_SERVICE, 
            "localhost", "LOCALHOST", 9200, 1));
        uis.initForUIServiceOnlyThread();
    }
    
    @Test
    public void testRecvDBMetadata()
    {
        String dbms = null;
        try
        {
            dbms = uis.getDBMetadataJsonString();
        }
        catch (CRPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(dbms);
        assertTrue(true);
    }

    @Test
    public void testRecvMemMgrInfo()
    {
        String memmgr = null;
        try
        {
            memmgr = uis.getMemMgrInfoJsonString();
        }
        catch (CRPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(memmgr != null);
        System.out.println(memmgr);
    }
    @Test
    public void testRecvThreadPoolInfo()
    {
        String thread = null;
        try
        {
            thread = uis.getThreadPoolInfoJsonString();
        }
        catch (CRPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertTrue(thread != null);
        System.out.println(thread);
    }
    
    @Test
    public void testStartCapture()
    {
        try
        {
            uis.startCapture();
        }
        catch (CRPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    @Test
    public void testStopCapture()
    {
        try
        {
            uis.stopCapture();
        }
        catch (CRPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }  
}
