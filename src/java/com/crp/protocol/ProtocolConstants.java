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
 * A simple interface that holds constants used within this package.
 * 
 * @author subbu
 * @version $Id$
 */
public interface ProtocolConstants
{
    /**
     * Constant used as a prefix in the caplet to identify the source IP/host 
     * of a HTTP request.
     */
    public static final String SOURCE_PREFIX = "SRC";
    
    /**
     * Constant to identify the client host in a caplet.
     */
    public static final String CLIENT_HOST = "Client-Host";
    
    /**
     * Constant to identify the server name/url in a caplet.
     */
    public static final String SERVER_HOST = "Host";
    
    /**
     * Constant to identify the GET method used in HTTP protocol.
     */
    public static final String PROTOCOL_HTTP_GET = "GET";
    
    /**
     * Constant to identify the PUT method used in HTTP protocol.
     */
    public static final String PROTOCOL_HTTP_PUT = "PUT";
    
    /**
     * Constant to identify the POST method used in HTTP protocol.
     */
    public static final String PROTOCOL_HTTP_POST = "POST";
    
    /**
     * Constant used as a prefix in the caplet to identify the nature of 
     * connection. E.g. keep-alive
     */
    public static final String PARAM_CONNECTION_PREFIX = "Connection";
    
    /**
     * Constant used as a prefix in the caplet to identify the type of data the
     * HTTP request expects in result. E.g. text/html, application/xhtml+xml
     */
    public static final String PARAM_ACCEPT_PREFIX = "Accept";
    
    /**
     * Constant used as a prefix in the caplet to identify the encoding used by
     * the HTTP reqeust. E.g. gzip,deflate
     */
    public static final String PARAM_ACCEPT_ENCODING_PREFIX = "Accept-Encoding";
    
    /**
     * Constant used as a prefix in the caplet to identify the language used by
     * the HTTP request. E.g. en-US,en
     */
    public static final String PARAM_ACCEPT_LANGUAGE_PREFIX = "Accept-Language";
    
    /**
     * Constant used as a prefix in the caplet to identify the character set
     * used by the HTTP request. E.g. ISO-8859-1,UTF-8
     */
    public static final String PARAM_ACCEPT_CHARSET_PREFIX = "Accept-Charset";
    
    /**
     * Constant used as a prefix in the caplet to identify the user agent that
     * the HTTP request originated from. 
     * E.g Mozilla/5.0 (Windows NT 6.1; rv:7.0.1) Gecko/20100101 Firefox/7.0.1
     */
    public static final String PARAM_USER_AGENT = "User-Agent";
}
