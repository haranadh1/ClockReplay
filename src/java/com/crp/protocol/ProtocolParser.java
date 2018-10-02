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

import java.io.InputStream;
import java.util.List;

/**
 * Interface defines contract for parsing caplets extracted for different 
 * protocol.
 * 
 * @author subbu
 * @version $Id$
 */
public interface ProtocolParser
{
    /**
     * Defines a contract method for parsing caplets extracted by the capture 
     * service. The caplet for each protocol could be in different format, which
     * only the specific protocol parser would be knowing.
     * 
     * @return List of {@link CRPRequest} objects specific to the protocol the 
     * parser is being run for.
     */
    public List<? extends CRPRequest> parseCaplets();
    
    /**
     * Defines a contract method for parsing caplets extracted by the capture 
     * service. The caplet for each protocol could be in different format, which
     * only the specific protocol parser would be knowing.
     * 
     * @param is {@link java.io.InputStream} of caplets.
     * @return List of {@link CRPRequest} objects specific to the protocol the 
     * parser is being run for.
     */
    public List<? extends CRPRequest> parseCaplets(InputStream is);
}
