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
 * json key strings for all the modules.
 * @author hpoduri
 * @version $Id$
 */
public class JSON_KEYS
{

    public static final class JsonKeyStrings
    {
        public static final String JSON_HOST = "host";
        public static final String JSON_CAP_SERVICE="capture-service";
        public static final String JSON_REP_SERVICE="replay-service";
        public static final String JSON_DB_SERVICE="db-service";
        public static final String JSON_SERVICE_NAME ="name";
        public static final String JSON_SERVICE_PORT = "port";
        public static final String JSON_SERVICE_CONFIG_ROOT = "service-config";
        public static final String JSON_CAP_SERVICE_LIST = "capture-services";
        public static final String JSON_DB_SERVICE_LIST = "db-services";
        public static final String JSON_REP_SERVICE_LIST = "replay-services";
        public static final String JSON_UI_SERVICE = "ui-client-service";
        public static final String JSON_UI_SERVICE_LIST = "ui-client-services";
        public static final String JSON_CRP_DB_META_DATA = "crp-db-metadata";
        public static final String JSON_CRP_DB_FILE_DETAILS = "crp-db-file-details";
        public static final String JSON_CRP_DB_FILE_NAME = "crp-db-file-name";
        public static final String JSON_CRP_DB_NUM_DB_PAGES = "crp-db-num-pages-in-file";
        public static final String JSON_CAP_LIST_DEVICES = "network_interfaces";
        public static final String JSON_CAP_NETWORK_INT_NAME = "network-interface-name";
        public static final String JSON_CAP_NETWORK_INT_DESC="network-interface-desc";
        public static final String JSON_CAP_NETWORK_INT_DETAILS = "network-interface-details";
        public static final String JSON_CTXT_DESC = "context-description";
        public static final String JSON_CTXT_DETAILS = "context-details";
        public static final String JSON_OBJECT_DESCRIPTION = "obj-desc";
        public static final String JSON_OBJECT_DETAILS = "obj-details";
        public static final String JSON_CTXT_LIST = "context-list";
        public static final String JSON_CAPLETS_CAPTURED = "num-caplets-captured";
        public static final String JSON_CAPTURE_STATUS ="capture-status";
        public static final String JSON_THREAD_NAME = "thread-name";
        public static final String JSON_THREAD_STATUS = "thread-status";
        public static final String JSON_THREAD_CREATOR = "creator-thread-id";
    }

}
