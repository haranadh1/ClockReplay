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

import com.crp.common.CRPException;
import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CRPLogger;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.ether.EtherData;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.MessageHandlerCallBack;
import com.crp.ether.NetworkChannel;
import com.crp.ether.Photon;
import com.crp.interfaces.ConnectionInterface;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadGroup;
import com.crp.thread.CRPThreadPoolManager;
/**
 * capture service class.
 * handles all the requests to capture service.
 * @author hpoduri
 * @version $Id$
 */
public class CaptureService extends CRPService 
{
    /**
     * create a separate thread group for capture engine threads.
     */
    private CRPThreadGroup capEngineThreadGroup;
    /**
     * constructor. 
     * @param crps capture service information class.
     */
    public CaptureService(final CRPServiceInfo crps)
    {
        super(crps);
        capEngineThreadGroup = new CRPThreadGroup();
    }
    
    /**
     * returns capture engine thread group.
     * @return CRPThreadGroup object.
     */
    public final CRPThreadGroup getCapEngineThreadGroup()
    {
        return capEngineThreadGroup;
    }
    /**
     * create a separate logger for CaptureService.
     */
    public static final Category CAP_SERVICE_LOG = 
        CRPLogger.initializeLogger(
            "com.crp.ether.CaptureService");
    /*
    @Override
    public void run()
    {
        CommonLogger.CMN_LOG.info("Capture Service Started");
        CRPThread crpt = (CRPThread)Thread.currentThread();
        while (true)
        {
            //check control messages.
            
            //check non-control messages.
            
            // --- process non-control messages if any.
            
            //if no messages
            // yield for some iterations...
            // after some iterations do sleep for ever.
            // wake up thru interrupt by the process main thread.
            
            // testing....
            try
            {
                //for now comment this for testing.
                // jump into connecting to db service.
                // Message msg = crpt.popCtrlMessage();
                Photon p = new Photon(GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT,
                    GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE);
                p.initialize();
               
                CRPServiceInfo crps = new CRPServiceInfo(
                    "DBService", "localhost", "database service", 8806, 0);
                p.connect(crps);
                // send extra information
                Message m = Message.createSMS(MESSAGE_CODE.MSG_NEW_CONNECTION);
                p.bsendSMS(m);
                
                
            }
            catch (Exception e)
            {
                CaptureService.CAP_SERVICE_LOG.error(e.getMessage());
                e.printStackTrace();
                CaptureService.CAP_SERVICE_LOG.error(e.getStackTrace());
            }
        }

    }
*/
    /**
     * handle all capture service messages.
     * @param m input message.
     * @return Message object.
     * @throws EtherException on error.
     */
    @Override
    public final Message handleMessage(final Message m) throws EtherException
    {
        CAP_SERVICE_LOG.info("inside capture service handle message");
        try
        {
            switch (m.getMessageHeader().messageCode()) 
            {
                case MESSAGE_CODE.MSG_REMOTE_NEW_CONNECTION:
                case MESSAGE_CODE.MSG_LOCAL_NEW_CONNECTION:    
                {
                    handleConnectionMessage(m);
                    break;
                }
               
            }
        }
        catch (CRPException e)
        {
            throw new EtherException("CRP_ETHERM_ERROR_016", null);
        }
        return null;
    }
    
}
