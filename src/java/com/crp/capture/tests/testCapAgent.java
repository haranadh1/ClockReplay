package com.crp.capture.tests;


import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.crp.capture.CaptureContextObject;
import com.crp.capture.CaptureEngineException;
import com.crp.capture.JnetPcapCaptureEngine;
import com.crp.common.JSON_KEYS;

public class testCapAgent
{
    

    @Before
    public void setUp() throws Exception
    {
        
    }
    
    @Test
    public void testListDevices()
    {
        
        JnetPcapCaptureEngine jpcapE = null;
        try
        {
            jpcapE = new JnetPcapCaptureEngine();
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
      //  try
        {
         //   GLOBAL_UTILS.CRPifyJavaThread();
        }
       // catch (CRPException e2)
        {
            // TODO Auto-generated catch block
         //   e2.printStackTrace();
        }
        
        JSONObject jo = null; 
        try
        {
            jo = jpcapE.listAllNetworkInterfaces();
            System.out.println(jo.toString());
        }
        catch (CaptureEngineException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JSONObject device = null;
        try
        {
            device = jo.getJSONArray(JSON_KEYS.JsonKeyStrings.JSON_CAP_LIST_DEVICES).getJSONObject(2);
        }
        catch (JSONException e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        System.out.println(device.toString());
        try
        {
            jpcapE.setupCapture(
                device.getJSONArray(
                    JSON_KEYS.JsonKeyStrings.JSON_CAP_NETWORK_INT_DETAILS).getJSONObject(0).getString(
                        JSON_KEYS.JsonKeyStrings.JSON_CAP_NETWORK_INT_NAME));
            jpcapE.startCapture(new CaptureContextObject());
        }
        catch (CaptureEngineException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
