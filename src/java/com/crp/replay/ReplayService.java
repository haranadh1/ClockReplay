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

import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.GLOBAL_ENUMS.THREAD_TYPE;
import com.crp.protocol.ProtocolHandlerManager;
import com.crp.protocol.http.CRPHttpRequest;
import com.crp.protocol.http.HttpProtocolHandler;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A service class to replay the requests obtained from Ether. As of now this
 * service supports fast session replay model. In this model, all the requests in a 
 * user session will be replayed by any one worker thread at any given time.
 *
 * @author subbu
 * @version $Id$
 */
public class ReplayService extends CRPService
{
    /**
     * HTTP Protocol handler to read the caplets and make CRPHttpRequest objects
     * out of them. 
     */
    private HttpProtocolHandler httpHandler = 
        ProtocolHandlerManager.getHttpProtocolHandler();
    
    /**
     * List of request objects.
     */
    private List<CRPHttpRequest> httpRequests = null;
    
    /**
     * Maximum number of parallel sessions the service can emulate at any given
     * time. This should be read from a property later.
     */
    private final int NUM_PARALELL_SESSIONS = 10;
    
    /**
     * A list of all the replay threads created.
     */
    private List<CRPThread> replayThreads;

    /**
     * A map of session key (host + client port) to Thread that replays the 
     * requests arising out of the session used by the replay service to replay events.
     */
    private Map<String, CRPThread> sessionToThreadMap = 
        new HashMap<String, CRPThread>();
    
    /** 
     * Constructor.
     * 
     * @param crps Info object of this replay service. 
     */
    public ReplayService(CRPServiceInfo crps)
    {
        super(crps);
    }

    /**
     * {@inheritDoc}
     */
    /*
    @Override
    public void run()
    {
        try {
            for (short i = 0; i < NUM_PARALELL_SESSIONS; i++) {
                replayThreads.add(CRPThreadPoolManager.createNewThread(
                    "ReplayService",
                    new ReplayWorker(),
                    THREAD_TYPE.WORKER_THREAD,
                    ((CRPThread) Thread.currentThread()).getThreadID()));
            }
        }
        catch (CRPThreadException e) { 
            // handle exception here.
        }
        
        while (true) {
            
            // Read the set of requests to be replayed
            if (httpRequests == null || httpRequests.isEmpty()) {
                httpRequests = httpHandler.parseAndCreateRequests();
                System.out.println("Request list size " + httpRequests.size());
            }

            for (CRPThread thread : replayThreads) {
                if (((ReplayWorker) thread.getWorker()).getTaskList().isEmpty()) {
                    System.out.printf("Setting tasks to %s\n", thread.getName());
                    if (httpRequests.size() == 0) {
                        // Yield the current thread which is the CRPThread so
                        // that other threads can execute.
                        Thread.yield();
                        break;
                    }

                    setThreadTasks(thread);

                    if (thread.isAlive()) {
                        // Wake the thread so it can start playing the tasks
                        // again
                        thread.resumeMe();
                    }
                    else {
                        // First time thread start
                        System.out.println("Starting thread "
                            + thread.getName());
                        thread.start();
                    }
                }
            }
            
            // Yield the current thread which is the CRPThread so
            // that other threads can execute.
            Thread.yield();
        }
    }
    */
    /**
     * Sets the tasks for the given thread.
     * 
     * @param thread to which the tasks are set.
     */
    private void setThreadTasks(CRPThread thread)
    {
        ReplayWorker w = (ReplayWorker) thread.getWorker();
        List<CRPHttpRequest> tasks = w.getTaskList();
        for (Iterator<CRPHttpRequest> itr = httpRequests.iterator(); itr
            .hasNext();)
        {
            CRPHttpRequest req = itr.next();
            String sesKey = req.getClientHost() + req.getClientPort();
            
            CRPThread t = sessionToThreadMap.get(sesKey);
            if (t == null) {
                tasks.add(req);
                itr.remove();
            }
        }
    }
}
