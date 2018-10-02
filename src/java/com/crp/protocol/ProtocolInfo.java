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

/**
 * A simple info class that holds information about a protocol being used in the
 * replay process.
 * 
 * @author subbu
 * @version $Id$
 */
public class ProtocolInfo
{
    /**
     * The name of the protocol being used by the replay. e.g. HTTP/1.1
     */
    public String protocolName;
    
    /**
     * The protocol handler class name. e.g. com.crp.protocol.MyProtocolHandler
     */
    public String protocolClassName;
    
    /**
     * Default constructor, takes two arguments to initialize the instance.
     * 
     * @param name The underlying protocol's name.
     * @param className The name of the protocol handler class.
     */
    public ProtocolInfo(final String name, final String className)
    {
        protocolName  = name;
        protocolClassName = className;
    }
    
    /**
     * @return Returns the the name of the underlying protocol. e.g. HTTP/1.1
     */
    public final String getProtocol()
    {
        return protocolName;
    }
    
    /**
     * @return Returns the class used by the underlying protocol handler.
     * e.g. com.crp.protocol.MyProtocolHandler
     */
    public final String getProtocolHandlerClassName()
    {
        return protocolClassName;
    }
}
