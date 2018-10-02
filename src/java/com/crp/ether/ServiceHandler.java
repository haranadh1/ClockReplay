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

package com.crp.ether;

import org.apache.log4j.Category;

import com.crp.capture.CaptureService;
import com.crp.common.CRPException;
import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CRPLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.common.GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE;
import com.crp.common.GLOBAL_ENUMS.ETHER_ELEMENT_TYPE;
import com.crp.db.DBService;
import com.crp.ether.Message.MessageStatus;
import com.crp.interfaces.CRPString;
import com.crp.interfaces.ConnectionInterface;
import com.crp.memmgr.MemoryManager;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;
import com.crp.thread.CRPThread.CRPThreadStatus;

/**
 * service handler class.
 * handles all the services hosted by a crp process.
 * this acts like a inetd daemon in unix, this handles all the
 * events that go to different services(including the connections).
 * with new implementation each service do not act as a thread.
 * instead, each service has its own context, but all the tasks
 * by the service are executed in the service handler thread.
 * However, all the data communication happens between multiple
 * threads.
 * @author hpoduri
 * @version $Id$
 */
public class ServiceHandler extends CRPService implements Runnable
{
    
    /**
     * private listening photon.
     */
    private Photon listenerPh;
    
    /**
     * constructor.
     * @param inpSi service info object.
     */
    public ServiceHandler(final CRPServiceInfo inpSi)
    {
        super(inpSi);
    }

    /**
     * create a separate logger for service handler.
     */
    public static final Category SERVICE_LOG = 
        CRPLogger.initializeLogger(
            "com.crp.ether.ServiceHandler");
    
    /**
     * creates main thread instance for this service.
     * @return crp thread instance object.
     * @throws CRPThreadException on error.
     */
    public final CRPThread createMainThread() throws CRPThreadException
    {
        CRPThread crpt = CRPThreadPoolManager.createNewThread(
            GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,
            this,
            GLOBAL_ENUMS.THREAD_TYPE.SERVICE_MAIN_THREAD,
            GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID);
        PROCESS_GLOBALS.SERVICE_HANDLER_THREAD_ID = crpt.getCRPThreadID();
        return crpt;
    }

    @Override
    public void run()
    {
        CRPThread crpt = (CRPThread) Thread.currentThread();

        try 
        {
            crpt.initMsgPools();
        }
        catch(CRPThreadException e)
        {
            SERVICE_LOG.error(e.getMessage());
        }
        SERVICE_LOG.info(" Message pools for the Service Thread are initialized");
        if (crpt == null)
        {
            SERVICE_LOG.fatal("thread object null after casting to crp thread");
        }
        try
        {
            listenerPh = new Photon(
                ETHER_ELEMENT_TYPE.LISTENER, ETHER_COMMUNICATION_TYPE.REMOTE);
            listenerPh.initializeListener(this.getServiceInfo().getPort());
            crpt.getMessageCollector().registerPhoton(listenerPh);
            
        }
        catch(EtherException e)
        {
            SERVICE_LOG.error(e.getMessage());
            return;
        }
       
      
        //first get the connection service object
        
        try
        {
            while (true)
            {
                if(CRPThread.getCurrentThread().shutdownInProgress())
                {
                    SERVICE_LOG.info("SERVICE HANDLER THREAD SHUT DOWN");
                    CRPThread.getCurrentThread().shutdownCompleted();
                    break;
                }
                //check local 
                MessageHolder mh = null;
                Message m = null;
                
                try
                {
                    
                    mh = crpt.getMessageCollector().checkForNewMessages();
                    int curNumOfMsgs = mh.size();
                    for (int i = 0; i < curNumOfMsgs; i++)
                    {
                        m = mh.getNextMessage(i);
                        if(m != null)
                        {
                            handleMessage(m);
                        }
                    }
                    mh.reset();
                }
                catch (EtherException e)
                {
                    SERVICE_LOG.error(e.getMessage());
                    crpt.finishProcessingRequest();
                    // send  a error message back.
                    Message errMsg = crpt.getMyMsgWorld().getFreeMsg(
                        GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
                    if( m != null )
                    {
                        m.getPhoton().bsendMessage(errMsg);
                    }
                }
            }
        }
        catch (Exception e)
        {
            SERVICE_LOG.error(e.getMessage());
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
    @Override
    public Message handleMessage(final Message m) throws EtherException
    {
        SERVICE_LOG.info(" message code [" + String.valueOf(m.getMessageHeader().messageCode()) + "]");

        if (m != null)
        {
            try
            {
                switch (m.getMessageHeader().messageCode()) 
                {
                    case MESSAGE_CODE.MSG_REMOTE_NEW_CONNECTION:
                    case MESSAGE_CODE.MSG_LOCAL_NEW_CONNECTION:
                    {
                        ConnectionInterface ci 
                            = (ConnectionInterface) m.getFirstObject();
                        SERVICE_LOG.info("New Connection FROM " 
                            + ci.getSrcService().toString() 
                            + " --->TO---> " 
                            + ci.getDestService().toString());
                        
                        
                        switch(ci.getDestService())
                        {
                            case DB_HANDLER_SERVICE:
                                //get the db service
                                //(should be local to this process).
                                CRPService service = null;
                                
                                service = (DBService)PROCESS_GLOBALS.PSERVICES.get(
                                    GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE);
                                
                                if(service == null)
                                {
                                    throw new CRPException(
                                        "CRP_COMMON_ERROR_003",
                                        new String[]{
                                            GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE.toString()});
                                }
                                service.handleMessage(m);
                                break;   
                            case CAPTURE_SERVICE:
                                CRPService cs = null;
                                cs = (CaptureService)PROCESS_GLOBALS.PSERVICES.get(
                                    GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE);
                                cs.handleMessage(m);
                                break;
                                
                            default:
                            {
                                assert(false);
                            }

                        }
                        break;
                    }
                    case MESSAGE_CODE.MSG_REQ_MEMMGR_INFO:
                    {
                        SERVICE_LOG.info("Memory Manager Dump Msg Received");
                        Message sendMsg = 
                            CRPThread.getCurrentThread().getMyMsgWorld()
                            .getFreeMsg(
                                GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CRP_STRING_KEY);
                        sendMsg.updateStatus(MessageStatus.CURRENTLY_IN_USE);
                        sendMsg.getMessageHeader().setMessageCode(
                            MESSAGE_CODE.MSG_RES_MEMMGR_INFO);
                        
                        CRPString crpJSON = (CRPString) sendMsg.getPayloadMBO(
                            ).getObject();
                        crpJSON.setContent(MemoryManager.toJSON(
                            ).toString().getBytes());
                        SERVICE_LOG.info("Sending Mem Mgr Info to client" );
                        // consider this as a short circuited photon, as the initial handshake
                        // of MSG_LOCAL/REMOTE_CONNECTION message is ignored.
                        m.getPhoton().setShortCircuited();
                        m.getPhoton().bsendMessage(sendMsg);
                        m.getPhoton().getChannel().flushChannel();
                        break;
                    }
                    case MESSAGE_CODE.MSG_REQ_CRP_THREADS_INFO:
                    {
                        SERVICE_LOG.info("CRP Threads Dump Msg Received");
                        Message sendMsg = CRPThread.getCurrentThread(
                            ).getMyMsgWorld().getFreeMsg(
                            GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CRP_STRING_KEY
                            );
                        sendMsg.updateStatus(MessageStatus.CURRENTLY_IN_USE);
                        sendMsg.getMessageHeader().setMessageCode(
                            MESSAGE_CODE.MSG_RES_THRDPOOL_INFO);
                        
                        // for now do it without json objects.
                        CRPString crpJSON = (CRPString) sendMsg.getPayloadMBO(
                            ).getObject();
                        crpJSON.setContent(CRPThreadPoolManager.toJSON().toString());
                        SERVICE_LOG.info("Sending CRP Threads Info to client" );
                        // consider this as a short circuited photon, as the initial handshake
                        // of MSG_LOCAL/REMOTE_CONNECTION message is ignored.
                        m.getPhoton().setShortCircuited();
                        m.getPhoton().bsendMessage(sendMsg);
                        m.getPhoton().getChannel().flushChannel();
                        break;
                    }
                    case MESSAGE_CODE.MSG_SHUTDOWN:
                    {
                        handleShutdown();
                        break;
                    }
                }
            }
            catch (CRPException e)
            {
                throw new EtherException("CRP_ETHERM_ERROR_016", null);
            }
            m.updateStatus(MessageStatus.AVAILABLE);
        }
        return null;
    }

    /**
     * method to handle shutdown request.
     * @throws EtherException on error.
     */
    private void handleShutdown() throws EtherException
    {
        SERVICE_LOG.info("SHUTDOWN MESSAGE RECEIVED");
        
        
        int threadCount = CRPThreadPoolManager.numOfCRPThreads();
        for(int i = 0; i <= threadCount; i++)
        {
            
            if(CRPThread.getCurrentThreadID() == (short)i)
            {
                //ignore the current thread.
                continue;
            }
            CRPThread crpt = CRPThreadPoolManager.getCRPThread(
                (short)i);
            
            if(crpt == null || (crpt.getCRPThreadStatus() == CRPThreadStatus.UNINIT))
            {
                continue;
            }
            if(crpt.isAlive())
            {
                try
                {
                    SERVICE_LOG.info("Sending Shutdown Msg to : " + crpt.getName());
                    // send shutdown messages to all the threads.
                    // and wait for them to shutdown.
                    Message shutMsg = CRPThread.getCurrentThread().getMyMsgWorld().getFreeMsg(
                        GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
                    shutMsg.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_SHUTDOWN);
                    
                    if(GLOBAL_UTILS.msgDebug())
                    {
                        // add some debug info to msg.
                        shutMsg.getDebugger().destService = crpt.getServiceName();
                        
                    }
                    crpt.pushCtrlMessage(shutMsg);
                }
                catch (CRPException e)
                {
                    SERVICE_LOG.error(e.getMessage());
                    throw new EtherException("CRP_ETHERM_ERROR_004", null);
                }
            }
            else
            {
                SERVICE_LOG.info("thread is dead : " + crpt.getName());
            }
        }
        for(int i = 0;
            i <= threadCount;
            i++)
        {
            if(CRPThread.getCurrentThreadID() == i)
            {
                //ignore the current thread.
                continue;
            }
            CRPThread crpt = CRPThreadPoolManager.getCRPThread(
                (short)i);
            if(crpt == null)
            {
                continue;
            }
            if(crpt == null || (crpt.getCRPThreadStatus() == CRPThreadStatus.UNINIT))
            {
                continue;
            }
            if(crpt.isAlive())
            {
                try
                {
                    crpt.join();
                }
                catch (InterruptedException e)
                {
                    SERVICE_LOG.error(e.getMessage());
                }
            }
        }
        CRPThread.getCurrentThread().startShutdown();
        listenerPh.close();
        
    }
}
