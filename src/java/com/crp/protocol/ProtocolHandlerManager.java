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

import com.crp.protocol.http.HttpProtocolHandler;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory to create specific protocol handlers as needed. This class is holds
 * one instance of each protocol handler and returns the same to clients.
 * 
 * @author subbu
 * @version $Id$
 */
public class ProtocolHandlerManager
{
    /**
     * Specifically handle HTTP protocol.
     */
    private static HttpProtocolHandler httpHandler;
    
    private Map<String, CRPProtocolHandler> dynamicProtocolsMap =
        new HashMap<String, CRPProtocolHandler>();
    
    /**
     * @return Return an instance of the HTTP protocol handler implementation.
     */
    public static HttpProtocolHandler getHttpProtocolHandler()
    {
        if (httpHandler == null) {
            httpHandler = new HttpProtocolHandler();
        }
        
        return httpHandler;
    }
    
    /**
     * A convenience method to add protocol handler dynamically to the factory. 
     * This way clients can implement their own protocols and add them to the
     * replay process without having to restart the application.
     * 
     * @param pinfo The information object the protocol handler.
     */
    @SuppressWarnings("unchecked")
    public void addProtocolHandler(ProtocolInfo pinfo)
    {
        CRPProtocolHandler ph = null;
        
        try {
            URL ur = new URL("jar", "", "file:");
            URL[] urls = new URL[]{ur};
            ClassLoader cl = URLClassLoader.newInstance(urls);
            Class<? extends CRPProtocolHandler> cls = 
                (Class<? extends CRPProtocolHandler>) cl.loadClass("mypackage.MyClass");
            Constructor<? extends CRPProtocolHandler> ctor = cls.getConstructor();
            ph = ctor.newInstance();
            dynamicProtocolsMap.put(pinfo.protocolName, ph);
        }
        catch (Exception ex) {

        }
    }
}
