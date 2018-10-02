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

package com.crp.protocol.http;

import com.crp.common.CRPException;
import com.crp.common.CRPRequest;
import com.crp.protocol.ProtocolConstants;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

/**
 * A custom implementation of the HTTP request and response sequence. This class
 * encapsulates both request and its response.
 * 
 * @author subbu
 * @version $Id$
 */
public class CRPHttpRequest implements CRPRequest
{
    /**
     * Interface to HTTP request provided by Apache.
     */
    private HttpClient   client;
    
    /**
     * HTTP request representing the GET method. 
     */
    private HttpGet      get;
    
    /**
     * HTTP request representing the POST method.
     */
    private HttpPost     post;
    
    /**
     * The HTTP response object.
     */
    private HttpResponse response;
    
    /**
     * The entity object holding the response within the HttpResponse object.
     */
    private HttpEntity   entity;
    
    /**
     * The HTTP request destination URI.
     */
    private URI          requestUri;
    
    /**
     * The method used in the HTTP request. E.g GET, POST, etc.
     */
    private String       method;
    
    /**
     * Name of the client host from which this request has been generated.
     */
    private String       clientHostName;
    
    /**
     * Port number on the client side.
     */
    private int          clientPort;
    
    private Map<String, HttpRequestBase> methodToRequestMap;
    
    /**
     * Constructor
     */
    public CRPHttpRequest() 
    {
        // Instantiate only the get method of the HTTP client protocol.
        get  = new HttpGet();
        post = new HttpPost();
        
        methodToRequestMap = new HashMap<String, HttpRequestBase>();
        // For now we will handle only get and post requests.
        methodToRequestMap.put(ProtocolConstants.PROTOCOL_HTTP_GET,  get);
        methodToRequestMap.put(ProtocolConstants.PROTOCOL_HTTP_POST, post);
        
        try {
            requestUri = URI.create("http://localhost");
        }
        catch (IllegalArgumentException ie) {
            ie.printStackTrace();
        }
    }
    
    /**
     * Constructor.
     * 
     * @param url The web URL of the request.
     * @param cl {@link HttpClient} object.
     */
    public CRPHttpRequest(String url, HttpClient cl)
    {
        client             = cl;
        get                = new HttpGet();
        post               = new HttpPost();
        methodToRequestMap = new HashMap<String, HttpRequestBase>();
        
        try {
            requestUri = URI.create(url);
        }
        catch (IllegalArgumentException ie) {
            ie.printStackTrace();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public URI getUri()
    {
        return requestUri;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getMethod()
    {
        return method;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void send() throws CRPException
    {
        if (requestUri == null) {
            throw new CRPException("CRP_PROTOCOL_SEND_ERROR", 
                new String[] {"No URL to request from."});
        }
        
        System.out.println("Sending " + method + " request to " + requestUri.toString());
        try {
            HttpRequestBase base = methodToRequestMap.get(method);
            response = client.execute(base);
            entity   = response.getEntity();
            
            if (entity != null) {
//                System.out.println("Received " + entity.getContentLength() + " bytes.");
//                System.out.println(entity.getContentType());
//                System.out.println(EntityUtils.toString(entity));
                // Don't do anything for now.
                EntityUtils.toString(entity);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] receive() throws CRPException
    {
        byte[] b = null;
        try {
            EntityUtils.toByteArray(entity);
        }
        catch (IOException ioe) {
            // Log the message
        }
        
        return b;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void closeConnection() throws CRPException
    {
        try {
            client.getConnectionManager().shutdown();
        }
        catch (Exception ex) {
            throw new CRPException("CRP_PROTOCOL_CONNECTION_ERROR", 
                new String[] {ex.getMessage()});
        }
    }
    
    /**
     * Set the {@link HttpClient}.
     * 
     * @param cl {@link HttpClient} object.
     */
    public void setHttpClient(HttpClient cl)
    {
        client = cl;
    }
    
    /**
     * @return The host name of the client from which this request originated.
     */
    public String getClientHost()
    {
        return clientHostName;
    }
    
    /**
     * Set the host name of the client who generated this request.
     * 
     * @param clHost Client host name.
     */
    public void setClientHost(String clHost)
    {
        clientHostName = clHost;
    }
    
    /**
     * @return The client port number.
     */
    public int getClientPort()
    {
        return clientPort;
    }
    
    /**
     * Set the port number on the client side.
     * 
     * @param clPort Client port number.
     */
    public void setClientPort(int clPort)
    {
        clientPort = clPort;
    }
    
    /**
     * Sets the request URI.
     * 
     * @param uri URI string.
     */
    public void setUri(String uri) throws CRPException
    {
        if (uri == null || uri.equals("")) {
            throw new CRPException("CRP_PROTOCOL_URI_ERROR", 
                new String[] {"Illegal URI, null or empty."});
        }
        
        // This will create a new URI object. Find a better way to reuse the
        // object.   
        requestUri = requestUri.resolve(uri);
        
        HttpRequestBase base = methodToRequestMap.get(method);
        base.setURI(requestUri);
    }
    
    /**
     * Sets the protocol being used for the request.
     * 
     * @param meth The method as defined by the HTTP protocol. E.g GET, POST, 
     * etc.
     */
    public void setMethod(String meth)
    {
        method = meth;
    }
        
    /**
     * Set the parameters of the web request.
     * 
     * @param paramNames An array of parameter names.
     * @param paramValues An array of parameter values.
     */
    public void setParams(String[] paramNames, String[] paramValues)
    {
        for (int i = 0; i < paramNames.length; i++) {
            HttpRequestBase base = methodToRequestMap.get(method);
            base.getParams().setParameter(paramNames[i], paramValues[i]);
        }
    }
    
    /**
     * Set the given parameter of the web request.
     * 
     * @param paramName Parameter name.
     * @param paramValues Parameter value.
     */
    public void setParam(String paramName, String paramValue)
    {
        HttpRequestBase base = methodToRequestMap.get(method);
        base.getParams().setParameter(paramName, paramValue);
    }
    
    /**
     * Reset the fields in the request so that it can be reused from the pool
     * again.
     */
    public void reset()
    {
        get.setURI(null);
        post.setURI(null);
        response   = null;
        entity     = null;
        // No easy way to reset the URI
        requestUri = requestUri.resolve("http://localhost");
    }
}
