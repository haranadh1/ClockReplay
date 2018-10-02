/**
 *
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of ClockReplay Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or
 * copyright law.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from ClockReplay Incorporated.
 */

package com.crp.memmgr;

import java.util.Vector;

import com.crp.common.CommonLogger;
import com.crp.interfaces.AbstractInterfaceBase;

/**
 * "generics" interface for an object pool
 * @author vivekt
 *
 * @param <T>
 */
interface CRPObjectPool<T>
{
    /**
     * creates object.
     * @return object from the pool
     */
    T getObject();
    /**
     * resets all the objects in the pool.
     */
    void resetPool();
}

/**
 * class to represent MemoryBlockObjectPool.
 * objects are allocated from this pool.
 * @param <T> object type to be created.
 * @author hpoduri
 * @author vivekt
 * @version $Id$
 */
public class MemoryBlockObjectPool
    <T> extends MemoryBlock implements CRPObjectPool<T>
{
	/**
	 * helper memory block byte buffer used for the variable length fields
	 * inside the object pool class.
	 */
    private MemoryBlockByteBuffer mbb;
    
    private Vector<T>     objList;
    private int           objIndex;
    private ObjectFactory objFactory;
    /**
     * constructor.
     * @param inpBlockSize ref super class constructor.
     * @param of is the object factory to be used for allocation and other type
     * specific operations
     * @param inpDesc small note for this mbo.
     * var length data field types.
     * @throws MemoryManagerException on error.
     */
    public MemoryBlockObjectPool(final int inpBlockSize,final ObjectFactory of,
        final String inpDesc) 
        throws MemoryManagerException
    {
    	// round off the block size to an exact
    	// multiple of the object size
        super(inpBlockSize, inpDesc);
        if(getStatus() != MemoryBlockStatus.INIT 
                && getStatus() != MemoryBlockStatus.WRITING)
        {
            throw new MemoryManagerException("CRP_MEMMGR_ERROR_009",
                new String [] {String.valueOf(getStatus())});
        }
        objList = new Vector<T>();
        objIndex = 0;
        objFactory = of;
        mbb = null;
    }
    
    /**
     * initialization necessary to create the object references.
     * @param inpMBB input mbb.
     * @throws MemoryManagerException on error.
     */
    public final void init(
        final MemoryBlockByteBuffer inpMBB) throws MemoryManagerException
    {
        mbb = inpMBB;
        if(objList.size() > 0)
        {
            // already initialized before; can be a freed block being reallocated.
            this.reset();
            if(inpMBB != null)
            {
                for(int i = 0; i < objList.size(); i++)
                {
                    ObjectFactory Obj =  (ObjectFactory)objList.get(i);
                    Obj.reset(inpMBB);
                }
            }
        }
        try
        {
            while (true)
            {
                if (remainingSpace() < objFactory.getObjSize())
                {
                    break;
                }   
                objList.add((T) objFactory.createObjectInstance(mbb));  
                finishWritingRecord(objFactory.getObjSize());
            }
        }
        catch (OutOfMemoryError e)
        {
            String[] args = new String[1];
            args[0] = String.valueOf(getSize());
            MemoryManager.MEM_LOG.error("OutofMemory Error " + e.getMessage());
            throw (new MemoryManagerException("CRP_MEMMGR_ERROR_001", args));
        }
        // reset the offset, as we have not created any real objects yet.
        resetOffset();
    }
    /**
     * returns object factory.
     * @return of
     */
    public final ObjectFactory getObjectFactory()
    {
        return objFactory;
    }
    /**
     * create an object of type 'T', this just returns a reference from the
     * existing pool of objects.
     * @return returns object from pool. 
     */
    public final T getObject()
    {
        //CommonLogger.CMN_LOG.info(" name: " + objFactory.getMyName() + " obj index: " + String.valueOf(objIndex) + " obj List size : " + String.valueOf(objList.size()));
        if (objIndex >= objList.size())
        {
            return null;
        }
        T instance = objList.elementAt(objIndex++);
        
       
        AbstractInterfaceBase ibo = null;
        try
        {
            ibo = (AbstractInterfaceBase) instance;
        }
        catch (ClassCastException e)
        {
            MemoryManager.MEM_LOG.fatal(
                "Cast Error: Object Pool classes should extend InterfaceBase class");
            return null;
        }
        if(instance == null)
        {
            CommonLogger.CMN_LOG.info(" object instance is null: " + ibo.getMyName());
        }
        assert(ibo != null);
        ibo.setMemoryBlocks(mbb, this);
        ibo.reset();
        return instance;
    }
    /**
     * resets all the objects in the pool.
     */
    public final void resetPool()
    {
        for (int i = 0; i < objList.size(); i++)
        {
            objFactory.resetObject(objList.elementAt(i));
        }
        objIndex = 0;
        if(mbb != null)
        {
            mbb.resetOffset();
            mbb.resetLocalOffset();
        }
    }

    /**
     * reset MemBlock.
     */
    public final void reset()
    {
        super.resetOffset();
        resetPool();
    }
    /**
     * get the number of objects in the pool.
     * @return returns the number of objects in the pool.
     */
    public final int getNumOfObjects()
    {
        return objList.size();
    }
    /**
     * assign mbb for this memory block.
     * @param inpMBB input memory block byte buffer.
     */
    public final void setMBB(final MemoryBlockByteBuffer inpMBB)
    {
        mbb = inpMBB;
    }
    /**
     * returns active number of objects.
     * meaning objects currently being used out of
     * getNumOfObjects initially created.
     * @return number of active objects.
     */
    public final int getActiveObjects()
    {
        return objIndex;
    }
    /**
     * return the element at index.
     * no side effects.
     * @param ind index at which the object to be returned.
     * @return returns the object.
     */
    public final T getObjectAtIndex(final int ind)
    {
        T instance = objList.elementAt(ind);
        AbstractInterfaceBase ibo = null;
        try
        {
            ibo = (AbstractInterfaceBase) instance;
        }
        catch (ClassCastException e)
        {
            MemoryManager.MEM_LOG.fatal(
                "Cast Error: Object Pool classes should extend InterfaceBase class");
            return null;
        }
        ibo.setMemoryBlocks(mbb, this);
        return instance;
    }

    @Override
    public void doBeforeFinishWritingRecord(final int size)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void doBeforeStartWritingRecord(final int size)
        throws MemoryManagerException
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * returns mbb for this memory object pool block.
     * @return mbb.
     */
    public final MemoryBlockByteBuffer getMBB()
    {
        return mbb;
    }

    @Override
    public void doBeforeFinishReadingRecord(int size)
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * returns name of the object stored.
     * @return string.
     */
    public final String getObjectName()
    {
        return objFactory.getMyName();
    }
}
