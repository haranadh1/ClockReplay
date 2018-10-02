package com.crp.protocol;

import com.crp.common.CRPException;

/**
 * protocol exception.
 * @author hpoduri
 * @version $Id$
 *
 */
public class CRPProtocolException extends CRPException
{

    /**
     * constructor.
     * @param errorCode
     * @param args
     * @param preamble
     */
    public CRPProtocolException(
        String errorCode,
        String[] args, String preamble)
    {
        super(errorCode, args, preamble);
        // TODO Auto-generated constructor stub
    }

}
