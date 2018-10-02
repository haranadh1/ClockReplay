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

package com.crp.thread;

import java.util.HashMap;
import java.util.Map;





/**
 * place to store all crp thread local variables.
 * non-crp thread cannot use the crp data structures like
 * memory manager, message world, message collectors etc.,
 * this is a back door entry for any external thread to behave
 * like a crp thread. we create a fake crpthread object, though we
 * never start the thread. we only use its context.
 * this is really not a java thread local storage.
 * kind of cheating the java threads, to make it look like
 * thread local storage. only to be used for non-performance
 * applications.
 * @author hpoduri
 * @version $Id$
 */
public abstract class CRPThreadLocalStore 
{

    /**
     * map for thread local variables.
     */
    private static Map<Integer, CRPThread> tLMap = null;
    
    /**
     * constructor.
     */
    public CRPThreadLocalStore()
    {
        
    }
    
    /**
     * register non crp thread.
     * @param threadID input thread id.
     * @param crpt thread context.
     * @throws CRPThreadException on error.
     */
    public static final void registerThread(
        final int threadID, final CRPThread crpt) throws CRPThreadException
    {
        if(tLMap == null)
        {
            tLMap = new HashMap<Integer, CRPThread>();
        }
         /**
         * initialize the crp items.
         */
        tLMap.put(threadID, crpt);
    }
    
    /**
     * returns message world object for this thread.
     * @param tid input thread id.
     * @return message world object.
     */
    public static final CRPThread getCRPThreadContext(final int tid)
    {
        if(tLMap == null)
        {
            return null;
        }
        return (tLMap.get(tid));
    }
    
    /**
     * clears the external thread context.
     */
    public static final void unRegisterThread()
    {
        if(tLMap == null)
        {
            return;
        }
        tLMap.clear();
    }
}
