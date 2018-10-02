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

/**
 * fixed size queue.
 * NOTE that this queue is not thread safe.
 * q is represented as array of generic objects.
 * @author hpoduri
 * @version $Id$
 */
public class FixedSizeQueue
{
    /**
     * array representation of objects.
     */
    private Object [] array;
    
    /**
     * represents the front pointer to the queue.
     * insertions always happen at front.
     */
    private int front;
    
    /**
     * represents the current number of elems in Q.
     * deletions happen using count.
     */
    private int count;
    
    /**
     * size of the queue.
     */
    private int size;
    
    
    /**
     * constructor.
     * represents the number of elements in the array.
     */
    public FixedSizeQueue()
    {  
        front = 0;
        count = 0;
        size = -1;
    }
    
    /**
     * initialize the array.
     * @param inpSize total size of the array.
     * represents the number of elements in the array.
     *
     */
    public final void initialize(final int inpSize)
    {
        size = inpSize;
        array = new Object[size];
        for(int i = 0; i < size; i++)
        {
            array[i] = new Object();
        }
        front = 0;
        count = 0;
    }
    /**
     * add object to q.
     * @param o input object.
     * @return true/false
     */
    public final boolean addToQ(final Object o)
    {
        assert(front >= 0 && front < size);
        assert(count <= size);
        
        if( !isFull())
        {
            array[(front + count) % size] = o;
            count++;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * retrieve element from queue.
     * @return null if q has zero elements.
     */
    public final Object getElemFromQ()
    {
        Object o = null;
        if(isEmpty())
        {
            return null;
        }
        else
        {
            o = array[front];
            count--;
            front = (front + 1) % size;
            return o;
        }
    }
    
    /**
     * returns cur num of elements in q.
     * @return num of elems.
     */
    public final int getNumOfElemsinQ()
    {
        return count;
    }

    /**
     * checks to see if the queue is full.
     * we can add different fill factors here.
     * @return true if full, false otherwise.
     */
    public final boolean isFull()
    {
        if(count == size)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    /**
     * checks to see if queue is empty.
     * @return true if empty, false otherwise.
     */
    public final boolean isEmpty()
    {
        if (count == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
