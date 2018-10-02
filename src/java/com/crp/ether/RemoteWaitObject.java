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

package com.crp.ether;

import java.nio.channels.Selector;

/**
 * class to represent the remote wait object.
 * this can simply be a selector waiting on some network I/O.
 * @author hpoduri
 * @version $Id$
 */
public class RemoteWaitObject
{
    /**
     * selector java nio.
     */
    private Selector remoteSelector;
    
    /**
     * constructor.
     * @param inpSel selector waiting on.
     */
    public RemoteWaitObject(final Selector inpSel)
    {
        remoteSelector = inpSel;
    }
    
    /**
     * wake up from select call.
     */
    public final void wakeup()
    {
        if(remoteSelector !=null)
        {
            remoteSelector.wakeup();
        }
    }
}
