package com.crp.db.tests;


import static org.junit.Assert.*;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.crp.common.CRPException;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CRPTailorMadeConfigs;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.db.DBException;
import com.crp.db.DBUtils;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;
import com.crp.interfaces.CRPString;
import com.crp.process.ProcessMain;

public class testDBUtils
{

    @Before
    public void setUp() throws Exception
    {
    }
    
    @Test
    public void testListDir()
    {
        // read the db dir.
        String galoreRoot = System.getenv(GLOBAL_CONSTANTS.CRP_ENV);
        String fileSep = System.getProperty("file.separator");
        String crpDBFilePath = 
            galoreRoot + fileSep + "db" + fileSep;
        
        JSONObject jo = null;
        
        try
        {
            jo = DBUtils.listFilesInDir(crpDBFilePath);
        }
        catch (DBException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String jStr = jo.toString();
        System.out.println(" json string: " + jStr);
        assertTrue(true);
    }
    
    @Test
    public void testListDirThruCRPMsgs()
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
        CRPServiceInfo crps = new CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE,
                    "localhost",
                    "db handler service",
                    9100, 0);
        
        Photon ph = null;
        crps.forceRemote();

        try
        {
            GLOBAL_UTILS.CRPifyJavaThread();
        }
        catch (CRPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        try
        {
            ph = PhotonPool.createPhoton(crps, false);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Message m = new Message(MESSAGE_CODE.MSG_REQ_CRP_DB_METADATA);
        
        try
        {
            ph.sendMessageNoFail(m);

            while( (m = ph.brecvMessage()) == null )
                ;
            PhotonPool.closePhoton(ph, false);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertEquals(m.getMessageHeader().messageCode(),
            MESSAGE_CODE.MSG_RES_CRP_DB_METADATA_CLIENT);
        CRPString crpstring = (CRPString)(m.getPayloadMBO().getObjectAtIndex(0));
        System.out.println(" json: " + crpstring.toString());
        GLOBAL_UTILS.shutDownAndWaitForServiceHandler(false);
        assertTrue(true);
    }

}
