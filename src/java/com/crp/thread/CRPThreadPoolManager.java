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

package com.crp.thread;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Category;
import org.json.JSONException;
import org.json.JSONObject;

import com.crp.common.CRPException;
import com.crp.common.CRPLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_ENUMS.THREAD_TYPE;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.process.PROCESS_GLOBALS;

/**
 * thread pool to maintain all CRPThreads in a process.
 * @author hpoduri
 * @version $Id$
 */
public final class CRPThreadPoolManager
{
    /**
     * static array to represent all the threads in the process.
     */
    final private static CRPThread [] tPool =
        new CRPThread[GLOBAL_CONSTANTS.MAX_THREADS_IN_PROCESS];
    
    /**
     * logger for thread pool manager.
     */
    public static final Category THRD_POOL_LOG = 
        CRPLogger.initializeLogger(
            "com.crp.thread.CRPThreadPoolManager");
    /**
     * thread count. Also acts as an index in thread pool.
     * thread count only grows, when thread exits, we should 
     * figure out if the thread is alive or not by the status.
     */
    private static AtomicInteger tCount = new AtomicInteger(0);
    /**
     * lets make constructor private.
     */
    private CRPThreadPoolManager()
    {
        //do nothing.
    }
    /**
     * create a new thread and add to the thread pool.
     * @param serviceName service that is instantiating the thread.
     * refer {@link #CRPServiceInfo}
     * @param crpTask runnable task.
     * @param tt thread type.
     * @param ownerID thread Id that creates this thread.
     *        give GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD, if the
     *        main thread is the one creating this thread.
     * @return crp thread instance.
     * @throws CRPThreadException on error
     */
    public static CRPThread createNewThread(
        final GLOBAL_ENUMS.SERVICE_CATALOG serviceName,
        final Runnable crpTask,
        final GLOBAL_ENUMS.THREAD_TYPE tt,
        final int ownerID) throws CRPThreadException
    {
        //validate the service name first.
        try
        {
            if (PROCESS_GLOBALS.PSERVICES.get(serviceName) == null)
            {
                throw new CRPException(
                    "CRP_COMMON_ERROR_003",
                    new String[]{serviceName.toString()});
            }
        }
        catch (CRPException e)
        {
            THRD_POOL_LOG.error(e.getMessage());
            throw new CRPThreadException("CRP_THREAD_ERROR_001", null);
        }
        CRPThread crpt = new CRPThread(crpTask, serviceName, ownerID);
        StringBuilder sb = new StringBuilder(
            GLOBAL_CONSTANTS.MEDIUM_STRING_SIZE);
        sb.append(PROCESS_GLOBALS.PSERVICES.get(
            serviceName).getServiceInfo().getDesc());
        
        short tIndex = (short) tCount.getAndIncrement();
        
        sb.append("_");
        sb.append(String.valueOf(tt));
        sb.append(String.valueOf(tIndex));
        /**
         * name of the thread should be like
         * <service_name>_<thread_type>_<thread_id>.
         */
        crpt.setName(sb.toString());
        crpt.setCRPThreadID(tIndex);
        //add to pool.
        tPool[tIndex] = crpt;
        // register this thread to Memory Manager.
        try 
        {
            MemoryManager.registerThread(tIndex);
        }
        catch (MemoryManagerException e)
        {
            THRD_POOL_LOG.error(e.getMessage());
            throw new CRPThreadException("CRP_THREAD_ERROR_001", null);
        }
        return crpt;
    }
    /**
     * returns crp thread object for given thread id.
     * @param tid thread id
     * @return crp thread object.
     */
    public static CRPThread getCRPThread(final short tid)
    {
        if(tid == GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID)
        {
            return CRPThreadLocalStore.getCRPThreadContext(tid);
        }
        else
        {
            return tPool[tid];
        }
    }
    /**
     * zip string representation of thread pool class.
     * @return string representation of this class.
     */
    public static String zipString()
    {
        StringBuilder sb = new StringBuilder(
            GLOBAL_CONSTANTS.MEDIUM_STRING_SIZE);
        for ( int i = 0; i < tCount.get(); i++)
        {
            sb.append("<br> " + tPool[i].zipString());
        }
        
        return sb.toString();
        
    }
    
    /**
     * returns number of crp threads currently.
     * @return integer.
     */
    public static int numOfCRPThreads()
    {
        return (tCount.get());
    }
    
    /**
     * returns json representation of this string.
     * @return JSONObject.
     */
    public static JSONObject toJSON()
    {
        JSONObject ret = new JSONObject();
        
        for(int i = 0; i < tCount.get(); i++)
        {
            try
            {
                ret.put(String.valueOf(i), tPool[i].toJSON());
            }
            catch (JSONException e)
            {
                CRPThreadPoolManager.THRD_POOL_LOG.error(e.getMessage());
                
            }
        }
        return ret;
    }
}
