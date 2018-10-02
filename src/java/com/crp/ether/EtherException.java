/**
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
package com.crp.ether;

import com.crp.common.CRPException;
/**
 * general ether exception.
 * @author hpoduri
 * @version $Id$
 */
public class EtherException extends CRPException
{
    /**
     * make java compiler happy.
     */
    private static final long serialVersionUID = 1L;

    /**
     * constructor.
     * @param errorCode input error code
     * @param args : runtime arguments to the string.
     */
    public EtherException(final String errorCode,
        final String [] args) 
    {
        //TODO : add a specific ether string.
        super(errorCode, args, "ETHER_ERROR:");
    }
}
