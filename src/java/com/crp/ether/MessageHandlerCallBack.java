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
/**
 * interface to handle the messages. 
 * Every CRPService is expected to extend this interface.
 * @author hpoduri
 * @version $Id$
 */
public interface MessageHandlerCallBack
{
    /**
     * handles a message.
     * @param m input message.
     * @return returns EtherData object.
     * @throws EtherException on error.
     */
    public Message handleMessage(final Message m) throws EtherException;
}
