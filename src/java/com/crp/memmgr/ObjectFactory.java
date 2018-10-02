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
package com.crp.memmgr;

import org.apache.log4j.Category;

import com.crp.common.CRPLogger;

/**
 * interface for an object factory. Implementations of this interface would
 * need to provide type specific logic for these operations.
 * @author vivekt
 *
 */
public interface ObjectFactory
{
    /**
     * create a separate logger for MessageWorld.
     */
    public static final Category OF_LOG = CRPLogger
                                             .initializeLogger("com.crp.memmgr.ObjectFactory");
    
    /**
     * create a new object instance.
     * @param mbb input MemoryBlockByteBuffer, to be used for var length fields.
     * @return returns created object.
     */
    public Object createObjectInstance(final MemoryBlockByteBuffer mbb);

    /**
     * resets the current object with the input object.
     * @param obj object to be reset with.
     */
    public void resetObject(Object obj);

    /**
     * resets the current object.
     */
    public void reset();
    
    /**
     * reset with memory block byte buffer.
     */
    public void reset(MemoryBlockByteBuffer inpMBB);
    
    /**
     * returns object size.
     * @return size of this object.
     */
    public int getObjSize();
    
    /**
     * this is how this object pool is registered.
     * should return a string representing the object
     * name.
     * NOTE: string should not contain spaces or any other white chars.
     * @return name of the class.
     */
    public String getMyName();
    
    /**
     * represents if this class contains any variable fields.
     * it is necessary to know this information; if it does have
     * variable fields, we have to instantiate the memory block byte buffer
     * for the class and attach it to memory block object pool.
     * @return true if the class has any var length fields or false.
     */
    public boolean anyVarFields();
    
    /**
     * returns variable length fileds size in this object.
     * @return integer, represents var fields length.
     * -1 for an object dont have var length fields.
     */
    public int getVarLenFieldsSize(); 
    /**
     * to know if a specified var length object can fit in this object.
     * only works for var length object type.
     * should check if the mbb of this object can store the var length
     * specified.
     * @param length length needed in the mbb of this object.
     * @return true if it can fit, otherwise false.
     */
    public boolean anyRoomForObjOfVarLength(final int length);
}
