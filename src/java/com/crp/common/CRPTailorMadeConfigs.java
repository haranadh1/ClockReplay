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

/**
 * tailor-made configs for crp.
 * @author hpoduri 
 * @version $Id$
 *
 */
public final class CRPTailorMadeConfigs
{
    /**
     * this is all services in one process.
     */
    private String allInOne;
    
    /**
     * ui only config.
     */
    private String uiConfig;
    
    /**
     * returns all-in-one config setting.
     * @return string (json formatted).
     */
    public final CRPConfig getAllInOneConfig()
    {
        allInOne = String.format(
            "{\"service-config\":{" +
                "\"capture-services\"" +
                    ":{\"capture-service\"" +
                        ":[{\"name\":\"capservice\"" +
                        ",\"host\":\"%s\"," +
                        "\"port\":\"%d\"}]}," +
                
                "\"db-services\"" +
                    ":{\"db-service\"" +
                        ":[{\"name\":\"dbservice\"" +
                        ",\"host\":\"%s\"," +
                        "\"port\":\"%d\"}]}," +
                 
                "\"ui-client-services\"" +
                    ":{\"ui-client-service\"" +
                        ":[{\"name\":\"uiservice\"" +
                        ",\"host\":\"%s\"," +
                        "\"port\":\"%d\"}]}," +
                        
                "\"replay-services\"" +
                    ":{\"replay-service\"" +
                        ":[{\"name\":\"replayservice\"" +
                        ",\"host\":\"%s\"," +
                        "\"port\":\"%d\"}]}" +
            "}}", GLOBAL_CONSTANTS.LOCAL_HOST,
            9980,
            GLOBAL_CONSTANTS.LOCAL_HOST,
            9980,
            GLOBAL_CONSTANTS.LOCAL_HOST,9980,
            GLOBAL_CONSTANTS.LOCAL_HOST,9980);
        return (new CRPConfig(allInOne));
    }
    
    /**
     * returns ui only config.
     * @return crp config object.
     */
    public final CRPConfig getUIOnlyConfig()
    {
        uiConfig = String.format(
            "{\"service-config\":{" +
            "\"ui-client-services\"" +
                ":{\"ui-client-service\"" +
                    ":[{\"name\":\"uiservice\"" +
                    ",\"host\":\"%s\"," +
                    "\"port\":\"%d\"}]} }}",
                    GLOBAL_CONSTANTS.LOCAL_HOST,
                    9980);
        return(new CRPConfig(uiConfig));
    }
}
