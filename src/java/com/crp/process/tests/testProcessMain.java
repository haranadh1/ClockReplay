package com.crp.process.tests;


import org.junit.Before;
import org.junit.Test;

import com.crp.client.UIService;
import com.crp.common.CRPException;
import com.crp.common.CRPServiceInfo;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.process.ProcessMain;

/**
 * tests proces main.
 * @author hpoduri
 *
 */
public class testProcessMain
{

    @Before
    public void setUp() throws Exception
    {
    }
    
    @Test
    public final void testStartProcess()
    {
        ProcessMain.startMainProcess();
        
        UIService uis = null;
        uis = new UIService(new CRPServiceInfo(
            GLOBAL_ENUMS.SERVICE_CATALOG.UI_CLIENT_SERVICE, 
            "localhost", "LOCALHOST", 9200, 1));
        try
        {
            uis.initForUIServiceOnlyThread();
        }
        catch (CRPException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
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
        try
        {
            uis.shutDownCRPProcess();
        }
        catch (CRPException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
