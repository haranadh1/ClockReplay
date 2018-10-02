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
import com.crp.protocol.ProtocolConstants;
import com.crp.protocol.ProtocolParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.params.CoreProtocolPNames;

/**
 * Parses raw caplets assuming them to be in compliance with HTTP standard and
 * generates {@link CRPHttpRequest} objects out of them.
 * 
 * @author programmer
 */
public abstract class AbstractHttpParser implements ProtocolParser
{
    /*
     * Constants used within this class.
     */
    public static final String COLON        = ":";
    public static final String EMPTY_STRING = "";
    public static final String SPACE_STRING = " ";
    
    /**
     * Protocol handler for HTTP.
     */
    private HttpProtocolHandler httpHandler;
    
    /**
     * Constructor
     */
    public AbstractHttpParser() {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<CRPHttpRequest> parseCaplets()
    {
        BufferedReader br = null;
        List<CRPHttpRequest> crps = new ArrayList<CRPHttpRequest>();
        
        try {
            File f        = new File("D:\\clock replay\\http-sample.dat.0");
            FileReader fr = new FileReader(f);
            br            = new BufferedReader(fr);
            
            String line  = null;
            String[] tmp = null;
            String clientHost = null;
            String charSet = null;
            String userAgent = null;
            String servHost = null;
            String hostUrl = null;
            String method = null;
            
            int newlineCnt = 0;
            int port = 0;
            CRPHttpRequest req = null;
            
            while ((line = br.readLine()) != null) {

                if (line.equals(EMPTY_STRING) || line.equals("\\n") || line.equals("\\r")) {
                    newlineCnt++;
                }

                // New line marks the beginning of a new request data
                // per HTTP guidelines
                if (newlineCnt == 1) {
                    req = httpHandler.setHttpClient(clientHost, port);
                    req.setMethod(method);
                    req.setParam(CoreProtocolPNames.HTTP_CONTENT_CHARSET, charSet);
                    req.setParam(CoreProtocolPNames.USER_AGENT, userAgent);
                    
                    try {
                        req.setUri(servHost + hostUrl);
                    }
                    catch (CRPException ce) {
                    }
                    
                    crps.add(req);
                    
                    newlineCnt = 0;
                    charSet = null;
                    clientHost = null;
                    userAgent = null;
                    servHost = null;
                    hostUrl = null;
                    method = null;
                    continue;
                }
                
                if (line.startsWith(ProtocolConstants.SOURCE_PREFIX)) {
                    tmp = line.split(COLON);
                    try {
                        port = Integer.valueOf(tmp[1]);
                    }
                    catch (NumberFormatException ne) {
                    }
                    
                    continue;
                }
                
                if (line.startsWith(ProtocolConstants.PARAM_ACCEPT_CHARSET_PREFIX)) {
                    tmp = line.split(COLON);
                    charSet = tmp[1].trim();
                    continue;
                }
                
                if (line.startsWith(ProtocolConstants.PROTOCOL_HTTP_GET) ||
                    line.startsWith(ProtocolConstants.PROTOCOL_HTTP_POST)) 
                {
                    tmp = line.split(SPACE_STRING);
                    hostUrl = tmp[1].trim();
                    method = tmp[0].trim();
                    continue;
                }
                
                if (line.startsWith(ProtocolConstants.SERVER_HOST)) {
                    tmp = line.split(COLON);
                    servHost = "http://" + tmp[1].trim() + COLON + tmp[2].trim();
                    continue;
                }
                
                if (line.startsWith(ProtocolConstants.PARAM_USER_AGENT)) {
                    tmp = line.split(COLON);
                    userAgent = tmp[1].trim();
                    continue;
                }
                
                if (line.startsWith(ProtocolConstants.CLIENT_HOST)) {
                    tmp = line.split(COLON);
                    clientHost = tmp[1].trim();
                    continue;
                }
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException ioe) {
                }
            }
        }
        
        return crps;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<CRPHttpRequest> parseCaplets(InputStream is)
    {
        return null;
    }
    
    /**
     * Sets the HTTP protocol handler on the parser.
     * 
     * @param handler HTTP protocol handler.
     */
    public void setHttpHandler(HttpProtocolHandler handler) 
    {
        httpHandler = handler;
    }
}
