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

import org.json.JSONObject;

/**
 * base class for all the capture agents.
 * @author hpoduri
 * crp should be able to support different types of
 * agents.
 */
public class CaptureAgentBase
{

    /**
     * returns all the list of network interfaces.
     * @return json string.
     * @throws CaptureEngineException on error.
     */
    public JSONObject listAllNetworkInterfaces() throws CaptureEngineException
    {
        return null;
    }
    
    /**
     * start capture for the device given.
     * @param device device name, as shown in list of devices.
     */
    public void setupCapture(final String device) throws CaptureEngineException
    {
        return;
    }
}
