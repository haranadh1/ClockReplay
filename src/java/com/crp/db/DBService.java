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

import com.crp.common.CRPException;
import com.crp.common.CRPLogger;
import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.ether.EtherData;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.Message.MessageStatus;
import com.crp.interfaces.ConnectionInterface;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadGroup;
import com.crp.thread.CRPThreadPoolManager;
/**
 * DB Service main class. this class should handle
 * all db requests. 
 * @author hpoduri
 * @version $Id$
 */
public class DBService extends CRPService 
{
    /**
     * thread group for db service.
     */
    private CRPThreadGroup dbThrdGroup;

    /**
     * create a separate logger for ConnectionService.
     */
    public static final Category DB_SERVICE_LOG = 
        CRPLogger.initializeLogger(
            "com.crp.db.DBService");
    
    /**
     * constructor.
     * @param inpSi input service info object.
     */
    public DBService(final CRPServiceInfo inpSi)
    {
        super(inpSi);
        dbThrdGroup = new CRPThreadGroup();
    }
   
    /*
    @Override
    public void run()
    {
        CommonLogger.CMN_LOG.info("DB Service Started");
        CRPThread crpt = (CRPThread)Thread.currentThread();
        if (crpt == null)
        {
            CommonLogger.CMN_LOG.fatal(
                "thread object null after casting to crp thread");
        }
        setMainThreadID(crpt.getThreadID());
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
            
            try 
            {
                Message m = crpt.popCtrlMessage();
                if (m == null)
                {
                    crpt.suspendMe();
                }
                
            }
            catch (Exception e)
            {
                
            }
        }

    }
    */
    /**
     * handle all capture service messages.
     * @param m input message.
     * @return EtherData object.
     * @throws EtherException on error.
     */
    @Override
    public final Message handleMessage(final Message m) throws EtherException
    {
        switch(m.getMessageHeader().messageCode())
        {
            case MESSAGE_CODE.MSG_REMOTE_NEW_CONNECTION:
            case MESSAGE_CODE.MSG_LOCAL_NEW_CONNECTION:
            {
                handleConnectionMessage(m);
                break;
            }
        }
        m.updateStatus(MessageStatus.AVAILABLE);
        return null;
    }
    
    /**
     * returns db thread group.
     * @return CRPThreadGroup object.
     */
    public final CRPThreadGroup getDBThreadGroup()
    {
        return dbThrdGroup;
    }
}
