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

package com.crp.protocol;

import com.crp.common.CRPRequest;

import java.util.List;

/**
 * A common protocol interface for all handlers that work on a designated
 * protocol.
 * 
 * @author subbu
 * @version $Id$
 */
public interface CRPProtocolHandler
{
    /**
     * Read a block of raw data from ether, try to make sense out of it and 
     * construct appropriate protocol specific request objects or entities that
     * can communicate with a remote program/service and get desired response. 
     * All implementing classes will be able to return CRPRequest objects 
     * specific to the protocol they implement.
     * 
     * @return Array of request objects specific to the protocol implementation.
     * 
     * TODO: Ideally a MemoryBlock<CRPRequest> should b returned.
     */
    public List<? extends CRPRequest> parseAndCreateRequests();
}
