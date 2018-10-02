/*
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved. NOTICE: All
 * information contained herein is, and remains the property of ClockReplay
 * Incorporated and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from ClockReplay Incorporated.
 */
/**
 * @author hpoduri
 * @version $Id$ General exception for crp.
 */
package com.crp.common;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * General exception for crp. we can add any other information before start
 * writing the error to log.
 * @author hpoduri
 */
public class CRPException extends Exception
{

    /**
     * error code.
     */
    private String ec;
    
    /**
     * make java happy.
     */
    private static final long serialVersionUID = 1L;

    /**
     * constructor.
     * @param errorCode : input error code specified in the properties file
     * @param args : array of string arguments.
     */
    public CRPException(final String errorCode, final String[] args)
    {
        super(CRErrorHandling.getError(errorCode, args));
        ec = errorCode;
    }

    /**
     * constructor with preamble to error message. The error message is prefixed
     * with a custom string, which can be used by any exception class.
     * @param errorCode : input error code specified in the properties file
     * @param args : array of string arguments.
     * @param preamble : prefixed to the error message.
     */
    public CRPException(final String errorCode, final String[] args,
        final String preamble)
    {
        super(preamble + ":" + CRErrorHandling.getError(errorCode, args));
        ec = errorCode;
    }

    /**
     * get error message;we can annotate it the way we want. this method can
     * have a very general boiler plate error preamble
     * @return error message.
     */
    public String getError()
    {
        // write the stack trace to the log and let the caller decide
        // what to do with the error message.
        CommonLogger.CMN_LOG.error(this.getStackTrace());
        return super.getMessage();
    }

    /**
     * override getMessage. can customize it the way we want here.
     * @return error message string
     */
    public static String getPrintStack(Exception e)
    {
        // first get the stack trace:
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        String stacktrace = sw.toString();
        return (" STACK TRACE: " + stacktrace + " \n" + System.getProperty("line.separator") + stacktrace);
    }
    
    /**
     * converts a error code string to int.
     * @return integer representation of error code string.
     */
    public static final int convertErrorCodeToInt()
    {
        return -1;
    }
}
