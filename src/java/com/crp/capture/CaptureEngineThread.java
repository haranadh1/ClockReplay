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

package com.crp.capture;

import com.crp.common.CRPContext;

/**
 * external thread representing capture engine.
 * this thread need not be a crp thread, mainly it does
 * not need all the the default memory blocks initialized.
 * this thread should be invoked by CaptureWorker thread.
 * @author hpoduri
 * @version $Id$
 */
public class CaptureEngineThread implements Runnable
{

    /**
     * context object should be passed, before start this
     * thread. when the thread starts to run, it always
     * assumes a valid crp context object.
     */
    private CRPContext ctx;
    
    /**
     * set crp context.
     * @param inpCtx input context object.
     */
    public final void setCRPContext(final CRPContext inpCtx)
    {
        ctx = inpCtx;
        
    }
    @Override
    public void run()
    {
        if(ctx == null)
        {
            CaptureService.CAP_SERVICE_LOG.error(" crp context object is null, cannot start capture");
            return;
        }
        CaptureContextObject cco = 
            (CaptureContextObject)ctx.getObjectAtIndex(0).o;
        cco.ce.startCapture(cco);
    }

}
