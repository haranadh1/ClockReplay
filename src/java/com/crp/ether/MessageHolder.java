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

import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;

/**
 * this is just a holder to hold some messages.
 * dont get confused with MessagGroup, which deals
 * with memory blocks etc.,
 * this class is just a holder of some messages.
 * @author hpoduri
 * @version $Id$
 */
public class MessageHolder
{

    /**
     * message array.
     */
    private Message [] messages;
    
    /**
     * size of the holder.
     */
    private int writeCounter;
    
    /**
     * use read counter.
     * while reading messages, there is a chance that we might add more
     * messages as part of thread suspend/resume. as the whole suspend/
     * resume works based on the remote message sensor check new messages.
     * 
     */
    private int readCounter;
    
    /**
     * constructor.
     */
    public MessageHolder()
    {
        messages = new Message [GLOBAL_CONSTANTS.MAX_MSGS_IN_HOLDER];
        writeCounter = 0;
        readCounter = 0;
    }
    
    /**
     * add message to holder.
     * @param m input message.
     */
    public final void addMessage(final Message m)
    {
        //RemoteMessageSensor.RMS_LOG.info(" write counter: " + String.valueOf(writeCounter)
        //    + "read counter : " + String.valueOf(readCounter));
        assert(writeCounter >= 0);
        messages[writeCounter++] = m;
    }
    
    /**
     * returns message at given index.
     * @param inpIndex index at which message is needed.
     * @return message object.
     */
    public final Message getNextMessage(final int inpIndex)
    {
        assert(writeCounter >= 0);
        //RemoteMessageSensor.RMS_LOG.info(" write counter: " + String.valueOf(writeCounter)
        //   + "read counter : " + String.valueOf(readCounter));
        if(writeCounter == 0)
        {
            CommonLogger.CMN_LOG.error(
                "Cannot read from Message Holder, size 0");
            return null;
        }
        Message m = messages[readCounter++];
        if(readCounter == writeCounter)
        {
            readCounter = writeCounter = 0;
        }
        return m;
    }
    
    /**
     * returns size of the holder.
     * NOTE: DO not ever use it in a for loop, as the
     * size keep changing.
     * @return integer.
     */
    public final int size()
    {
        return (writeCounter - readCounter);
    }

    /**
     * resets the counter.
     */
    public final void reset()
    {
        writeCounter = readCounter;
        readCounter = 0;
    }

    /**
     * simple string representation of this object.
     * @return string object.
     */
    public String zipString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" read counter: " + String.valueOf(this.readCounter));
        sb.append(" write counter: " + String.valueOf(this.writeCounter));
        return sb.toString();
    }
}
