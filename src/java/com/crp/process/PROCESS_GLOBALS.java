/*
 *
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of ClockReplay Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or
 * copyright law.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from ClockReplay Incorporated.
 */
package com.crp.process;

import com.crp.common.CRPService;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.thread.CRPThread;

import java.util.HashMap;
import java.util.Map;

/**
 * Keep all the process global objects here.
 * @author hpoduri
 * @version $Id$
 */
public final class PROCESS_GLOBALS 
{
    /**
     * represents a process status.
     * Note that this is for the whole process, not to the
     * individual threads.
     */
    public static GLOBAL_ENUMS.PROCESS_STATUS PSTATUS =
        GLOBAL_ENUMS.PROCESS_STATUS.PROCESS_INIT;
    
    /**
     * all services in a process are added to this hash map.
     * key: service name string.
     * value: service object.
     */
    public static final Map<GLOBAL_ENUMS.SERVICE_CATALOG, CRPService> PSERVICES =
            new HashMap<GLOBAL_ENUMS.SERVICE_CATALOG, CRPService>();
    
    /**
     * service handler thread id.
     * should be updated only by service handler thread. 
     */
    public static volatile short SERVICE_HANDLER_THREAD_ID = -1;
    
    /**
     * disable default constructor.
     */
    private PROCESS_GLOBALS()
    {
        // disable default constructor.
    }
    /**
     * add service to process global services.    
     * @param sClass service object.
     */
    public static void addService(
        final CRPService sClass)
    {
        PROCESS_GLOBALS.PSERVICES.put(
            sClass.getServiceInfo().getName(), sClass);
    }
    /**
     * get the service info of current thread.
     * @return string info about the service.
     */
    public static String getMyServiceInfoString()
    {
        String s;
        CRPThread crpt = CRPThread.getCurrentThread();
        if(crpt == null)
        {
            s = "The thread is not a CRPThread";
        }
        else
        {
            s = PROCESS_GLOBALS.PSERVICES.get(
                crpt.getServiceName()).getServiceInfo().zipString();
        }
        return s;
    }
    /**
     * returns service name of the current thread.
     * @return service name string.
     */
    public static GLOBAL_ENUMS.SERVICE_CATALOG getMyServiceName()
    {
        CRPThread crpt = (CRPThread)Thread.currentThread();
        return crpt.getServiceName();
    }
    
    /**
     * returns the current service this thread belongs to.
     * return CRPService Object.
     */
    public static CRPService getMyService()
    {
        CRPThread crpt = (CRPThread)Thread.currentThread();
        return crpt.getMyService();
    }
    /**
     * returns if this service is configured on this process.
     * @param service input service name
     * @return boolean(true, if configured; else false)
     */
    public static boolean isConfiguredOnThisProcess(
        final GLOBAL_ENUMS.SERVICE_CATALOG service)
    {
        if(PROCESS_GLOBALS.PSERVICES.get(
            GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE) == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
}
