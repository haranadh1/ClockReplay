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

import java.util.BitSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;

import com.crp.common.CRPContextGroup;
import com.crp.common.CRPException;
import com.crp.common.CRPService;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.JSON_KEYS;
import com.crp.common.LockFreeStack;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.MessageCollector;
import com.crp.ether.MessageGroup;
import com.crp.ether.MessageWorld;
import com.crp.ether.Photon;
import com.crp.ether.Message.MessageStatus;
import com.crp.memmgr.MemoryBlock;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.process.PROCESS_GLOBALS;

/**
 * Generic implementation of crp thread.
 * @author hpoduri
 * @version $Id$
 */
public class CRPThread extends Thread
{
    /**
     * our own thread id.
     * this would ensure that a thread id is never reused.
     */
    private short mytid;
    /**
     * owner thread id. the thread that creates this thread.
     */
    private short ownertid;
    
    /**
     * A worker task given to this thread.
     */
    private Runnable worker;
    
    /**
     * message world, all the messages this thread owns live here.
     */
    private MessageWorld mw;
    
    /**
     * photon associated with this thread.
     */
    private Photon ph;
    
    /**
     * shutdown flag.
     */
    private boolean startShutdown;
    
    /**
     * crp context group.
     * useful to store context in between streaming messages.
     */
    private CRPContextGroup cg;
    
    /**
     * thread status, possible states.
     */
    public enum CRPThreadStatus 
    {
        UNINIT,
        INIT,
        IDLE,
        KILLED,
        SUSPENDED,
        BUSY,
        FINISHED,
    }
    /**
     * thread request processing status.
     * should not be confused with the thread status.
     * ex: a thread can be idle while processing a request.
     * a request can be a capture streaming, only be finished
     * when the full capture is finished. in the mean time it 
     * can be waiting/suspended for the incoming messages.
     */
    private enum ThreadReqProcessStatus
    {
        READY_TO_PROCESS_REQUEST,
        PROCESSING_REQUEST,
        
    }
    
    /**
     * request processing status.
     */
    private volatile ThreadReqProcessStatus trps;
    /**
     * lock free control message stack for each thread.
     * this is used to control the thread while running/idle.
     * this is a temporary implementation, eventually 
     * replaced by vivek's local message queues.
     */
    private LockFreeStack<Message> ctrlMsgs;
    
    /**
     * status of the thread.
     */
    private volatile CRPThreadStatus tStatus;
    /**
     * service name key.
     * represents the service that is creating this thread.
     */
    private GLOBAL_ENUMS.SERVICE_CATALOG sName;
    
    /**
     * message collector for this thread.
     */
    private MessageCollector mc;
    
    /**
     * flag to indicate, if this thread is available or not.
     */
    private AtomicBoolean availabilityFlag;
    
    /**
     * message code, recently processed by this thread.
     */
    private short lastProceseedMessageCode;
    
    /**
     * constructor for crp thread.
     * @param crpTask runnable task.
     * @param inpSName service creating this thread.
     * @param inpOwner owner thread creating this thread.
     * @throws CRPThreadException on error.
     */
    public CRPThread (final Runnable crpTask,
        final GLOBAL_ENUMS.SERVICE_CATALOG inpSName,
        final int inpOwner) throws CRPThreadException
    {
        super(crpTask);
        this.worker = crpTask;
        this.mytid = 0;
        this.ownertid = 0;
        startShutdown = false;
        setLastProceseedMessageCode(MESSAGE_CODE.INVALID_MESSAGE);
        
        cg = new CRPContextGroup();
        try
        {
            mc = new MessageCollector();
        }
        catch (EtherException e)
        {
            CRPThreadPoolManager.THRD_POOL_LOG.error(e.getMessage());
            throw new CRPThreadException("CRP_THREAD_ERROR_001", null);
        }
        /**
         * set the status to uninitialized, force the developer
         * to do the init from the thread pool manager.
         */
        tStatus = CRPThreadStatus.UNINIT;
        sName = inpSName;
        
        //differentiate between the process main thread vs crp thread.
        if(inpOwner == GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID)
        {
            ownertid = -1;
        }
        else
        {
            ownertid = ((CRPThread) (Thread.currentThread())).getCRPThreadID();
        }
        // for now keep 50.
        ctrlMsgs = new LockFreeStack<Message>((short)50); 
        trps = ThreadReqProcessStatus.READY_TO_PROCESS_REQUEST;
        availabilityFlag = new AtomicBoolean(false);
    }
    /**
     * initialize message pools for this thread.
     * @throws CRPThreadException on error.
     */
    public final void initMsgPools() throws CRPThreadException
    {
        
        mw = new MessageWorld();
        try
        {
            mw.createCommonMsgGroupPools();
        }
        catch(EtherException e)
        {
            CRPThreadPoolManager.THRD_POOL_LOG.error(e.getMessage());
            throw new CRPThreadException("CRP_THREAD_ERROR_002", null);
        }
    }
    /**
     * returns current CRP thread ID.
     * @return tid.
     */
    public final short getCRPThreadID()
    {
        return mytid;
    }
    
    /**
     * @return The thread's worker.
     */
    public final Runnable getWorker()
    {
        return worker;
    }
    
    /**
     * our implementation of resume. 
     * you only should resume the thread, if you find the
     * thread in suspended state, or you would risk interrupting
     * the ongoing i/o or network blocking call.
     * java is not supporting suspend and resume.
     */
    public final void resumeMe()
    {
        assert(mc != null);
        if(mc != null)
        {
            wakeupFromBlocking();
            return;
        }
    }
    
    /**
     * our own implementation of thread suspend.
     * only to be used, when the thread has no work to do.
     * @throws CRPThreadException on error.
     */
    public final void suspendMe() throws CRPThreadException
    {
        if(tStatus  == CRPThreadStatus.SUSPENDED)
        {
            return;
        }
        try
        {
            tStatus = CRPThreadStatus.SUSPENDED;
            mc.checkForNewMessages();
        }
        catch(EtherException e)
        {
            tStatus = CRPThreadStatus.BUSY;
            CRPThreadPoolManager.THRD_POOL_LOG.error(e.getMessage());
            throw new CRPThreadException("CRP_THREAD_ERROR_004",
                new String [] {this.getName()});
        }
        tStatus = CRPThreadStatus.BUSY;
    }
    /**
     * string representation of crp thread.
     * @return returns string.
     */
    public final String zipString()
    {
        StringBuilder sb = new StringBuilder(
            GLOBAL_CONSTANTS.MEDIUM_STRING_SIZE);
        sb.append("<br> -----------------------------<br>");
        sb.append(System.getProperty("line.separator"));
        sb.append(" thread name: "); sb.append(this.getName());
        sb.append(" thread status: "); sb.append(this.tStatus);
        sb.append(" thread creator : ");
        sb.append(String.valueOf(this.ownertid));
        sb.append(" thread index : "); sb.append(String.valueOf(this.mytid));
        sb.append(System.getProperty("line.separator"));
        sb.append("<br> -----------------------------<br>");

        return sb.toString();
    }
    /**
     * returns service index.
     * @return sIndex 
     */
    public final GLOBAL_ENUMS.SERVICE_CATALOG getServiceName()
    {
        return sName;
    }
    /**
     * push a control message on this thread control stack.
     * @param m Message to be pushed.
     * @throws CRPException 
     */
    public final void pushCtrlMessage(final Message m) throws CRPException
    {
        assert(ctrlMsgs != null);
        ctrlMsgs.push(m);
        resumeMe();
    }
    /**
     * receive/pop a ctrl message from the stack.
     * @return Message popped or null.
     */
    public final Message popCtrlMessage() 
    {
        Message m = null;
        try 
        {
            m = ctrlMsgs.pop();
        }
        catch (CRPException e)
        {
            return null;
        }
        return m;
    }
    /**
     * get current crp thread id.
     * NOTE: THIS IS A VERY EXPENSIVE METHOD.
     * JAVA DO NOT HAVE EFFICIENT THREAD.CURRENTTHREAD IMPLEMENTATION.
     * @return crp thread id. 
     */
    public static short getCurrentThreadID()
    {
        CRPThread crpt = null;
        try
        {
            crpt = (CRPThread)Thread.currentThread();
        }
        catch(ClassCastException e)
        {
            // not a crp thread.
            if(CRPThreadLocalStore.getCRPThreadContext(
                GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID) == null)
            {
                return GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID;
            }
            else
            {
                return CRPThreadLocalStore.getCRPThreadContext(
                    GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID).getCRPThreadID();
            }
        }
        return (crpt.getCRPThreadID());
    }
    /**
     * simple test.
     * @param args arguments.
     */
    public static void main( final String [] args )
    {
        //CRPThread crpt = new CRPThread();
        //crpt.start();
        
    }
    
    /**
     * returns message world object.
     * @return returns message world object for this thread.
     */
    public final MessageWorld getMyMsgWorld()
    {
        return  mw;
    }
    /**
     * start processing request.
     */
    public final void startProcessingRequest()
    {
        trps = ThreadReqProcessStatus.PROCESSING_REQUEST;
    }
    /**
     * check to see if a thread is ready to process request.
     * @return true if ready or false.
     */
    public final boolean isReadyToProcessRequest()
    {
        return (trps == ThreadReqProcessStatus.READY_TO_PROCESS_REQUEST);
    }
    /**
     * should be called after processing the request.
     */
    public final void finishProcessingRequest()
    {
        trps = ThreadReqProcessStatus.READY_TO_PROCESS_REQUEST;
        
    }
    /**
     * sets photon for this thread.
     * @param inpPH photon object.
     */
    public final void setPhoton(final Photon inpPH)
    {
        ph = inpPH;
        try
        {
            mc.registerPhoton(inpPH);
        }
        catch (EtherException e)
        {
            CRPThreadPoolManager.THRD_POOL_LOG.error(e.getMessage());
        }
    }
    /**
     * return photon. 
     * @return ph
     */
    public final Photon getPhoton()
    {
        return ph;
    }
    
    /**
     * returns the service object for this thread.
     * @return crpservice object.
     */
    public final CRPService getMyService()
    {
        return PROCESS_GLOBALS.PSERVICES.get(sName);
    }
    
    /**
     * wake up from blocking.
     */
    public final void wakeupFromBlocking()
    {
        if(mc != null)
        {
            mc.wakeup();
        }
    }
    
    /**
     * returns current crp thread object.
     * NOTE: THIS IS A VERY EXPENSIVE METHOD.
     * JAVA DO NOT HAVE EFFICIENT THREAD.CURRENTTHREAD IMPLEMENTATION.
     * @return crp thread.
     */
    public static final CRPThread getCurrentThread()
    {
        short tid = 
            CRPThread.getCurrentThreadID();
        if(tid == GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID)
        {
            return CRPThreadLocalStore.getCRPThreadContext(tid);
        }
        else
        {
            return CRPThreadPoolManager.getCRPThread(tid);
        }
    }
    
    /**
     * returns crp thread status.
     * @return ThreadStatus variable.
     */
    public final CRPThreadStatus getCRPThreadStatus()
    {
        return tStatus;
    }
    
    /**
     * crp thread id is assigned here.
     * the thread ids are created by crp thread pool manager.
     * @param tIndex thread ID for this thread.
     */
    public final void setCRPThreadID(final short tIndex)
    {
        mytid = tIndex;
        
    }
    
    /**
     * sets crp thread status.
     * @param inpTS input status variable.
     */
    public final void setCRPThreadStatus(final CRPThreadStatus inpTS)
    {
        tStatus = inpTS;
    }
    
    /**
     * returns message collector instance for this thread.
     * @return MessageCollector object of this thread.
     */
    public final MessageCollector getMessageCollector()
    {
        assert(mc != null);
        return mc;
    }
    
    /**
     * returns crp thread context group.
     * @return context group obj.
     */
    public final CRPContextGroup getThreadContextGroup()
    {
        return cg;
    }
    /**
     * starts this thread's shutdown.
     */
    public final void startShutdown()
    {
        startShutdown = true;
    }
    
    /**
     * returns true if shutdown in progress.
     * @return boolean.
     */
    public final boolean shutdownInProgress()
    {
        return startShutdown;
    }
    
    /**
     * frees up byte buffer(only for now) memory blocks, this thread owns.
     * @throws MemoryManagerException on error.
     */
    public final void freeMemoryBlocks() throws MemoryManagerException
    {
        short tid = CRPThread.getCurrentThreadID();
        
        // check if we have any pending messages; if we do, we have to wait until
        // the other party makes(who is presumably using the message) it available.
        
        Iterator it = mw.getMsgGrpMap().values().iterator();
        while(it.hasNext())
        {
            MessageGroup mg = (MessageGroup) it.next();
            
            for(int i = 0; i < mg.getMessageMBO().getNumOfObjects(); i++)
            {
                Message m = mg.getMessageMBO().getObjectAtIndex(i);
                int counter = 0;
                while(m.getStatus() == MessageStatus.CURRENTLY_IN_USE)
                {
                    // wait until the message becomes AVAILABLE.
                    Thread.yield();
                    counter++;
                    if(counter > GLOBAL_CONSTANTS.MAX_WAIT_COUNT)
                    {
                        break;
                    }
                }
                    
            }
        }
        
        BitSet mbBS = MemoryManager.getMBBitMapStatusForTheThread(tid);
        MemoryBlock mb = null;
        
        for(int i = 0; i < mbBS.length(); i++)
        {
            if(mbBS.get(i))
            {
                mb = MemoryManager.getMBAtIndex(i);
                if(mb != null)
                {
                    //if( mb instanceof MemoryBlockByteBuffer )
                    {
                        mb.freeMe();
                    }
                }
            }
        }
    }
    /**
     * sets the thread status to completed.
     */
    public final void shutdownCompleted()
    {
        // do the cleanup before setting the variable status.
        try
        {
            freeMemoryBlocks();
        }
        catch (MemoryManagerException e)
        {
            CRPThreadPoolManager.THRD_POOL_LOG.error(e.getMessage());
            
        }
        tStatus = CRPThreadStatus.FINISHED;
    }
    
    /**
     * over load the thread start method.
     */
    public final void start()
    {
        super.start();
        this.wakeupFromBlocking();
    }
    
    /**
     * mark the thread as free.
     * should be called while handling the close connection.
     */
    public final void markThreadAsFree()
    {
        assert(!availabilityFlag.getAndSet(true));
    }
    
    /**
     * mark the thread as not free.
     */
    public final void markThreadAsNotFree()
    {
        assert(availabilityFlag.getAndSet(false));
    }
    
    /**
     * returns if this thread is available.
     * @return true/false.
     */
    public final boolean isAvailable()
    {
        return(availabilityFlag.get());
    }
    
    
    /**
     * json representation of this thread.
     * @return JSONObject.
     * @throws JSONException on error.
     */
    public final JSONObject toJSON() throws JSONException
    {
        JSONObject jo = new JSONObject();
        
        jo.put(JSON_KEYS.JsonKeyStrings.JSON_THREAD_NAME, this.getName());
        jo.put(JSON_KEYS.JsonKeyStrings.JSON_THREAD_STATUS,
            this.getState().toString());
        jo.put(JSON_KEYS.JsonKeyStrings.JSON_THREAD_CREATOR, this.ownertid);
        
        return jo;
    }
    
    /**
     * @param lastProceseedMessageCode the lastProceseedMessageCode to set
     */
    public void setLastProceseedMessageCode(short lastProceseedMessageCode)
    {
        this.lastProceseedMessageCode = lastProceseedMessageCode;
    }
    /**
     * @return the lastProceseedMessageCode
     */
    public short getLastProceseedMessageCode()
    {
        return lastProceseedMessageCode;
    }
}
