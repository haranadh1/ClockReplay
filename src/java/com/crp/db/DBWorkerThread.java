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

package com.crp.db;

import org.apache.log4j.Category;
import org.json.JSONObject;

import com.crp.common.CRPException;
import com.crp.common.CRPLogger;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.common.GLOBAL_ENUMS.DBAccessMode;
import com.crp.common.GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE;
import com.crp.db.DBPageManager;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.MessageHandlerCallBack;
import com.crp.ether.MessageHolder;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;
import com.crp.ether.Channel.CommunicatorType;
import com.crp.ether.Message.MessageStatus;
import com.crp.interfaces.CRPString;
import com.crp.interfaces.Caplet;
import com.crp.memmgr.ObjectFactory;
import com.crp.pkunpk.UnPacker;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;
import com.crp.thread.CRPThread.CRPThreadStatus;

/**
 * db worker thread.
 * thread that processes all the requests.
 * @author hpoduri
 * @version $Id$
 */
public class DBWorkerThread implements Runnable, MessageHandlerCallBack
{
    /**
     * create a logger for db worker thread.
     */
    /**
     * create a separate logger for ConnectionService.
     */
    public static final Category DB_WORKER_LOG = 
        CRPLogger.initializeLogger(
            "com.crp.db.DBWorkerThread");
    
    /**
     * used to save photon object between req/response pairs.
     */
    private Photon phWaitingForResponse;
    
    /**
     * page handler object.
     */
    private DBPageManager dbpgm;
    
    /**
     * photon to connect to IO Thread.
     */
    
    private Photon phToIO;
    
    @Override
    public void run()
    {
        phWaitingForResponse = null;
        phToIO = null;
        CRPThread crpt = CRPThread.getCurrentThread();
        assert(crpt != null);
        try
        {
            crpt.initMsgPools();
        }
        catch (CRPThreadException e)
        {
            DB_WORKER_LOG.error(e.getMessage());
            crpt.setCRPThreadStatus(CRPThreadStatus.KILLED);
            return;
        }
        crpt.setCRPThreadStatus(CRPThreadStatus.INIT);

        //first create a IO thread.
        //create a new thread and start it.
        try
        {
            IOWorkerThread iot = new IOWorkerThread();
            CRPThread t = CRPThreadPoolManager
                .createNewThread(
                    GLOBAL_ENUMS.SERVICE_CATALOG.IO_SERVICE,
                    iot,
                    GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                    CRPThread.getCurrentThreadID());
            
            DBService dbs = (DBService)crpt.getMyService();
            dbs.getDBThreadGroup().addThread(t.getCRPThreadID());
            t.initMsgPools();
            CRPServiceInfo ios = new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.IO_SERVICE,
                "localhost", "localhost", 9200,1);
            try
            {
                
                phToIO = PhotonPool.createPhotonHelper(
                    ETHER_COMMUNICATION_TYPE.LOCAL);
                
                phToIO.setSenderSpinning(false);
                phToIO.setReceiverSpinning(false);
            }
            catch (EtherException e)
            {
                DBService.DB_SERVICE_LOG.error(e.getMessage());
                return;
            }
            t.setPhoton(phToIO);
            
            t.startProcessingRequest();
            DBService.DB_SERVICE_LOG.info("IO Worker Thread Started");
            t.start();
            
            // first connect to io thread.
            try
            {
                phToIO.connect(ios, CommunicatorType.SOURCE);
            }
            catch (EtherException e)
            {
                DB_WORKER_LOG.error(e.getMessage());
                return;
            }
            // call wake up once, to make sure that
            // the io thread calls a recv on the photon.
            // basically the first receive registers it as consumer.
            t.wakeupFromBlocking();
            try
            {
                crpt.getMessageCollector().registerPhoton(phToIO);
            }
            catch (EtherException e)
            {
                DB_WORKER_LOG.error(e.getMessage());
                return;
            }
        }
        catch (CRPThreadException e)
        {
            CRPThreadPoolManager.THRD_POOL_LOG.error(
                e.getMessage());
            return;
        }
        dbpgm = new DBPageManager();
        
        if (crpt == null)
        {
            CommonLogger.CMN_LOG.fatal(
                "thread object null after casting to crp thread");
            return;
        }
        else
        {
            DBService.DB_SERVICE_LOG.info("DB Worker Thread id : "
                + String.valueOf(crpt.getCRPThreadID() + " started"));
            
        }
        // now lets connect to the io thread.
        // phToIO.connect(crps); for now we can only connect to service.
        while (true)
        {
            //check control messages.
            
            //check non-control messages.
            
            // --- process non-control messages if any.
            
            // if no messages
            // yield for some iterations...
            // after some iterations do sleep for ever.
            // wake up thru interrupt by the process main thread.
            
            // testing....
            Message m = null;
            
            try 
            {
                if (crpt.shutdownInProgress())
                {
                    DB_WORKER_LOG.info("Thread ID : "
                        + String.valueOf(CRPThread.getCurrentThreadID()
                            + " finished for service: "
                            + crpt
                                .getMyService()
                                .getServiceInfo()
                                .getName()
                                .toString()));
                    crpt.shutdownCompleted();
                    break;
                }
                MessageHolder mh = null;
                
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
                    //mh.reset();
                }
                catch (Exception e)
                {
                    if(m != null)
                    {
                        m.updateStatus(MessageStatus.AVAILABLE);
                    }
                    DBService.DB_SERVICE_LOG.error(e.toString());
                    //DBService.DB_SERVICE_LOG.error(e.getMessage());
                    DBService.DB_SERVICE_LOG.error(CRPException.getPrintStack(e));
                    crpt.finishProcessingRequest();
                    Message errMsg = crpt.getMyMsgWorld().getFreeMsg(
                        GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
                    errMsg.getMessageHeader().setMessageCode(
                        MESSAGE_CODE.MSG_FAIL);
                    
                    assert(errMsg != null);
                    // send  a error message back.
                    if(phWaitingForResponse != null)
                    {
                        phWaitingForResponse.sendMessageNoFail(errMsg);
                    }
                    else
                    {
                        if( m != null )
                        {
                            m.getPhoton().sendMessageNoFail(errMsg);
                        }
                    }
                    
                }
            }
            catch (Exception e)
            {
                DB_WORKER_LOG.error(e.toString());
                DB_WORKER_LOG.error(GLOBAL_UTILS.getStackTraceAsString(e));
                CRPThread.getCurrentThread().setCRPThreadStatus(CRPThreadStatus.KILLED);
                break;
            }
        }
    }
    @Override
    public Message handleMessage(final Message m) throws EtherException
    {
        //DB_WORKER_LOG.info(" message code [" + String.valueOf(m.getMessageHeader().messageCode()) + "]");
        CRPThread crpt = CRPThread.getCurrentThread();
        assert(crpt != null);
        
        switch(m.getMessageHeader().messageCode())
        {
            case MESSAGE_CODE.MSG_REGISTER_PHOTON:
            {
                crpt.getMessageCollector().registerPhoton(m.getPhoton());
                m.getPhoton().setDestThrdId(crpt.getCRPThreadID());

                break;
            }
            case MESSAGE_CODE.MSG_CS_WRITE_CAPLET_STREAM_DB:
            {
                handleWriteCapletStreamToDB(m);
                break;
            }
            case MESSAGE_CODE.MSG_REQ_CLIENT_CAPLET_STREAM_DB:
            {
                handleDBSeqReadRequestFromClient(m);
                
                break;
            }
            case MESSAGE_CODE.MSG_RES_IO_PAGE_SEQ_READ_TO_DB:
            {
                handleDBSeqResponseFromIO(m);
                break;
            }
            case MESSAGE_CODE.MSG_END_OF_DATA:
            {
                handleEOD(m);
                break;
            }
            case MESSAGE_CODE.MSG_FAIL:
            {
                handleIncomingError(m);
                break;
            }
            case MESSAGE_CODE.MSG_REQ_CRP_DB_METADATA:
            {
                handleReqDBMetadata(m);
                break;
            }
            case MESSAGE_CODE.MSG_SHUTDOWN:
            {
                DB_WORKER_LOG.info("SHUT DOWN MESSAGE RECEIVED");
                if(phToIO.isConnected())
                {
                    //if it is still connected; close it first.
                    
                    // add the message again to read it next time.
                    // this makes this message status not to be updated as AVAILABLE.
                    m.dontConsiderForProcessing();
                    crpt.getMessageCollector().getMessageHolder().addMessage(m);
                    break;
                }
                else
                {
                    // enable the processed flag, just in case if this is a repeated message.
                    m.considerForProcessing();
                    CRPThread.getCurrentThread().startShutdown();
                }

                //note that there is no break here...fall thru.
            }
            case MESSAGE_CODE.MSG_CLOSE_CONNECTION:
            {
                DB_WORKER_LOG.info("Received Close Connection Message");
                Message im = dbpgm.getCurrentMsg();
                if(im != null)
                {
                    try
                    {
                        if(dbpgm.getDBM() == DBAccessMode.SEQUENTIAL_WRITE 
                        		&& im.getPackerForObjs() != null)
                        {
                            DB_WORKER_LOG.info("Sending Message to IOWorker Thread: " + String.valueOf(im.getMessageHeader().messageCode()));
                            try
                            {
                                dbpgm.finishWritingCaplets(im.getPackerForObjs());
                            }
                            catch (DBException e)
                            {
                                DB_WORKER_LOG.error(e.getMessage());
                                throw new EtherException("CRP_EHTERM_ERROR_017",
                                    new String[] {
                                        String.valueOf(
                                            m.getMessageHeader().messageCode())
                                    });
                            }
                            phToIO.sendMessageNoFail(im);
                        }
                    }
                    catch(EtherException e)
                    {
                        DB_WORKER_LOG.error(e.getMessage());
                    }
                    dbpgm.reset();
                }
                if(phToIO.isConnected())
                {
                    PhotonPool.closePhoton(phToIO, false);
                }
                
                // now handle the case if the photon associated with the message is remote.
                // if it is remote, we create the photon object local to this process.
                // which needs to be cleaned up by adding it back to pool.
                // here we should not call PhotonPool.closePhoton, that tries to send
                // a close message back to the remote source.
                // we should just update the photon pool in this case.
                if((m.getPhoton() != null ) && !m.getPhoton().isLocal())
                {
                    // do it only for the MSG_CLOSE_CONNECTION, not fall thru for shutdown.
                    // unregister the photon from this thread.
                    if(m.getMessageHeader().messageCode() == MESSAGE_CODE.MSG_CLOSE_CONNECTION)
                    {
                        crpt.getMessageCollector().unregisterPhoton(m.getPhoton());
                        PhotonPool.returnPhotonToPool(m.getPhoton());
                    }
                }
                // mark this thread as free.
                if(!crpt.isAvailable())
                {
                    crpt.markThreadAsFree();
                }
                break;
            }
        }
        m.updateStatus(MessageStatus.AVAILABLE);
        return null;
    }
    
    /**
     * handle db request for meta data.
     * @param m input message.
     * @throws EtherException on error.
     */
    private void handleReqDBMetadata(final Message m) throws EtherException
    {
        Message dbMetadataMsg = CRPThread
            .getCurrentThread()
            .getMyMsgWorld()
            .getFreeMsg(GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CRP_STRING_KEY);
        dbMetadataMsg.getMessageHeader().setMessageCode(
            MESSAGE_CODE.MSG_RES_CRP_DB_METADATA_CLIENT);
        try
        {
            JSONObject jo = DBUtils.listFilesInDir(
                GLOBAL_UTILS.getDBLocation());
            CRPString crps = (CRPString) dbMetadataMsg.getPayloadMBO().getObject();
            crps.setContent(jo.toString());
        }
        catch (DBException e)
        {
            DB_WORKER_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_017",
                new String[] {
                    String.valueOf(m.getMessageHeader().messageCode())
                });
        }
        m.getPhoton().sendMessageNoFail(dbMetadataMsg);
        
    }
    /**
     * pass the eod message to the client.
     * @param m eod message.
     * @throws EtherException on error.
     */
    private void handleEOD(final Message m) throws EtherException
    {
        DB_WORKER_LOG.info(" received end of data io msg from io service:");

        DB_WORKER_LOG.info(" total number of caplets read : " 
            + dbpgm.getTotalNumberOfCaplets());
        Message eodMsg = CRPThread
            .getCurrentThread()
            .getMyMsgWorld()
            .getFreeMsg(GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
        eodMsg.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_END_OF_DATA);
    
        if (phWaitingForResponse != null)
        {
            // send the error back to the client.
            
            phWaitingForResponse.sendMessageNoFail(eodMsg);
            
        }
    }
    /**
     * handles all error messages.
     * @param m error message.
     * @throws EtherException on error.
     */
    private void handleIncomingError(final Message m) throws EtherException
    {
        DB_WORKER_LOG.info(" received error io msg from io service:");

        Message errMsg = CRPThread
            .getCurrentThread()
            .getMyMsgWorld()
            .getFreeMsg(GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
        errMsg.getMessageHeader().setMessageCode(
            MESSAGE_CODE.MSG_FAIL);
        
        if (phWaitingForResponse != null)
        {
            // send the error back to the client.
            
            phWaitingForResponse.sendMessageNoFail(errMsg);
            
        }
    }
    /**
     * handles response from io, send it over to client.
     * @param m msg object.
     * @throws EtherException on error.
     */
    private void handleDBSeqResponseFromIO(
        final Message m) throws EtherException
    {
        // first get a message with caplets to fill in.
        DB_WORKER_LOG.info(" received io msg from io service:");
        CRPThread crpt = CRPThread.getCurrentThread();
        
        Message capMsg = null;
        UnPacker unp = new UnPacker(m.getPackerForObjs().getMBB());
        unp.reset();
        
        try
        {
            capMsg = dbpgm.readNextCapletsIntoMsg(unp);
        }
        catch (DBException e)
        {
            DB_WORKER_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_017",
                new String[] {
                    String.valueOf(m.getMessageHeader().messageCode())
                });
        }
        if(capMsg != null)
        {
            capMsg.getMessageHeader().setMessageCode(
                MESSAGE_CODE.MSG_RES_DB_CAPLET_STREAM_CLIENT);
            phWaitingForResponse.sendMessageNoFail(capMsg);
            
        }
        else
        {
            //TODO : handle this case.
        }
    }
    /**
     * handles db seq read request from client.
     * @param m Message.
     * @throws EtherException on error.
     */
    private void handleDBSeqReadRequestFromClient(
        final Message m) throws EtherException
    {

        try
        {
            assert(dbpgm != null);
            if(!dbpgm.isInitialized())
            {
                dbpgm.init(GLOBAL_CONSTANTS.MAX_DB_PAGES_PER_IO,
                    DBAccessMode.SEQUENETAL_READ);
            }
        }
        catch(DBException e)
        {
            DB_WORKER_LOG.info(" inside error: " + e.getMessage());
            DB_WORKER_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_017",
                new String[] {
                    String.valueOf(m.getMessageHeader().messageCode())
                });
        }

        CRPThread crpt = CRPThread.getCurrentThread();
        Message ioMsg = crpt.getMyMsgWorld().getFreeMsg(
            GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);

        ioMsg.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_REQ_DB_SEQ_READ_FROM_IO);
        phWaitingForResponse = m.getPhoton();
        
        phToIO.sendMessageNoFail(ioMsg);
        
    }
    
    /**
     * handles caplet stream to db.
     * @param m incoming message
     * @throws EtherException on error.
     */
    private final void handleWriteCapletStreamToDB(
        final Message m) throws EtherException
    {
        if(!phToIO.isConnected())
        {
            // cant do anything.
            DB_WORKER_LOG.error(
                "IO Photon is not connected, cannot write caplets");
            return;
        }
        try
        {
            if(!dbpgm.isInitialized())
            {
                dbpgm.init(GLOBAL_CONSTANTS.MAX_DB_PAGES_PER_IO,
                    DBAccessMode.SEQUENTIAL_WRITE);
            }
        }
        catch(DBException e)
        {
            DBService.DB_SERVICE_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_017",
                new String[] {
                    String.valueOf(m.getMessageHeader().messageCode())
                });
        }
        //DB_WORKER_LOG.info("Received Msg: " + String.valueOf(m.getMessageHeader().messageCode()));
        Message ioMsg = null;
        DB_WORKER_LOG.info(" Msg: objs: " + String.valueOf(m.getPayloadMBO().getActiveObjects()));

        // this means, we got caplets in message.
        for(int i=0; i < m.getPayloadMBO().getActiveObjects(); i++)
        {
            assert(m.getStatus() == Message.MessageStatus.CURRENTLY_IN_USE);
            Caplet c = (Caplet)m.getPayloadMBO().getObjectAtIndex(i);
            try
            {
                ioMsg = dbpgm.addCaplet(c, phToIO);
            }
            catch(DBException e)
            {
                DBService.DB_SERVICE_LOG.error(e.getMessage());
                break;
            }
            if(ioMsg != null)
            {
                DBService.DB_SERVICE_LOG.info(" DB : sending msg to IO : ");
                //send it to io thread.
                phToIO.sendMessageNoFail(ioMsg);
               
            }
        }

        DB_WORKER_LOG.info(" caplets in current page: " 
            + String.valueOf(
                dbpgm.getCurrentPage().getPageHeader().numOfCaplets));
    }
}