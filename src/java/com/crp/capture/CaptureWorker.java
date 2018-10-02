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


package com.crp.capture;

import org.apache.log4j.Category;

import com.crp.common.CRPContext;
import com.crp.common.CRPException;
import com.crp.common.CRPLogger;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.MessageHolder;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;
import com.crp.ether.Message.MessageStatus;
import com.crp.interfaces.Caplet;
import com.crp.interfaces.TestCaplet;
import com.crp.memmgr.ObjectFactory;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;
import com.crp.thread.CRPThread.CRPThreadStatus;

/**
 * worker thread for capture service.
 * responsible for all the capture data streaming
 * into db.
 * @author hpoduri
 * @version $Id$
 */
public class CaptureWorker implements Runnable
{ 
    /**
     * holds current db message to be sent to db.
     */
    Message currentDBMessage;
    
    /**
     * caplet counter.
     */
    private int capCounter;
    
    /**
     * create a separate logger for CaptureService.
     */
    public static final Category CAP_WORKER_LOG = 
        CRPLogger.initializeLogger(
            "com.crp.ether.CaptureWorker");

    /**
     * photon object for db connection.
     */
    private Photon dbPH;
    
    /**
     * crp thread object.
     */
    private CRPThread crpt;
    
    /**
     * handle capture worker thread messages.
     * @param m message to be handled.
     * @throws EtherException on error.
     */
    public final void handleMessage(final Message m) throws EtherException
    {
        //CAP_WORKER_LOG.info("capworker: message received: " 
        //    + String.valueOf(m.getMessageHeader().messageCode()));
        try
        {
            switch (m.getMessageHeader().messageCode()) 
            {
                case MESSAGE_CODE.MSG_REGISTER_PHOTON:
                {
                    crpt.getMessageCollector().registerPhoton(m.getPhoton());
                    m.getPhoton().setDestThrdId(crpt.getCRPThreadID());

                    break;
                }
                case MESSAGE_CODE.MSG_CS_START_CAPTURE:
                {
                    handleStartCapture(m);
                    break;
                    
                }
                case MESSAGE_CODE.MSG_TEST_CAPURE_AGENT:
                {
                    assert(dbPH != null);
                    // handle the test capture object.
                    // this should only work with local connection.
                    // convert the test caplet into Caplet object and send across.
                    TestCaplet tc = (TestCaplet) m.getTestObject();
                    //CAP_WORKER_LOG.info("Received a test caplet from agent, len: " + String.valueOf(tc.payLoad.length()));
                    //CAP_WORKER_LOG.info("memory block info: " + currentDBMessage.getPayloadMBO().getMBB().toString());
                    if(currentDBMessage == null)
                    {
                        try
                        {
                            currentDBMessage = crpt.getMyMsgWorld().getFreeMsg(
                                GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY);
                            currentDBMessage.getMessageHeader().setMessageCode(
                                MESSAGE_CODE.MSG_CS_WRITE_CAPLET_STREAM_DB);
                        }
                        catch (EtherException e)
                        {
                            CAP_WORKER_LOG.error(e.getMessage());
                            return;
                        }
                    }
                    if( !currentDBMessage.anyRoomForObjOfVarLength(
                        tc.payLoad.length()))
                    {
                        // no space to fit in the next caplet in this msg.
                        
                        dbPH.sendMessageNoFail(currentDBMessage);
                        CAP_WORKER_LOG.info("Sending caplets to db : [" 
                            + String.valueOf(currentDBMessage.getPayloadMBO().getActiveObjects())+ "]");
                        
                        while ( (currentDBMessage 
                                = crpt.getMyMsgWorld().getFreeMsg(
                              GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY))
                            == null )
                        {
                            //TODO: should handle this more carefully.
                            Thread.yield();
                            dbPH.getChannel().flushChannel();
                            dbPH.getChannel().notifyReceiver();
                        }
                        
                        assert(currentDBMessage != null);
                        currentDBMessage.getMessageHeader().setMessageCode(
                            MESSAGE_CODE.MSG_CS_WRITE_CAPLET_STREAM_DB);

                    }
                    
                    {

                        Caplet c = (Caplet) currentDBMessage.getPayLoadObject();
                        c.setAttr(
                            tc.remoteHost,
                            tc.remotePort,
                            tc.remoteIP,
                            tc.ts,
                            tc.payLoad.getBytes());
                        capCounter++;
                        
                    }
                    break;
                }
                case MESSAGE_CODE.MSG_SHUTDOWN:
                {
                    CAP_WORKER_LOG.info("SHUTDOWN MESSAGE RECEIVED");
                    CRPThread.getCurrentThread().startShutdown();

                }
                case MESSAGE_CODE.MSG_CLOSE_CONNECTION:
                {
                    CAP_WORKER_LOG.info("Received close connection : " + String.valueOf(capCounter));
                    if(dbPH == null || !dbPH.isConnected())
                    {
                        break;
                    }
                    // first look for any pending caplets in the current msg.
                    if(currentDBMessage != null && currentDBMessage.getPayloadMBO().getActiveObjects() > 0)
                    {
                        CAP_WORKER_LOG.info("sending pending caplets to db: " + String.valueOf(currentDBMessage.getPayloadMBO().getActiveObjects()));
                        dbPH.sendMessageNoFail(currentDBMessage);
                    }
                    //close db connection.
                    
                    if(dbPH != null)
                    {
                        PhotonPool.closePhoton(dbPH, false);
                    }
                    
                    // mark this thread as free.
                    if(!crpt.isAvailable())
                    {
                        crpt.markThreadAsFree();
                    }
                }
            }
        }
        catch(CRPException e)
        {
            CAP_WORKER_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_017",
                new String[]{
                    String.valueOf(m.getMessageHeader().messageCode())});
        }
    }
    @Override
    public void run()
    {
        capCounter = 0;
        try
        {
            dbPH = null;
            // now that we have a db photon, we can proceed to streaming.
            crpt = CRPThreadPoolManager.getCRPThread(CRPThread
                .getCurrentThreadID());
            crpt.setCRPThreadStatus(CRPThreadStatus.INIT);
            try
            {
                crpt.initMsgPools();
            }
            catch (CRPThreadException e)
            {
                CAP_WORKER_LOG.fatal("Capture Worker Initialization failed ");
                return;
            }
            // create a message pool for caplet streaming from capture service
            // to db service.
            ObjectFactory ofcap = new Caplet(null);
            try
            {
                crpt.getMyMsgWorld().createMsgGroupPool(
                    ofcap,
                    GLOBAL_CONSTANTS.KB,
                    GLOBAL_CONSTANTS.CAPLET_MEMORY_BLOCK_SIZE);
            }
            catch (EtherException e)
            {
                CAP_WORKER_LOG.error(e.getMessage());
                return;
            }
            currentDBMessage = null;
           

            // connect to database before anything else.
            CRPServiceInfo crps = new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE,
                "localhost",
                "dummy handler service",
                9010,
                0);
            try
            {
                dbPH = PhotonPool.createPhoton(crps, false);
                dbPH.setSenderSpinning(false);
                dbPH.setReceiverSpinning(false);
                CAP_WORKER_LOG.info("Capture Worker Thread: DB Photon Created");
            }
            catch (EtherException e)
            {
                CAP_WORKER_LOG.error(e.getMessage());
                return;
            }


            Message m = null;

            CAP_WORKER_LOG.info("Capture Worker Thread Initialized");
            // main loop. wait for messages, and do the needful.
            while (true)
            {
                if (crpt.shutdownInProgress())
                {
                    CAP_WORKER_LOG.info("Thread ID : " 
                        + String.valueOf(CRPThread.getCurrentThreadID()
                            + " finished for service: " 
                            + crpt.getMyService().getServiceInfo().getName().toString()));
                    crpt.shutdownCompleted();
                    break;
                }
                try
                {
                    MessageHolder mh = null;
                    mh = crpt.getMessageCollector().checkForNewMessages();
                    int curNumOfMsgs = mh.size();
                    for (int i = 0; i < curNumOfMsgs; i++)
                    {
                        m = mh.getNextMessage(i);
                        if(m != null)
                        {
                            handleMessage(m);
                            m.updateStatus(MessageStatus.AVAILABLE);
                        }
                    }
                    //mh.reset();
                }
                catch (EtherException e)
                {
                    CAP_WORKER_LOG.error(e.getMessage());
                    crpt.finishProcessingRequest();
                    // send  a error message back.
                    Message errMsg = crpt.getMyMsgWorld().getFreeMsg(
                        GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
                    errMsg.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_FAIL);
                    if( m != null )
                    {
                        m.getPhoton().bsendMessage(errMsg);
                    }
                }
                if(m != null)
                {
                    m.updateStatus(MessageStatus.AVAILABLE);
                }
            }
        }
        catch(Exception e)
        {
            crpt.setCRPThreadStatus(CRPThreadStatus.KILLED);
            CommonLogger.CMN_LOG.error(e.toString());
            e.printStackTrace();
        }
    }
    
    /**
     * handles start capture.
     * @param m start cap message.
     * @throws CRPException on error.
     */
    private final void handleStartCapture(final Message m) throws CRPException
    {
        // check if we have any existing capture context
        // before creating a new one.
        
        // create a context for this capture.
        if(!crpt.getThreadContextGroup().isEmpty())
        {
            // throw error.; we only support for one context
            // per capture thread.
            throw new CRPException("CRP_COMMON_ERROR_009",
                new String[] {crpt.getServiceName().toString()});
        }
        CRPContext ctx = crpt.getThreadContextGroup(
            ).createContext("CAPTURE ENGINE");
        
        CaptureContextObject cco = new CaptureContextObject();
        
        ctx.addObject(cco, "capture context");
        cco.crpt = crpt;
        cco.capLogger = CAP_WORKER_LOG;
        cco.ce.setupCapture("\\Device\\NPF_{1C839DC8-574F-4BF3-A4AD-81053B282E68}");
        cco.dbPhoton = dbPH;
        cco.ce.startCapture(cco);
    }
    
    /**
     * returns the current thread, crp thread object.
     * @return CRPThread.
     */
    public final CRPThread getThreadObject()
    {
        
        return crpt;
    }
    
    /**
     * handle stop capture message.
     * @param m stop capture message
     * @throws EtherException on error.
     */
    public final void handleStopCapture(
        final Message m) throws EtherException
    {
        getThreadObject().getThreadContextGroup().deleteContext(0);
        getThreadObject().markThreadAsFree();
    }
}
