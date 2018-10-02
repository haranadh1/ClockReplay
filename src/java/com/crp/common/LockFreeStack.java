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

import java.util.concurrent.atomic.AtomicReference;


/**
 * class that Implements lock free stack.
 * for now, only supports standard java objects like
 * (int, short, String) that does not require a new for
 * instantiation.
 * It is implemented using a simple atomic top variable.
 * only top is atomically updated.
 * @author hpoduri
 * @version $Id$
 * @param <E> object type, that you want to store in stack.
 */
public class LockFreeStack<E>
{
    /**
     * atomic variable to make the stack access lock-free.
     */
    private AtomicReference<Item<E>> top;
    /**
     * top of the stack.
     */
    private short size;
    
    /**
     * the instantiation of the class not thread safe.
     * @param inpSize ; max size of the stack.
     */
    public LockFreeStack (final short inpSize)
    {
        assert(inpSize > 0);
        size = inpSize;
        top = new AtomicReference<Item<E>>(null);
    }
    /**
     * atomic push on the stack.
     * @param inpElem element to be inserted on top of the stack.
     * @throws CRPException when stack overflows.
     */
    public final void push (final E inpElem) throws CRPException
    {
        Item<E> newVal = new Item<E>();
        newVal.data = inpElem;
        Item<E> oldVal = null;
        do 
        {
            oldVal = top.get();
            newVal.next = oldVal;
        } while (!top.compareAndSet(oldVal, newVal)); 
    }
    /**
     * atomic pop from the stack.
     * @return returns the current element top of the stack.
     * @throws CRPException when no elements in stack.
     */
    public final E pop() throws CRPException
    {
        Item<E> newVal = null;
        Item<E> oldVal = null;
        do
        {

            oldVal = top.get();
            if (oldVal == null)
            {
                // No elements in stack.
                throw new CRPException("CRP_COMMON_ERROR_002", null);
            }
            else
            {
                newVal = oldVal.next;
            }
            
        }
        while (!top.compareAndSet(oldVal, newVal));
        
        return oldVal.data;
    }
    /**
     * returns the max size of the stack.
     * @return size.
     */
    public final short getMaxSize()
    {
        return size;
    }
    /**
     * return the string representation of the class.
     * @return string rep of class.
     */
    public final String zipString()
    {
        StringBuilder sb = new StringBuilder(
            GLOBAL_CONSTANTS.MEDIUM_STRING_SIZE);
        Item<E> it = top.get();
        while(it!=null)
        {
            it = it.next;
            sb.append(" memmgr index: " );
            if (it != null)
            {
                sb.append(String.valueOf(it.data));
            }
        }
        return sb.toString();
    }
    /**
     * return the input array as is. 
     * this should only be used as read only.
     * @return array.
     */
    public final Item<E> getTop()
    {
        
        return top.get();
    }
    /**
     * represents element in the stack.
     * @param <E> data type.
     */
    public static class Item<E>
    {
        /**
         * represents the next element in the stack.
         */
        public Item<E> next;
        /**
         * represents the data of the element in stack.
         */
        public E data;
    }
}
