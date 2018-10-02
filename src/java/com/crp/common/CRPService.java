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

import com.crp.capture.CaptureWorker;
import com.crp.db.DBWorkerThread;
import com.crp.ether.EtherData;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.MessageHandlerCallBack;
import com.crp.interfaces.ConnectionInterface;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadGroup;
import com.crp.thread.CRPThreadPoolManager;

/**
 * this class implements a very generic crp service.
 * the class gives an idea as to how any crp service works.
 * @author hpoduri
 * @version $Id$
 */
public class CRPService implements MessageHandlerCallBack
{
    /**
     * main thread ID for the service.
     */
    private short mainThreadID;

   /**
    * service information object.
    */
    private CRPServiceInfo csi;
    
    /**
     * thread group for the service.
     * use this to create new threads for the service.
     */
    private CRPThreadGroup crptg;
    
    /**
     * constructor.
     * @param inpSi service info object.
     */
    public CRPService( final CRPServiceInfo inpSi )
    {
        csi = inpSi;
        mainThreadID = -1;
        crptg = new CRPThreadGroup();
    }
    /**
     * returns the main thread id for the service.
     * @return main thread id
     */
    public final short getMainThreadID()
    {
        return mainThreadID;
    }
    /**
     * sets the main thread id for the service.
     * @param inpTid thread id of the main thread.
     */
    public final void setMainThreadID(final short inpTid)
    {
        mainThreadID = inpTid;
    }
    /**
     * get service info of this service.
     * @return csi CRPServiceInfo object.
     */
    public final CRPServiceInfo getServiceInfo()
    {
        return csi;
    }
   
    /**
     * handle all capture service messages.
     * @param m input message.
     * @return EtherData object.
     * @throws EtherException on error.
     */
    @Override
    public Message handleMessage(final Message m) throws EtherException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * handles MSG_LOCAL/REMOTE_CONNECTION message.
     * generic implementation here.
     * @param m connection message.
     * @throws EtherException on error.
     */
    public final void handleConnectionMessage(
        final Message m) throws EtherException
    {
        assert(m.getMessageHeader().messageCode() 
            == MESSAGE_CODE.MSG_LOCAL_NEW_CONNECTION 
            || m.getMessageHeader().messageCode() 
            == MESSAGE_CODE.MSG_REMOTE_NEW_CONNECTION);
        ConnectionInterface ci = (
                ConnectionInterface)m.getFirstObject();
       
        CommonLogger.CMN_LOG.info("New Connection from " 
            + ci.getDestService().toString());
        
        CRPThread workerThrd = null;
        boolean newThread = true;
        
        // check if we can reuse any existing thread running.
        // if not create new one and add to the thread group.
        try
        {
            switch (ci.getDestService())
            {
                case CAPTURE_SERVICE:
                {
                    workerThrd = crptg.getAvailableThread();
                    if (workerThrd == null)
                    {
                        CaptureWorker cw = new CaptureWorker();
                        workerThrd = CRPThreadPoolManager.createNewThread(
                            GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
                            cw,
                            GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                            CRPThread.getCurrentThreadID());
                        crptg.addThread(workerThrd.getCRPThreadID());
                    }
                    else
                    {
                        newThread = false;
                    }
                    break;
                }
                case DB_HANDLER_SERVICE:
                {
                    workerThrd = crptg.getAvailableThread();
                    if (workerThrd == null)
                    {
                        DBWorkerThread dbt = new DBWorkerThread();
                        workerThrd = CRPThreadPoolManager.createNewThread(
                            GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE,
                            dbt,
                            GLOBAL_ENUMS.THREAD_TYPE.WORKER_THREAD,
                            CRPThread.getCurrentThreadID());
                        crptg.addThread(workerThrd.getCRPThreadID());
                    }
                    else
                    {
                        newThread = false;
                    }
                    break;
                }
                default:
                {
                    assert(false);
                }
            }
            // send register photon message.
            Message regPHMsg = CRPThread.getCurrentThread(
                    ).getMyMsgWorld().getFreeMsg(
                    GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
            assert(regPHMsg != null);
            regPHMsg.getMessageHeader().setMessageCode(               
                MESSAGE_CODE.MSG_REGISTER_PHOTON);
            // set the current photon to register message object, 
            // this is the photon to be registered to the new thread.
            regPHMsg.setPhoton(m.getPhoton());
            
            // send the message.
            workerThrd.pushCtrlMessage(regPHMsg);
            if(newThread)
            {
                workerThrd.start();
            }
            else
            {
                workerThrd.markThreadAsNotFree();
            }
        }
        catch (CRPException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            throw new EtherException("CRP_ETHERM_ERROR_016", null);

        }  
    }
}
