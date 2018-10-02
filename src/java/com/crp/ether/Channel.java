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


import com.crp.common.CRPServiceInfo;


/**
 * generic channel interface.
 * @author hpoduri
 * @version $Id$
 */
public interface Channel
{
    /**
     * communicator type.
     * useful for streaming types.
     */
    public enum CommunicatorType
    {
        INVALID,
        /**
         * means, this acts as a source for streaming data.
         */
        SOURCE,
        /**
         * this acts as a sink to receive data.
         */
        SINK,
    }
    /**
     * initialize the channel for listener.
     * @param port , port on which it should listen to.
     * @throws EtherException on error.
     */
    public void initListener(int port) throws EtherException;
    /**
     * initializes the channel for ether element type client.
     * @throws EtherException on error.
     */
    public void initClient() throws EtherException;
    /**
     * @param crps CRP service to connect to.
     * @param ct indicates if this acts as a source or sink.
     * should only be used for ether element type == client.
     * @throws EtherException on error.
     */
    public void connect(final CRPServiceInfo crps, final CommunicatorType ct)
        throws EtherException;
    /**
     * blocking send.
     * @param o object to be sent.
     * @throws EtherException on error while sending.
     */
    public boolean blockingSend(final Object o)
        throws EtherException;
    /**
     * blocking recv.
     * @param o receive object.
     * @throws EtherException on error while receiving.
     * @return returns the number of bytes read into the Message.
     * it only reads the bytes that can fit in the byte buffer.
     */
    public int blockingRecv(final Object o)
        throws EtherException;
    
    /**
     * flushes the channel for any pending messages/bytes.
     */
    public void flushChannel();
    
    /**
     * notifies receiver about incoming messages.
     */
    public void notifyReceiver();
    
    /**
     * close communication.
     * @throws EtherException on error.
     */
    public void close() throws EtherException;
    
    /**
     * makes the sender spinning as opposed to wait.
     * 
     * @param spinFlag true is for spinning, false is to wait.
     */
    public void setSenderSpinning(final boolean spinFlag);
    
    /**
     * makes the receiver spinning as opposed to wait.
     * @param spinFlag true is for spinning, false is to wait.
     */
    public void setReceiverSpinning(final boolean spinFlag);
    
    /**
     * convenience method that passes thread id to send method.
     * this way, the caller subscribes its thread id before
     * communication.
     * this is introducer to minimize the usage of thread.currentThread
     * in local communication.
     * thread.currentthread is unfortunately super expensive in java 
     * as of now.
     * @param o object to sent across.
     * @param tid caller's thread id.
     * @return true/false, based on success.
     */
    public boolean blockingSend(final Object o, final short tid);
    
    /**
     * convenience method that passes thread id to recv method.
     * this way, the caller subscribes its thread id before
     * communication.
     * this is introducer to minimize the usage of thread.currentThread
     * in local communication.
     * thread.currentthread is unfortunately super expensive in java 
     * as of now.
     * @param o object to receive.
     * @param tid caller's thread id.
     * @return number of bytes returned.(0) for local communication.
     */
    public int blockingRecv(final Object o, final short tid);
    
    /**
     * resets the channel.
     * should call connect to make any sense out of this channel.
     */
    public void reset();
}
