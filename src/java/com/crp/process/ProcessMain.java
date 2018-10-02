/**
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved. NOTICE: All
 * information contained herein is, and remains the property of ClockReplay
 * Incorporated and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from ClockReplay Incorporated.
 */

package com.crp.process;

import java.io.IOException;

import com.crp.capture.CaptureService;
import com.crp.common.CRErrorHandling;
import com.crp.common.CRPConfig;
import com.crp.common.CRPException;
import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CRPTailorMadeConfigs;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.db.DBService;
import com.crp.ether.ServiceHandler;

import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.thread.CRPThread;

/**
 * main process for crp services.
 * @author hpoduri
 * @version $Id$
 */
public final class ProcessMain
{
    /**
     * make the constructor private.
     */
    private ProcessMain()
    {
        
    }
    /**
     * create dummy service for testing.
     */
    public static final void createDummyService()
    {
        PROCESS_GLOBALS.PSTATUS = GLOBAL_ENUMS.PROCESS_STATUS.PROCESS_INIT;
        CRErrorHandling.load();
        ServiceHandler sh = new ServiceHandler(
            new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                "localhost",
                "dummy handler service",
                9010,
                0));
        PROCESS_GLOBALS.addService(
             sh);
     // Initialize MemoryManager
        try 
        {
            MemoryManager.init(100 * GLOBAL_CONSTANTS.MB);
        }
        catch(MemoryManagerException e)
        {
            MemoryManager.MEM_LOG.error(e.getMessage());
        }
        
        PROCESS_GLOBALS.PSTATUS = GLOBAL_ENUMS.PROCESS_STATUS.PROCESS_READY;
    }
    
    /**
     * initialized environment for crp process start.
     * also starts service handler service.
     * creates service handler service.
     * @param sHandlerService service handler service info object.
     */
    public static final void init(final CRPServiceInfo sHandlerService)
    {
        try
        {
            // First things first, set the process status to init.
            PROCESS_GLOBALS.PSTATUS = GLOBAL_ENUMS.PROCESS_STATUS.PROCESS_INIT;
            // Load the error messages.
            CRErrorHandling.load();
            
            
            // Initialize MemoryManager
            MemoryManager.init(200 * GLOBAL_CONSTANTS.MB);
            
            PROCESS_GLOBALS.PSTATUS = GLOBAL_ENUMS.PROCESS_STATUS.PROCESS_READY;
            
            ServiceHandler sh = new ServiceHandler(
                sHandlerService);
            PROCESS_GLOBALS.addService(sh);
           
            // start the service handler service first,
            // which will keep the ball rolling.
            
            CRPThread crpt = sh.createMainThread();
            crpt.start();
            
            try
            {
                Thread.sleep(1000);
                Thread.sleep(500);
            }
            catch(InterruptedException e)
            {
                CommonLogger.CMN_LOG.error(e.getMessage());
            }
            
        }
        catch (CRPException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            e.printStackTrace();
            //TODO : should find the correct method for the logger to flush.
            CommonLogger.CMN_LOG.shutdown();
        }        
    }
    /**
     * configure necessary services on this process.
     * @param conf input configuration object.
     * @param startServiceThread true, if the service thread needs to be started.
     */
    public static final void initServices(final CRPConfig conf,
        final boolean startServiceThread)
    {
        try
        {
            // First things first, set the process status to init.
            PROCESS_GLOBALS.PSTATUS = GLOBAL_ENUMS.PROCESS_STATUS.PROCESS_INIT;
            // Load the error messages.
            CRErrorHandling.load();
            
            // Initialize MemoryManager
            MemoryManager.init(300 * GLOBAL_CONSTANTS.MB);
            
            PROCESS_GLOBALS.PSTATUS = GLOBAL_ENUMS.PROCESS_STATUS.PROCESS_READY;
            
            ServiceHandler sh = new ServiceHandler(
                new CRPServiceInfo(
                    GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,
                    "localhost","localhost",
                    GLOBAL_CONSTANTS.DEFAULT_SERVICE_HANDLER_PORT,1));
            PROCESS_GLOBALS.addService(sh);

            // add a dummy handler service, to make sure that our tests work.
            ServiceHandler dhs = new ServiceHandler(
                new CRPServiceInfo(
                    GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
                    "localhost","localhost",
                    GLOBAL_CONSTANTS.DEFAULT_SERVICE_HANDLER_PORT,1));
            PROCESS_GLOBALS.addService(dhs);

            // add the necessary services based on the config.
            
            String tempNode = conf.getServiceNode(
                GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE);
            
            if ( tempNode != null && tempNode.equalsIgnoreCase(
                GLOBAL_CONSTANTS.LOCAL_HOST))
            {
                //add the service to this process.
                CaptureService cs = new CaptureService(
                    conf.getService(
                        GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE));
                PROCESS_GLOBALS.addService(cs);
            }
            
            tempNode = conf.getServiceNode(
                GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE);
            if(tempNode != null && tempNode.equalsIgnoreCase(
                    GLOBAL_CONSTANTS.LOCAL_HOST))
            {
                //add the service to this process.
                DBService ds = new DBService(
                    conf.getService(
                        GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE));
                PROCESS_GLOBALS.addService(ds);
                
                // add io service, same like db service.
                
                CRPService ios = new CRPService( new CRPServiceInfo(
                    GLOBAL_ENUMS.SERVICE_CATALOG.IO_SERVICE,
                    "localhost",
                    "io service",
                    9090,
                    1
                    ));
                PROCESS_GLOBALS.addService(ios);
            }
            
            
            tempNode = conf.getServiceNode(
                GLOBAL_ENUMS.SERVICE_CATALOG.UI_CLIENT_SERVICE);
            if (tempNode !=null && tempNode.equalsIgnoreCase(
                GLOBAL_CONSTANTS.LOCAL_HOST))
            {
                //TODO : do the right thing when we have ui client service.
                // for now using crp generic service.
                
                //add the service to this process.
                CRPService cs = new CRPService(
                    conf.getService(
                        GLOBAL_ENUMS.SERVICE_CATALOG.UI_CLIENT_SERVICE));
                PROCESS_GLOBALS.addService(cs);
            }
            
           
            // start the service handler service first,
            // which will keep the ball rolling.
            
            CRPThread crpt = sh.createMainThread();
            if(startServiceThread)
            {
                crpt.start();
            }
        }
        catch (CRPException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            e.printStackTrace();
            //TODO : should find the correct method for the logger to flush.
            CommonLogger.CMN_LOG.shutdown();
        }       
    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        /**
         * CRPServiceInfo sh = new CRPServiceInfo(
         
                GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,
                "localhost",
                "service handler service",
                GLOBAL_CONSTANTS.DEFAULT_SERVICE_HANDLER_PORT,
                0);
        init(sh); */
        CRPTailorMadeConfigs crpt = new CRPTailorMadeConfigs();
        CRPConfig conf = crpt.getAllInOneConfig();
        
        /**
         * receive config object from the crp server.
         */
        initServices(conf, true);    
        try
        {
            Thread.sleep(GLOBAL_CONSTANTS.SLEEP_FOR_EVER);
        }
        catch(InterruptedException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
        }
    }
    
    /**
     * used when need to start the process from another java process.
     * useful for testing.
     */
    public static final void startMainProcess()
    {
        ProcessBuilder pb = new ProcessBuilder("ProcessMain");
        try
        {
            Process p = pb.start();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
