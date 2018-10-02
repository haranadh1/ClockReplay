/**
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


/**
 * this class holds all constants across crp.
 * @author hpoduri
 * @version $Id$
 */
public final class GLOBAL_CONSTANTS 
{

    public static final int INT_SIZE = 4;
    public static final int LONG_SIZE = 8;
    public static final int IP_SIZE   = 15;
    /**
     * this is too low. we can revisit it when required.
     * however, we cannot store too much data here, as it
     * repeats for every caplet.
     */
    public static final int HOST_SIZE = 32;
    public static final int KB        = 1024;
    public static final int MB        = KB * KB;
    
    public static final long INVALID_TIME_STAMP = 0xFFFFFFFFFFFFFFFFL;
    
    /**
     * local host string.
     */
    public static final String LOCAL_HOST = "localhost";
    /**
     * crp env root.
     */
    public static final String CRP_ENV = "CRP_ROOT";
    /**
     * mask mem mgr used space.
     */
    public static final long MEMMGR_MASK_USEDSPACE = 0xFFFF000000000000L;
    /**
     * mask memblock length.
     */
    public static final long MEMMGR_MASK_INDEX  = 0x0000FFFFFFFFFFFFL;
    /**
     * Max Number of memory blocks inside MemMgr.
     */
    public static final short MAX_MEM_BLOCKS_IN_MEM_MGR = 1024;
    /**
     * 6 bytes for mem mgr offset.
     */
    public static final short BITS_FOR_MEM_MGR_OFFSET = 48;
    /**
     * max number of services hosted in a process.
     */
    public static final int MAX_SERVICES = 8;
    /**
     * MAX threads in a crp process.
     */
    public static final int MAX_THREADS_IN_PROCESS = 128;
    /**
     * DEBUG MODES FOR CRP MODULES.
     *  0 - NODEBUG-RELEASE MODE
     *  1 - MemoryManager
     *  2 - common
     *  4 - db
     *  8 - capture
     * 16 - replay
     * 32 - sqldb
     * 64 - ether
     *128 - pkunpk(protobuf)
     *256 - Performance logging
     *512 - Message Debug
     *If you want to enable capture+db debug, specify DEBUG=12
     */
    public static final int CRP_DEBUG = 512;
    /**
     * let the thread sleep for an hour.
     */
    public static final long SLEEP_FOR_EVER = 1000*3600;
    /**
     * short string size. only to be used by StringBuffer initialization.
     */
    public static final int SHORT_STRING_SIZE = 32;
    /**
     * medium string size.only to be used by StringBuffer initialization.
     */
    public static final int MEDIUM_STRING_SIZE = 128;
    /**
     * db page size.
     */
    public static final int DB_PAGE_SIZE = 2 * MB;
    
    /**
     * number of db pages written to disk as one io.
     */
    public static final int MAX_DB_PAGES_PER_IO = 4;
    
    /**
     * number of db page messages to be in memory.
     * each db page msg can contain "MAX_DB_PAGES_PER_IO" db pages.
     */
    public static final int NO_DB_PAGE_MSGS = 5;
    /**
     * Max db file size.
     */
    public static final long DB_MAX_FILE_SIZE = ((long)DB_PAGE_SIZE * (long)KB);
	/**
     * (set to 127 to differentiate with other CRPThreads).
     */
    public static final short PROCESS_MAIN_THREAD_ID = 127;
    
    /**
     * invalid thread id.
     */
    public static final short INVALID_THREAD_ID = -1;
    
    /**
     * default number of memory blocks in a memory block group.
     * ideally you need one memory block for message objects,
     * one for the interface objects inside the message and
     * one for the variable fields inside the interfac object.
     */
    public static final int DEFAULT_MEMORY_BLOCKS_IN_MEMORY_BLOCK_GROUP = 4;
    /**
     * default number of MessageGroup objects in MessageGroupSuite class.
     */
    public static final int DEFAULT_MESSAGE_GROUPS_IN_MESSAGE_GROUP_SUITE = 100;
    /**
     * maximum direct allocated byte buffer per photon object.
     */
    public static final int MAX_DIRECT_BUFFER_SIZE = MAX_DB_PAGES_PER_IO * DB_PAGE_SIZE;
    
    /**
     * memory block size for short messages.
     */
    public static final int MEM_BLOCK_SIZE_FOR_SMS = 32 * KB;
    
    /**
     * default port for service handler.
     */
    public static final int DEFAULT_SERVICE_HANDLER_PORT = 9100;
    /**
     * maximum number of message groups allowed per interface object type.
     */
    public static final int MAX_MSG_GRPS_PER_INTERFACE_OBJECT_TYPE = 5;

    /**
     * wire header length. if this changes there are at least three places
     * you should change the code.
     * photon(send/recv) methods. packer/unpacker methods that deal with the
     * wire header. ether methods that rely on wire header.
     */
    public static final int WIRE_HEADER_SIZE = 8;
    
    /**
     * queue size to be used for local communication.
     */
    public static final int MAX_QUEUE_SIZE = 2;
    
    /**
     * max elems in a lock free queue.
     */
    public static final int MAX_ELEMS_IN_QUEUE = 512;
    
    /**
     * memory block size for caplet objects.
     */
    public static final int CAPLET_MEMORY_BLOCK_SIZE = 2 * GLOBAL_CONSTANTS.MB;
    
    /**
     * default file prefix for the db files in crp.
     */
    public static final String CRP_FILE_PREFIX = "crp_work_load_";
    
    /**
     * average pay load size.(approx ) needed for mem mgr to allocate objects.
     */
    public static final int AVG_PAYLOAD_SIZE = 8 * GLOBAL_CONSTANTS.KB;
    
    /**
     * number of selectors to be added to a message sensor, to look for
     * any incoming messages.
     */
    public static final int MAX_PHOTONS_TO_SENSE = 32;
    
    /**
     * max number of messages, a holder can hold.
     */
    public static final int MAX_MSGS_IN_HOLDER =  128;
    
    /**
     * maximum thread wait count. should be used with thread yield
     */
    public static final int MAX_WAIT_COUNT = 1024;
    /**
     * maximum number of fragments a caplet can contain.
     * used while capturing the out of order tcp packets.
     */
    public static final int MAX_FRAGMENTS_IN_A_CAPLET = 40;
    
    /**
     * maximum size of a fragment.
     */
    public static final int MAX_FRAGMENT_SIZE = 512;

    /**
     * static strings used to get the key from the message world map.
     *
     */
    public static final class CRPObjNameStrings
    {
        public static final String DB_PAGE_KEY = "DBPage";
        public static final String MESSAGE_KEY = "Message";
        public static final String CRP_STRING_KEY = "CRPString";     
        public static final String MSG_CRP_STRING_KEY = MESSAGE_KEY + "_" + CRP_STRING_KEY;
        public static final String CONNECTION_KEY = "Connection";
        public static final String MSG_CONNECTION_KEY = MESSAGE_KEY + "_" + CONNECTION_KEY;
        public static final String CAPLET_KEY = "Caplet";
        public static final String MSG_CAPLET_KEY = MESSAGE_KEY + "_" + CAPLET_KEY ;
    }
    
    /**
     * strings to represent different protocol names.
     */
    public static final class CRPProtocolNameStrings
    {
        public static final String TCP_PROTO = "TCP";
        public static final String UDP_PROTO = "UDP";
        public static final String HTTP_PROTO = "HTTP";
        public static final String HTTPS_PROTO = "HTTPS";
        
    }
    /**
     * disable constructor.
     */
    private GLOBAL_CONSTANTS()
    {
        // DO NOTHING
    }
}
