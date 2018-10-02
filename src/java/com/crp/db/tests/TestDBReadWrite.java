package com.crp.db.tests;


import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.crp.common.CRPException;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CRPTailorMadeConfigs;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.common.tests.TestCapletGenerator;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;
import com.crp.ether.Message.MessageStatus;
import com.crp.interfaces.Caplet;
import com.crp.interfaces.TestCaplet;
import com.crp.memmgr.MemoryManager;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.process.ProcessMain;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadPoolManager;

public class TestDBReadWrite
{

    @Before
    public void setUp() throws Exception
    {
        
    }

    @Test
    public void testSimpleWrite()
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
        System.out.println("before creating photon");
        try
        {
            ph = PhotonPool.createPhoton(crps, true);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        for (int i = 0; i < 1000; i++)
        {
            TestCaplet tc = TestCapletGenerator.getNextTestCaplet();
            Message m = new Message(MESSAGE_CODE.MSG_TEST_CAPURE_AGENT);
            m.setForTesting();
            m.setTestObject(tc);
            try
            {
                while (!ph.bsendMessage(m))
                {
                    ph.getChannel().flushChannel();
                    ph.getChannel().notifyReceiver();
                }
            }
            catch (EtherException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            PhotonPool.closePhoton(ph, true);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try
        {
            // wait until close message goes first.
            Thread.sleep(15000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        GLOBAL_UTILS.shutDownAndWaitForServiceHandler(true);

        assertTrue(true);
        
    }
    
    @Test
    public void testLocalRead()
    {
        testSimpleRead(GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.LOCAL);
        System.out.println(MemoryManager.toJSON().toString());
    }
    
    @Test
    public void testRemoteRead()
    {
        testSimpleRead(GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE);
        System.out.println(MemoryManager.toJSON().toString());

    }
    
    private void testSimpleRead(GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE cType)
    {
    	// wait until the previous threads die.
    	
    	try 
    	{
			Thread.sleep(5000);
		} 
    	catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        CRPTailorMadeConfigs crptconf = new CRPTailorMadeConfigs();
        ProcessMain.initServices(crptconf.getAllInOneConfig(), true);
        
        //assumes a db exists for now.
        CRPServiceInfo dbs = new CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE,
            "localhost",
            "db handler service",
            9100, 0);
        
        if(cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE)
        {
            dbs.forceRemote();
            try
            {
                GLOBAL_UTILS.CRPifyJavaThread();
            }
            catch (CRPException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Photon dbPH = null;
        try
        {
            if((cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE))
            {
                dbPH = PhotonPool.createPhoton(dbs, false);
            }
            else
            {
                dbPH = PhotonPool.createPhoton(dbs, true);
            }
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     
        int numOfCaplets = 0;
        Message m = null;
        
        while(true)
        {
            m = new Message(MESSAGE_CODE.MSG_REQ_CLIENT_CAPLET_STREAM_DB);

            try
            {
                dbPH.sendMessageNoFail(m);
            }
            catch (EtherException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           
            try
            {
                while ( (m = dbPH.brecvMessage()) == null)
                {
                    Thread.yield();
                }
            }
            catch (EtherException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(m == null)
                continue;
            else
            {
                if (m.getMessageHeader().messageCode() == MESSAGE_CODE.MSG_FAIL
                    || m.getMessageHeader().messageCode() == MESSAGE_CODE.MSG_END_OF_DATA)
                {
                    try
                    {
                        m.updateStatus(MessageStatus.AVAILABLE);
                    }
                    catch (EtherException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                }
                else if (m.getMessageHeader().messageCode() == MESSAGE_CODE.MSG_RES_DB_CAPLET_STREAM_CLIENT)
                {
                    // read the caplets here.
                    m.getPayloadMBO().getMBB().resetOffset();
                    for (int i = 0; i < m.getPayloadMBO().getActiveObjects(); i++)
                    {
                        Caplet c = (Caplet) m.getPayloadMBO().getObjectAtIndex(
                            i);
                        numOfCaplets++;
                    }
                }
                try
                {
                    m.updateStatus(MessageStatus.AVAILABLE);
                }
                catch (EtherException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        assertEquals(m.getMessageHeader().messageCode(), MESSAGE_CODE.MSG_END_OF_DATA);
        assertEquals(numOfCaplets, 1000);
        try
        {
            if ((cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE))
            {
                PhotonPool.closePhoton(dbPH, false);
            }
            else
            {
                PhotonPool.closePhoton(dbPH, true);

            }
        }
        catch (EtherException e)
        {

        }
        
        // let the close message to sink in; this is to make sure we reuse the photon.
        try
        {
            Thread.sleep(10*1000);
        }
        catch(Exception e)
        {
            
        }
        if((cType == GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE))
        {
            GLOBAL_UTILS.shutDownAndWaitForServiceHandler(false);
        }
        else
        {
            GLOBAL_UTILS.shutDownAndWaitForServiceHandler(true);
        }
    }
    public static void main(String [] args)
    {
        System.out.println("inside main");
        TestDBReadWrite tdbrewr = new TestDBReadWrite();
        tdbrewr.testSimpleWrite();
        tdbrewr.testLocalRead();
        tdbrewr.testRemoteRead();
    }
}
