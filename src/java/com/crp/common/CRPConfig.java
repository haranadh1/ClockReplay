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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * crp config object.
 * @author hpoduri
 * @version $Id$
 */
public class CRPConfig
{

    /**
     * string representation of json object.
     */
    private String jsonString;
    
    /**
     * json object.
     */
    private JSONObject jo;
    
    /**
     * constructor.
     * @param inpJsonString string representation of json.
     */
    public CRPConfig(final String inpJsonString)
    {
        jsonString = inpJsonString;
        jo = null;
        try
        {
            jo = new JSONObject(inpJsonString);
        }
        catch(JSONException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage()); 
        }
    }
    
    /**
     * returns given service node name.
     * @param sName service name for which node name needed.
     * @return string node name.
     * @throws CRPException on error.
     */
    public final String getServiceNode(
        final GLOBAL_ENUMS.SERVICE_CATALOG sName) throws CRPException
    {
        /**
         * do the ugly thing for now, map the service_catalog
         * to json keys. eventually should use only one.
         * do away with the mapping.
         */
        String jsonService = null;
        String jsonServiceList = null;
        
        switch(sName)
        {
            case CAPTURE_SERVICE:
                jsonService = JSON_KEYS.JsonKeyStrings.JSON_CAP_SERVICE;
                jsonServiceList = JSON_KEYS.JsonKeyStrings.JSON_CAP_SERVICE_LIST;
                break;
            case REPLAY_SERVICE:
                jsonService = JSON_KEYS.JsonKeyStrings.JSON_REP_SERVICE;
                jsonServiceList = JSON_KEYS.JsonKeyStrings.JSON_REP_SERVICE_LIST;
                break;
            case DB_HANDLER_SERVICE:
                jsonService = JSON_KEYS.JsonKeyStrings.JSON_DB_SERVICE;
                jsonServiceList = JSON_KEYS.JsonKeyStrings.JSON_DB_SERVICE_LIST;
                break;
            case UI_CLIENT_SERVICE:
                jsonService = JSON_KEYS.JsonKeyStrings.JSON_UI_SERVICE;
                jsonServiceList 
                    = JSON_KEYS.JsonKeyStrings.JSON_UI_SERVICE_LIST;
                break;
            default:
                assert(false);
                break;
        }
        String node = null;
        if(jo == null)
        {
            CommonLogger.CMN_LOG.error("ill formatted json string");
            throw new CRPException("CRP_COMMON_ERROR_004", null);
        }
        try
        {
            node = jo.getJSONObject(
                JSON_KEYS.JsonKeyStrings.JSON_SERVICE_CONFIG_ROOT
                                    ).getJSONObject(
                        jsonServiceList).getJSONArray(
                                jsonService).getJSONObject(0).getString(
                                    JSON_KEYS.JsonKeyStrings.JSON_HOST);
        }
        catch(JSONException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            return null;
            //throw new CRPException(
            //    "CRP_COMMON_ERROR_005", new String[] {jsonService});
        }
        return node;
    }
    
    /**
     * return service info object for the given service.
     * @param inpService input service.
     * @return service info object
     * @throws CRPException on errror.
     */
    public final CRPServiceInfo getService(
        final GLOBAL_ENUMS.SERVICE_CATALOG inpService) throws CRPException
    {
        /**
         * do the ugly thing for now, map the service_catalog
         * to json keys. eventually should use only one.
         * do away with the mapping.
         */
        String jsonService = null;
        String jsonServiceList = null;
        String name = null;
        int port = -1;
        CRPServiceInfo crps =null;
        
        switch(inpService)
        {
            case CAPTURE_SERVICE:
                jsonService = JSON_KEYS.JsonKeyStrings.JSON_CAP_SERVICE;
                jsonServiceList = JSON_KEYS.JsonKeyStrings.JSON_CAP_SERVICE_LIST;
                break;
            case REPLAY_SERVICE:
                jsonService = JSON_KEYS.JsonKeyStrings.JSON_REP_SERVICE;
                jsonServiceList = JSON_KEYS.JsonKeyStrings.JSON_REP_SERVICE_LIST;
                break;
            case DB_HANDLER_SERVICE:
                jsonService = JSON_KEYS.JsonKeyStrings.JSON_DB_SERVICE;
                jsonServiceList = JSON_KEYS.JsonKeyStrings.JSON_DB_SERVICE_LIST;
                break;
            case UI_CLIENT_SERVICE:
                jsonService = JSON_KEYS.JsonKeyStrings.JSON_UI_SERVICE;
                jsonServiceList 
                    = JSON_KEYS.JsonKeyStrings.JSON_UI_SERVICE_LIST;
                break;
            default:
                assert(false);
                break;
        }
        String node = null;
        if(jo == null)
        {
            CommonLogger.CMN_LOG.error("ill formatted json string");
            throw new CRPException("CRP_COMMON_ERROR_004", null);
        }
        try
        {
            name = jo.getJSONObject(
                JSON_KEYS.JsonKeyStrings.JSON_SERVICE_CONFIG_ROOT
                                    ).getJSONObject(
                        jsonServiceList).getJSONArray(
                                jsonService).getJSONObject(0).getString(
                                    JSON_KEYS.JsonKeyStrings.JSON_SERVICE_NAME);
            
            node = jo.getJSONObject(
                JSON_KEYS.JsonKeyStrings.JSON_SERVICE_CONFIG_ROOT
                                    ).getJSONObject(
                        jsonServiceList).getJSONArray(
                                jsonService).getJSONObject(0).getString(
                                    JSON_KEYS.JsonKeyStrings.JSON_HOST);
            port = jo.getJSONObject(
                JSON_KEYS.JsonKeyStrings.JSON_SERVICE_CONFIG_ROOT
                                    ).getJSONObject(
                        jsonServiceList).getJSONArray(
                                jsonService).getJSONObject(0).getInt(
                                    JSON_KEYS.JsonKeyStrings.JSON_SERVICE_PORT);
            crps = new CRPServiceInfo(inpService, node, name,port, 1);
            
        }
        catch(JSONException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
        }
        return crps;
    }
}
