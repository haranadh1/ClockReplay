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


package com.crp.common;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThreadPoolManager;

/**
 * a general queue implementation using java atomics.
 * @author hpoduri
 * @version $Id$
 */
public class LockFreeQ
{
    /**
     * Implementation details.
     * producer and consumer should subscribe to the queue, with their roles
     * respectively.
     * one atomic int 64 bit variable with one byte for each of the queue.
     * at most we can support 8 such arrays.
     * each byte in the atomic 64 bit variable represents a corresponding
     * array status. ex: byte 0 represents the status of the array index 0.
     * each array status depends on a set of producer/consumer activities.
     * at any point of time, only one producer or one consumer should access
     * one queue.
     * PRODUCER AND CONSUMER should follow one simple rule, they should always
     * look ahead for the next available queue, should never look back.
     * at the end of all the queues, it should start over from the beginning.
     */
    private FixedSizeQueue [] queueGroup;
    
    /**
     * FULL DUPLEX COMMNICATION: Implementation Notes.
     * Usually, in CRP, we always have one-way data transfer(stream based).
     * However, there can be cases where we might need short controlled
     * communication in the other way. For those messages,
     * we use lock free stack.
     * as it is a stack, you should NOT assume the order.
     * How do we know, what is a streaming connection(we use LFQ)vs short
     * control messages?
     * 
     * 1. who ever starts sending the first data, will be producer for LFQ.
     * 2. the other would be receiver for LFQ, streaming connection.
     * 3. the producer and receiver are determined with the first message comm.
     * 4. if the producer ever has to receive a message, it should look for
     *    the LFS.
     * 5. if the consumer ever wants to send a message, it should do it thru
     *    the LFS.   
     */
    private LockFreeStack<Object> lfs;
    
    /**
     * status of individual queue represented as one byte in this variable.
     */
    private AtomicLong status;
    
    /**
     * make sure only one thread is suspended at any time.
     */
    private AtomicInteger  waitCounter;
    
    /**
     * producer crp thread id.
     */
    private volatile short producer;
    
    /**
     * current Q index that producer is writing to.
     */
    private volatile int pQIndex;
    
    /**
     * current Q index that consumer is reading from.
     */
    private volatile int cQIndex;
    
    /**
     * consumer crp thread id.
     */
    private volatile short consumer;

    /**
     * indicates if this lfq supports full duplex communication.
     */
    private boolean isFullDuplex;
    /**
     * indicates if the producer like to spin(when no space) as opposed to wait.
     */
    private boolean pSpin;
    
    /**
     * indicates if the consumer like to spin(when no data) as opposed to wait.
     */
    private boolean cSpin;
                  
    /**
     * sets the producer thread id.
     * @param tid thread id of the thread being subscribed as
     * producer.
     */
    public final void subscribeAsProducer(final short tid)
    {
        producer = tid;
        
        CommonLogger.CMN_LOG.info("LFQ: thread id: " + String.valueOf(tid) + " registered as producer");
    }
    
    /**
     * sets consumer thread id.
     * @param tid thread id of the thread being subscribed as
     */
    public final void subscribeAsConsumer(final short tid)
    {
        consumer = tid;
       
        CommonLogger.CMN_LOG.info("LFQ: thread id: " + String.valueOf(tid) + " registered as consumer");

    }
    
    /**
     * subscribe the other end.(p/c).
     * as one end should be specified as part of
     * connection, only one can get in.
     * @param threadID current thread id.
     */
    private void subscribeToQueue(final short threadID)
    {
        if(producer >= 0 && consumer >= 0)
        {
            return;
        }
        // check if both producer and consumer are subscribed.
        // if yes, this is a no op.
        if(producer == threadID || consumer == threadID)
        {
            return;
        }
        if( producer == -1 )
        {
            producer = threadID;
            return;
        }
        else if (consumer == -1)
        {
            consumer = threadID;
            return;
        }      
    }
    /**
     * returns element from queue.
     * @return object, null if empty.
     */
    public final Object getElemFromQ()
    {
        //logQueueStatus(GLOBAL_UTILS.getCurrentMethod());

        if(producer == -1)
        {
            // no point in receiving when producer is not registered yet.
            return null;
        }
        short tid = CRPThread.getCurrentThreadID();
        return getElemFromQ(tid);
        
    }

    /**
     * should only be used by a producer thread.
     * NOTE: USE IT CAREFULLY, MAY BE ONLY DURING TESTING,
     * AS IT CALLS THREAD.GETCURRENTTHREAD METHOD, WHICH IS
     * HORRIBLY XPENSIVE IN JAVA.
     * @param o object to be added to the Q.
     * @return true/false depending on success.
     */
    public final boolean addToQ(final Object o)
    {
    	short tid = CRPThread.getCurrentThreadID();
    	return(addToQ(o, tid));
    }
    
   

    /**
     * return next available queue index for producer.
     * @return variable representing new queue index
     */
    private final int getNextAvailableQForProducer()
    {
        long curVal = -1; 
        long newVal = -1;
        int tempIndex = -1;
        while(true)
        {
            curVal = status.get();
            tempIndex = (pQIndex + 1) % GLOBAL_CONSTANTS.LONG_SIZE;
            if(QueueStatus.isReadyForProducer(tempIndex, curVal))
            {
                newVal = QueueStatus.moveProducerToNextQ(
                    pQIndex, tempIndex, curVal);
                //wake up consumer if it is suspended.
                resumeConsumer(false);
            }
            else
            {
                if(pSpin)
                {
                    return -1;
                }
                else
                {
                    suspendProducer();
                    return -1;
                }
            }
            if(status.compareAndSet(curVal, newVal))
            {
                break;
            }         
        }
        //logQueueStatus(GLOBAL_UTILS.getCurrentMethod() + " : " + String.valueOf(tempIndex));

        resumeConsumer(false);

        return (tempIndex);
    }

    /**
     * return next available queue index for consumer.
     * @return variable representing new queue index
     */
    private final int getNextAvailableQForConsumer()
    {
        long curVal = -1; 
        long newVal = -1;
        int tempIndex = -1;
        
        while(true)
        {
            curVal = status.get();
            tempIndex = (cQIndex + 1) % GLOBAL_CONSTANTS.LONG_SIZE;
            if(QueueStatus.isReadyForConsumer(tempIndex, curVal))
            {
                newVal = QueueStatus.moveConsumerToNextQ(
                    cQIndex, tempIndex, curVal);
                //resume the producer in case it is suspended.
                resumeProducer();
            }
            else
            {
                if(cSpin)
                {
                    return -1;
                }
                else
                {
                    suspendConsumer();
                    return -1;
                }
            }
            if(status.compareAndSet(curVal, newVal))
            {
                break;
            }   
        }  
        resumeProducer();

        return (tempIndex);
    }
    
    /**
     * constructor.
     * @param inpSize size of each array to be used in the queue.
     */
    public LockFreeQ(final int inpSize, final boolean inpIsFullDuplex)
    {
        pQIndex = -1;
        cQIndex = -1;
        
        producer = consumer = -1;
        //set all the queue status to empty.
        status = new AtomicLong(0x0101010101010101L);
        waitCounter = new AtomicInteger(-1); // -1 indicates, wait for no one.
        queueGroup = new FixedSizeQueue[GLOBAL_CONSTANTS.LONG_SIZE];
        // initialize them.
        for(int i = 0; i < queueGroup.length; i++)
        {
            queueGroup[i] = new FixedSizeQueue();
            queueGroup[i].initialize(inpSize);
        }
        lfs = new LockFreeStack<Object>((short)GLOBAL_CONSTANTS.KB);
        isFullDuplex = inpIsFullDuplex;
    }

    /**
     * returns if this lfq supports full duplex.
     * @return true/false depending if full duplex or not.
     */
    public final boolean isFullDuplex()
    {
        return isFullDuplex;
    }
    /**
     * sets producer spinning flag.
     * if sets true, producer spins as opposed to wait
     * when queue(s) are full.
     * @param flag true/false.
     */
    public final void setProducerSpinning(final boolean flag)
    {
        pSpin = flag;
    }
    /**
     * sets consumer spinning flag.
     * when set to true, consumer spins as opposed to wait
     * when queue is empty.
     * @param flag true/false.
     */
    public final void setConsumerSpinning(final boolean flag)
    {
        cSpin = flag;
    }
    /**
     * should call when producer finished pumping data in.
     */
    public final void doneProducing()
    {
        if(pQIndex < 0)
        {
            //CommonLogger.CMN_LOG.warn(" LFQ: close called with negative pQIndex " );
            return;
        }
        //move the queue so that the consumer can
        //see the last data(if any)
        
        //this switches the queue, there by enabling the consumer
        // to pick the data.
        
        if(queueGroup[pQIndex].isEmpty())
        {
            // do nothing, as there are no entries in the queue.
            // no point in switching to a different queue.
            return;
        }
        int retVal = getNextAvailableQForProducer(); 
        //update only if the next queue is available.
        if(retVal >= 0)
        {
            pQIndex = retVal;
        }
        else
        {
            return;
        }    
    }
    /**
     * suspends producer.
     * usually happens when there is no space
     * for the producer to put the items on the queue.
     */
    public final void suspendProducer()
    {
        if(this.pSpin)
        {
            //do nothing if spinning is enabled.
            return;
        }
      
        do
        {
            // consumer is in suspended state.
            resumeConsumer(false);

        }
        while (!waitCounter.compareAndSet(-1, producer));
        try
        {
            CRPThreadPoolManager.getCRPThread(producer).suspendMe();
        }
        catch (CRPThreadException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
        }
        
        //reset the counter.
        do
        {
            ;
        } while(!waitCounter.compareAndSet(producer, -1));
    }
    
    /**
     * suspends consumer.
     * usually happens when there is no data
     * to consume.
     */
    public final void suspendConsumer()
    {
        if(this.cSpin)
        {
            //do nothing if spinning is enabled.
            return;
        }
        resumeProducer();
        if (CRPThread.getCurrentThreadID() == consumer)
        {
            try
            {
                CRPThreadPoolManager.getCRPThread(consumer).suspendMe();
            }
            catch (CRPThreadException e)
            {
                CommonLogger.CMN_LOG.error(e.getMessage());
            }
        }
    }
    
    /**
     * resumes producer.
     * if the producer thread is not suspended, then this method is no-op.
     */
    public final void resumeProducer()
    {

        if(pSpin)
        {
            // no need to resume.
            return;
        }
        if(producer == -1 || producer == GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID)
        {
            return;
        }
        //if (CRPThreadPoolManager.getCRPThread(
          //  producer).getState() == Thread.State.TIMED_WAITING)
        {
           
            CRPThreadPoolManager.getCRPThread(producer).resumeMe();
        }
    }  
    /**
     * resumes consumer.
     * @param force if true dont check for cSpin, always tries to
     * resume the thread.
     * if the consumer thread is not suspended, this method is no-op.
     */
    public final void resumeConsumer( final boolean force )
    {
        if( !force && cSpin)
        {
            // spinning, no need to wake up.
            return;
        }
        if(consumer == -1 || consumer == GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID)
        {
            // consumer not subscribed yet. ignore.
            return;
        }
      //  if (CRPThreadPoolManager.getCRPThread(
        //    consumer).getState() == Thread.State.TIMED_WAITING)
        {
           
            CRPThreadPoolManager.getCRPThread(consumer).resumeMe();
        }
    }
    
    /**
     * name tells all.
     */
    public final void unSubscribeProducer()
    {
        consumer = -1;
    }
    /**
     * name tells all.
     */
    public final void unSubscribeConsumer()
    {
        producer = -1;
    }
    /**
     * logs queue status value.
     */
    private final void logQueueStatus(String method)
    {
        
        String tempString = Long.toHexString(status.get());
        String statusString;
        if(tempString.length() < (GLOBAL_CONSTANTS.LONG_SIZE *2))
        {
            //ADD ZERO.
            statusString = "0" + tempString;
        }
        else
        {
            statusString = tempString;
        }
        //CommonLogger.CMN_LOG.debug("method: " + method + " queue status : " + statusString);
    }
    
    /**
     * convenience method to get around java currentthread problem.
     * @param o object to be sent across.
     * @param tid thread id calling this method.
     * @return true/false based on success/failure.
     */
    public final boolean addToQ(final Object o, final short tid) 
    {
    	
    	subscribeToQueue(tid);
        
        if(isFullDuplex && tid  == consumer)
        {
            try
            {
                lfs.push(o);
            }
            catch(CRPException e)
            {
                return false;
            }
            resumeProducer();
            return true;
        }
        if(pQIndex == -1)
        {
            //first time ever..deal with it.
            int retVal = getNextAvailableQForProducer(); 
            //update only if the next queue is available.
            if(retVal >= 0)
            {
                pQIndex = retVal;
            }
            else
            {
                return false;
            }    
        }
        else
        {
            if(queueGroup[pQIndex].isFull())
            {
                //update only if the next queue is available.
                int retVal = getNextAvailableQForProducer(); 
                if(retVal >= 0)
                {
                    pQIndex = retVal;
                }
                else
                {
                    return false;
                }
            }
        }
        assert(pQIndex >= 0 && pQIndex < GLOBAL_CONSTANTS.LONG_SIZE);
        //logQueueStatus(GLOBAL_UTILS.getCurrentMethod());

        // now call the appropriate queue add.
        boolean flag = queueGroup[pQIndex].addToQ(o);
        if( tid == producer )
        {
            resumeConsumer(false);
        }
        else
        {
            resumeProducer();
        }
        return (flag);

    }

    /**
     * convenience method to get around thread.getCurrentThread problem.
     * @param tid caller's thread id.
     * @return object if success, otherwise null.
     */
    public final Object getElemFromQ(final short tid)
    {
    	subscribeToQueue(tid);    
        Object o = null;
        if(isFullDuplex && tid == producer)
        {
            try
            {
                o = lfs.pop();
            }
            catch(CRPException e)
            {
                return null;
            }
            return o;
        }
        if(cQIndex == -1)
        {
            //first time ever..deal with it.
            int retVal = getNextAvailableQForConsumer();
            if(retVal >= 0)
            {
                cQIndex = retVal;    

            }
            else
            {
                return null;
            } 
        }
        else
        {
            if(queueGroup[cQIndex].isEmpty())
            {
                int retVal = getNextAvailableQForConsumer();
                if(retVal >= 0)
                {
                    cQIndex = retVal;    

                }
                else
                {
                    return null;
                }
            }
        }
        assert(cQIndex >= 0 && cQIndex < GLOBAL_CONSTANTS.LONG_SIZE);
        // now call the appropriate queue add.
        return (queueGroup[cQIndex].getElemFromQ());
    }
    
    /**
     * resets the lfq.
     * resets producer and consumer to -1.
     */
    public final void reset()
    {
        producer = -1;
        consumer = -1;
        
        pQIndex = -1;
        cQIndex = -1;
        
        //set all the queue status to empty.
        long oldVal = status.get();
        do
        {
            oldVal = status.get();
        } while(!status.compareAndSet(oldVal, 0x0101010101010101L));
    }
    
    /**
     * returns true if there is pending data in the queue.
     * @return true/false
     */
    public final boolean anyPendingData()
    {
        if(lfs.getTop() != null)
        {
            return true;
        }
        if(cQIndex != -1 
                && ((cQIndex+1) % GLOBAL_CONSTANTS.LONG_SIZE != pQIndex))
        {
            return true;
        }
        if(cQIndex != -1 && !queueGroup[cQIndex].isEmpty())
        {
            return true;
        }
        
        return false;
    }
   
    
    /**
     * set of constants to represent the status.
     * this is just for readability of the code.
     * eventually they are all represented in a long variable.
     *
     */
    public static class QueueStatus
    {
        /**
         * general status.
         */
        public static final long UNINIT = -1;
        /**
         * indicates a empty q.
         */
        public static final long EMPTY = 1;
        /**
         * producer related status.
         */
        /**
         * if producer is using.
         */
        public static final long PRODUCER_USING = 2;
        /**
         * producer finished writing.
         */
        public static final long PRODUCER_FIN = 4;
        
        /**
         * consumer status.
         */
        public static final long CONSUMER_USING = 16;
        /**
         * consumer finished reading.
         */
        public static final long CONSUMER_FIN = 32;
        
        /**
         * status mark for producer, consumer status.
         */
        private static long[]    StatusMask = new long[]{
            0x00000000000000ffL, 0x000000000000ff00L, 0x0000000000ff0000L,
            0x00000000ff000000L, 0x000000ff00000000L, 0x0000ff0000000000L,
            0x00ff000000000000L, 0xff00000000000000L,       };
        
        /**
         * status unmask.
         */
        private static long[]    StatusClear = new long[]{
            0xffffffffffffff00L, 0xffffffffffff00ffL, 0xffffffffff00ffffL,
            0xffffffff00ffffffL, 0xffffff00ffffffffL, 0xffff00ffffffffffL,
            0xff00ffffffffffffL, 0x00ffffffffffffffL,       };
        
        /**
         * some ready made masks.
         */
        
        public static final long PRODUCER_READY_MASK = EMPTY | CONSUMER_FIN;
        
        /**
         * consumer ready mask.
         */
        public static final long CONSUMER_READY_MASK = PRODUCER_FIN;
        
        /**
         * checks to see if the array index can be used by the producer.
         * @param index array index for which status is being checked.
         * @param status status field to extract bit flags from.
         * @return true if ready or false.
         */
        public static final boolean isReadyForProducer(
            final int index, final long status)
        {
            assert(index < GLOBAL_CONSTANTS.LONG_SIZE);
            
            long tempVal = status & StatusMask[index];
            
            if( (tempVal & (PRODUCER_READY_MASK << (8 * index))) > 0 )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        /**
         * checks to see if the array index can be used by the consumer.
         * @param index array index for which status is being checked.
         * @param status status field to extract bit flags from.
         * @return true if ready or false.
         */
        public static final boolean isReadyForConsumer(
            final int index, final long status)
        {
            assert(index < GLOBAL_CONSTANTS.LONG_SIZE);
            
            long tempVal = status & StatusMask[index];
            
            if( (tempVal & (CONSUMER_READY_MASK << (8 * index))) > 0 )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        /**
         * switch producer from the current queue to next available queue.
         * @param oldIndex the current queue index.
         * @param tempIndex next queue index, where producer will write into.
         * @param status status variable
         * @return updated status variable.
         */
        public static long moveProducerToNextQ(
            final int oldIndex, final int tempIndex,final long status)
        {
            long updStatus = status;
            
            if(oldIndex >= 0)
            {
                //update status only if the old index > 0,
                // ignore -1 case here(very first).
                // clear the bits first for this Q.
                updStatus = status & StatusClear[oldIndex];
                
                updStatus = updStatus | (
                    PRODUCER_FIN << (oldIndex * GLOBAL_CONSTANTS.LONG_SIZE));
            }
            //clear all the bits for this queue.
            updStatus = updStatus & StatusClear[tempIndex];
            updStatus = updStatus | (
                    PRODUCER_USING << (tempIndex * GLOBAL_CONSTANTS.LONG_SIZE));
            
            return updStatus;
        }
        
        /**
         * switch consumer from the current queue to next available queue.
         * @param oldIndex the current queue index.
         * @param tempIndex next queue index, where producer will write into.
         * @param status status variable
         * @return updated status variable.
         */
        public static long moveConsumerToNextQ(
            final int oldIndex, final int tempIndex,final long status)
        {
            
            long updStatus = status;
            if(oldIndex >= 0)
            {
                //update status only if the old index > 0,
                // ignore -1 case here(very first).
                // clear the bits first for this Q.
                updStatus = status & StatusClear[oldIndex];
                
                updStatus = updStatus | (
                    CONSUMER_FIN << (oldIndex * GLOBAL_CONSTANTS.LONG_SIZE));
            }
            //clear all the bits for this queue
            updStatus = updStatus & StatusClear[tempIndex];
            updStatus = updStatus | (
                    CONSUMER_USING << (tempIndex * GLOBAL_CONSTANTS.LONG_SIZE));
            //System.out.println(" consumer moved to queue: " + String.valueOf(tempIndex));
            if(updStatus == 0xffffffffffffffffL)
            {
                int i = 0;
                i++;
            }
            return updStatus;
        }
       
    }

  
	
}

