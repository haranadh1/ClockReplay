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


package com.crp.interfaces;

import java.sql.Timestamp;

/**
 * useful for capturing data from our own capture agent.
 * class to be used for testing purpose only.
 * it should not use any of crp data. only use standard java
 * data types.
 */

public class TestCaplet
{
    /**
     * remote machine name.
     */
    public String         remoteHost;
    /**
     * remote IP host name.
     */
    public String         remoteIP;
    /**
     * remote port.
     */
    public int               remotePort;

    /**
     * long is used for timestamp. we represent the timestamp as long variable
     * and java dont have typedef :-(
     */
    public long              ts;

    /**
     * length of the caplet.
     */
    public int               length;
    
    public String            payLoad;
    
    public TestCaplet(String inpRH, String inpRIP, int inpRP, String inpTS, String inpPL)
    {
        remoteHost = inpRH;
        remoteIP  = inpRIP;
        remotePort = inpRP;
        ts = Timestamp.valueOf(inpTS).getTime();
        payLoad = inpPL;
    }

    public TestCaplet(String inpRH, String inpRIP, int inpRP, long inpTS, String inpPL)
    {
        remoteHost = inpRH;
        remoteIP  = inpRIP;
        remotePort = inpRP;
        ts = inpTS;
        payLoad = inpPL;
    }
}
