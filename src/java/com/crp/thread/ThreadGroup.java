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

import com.crp.common.GLOBAL_CONSTANTS;

/**
 * CRPThread group class.
 * @author hpoduri
 * @version $Id$
 */
public class ThreadGroup
{
    /**
     * array to store threads.
     */
    private short [] threadArray;
    /**
     * number of threads in this thread group.
     */
    private short threadCount;
    
    /**
     * constructor.
     */
    public ThreadGroup()
    {
        threadArray = new short [GLOBAL_CONSTANTS.MAX_THREADS_IN_PROCESS];
        threadCount = 0;
    }
    /**
     * adds a thread to thread group.
     * @param index input thread index.
     */
    public final void addThread(final short index)
    {
        assert(index >=0);
        
        threadArray[threadCount++] = index;
    }
    /**
     * returns current thread count in this thread group.
     * @return returns the thread count.
     */
    public final short threadCount()
    {
        return threadCount;
    }

}
