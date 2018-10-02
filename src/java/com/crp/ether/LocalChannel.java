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
import com.crp.common.LockFreeQ;
import com.crp.interfaces.ConnectionInterface;
import com.crp.thread.CRPThread;

/**
 * class to implement the in-memory communication channel.
 * this is implemented by using lock free queue as the backbone.
 * @author hpoduri
 * @version $Id$
 */
public class LocalChannel implements Channel
{

    /**
     * lock free queue, used for local communication.
     */
    private LockFreeQ lfq;
    
    /**
     * connection interface object.
     * needed for the thread ids.
     */
    private ConnectionInterface ci;
    
    @Override
    public int blockingRecv(Object o) throws EtherException
    {
        o = lfq.getElemFromQ();
        return 0;
    }

    /**
     * convenience method for local communication.
     * @return object in the queue.
     * @throws EtherException on error.
     */
    public final Object blockingRecv() throws EtherException
    {
        return lfq.getElemFromQ();
    }
    
    @Override
    public boolean blockingSend(Object o) throws EtherException
    {
        return(lfq.addToQ(o));
    }

    @Override
    public void close() throws EtherException
    {
        lfq.doneProducing();
        //wait until the consumer consumes the data.
        while(lfq.anyPendingData())
        {
            Thread.yield();
        }
    }

    @Override
    public void connect(CRPServiceInfo crps,
        final CommunicatorType ct) throws EtherException
    {
        // make the thread connecting to a service as the producer.
        if(ct == CommunicatorType.SOURCE)
        {
            lfq.subscribeAsProducer(CRPThread.getCurrentThreadID());
        }
        else
        {
            lfq.subscribeAsConsumer(CRPThread.getCurrentThreadID());
        }
       
    }

    @Override
    public void initClient() throws EtherException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void initListener(int port) throws EtherException
    {
        // TODO Auto-generated method stub

    }
    
    /**
     * constructor.
     * @param qSize queue size.
     * @param inpCI input connection interface object.
     */
    public LocalChannel(final int qSize, final ConnectionInterface inpCI)
    {
        lfq = new LockFreeQ(qSize, true);
        lfq.setConsumerSpinning(true);
        lfq.setProducerSpinning(true);
        ci = inpCI;
    }

    @Override
    public void flushChannel()
    {
        lfq.doneProducing();
    }

    @Override
    public void notifyReceiver()
    {
        // this amounts to simply waking up the receiver thread.
        // if it is not one of the crp threads, ignore it.
        lfq.resumeConsumer(true);
        
    }

    @Override
    public void setReceiverSpinning(boolean spinFlag)
    {
        lfq.setConsumerSpinning(spinFlag);
        
    }

    @Override
    public void setSenderSpinning(boolean spinFlag)
    {
        lfq.setProducerSpinning(spinFlag);  
    }

    @Override
    public boolean blockingSend(Object o, short tid)
    {
        return (lfq.addToQ(o, tid));
    }

    @Override
    public int blockingRecv(Object o, short tid)
    {
        o = lfq.getElemFromQ(tid);
        return 0;
    }

    @Override
    public void reset()
    {
        lfq.reset();
    }

}
