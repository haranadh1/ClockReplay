package com.crp.capture;

import com.crp.common.CRPException;

/**
 * exception class for capture engine.
 * @author hpoduri
 * @version $Id$
 */
public class CaptureEngineException extends CRPException
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * constructor.
     * @param errorCode : input error code specified in the properties file
     * @param args : array of string arguments.
     */
    public CaptureEngineException(final String errorCode, final String[] args)
    {
        super(errorCode, args);
    }

}
