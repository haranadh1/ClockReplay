/*
 *
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of ClockReplay Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or
 * copyright law.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from ClockReplay Incorporated.
 */
package com.crp.common;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * One place to keep all globally accessed ENUMS.
 * @author hpoduri
 * @version $Id$
 */
public class GLOBAL_ENUMS
{
    /**
     * different capture agents.
     * pcap/apache server logs etc.
     */
    public static enum CRP_CAPTURE_AGENT_TYPE
    {
        /**
         * pcap library agent.
         */
        PCAP,
        /**
         * extract data from apache server logs.
         */
        APACHE_SERVER_LOGS,
        /**
         * extract data captured from IIS server logs.
         */
        IIS_SERVER_LOGS,
    }
    /**
     * Stages in CRP.
     */
    public static enum CRP_STAGES
    {
        INVALID_STAGE,
        INIT,
        CONFIGURE_STAGE,
        CAPTURE_STAGE,
        REPLAY_STAGE,
        RESULT_ANALYZER_STAGE,
        PURGE_STAGE,
        // want to add any stage! should add above the final stage
        FINAL_STAGE,
    }

    public static enum PROCESS_STATUS
    {
        PROCESS_INIT, PROCESS_READY, PROCESS_PROGRESS, PROCESS_TEARDOWN,
    }

    public static enum THREAD_TYPE
    {
        SERVICE_MAIN_THREAD, WORKER_THREAD, LISTENER_THREAD,

    }
    /**
     * element type in ether. 
     * can be a client(send/receive) or a listener.
     */
    public static enum ETHER_ELEMENT_TYPE
    {
        LISTENER, CLIENT,
    }
    /**
     * communication type.
     */
    public static enum ETHER_COMMUNICATION_TYPE
    {
        /**
         * intra process communication.
         */
        LOCAL,
        /**
         * inter process communication.(TCP/IP).
         */
        REMOTE,
    }
    /**
     * represents different types of db access. 
     *
     */
    public static enum DBAccessMode
    {
        /**
         * streaming data into db.
         */
        SEQUENTIAL_WRITE,
        /**
         * reading data from db. (sequentially)
         */
        SEQUENETAL_READ,
        /**
         * random read data from db.
         */
        RANDOM_WRITE,
        /**
         * random write data into db.
         */
        RANDOM_READ,
        
        /**
         * un initialized.
         */
        UNINIT,
    }
    /**
     * service name catalog.
     */
    public static enum SERVICE_CATALOG
    {
        /**
         * this should always be the first entry.
         * dont add any before this.
         */
        INVALID_HANDLER_SERVICE,
        /**
         * used for testing purpose.
         */
        DUMMY_HANDLER_SERVICE,
        SERVICE_HANDLER_SERVICE,
        CONNECTION_HANDLER_SERVICE,
        CAPTURE_AGENT_SERVICE,
        CAPTURE_SERVICE,
        DB_HANDLER_SERVICE,
        REPLAY_SERVICE,
        RESULT_HANDLER_SERVICE,
        RESOURCE_MANAGER_SERVICE,
        IO_SERVICE,
        UI_CLIENT_SERVICE,
        
        /**
         * this should always be the last entry.
         * dont add any after this.
         */
        MAX_HANDLER_SERVICE;
        
        /**
         * create a reverse lookup from int to enum. 
         */
        private static final Map<Integer, SERVICE_CATALOG> REVERSE_LOOKUP 
            = new HashMap<Integer, SERVICE_CATALOG>();
        /**
         * for now, have this ugly conversion.
         * until we figure out a better way of converting
         * int to enum, one more reason to hate java :-)
         */
        static
        {
            for(SERVICE_CATALOG s : EnumSet.allOf(SERVICE_CATALOG.class))
            {
                REVERSE_LOOKUP.put(s.ordinal(), s);
            }
        }
        /**
         * returns enum for the integer value.
         * @param eVal integer value relates to a enum.
         * @return enum.
         */
        public static SERVICE_CATALOG getEnumFromInt( final int eVal )
        {
            return(REVERSE_LOOKUP.get(eVal));
        }
    }
}
