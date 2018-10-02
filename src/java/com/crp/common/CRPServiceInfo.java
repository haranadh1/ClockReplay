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
/**
 * class represents all the information about a CRP Service.
 * @author hpoduri
 * @version $Id$
 *
 */
public class CRPServiceInfo
{
    /**
     * host name.
     */
    private String host;
    /**
     * name.
     */
    private GLOBAL_ENUMS.SERVICE_CATALOG name;
    /**
     * description of crp service.
     */
    private String desc;
    /**
     * port number listening to or "0"(if not).
     */
    private int port;
    /**
     * priority of the service. this will help the crp system take better
     * decisions. specially with memory manager and may be more in future.
     */
    private int    priority;
    /**
     * thread owns the service
     */
    private int tid;
    /**
     * scope of the service. 
     * GLOBAL means, the only one service can be there.
     * PROCESS means, one service of this kind for each process.
     * ex: listener/connection handler service is for each process,
     * while capture/replay services are global. 
     *
     */
    private enum ServiceScope 
    {
        GLOBAL,
        PROCESS,
    }
    /**
     * scope for this service.
     */
    private ServiceScope ss;

    /**
     * enables remote connection to this service.
     * makes all the clients to connect to this service
     * thru remote communication.(network channel).
     */
    private boolean forceRemote;
    
    /**
     * constructor.
     * @param inpName  service name
     * @param inpHost  host name where service is being hosted.
     * @param inpDesc description of the service.
     * @param inpPort port if any.
     * @param inpPriority priority of the service.
     */
    public CRPServiceInfo(final GLOBAL_ENUMS.SERVICE_CATALOG inpName,
        final String inpHost,
        final String inpDesc, final int inpPort,
        final int inpPriority)
    {
        name = inpName;
        desc = inpDesc;
        port = inpPort;
        host = inpHost;
        priority = inpPriority;
        forceRemote = false;
    }

    /**
     * force remote connections to this service.
     */
    public final void forceRemote()
    {
        forceRemote = true;
    }
    
    /**
     * returns if the client should use network channel.
     * to connect to this service.
     * @return boolean.
     */
    public final boolean isForcedRemoteConnection()
    {
        return forceRemote;
    }
    /**
     * returns name.
     * this is used as the key in PSERVICES hash map table.
     * @return name.
     */
    public final GLOBAL_ENUMS.SERVICE_CATALOG getName()
    {
        return name;
    }
    /**
     * returns port.
     * @return port
     */
    public final int getPort()
    {
        return port;
    }
    /**
     * returns host.
     * @return host
     */
    public final String getHost()
    {
        return host;
    }
    /**
     * returns description.
     * @return desc
     */
    public final String getDesc()
    {
        return desc;
    }

    /**
     * returns priority.
     * @return priority.
     */
    public final int getPriority()
    {
        return priority;
    }
    /**
     * set thread id for this service.
     * @param inpTid input thread id.
     */
    public final void setThreadID(final int inpTid)
    {
        assert(inpTid >=0 );
        tid = inpTid;
    }
    /**
     * returns the main thread id of this service.
     * @return thread id.
     */
    public final int getThreadID()
    {
        return tid;
    }
    /**
     * returns the zip string.
     * @return String representation of this object.
     */
    public final String zipString()
    {
        StringBuilder sb = new StringBuilder(
            GLOBAL_CONSTANTS.MEDIUM_STRING_SIZE);
        sb.append(this.desc);
        return sb.toString();
    }

}
