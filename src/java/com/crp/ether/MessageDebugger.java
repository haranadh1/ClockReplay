package com.crp.ether;

import java.util.concurrent.atomic.AtomicLong;

import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;

public class MessageDebugger
{
    /**
     * should only be used for message debugging. very expensive to use in
     * production.
     */

    public short                        srcThreadId;
    public short                        targetThreadId;

    public GLOBAL_ENUMS.SERVICE_CATALOG srcService;
    public GLOBAL_ENUMS.SERVICE_CATALOG destService;
    public long msgSequenceId;
    
    /**
     * we keep sequence number to keep track of messages.
     */
    public static AtomicLong msgSequenceGenerator = new AtomicLong(0);

    /**
     * constructor.
     */
    public MessageDebugger()
    {
        if(GLOBAL_UTILS.msgDebug())
        {
            msgSequenceId = msgSequenceGenerator.incrementAndGet();
        }
    }
    
}
