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

package com.crp.replay;

import com.crp.common.CRPException;
import com.crp.common.CRPRequest;
import com.crp.protocol.http.CRPHttpRequest;
import com.crp.thread.CRPThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 
 * @author subbu
 * @version $Id$
 * 
 * Worker class that represents a task for the threads in replay service.
 */
public class ReplayWorker implements Runnable
{
    /**
     * List of {@link CRPRequest} objects this worker thread will replay.
     */
    private List<CRPRequest> taskList1 = new ArrayList<CRPRequest>(10);
    
    /**
     * List of {@link CRPRequest} objects this worker thread will replay.
     */
    private List<CRPRequest> taskList2 = new ArrayList<CRPRequest>(10);
    
    /**
     * Indicates if task list 1 is in use by the thread.
     */
    private AtomicBoolean taskList1InUse = new AtomicBoolean();
    
    /**
     * Indicates if task list 2 is in use by the thread.
     */
    private AtomicBoolean taskList2InUse = new AtomicBoolean();
    
    /**
     * Each worker is wrapped in a separate thread of its own. It's the 
     * reference such a thread.
     */
    private CRPThread parentThread;

    /** Constructor. */
    public ReplayWorker() {}
       
    /**
     * {@inheritDoc}
     */
    @Override
    public void run() 
    {
        if (parentThread == null) {
            parentThread = (CRPThread) Thread.currentThread();
        }
        
        while (true) {
            // If the task list is not ready, suspend the activity.
            if (tasks.isEmpty()) {
                parentThread.suspendMe();
                continue;
            }
            
            for (int i = 0; i < tasks.size(); i++) {
                System.out.printf("Thread %s Processed tasks from queue.\n",
                    parentThread.getName());
                CRPHttpRequest request = tasks.get(i);
                try {
                    request.send();
                }
                catch (CRPException ce) {
                }
            }
            
            tasks.clear();
        }
    }
    
    
    /**
     * Set the tasks for this thread. There are two task lists for each thread,
     * at most one will be in use at any given time. So set the tasks on the
     * unused list.
     */
    public void setTasks(List<CRPRequest> tasks)
    {
        if (taskList1InUse.compareAndSet(false, true))
        {
            taskList1.addAll(tasks);
        }
        else if (taskList2InUse.compareAndSet(false, true)) 
        {
            taskList2.addAll(tasks);
        }
    }
 }
