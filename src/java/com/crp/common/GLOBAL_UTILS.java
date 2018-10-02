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

package com.crp.common;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadLocalStore;
import com.crp.thread.CRPThreadPoolManager;
import com.crp.thread.CRPThread.CRPThreadStatus;

/**
 * class to store the common utility functions. please do not write heavy weight
 * methods here. the purpose of this file is a collection of all simple utility
 * methods.
 * @author hpoduri
 * @version $Id$
 */
public final class GLOBAL_UTILS
{
    /**
     * first thing, make private constructor.
     */
    private GLOBAL_UTILS()
    {
    }

    /**
     * returns memory block index from long. which is nothing but short value of
     * two high order bytes in a long.
     * @param inpValue long
     * @return memblock index.
     */
    public static short getMemBlockIndex(final long inpValue)
    {
        return ((short) ((inpValue & GLOBAL_CONSTANTS.MEMMGR_MASK_USEDSPACE) >>> GLOBAL_CONSTANTS.BITS_FOR_MEM_MGR_OFFSET));
    }
    /**
     * Returns a current method.
     * @return method name string. 
     */
    public static String getCurrentMethod()
    {
        return (Thread.currentThread().getStackTrace()[2].getMethodName());
    }
   
    /**
     * returns stack trace for the exception as string.
     * @param e exception object.
     * @return string.
     */
    public static String getStackTraceAsString(final Exception e)
    {
        // first get the stack trace:
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    /**
     * this method converts a java thread into crp thread.
     * NOTE: this method assigns 127, as crp thread id; we can
     * only have on external thread at any point of time. crp
     * design only supports one.
     * now any external thread can communicate with crp modules.
     */
    public static void CRPifyJavaThread() throws CRPException
    {
        CRPThread crpt = null;
        
        Runnable r1 = new Runnable () 
        {
            public void run()
            {
                while (true)
                {
                    ;
                
                }
            }
        };
        
        try 
        {
            crpt = CRPThreadPoolManager.createNewThread(
                GLOBAL_ENUMS.SERVICE_CATALOG.UI_CLIENT_SERVICE, r1,
                GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                CRPThread.getCurrentThreadID());
        }
        catch (CRPThreadException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            throw new CRPThreadException("CRP_COMMON_ERROR_006", null);
        }
        crpt.setCRPThreadStatus(CRPThreadStatus.UNINIT);
        CRPThreadLocalStore.registerThread(
            GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID, crpt);
        crpt.initMsgPools();
    }
    
    /**
     * clears the external thread context.
     */
    public static void cleanUpExternalThreadContext()
    {
        CRPThreadLocalStore.unRegisterThread();
    }
    /**
     * returns offset of byte array in memmgr.
     * @param inpValue long var
     * @return offset.
     */
    public static long getUsedSpace(final long inpValue)
    {
        return ((inpValue & GLOBAL_CONSTANTS.MEMMGR_MASK_INDEX));
    }

    /**
     * reads db path from CRP_ROOT env variable.
     * @return db location (path) string.
     */
    public static final String getDBLocation()
    {
        String galoreRoot = System.getenv(GLOBAL_CONSTANTS.CRP_ENV);
        String fileSep = System.getProperty("file.separator");
        String crpDBFilePath = 
            galoreRoot + fileSep + "db" + fileSep;
        return crpDBFilePath;
    }
    /**
     * add memblock index to offset in memmgr.
     * @param inpOffset offset in memmgr
     * @param inpIndex index to be added.
     * @return long after adding the index.
     */
    public static long addMemBlockIndex(final long inpOffset,
        final short inpIndex)
    {
        long temp = (long) inpIndex;
        return ((temp << GLOBAL_CONSTANTS.BITS_FOR_MEM_MGR_OFFSET) | (inpOffset & GLOBAL_CONSTANTS.MEMMGR_MASK_INDEX));
    }

    /**
     * convenience method to increment index.
     * @param inpOffset offset in MemMgr
     * @return offset by incrementing only the index part.
     */
    public static long incrMemBlockIndex(final long inpOffset)
    {
        short index = GLOBAL_UTILS.getMemBlockIndex(inpOffset);
        index++; // bump the index
        return (GLOBAL_UTILS.addMemBlockIndex(inpOffset, index));
    }
    /**
     * if debug memory manager.
     * @return true if memory mgr debug.
     */
    public static boolean memMgrDebug() 
    {
        return ((GLOBAL_CONSTANTS.CRP_DEBUG & 1) > 0);
    }
   
    /**
     * true if debugging enabled for messages.
     * @return true if enabled.
     */
    public static boolean msgDebug()
    {
        return((GLOBAL_CONSTANTS.CRP_DEBUG & 512) > 0);
    }
    /**
     * test methods.
     */
    private static void testMemMgrOffsetIndex()
    {
        long tval = 3;
        short tindex = 1;
        long tvalIndex = addMemBlockIndex(tval, tindex);
        System.out.println(GLOBAL_UTILS.getCurrentMethod());
        System.out.println("-----------------------------");
        if (tval != getUsedSpace(tvalIndex)
            || tindex != getMemBlockIndex(tvalIndex))
        {
            System.out.println("Test Failed");
            System.out.println(" index: "
                + String.valueOf(getMemBlockIndex(tvalIndex)));
            System.out.println(" index: "
                + String.valueOf(getUsedSpace(tvalIndex)));
        }
        else
        {
            System.out.println(" index : " + String.valueOf(tindex));
            System.out.println(" offset: "
                + String.valueOf(getUsedSpace(tvalIndex)));
            System.out.println("Test Passed");
        }
        System.out.println("-----------------------------------");
        System.out.println(" index : "
            + String.valueOf(getMemBlockIndex(tvalIndex)));
        System.out.println(" offset: "
            + String.valueOf(getUsedSpace(tvalIndex)));
        // try incrementing.
        tvalIndex = incrMemBlockIndex(tvalIndex);
        System.out.println(" index : "
            + String.valueOf(getMemBlockIndex(tvalIndex)));
        System.out.println(" offset: "
            + String.valueOf(getUsedSpace(tvalIndex)));
        tvalIndex = incrMemBlockIndex(tvalIndex);
        System.out.println(" index : "
            + String.valueOf(getMemBlockIndex(tvalIndex)));
        System.out.println(" offset: "
            + String.valueOf(getUsedSpace(tvalIndex)));
    }

    /**
     * test main.
     * @param args string list.
     */
    public static void main(final String[] args)
    {
        testMemMgrOffsetIndex();
    }

    /**
     * utility function to issue shutdown command.
     * this method waits until service handler thread shuts down.
     * @param isLocal true if this is a local communication, false for remote.
     */
    public static void shutDownAndWaitForServiceHandler(final boolean isLocal)
    {
        CRPServiceInfo service = new CRPServiceInfo(
            GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,
            "localhost",
            "localhost",
            9100,0);
        if(!isLocal)
        {
            service.forceRemote();
        }
        Message m = new Message(MESSAGE_CODE.MSG_SHUTDOWN);
        Photon shutPH = null;
        try
        {
            shutPH = PhotonPool.sendQuickMessage(service, m);
        }
        catch (EtherException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CRPThread crpt = CRPThreadPoolManager.getCRPThread(PROCESS_GLOBALS.SERVICE_HANDLER_THREAD_ID);
        try
        {
            crpt.join();
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
