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
 * interface for crp context objects.
 * @author hpoduri
 * @version $Id$
 */
public interface CRPContextObjectInterface
{
    /**
     * json representation of the object.
     * @return
     */
    public JSONObject toJSON() throws JSONException;

}
