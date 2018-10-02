package com.crp.common.tests;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.crp.common.CRPTailorMadeConfigs;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;
import com.crp.interfaces.CRPString;
import com.crp.memmgr.MemoryManager;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.process.ProcessMain;

public class ResourceManagerTest
{

    @Before
    public void setUp() throws Exception
    {
    	
    }

    @Test
    public void testSimpleCase()
    {
        CRPTailorMadeConfigs crpConfig = new CRPTailorMadeConfigs();
        
        ProcessMain.initServices(crpConfig.getAllInOneConfig(),true);
        
        // wait until the service thread starts up.
        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Photon ph = null;
        // connect to resource manager thread.
        Message m = new Message(MESSAGE_CODE.MSG_REQ_MEMMGR_INFO);
        String memMgrJson = null;
        try
        
        {
            memMgrJson = MemoryManager.toJSON().toString();

            ph = PhotonPool.sendQuickMessage(
                PROCESS_GLOBALS.PSERVICES.get(
                    GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE
                ).getServiceInfo(), m);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // send the message directly to service handler thread.
        try
        {
            while((m = ph.brecvMessage()) == null)
            {
                ;
            }
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String jsonStr = m.getPayloadMBO().getObjectAtIndex(0).toString();
        System.out.println(jsonStr.toString());
        //bcoz we used some memory for crp string, lets reset it to get the same string.
        CRPString cs = (CRPString) (m.getPayloadMBO().getObjectAtIndex(0));
        System.out.println(memMgrJson);

        assertEquals(memMgrJson, jsonStr);
        Message shutMsg = new Message(MESSAGE_CODE.MSG_SHUTDOWN);
        
        try
        {
            PhotonPool.closePhoton(ph, true);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Photon shutPH = null;
        try
        {
            shutPH = PhotonPool.sendQuickMessage(
                PROCESS_GLOBALS.PSERVICES.get(
                    GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE
                ).getServiceInfo(), shutMsg);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
