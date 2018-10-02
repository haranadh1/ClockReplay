/* Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved. NOTICE: All
 * information contained herein is, and remains the property of ClockReplay
 * Incorporated and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from ClockReplay Incorporated.
 */

package com.crp.protocol.http;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.memmgr.MemoryBlockObjectPool;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.protocol.CRPProtocolHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Protocol handler specific to HTTP transport.
 * 
 * @author subbu
 * @version $Id$
 */
public abstract class AbstractHttpProtocolHandler implements CRPProtocolHandler
{
    /**
     * Hundred KB.
     */
    private static final int HUNDRED_KB = 100 * GLOBAL_CONSTANTS.KB;
        
    /**
     * An implementation of the {@link ObjectFactory} that creates an object of
     * type required by the {@link MemoryBlockObjectPool}.
     */
    private class CRPHttpRequestFactory implements ObjectFactory
    {
        /**
         * Default Constructor.
         */
        protected CRPHttpRequestFactory() {}
        
        /**
         * Create an instance of the {@link CRPHttpRequest} object to be pooled.
         * 
         * @return The {@link CRPHttpRequest} instance.
         */
        public Object createObjectInstance()
        {
            return new CRPHttpRequest();
        }
        
        /**
         * Reset the contents of the object so it can be reused inside the pool.
         * 
         * @param The {@link CRPHttpRequest} object to be reset.
         */
        public void resetObject(Object obj)
        {
            // Reset the request object
            CRPHttpRequest request = (CRPHttpRequest) obj;
            request.reset();
            
            // Reset the HTTP client objects as well.
            for (HttpClient cl : AbstractHttpProtocolHandler.this.uriHttpClientMap.values())
            {
                cl.getConnectionManager().shutdown();
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getMyName()
        {
            return getClass().getName();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean anyVarFields()
        {
            return false;
        }
        
        /**
         * @return This size of the {@link CRPHttpReqeust} object.
         */
        @Override
        public int getObjSize()
        {
            return 100;
        }
    }
    
    /**
     * A map to store URI, HTTP client pair. When the client IP and port number
     * repeat for a set of requests, then the HttpClient object will be reused 
     * for all such requests.
     */
    private final Map<String, HttpClient> uriHttpClientMap = 
        new HashMap<String, HttpClient>();
    
    /**
     * Parser to read caplets in HTTP standard and create {@link CRPHTTPRequest}
     * objects out of them.
     */
    private HttpParser parser;
    
    /**
     * Pool of {@link CRPHttpRequest} objects.
     */
    private MemoryBlockObjectPool<CRPHttpRequest> pool;

    /**
     * Constructor.
     */
    public AbstractHttpProtocolHandler()
    {
        parser = new HttpParser();
        try {
            pool = new MemoryBlockObjectPool<CRPHttpRequest>(
                HUNDRED_KB, new CRPHttpRequestFactory());
        }
        catch (MemoryManagerException me) {
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<CRPHttpRequest> parseAndCreateRequests()
    {
        // Use the DB service to get buffer full of raw data
        // read each packet and create a CRPHttpRequest
        return parser.parseCaplets();
    }
    
    /**
     * @return The {@link HttpParser} object.
     */
    public AbstractHttpParser getParser()
    {
        return parser;
    }
    
    /**
     * A useful method to get one request object from the pool and set the
     * client to execute the request.
     * 
     * @param host The client host name.
     * @param port The client port number.
     * @return {@link CRPHttpRequest} object.
     */
    public CRPHttpRequest setHttpClient(String host, int port)
    {
        String key = host + port;
        HttpClient client = uriHttpClientMap.get(key);
        if (client == null) {
            client = new DefaultHttpClient();
            uriHttpClientMap.put(key, client);
        }
        
        CRPHttpRequest req = pool.getObject();
        req.setHttpClient(client);
        req.setClientHost(host);
        req.setClientPort(port);
        return req;
    }
}
