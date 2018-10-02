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

import com.crp.common.GLOBAL_CONSTANTS;

/**
 * all supported message types in ether.
 * @author hpoduri
 * @version $Id$
 */
public final class MESSAGE_CODE
{
    /**
     * Message Type is represented in short with max value 99999.
     * |||||||| ||||||||
     * we can use the high order three bits for different pay load types.
     * PAY LOAD CONTENT TYPE: 
     * |- most significant bit, if turned on indicates a short message with
     *    payload size 0.
     *                    
     * RULES TO ADD NEW MESSAGE TYPE(S)
     * 1. Identify which component the message belongs to.
     * 2. Follow the message type range strictly with in the
     *    component.
     * 3. The range is used to find out which component the
     *    message belongs to.
     * Here are the ranges for all components.
     * 
     * 00000-00099   GLOBAL CONTROL MESSAGES(like shutdown, startup, ping etc)
     * 00100-00199   ETHER
     * 00200-00299   COMMON
     * 00300-00399   CAPTURE SERVICE
     * 00400-00499   CAPTURE AGENT
     * 00500-00599   CAPTURE WORKER THREAD
     * 00600-00699   DB SERVICE/DB WORKER THREAD
     * 00700-00799   IO WORKER THREAD
     * 00800-00899   REPLAY SERVICE
     * 00900-00999   REPLAY PROCESS
     * 01000-01099   REPLAY CLIENT
     * 01100-01199   RESULT HANDLER SERVICE
     * 01200-01299   RESULT HANDLER WORKERS
     * 01300-01399   UI PLAYBACK (DONT KNOW WHAT IT IS..YET)
     * 01400-01499   RESOURCE MANAGER
     * 01500-01599   UI CLIENT
     *  ---------
     * 05000-05999   INSTALLER 
     */
    /**
     * START WITH INVALID MESSAGE.
     */
    public final static short INVALID_MESSAGE = -1;
    /*********************************************************************
     *                  write control messages here.
     *********************************************************************/
    /**
     * shutdown.
     */
    public final static short MSG_SHUTDOWN = 0;
    /**
     * ping.
     */
    public final static short MSG_PING = 1;
    
    
    /*********************************************************************
     *                   write ether messages here.
     *********************************************************************/
    /**
     * ether start water mark.
     */
    public final static short MSG_ETHER_START_MARK = 100;
    /**
     * new remote connection request.
     */
    public final static short MSG_REMOTE_NEW_CONNECTION = 101;
    /**
     * new local connection request.
     */
    public final static short MSG_LOCAL_NEW_CONNECTION = 102;
    /**
     * close connection.
     */
    public final static short MSG_CLOSE_CONNECTION = 103;
    
    /**
     * register a photon to a thread.
     */
    public final static short MSG_REGISTER_PHOTON = 104;
    
    /**
     * unregister a photon from a thread.
     */
    public final static short MSG_UNREGISTER_PHOTON = 105;
    
    /**
     * ether end water mark.
     */
    public final static short MSG_ETHER_END_MARK = 199;
    
    /*********************************************************************
     *                   write common messages here.
     *********************************************************************/
    
    public final static short MSG_COMMON_MODULE_START_MARK = 200;
    
    /**
     * represents end of data in any stream.
     */
    public final static short MSG_END_OF_DATA = 201;
    
    /**
     * represents success.
     */
    public final static short MSG_SUCCESS = 202;
    
    /**
     * represents a failure.
     */
    public final static short MSG_FAIL = 203;
    
    /*********************************************************************
     *                   write capture service messages here.
     *********************************************************************/
    /**
     * capture service start water mark.
     */
    public final static short MSG_CAPTURE_SERVICE_START_MARK = 300;

    /**
     * used just for test purposes only.
     */
    public static final short MSG_TEST_CAPURE_AGENT = 301;

    /**
     * Stream caplet packets into db.
     */
    public static final short MSG_CS_WRITE_CAPLET_STREAM_DB = 302;
    
    /**
     * start capture message.
     */
    public static final short MSG_CS_START_CAPTURE = 303;
    
    /**
     * stop capture msg.
     */
    public static final short MSG_CS_STOP_CAPTURE = 304;
    
    /**
     * cancel current capture msg.
     */
    public static final short MSG_CS_CANCEL_CAPTURE = 305;
    
    /**
     * capture service end water mark.
     */
    public final static short MSG_CAPTURE_SERVICE_END_MARK = 399;
    
    /******************************************************************
     *                    write DB Messages here.
     * ****************************************************************
     */
    public final static short MSG_DB_SERVICE_START_MARK = 600;
    
    /**
     * msg from db service to file io.
     */
    public final static short MSG_DB_PAGE_TO_FILE_IO = 601;
    
    /**
     * msg request to read pages sequentially from io thread.
     */
    public final static short MSG_REQ_DB_SEQ_READ_FROM_IO = 602;
    
    /**
     * response to client requesting caplet stream from db.
     */
    public final static short MSG_RES_DB_CAPLET_STREAM_CLIENT = 603;
    
    /**
     * request for db metadata.
     */
    public final static short MSG_REQ_CRP_DB_METADATA = 604;
    
    /**
     * response with db metadata to client.
     */
    public final static short MSG_RES_CRP_DB_METADATA_CLIENT = 605;
    
    /******************************************************************
     *                    write IO thread Messages here.
     * ****************************************************************
     */
    public final static short MSG_IO_SERVICE_START_MARK = 700;
    
    /**
     * msg to read pages sequentially from io worker thread to db.
     */
    public final static short MSG_RES_IO_PAGE_SEQ_READ_TO_DB = 701;
    
    
    /****************************************************************
     *                       RESOURCE MANAGER MESSAGES.
     ****************************************************************
     */
    
    public final static short MSG_RESMGR_START_MARK = 1400;
    
    /**
     * request memory manager information.
     */
    public final static short MSG_REQ_MEMMGR_INFO = 1401;
    
    /**
     * response to mem mgr info request
     */
    public final static short MSG_RES_MEMMGR_INFO = 1402;
    /**
     * req crp threads info.
     */
    public final static short MSG_REQ_CRP_THREADS_INFO = 1403;
    
    /**
     * response to thread pool info request.
     */
    public final static short MSG_RES_THRDPOOL_INFO = 1404;
    
    
    /*********************************************************************
     *                   UI Client messages here.
     *********************************************************************/
    
    public final static short MSG_CLIENT_START_MARK = 1500;
    
    /**
     * read db caplets from the client.
     */
    public final static short MSG_REQ_CLIENT_CAPLET_STREAM_DB = 1501;
    
    /**
     * map for message code key to message pool(in message world).
     * @param mc message code.
     * @return string key.
     */
    public final static String getMsgPoolKeyForMessageCode(final int mc)
    {
        switch (mc) 
        {
            case MSG_REMOTE_NEW_CONNECTION:
                return (GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CONNECTION_KEY);
            case MSG_REQ_CLIENT_CAPLET_STREAM_DB:
                return (GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
            case MSG_RES_DB_CAPLET_STREAM_CLIENT:
            case MSG_CS_WRITE_CAPLET_STREAM_DB:
                return(GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY);
            case MSG_REQ_MEMMGR_INFO:
            case MSG_RES_MEMMGR_INFO:
            case MSG_REQ_CRP_THREADS_INFO:
            case MSG_RES_THRDPOOL_INFO:
            case MSG_REQ_CRP_DB_METADATA:
            case MSG_RES_CRP_DB_METADATA_CLIENT:
                return(GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CRP_STRING_KEY);
            case MSG_END_OF_DATA:
            case MSG_CS_START_CAPTURE:
            case MSG_CS_STOP_CAPTURE:
            case MSG_FAIL:
            case MSG_SUCCESS:
            case MSG_SHUTDOWN:
            case MSG_CLOSE_CONNECTION:
                return(GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
            default:
                assert(false);
        }
        return "InValidString";
    }
}