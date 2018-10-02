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
 * context class to keep the context between two msgs in stream.
 * crp is a message passing architecture, so it is important to
 * retain the context between two successive messages during a stream.
 * this class acts like a place holder to keep the objects that are
 * needed to be alive between two successive messages in a stream.
 * usually we should attach a context to a photon.
 * @author hpoduri
 * @version $Id$
 */
public class CRPContext
{
    /**
     * description about this context.
     */
    private String desc;
    
    /**
     * object array.
     * it is the callers responsibility to remember the
     * index to type of object.
     */
    private ObjectDetails [] objArray;
    
    /**
     * current index in the object array.
     */
    private int index;
    
    /**
     * default constructor.
     * @param inpDesc description about this context.
     */
    public CRPContext(final String inpDesc)
    {
        // for now allow only 8 objects.
        objArray = new ObjectDetails[GLOBAL_CONSTANTS.LONG_SIZE];
        for(int i = 0; i < GLOBAL_CONSTANTS.LONG_SIZE; i++)
        {
            objArray[i] = null;
        }
        desc = inpDesc; 
        index = 0;
    }
    
    /**
     * returns description about this context.
     * @return string obj.
     */
    public final String description()
    {
        return desc;
    }
    /**
     * add object to the object array.
     * @param o object to be added.
     * @param inpDesc description of the object.
     * @return index in the object array.
     */
    public final int addObject(final CRPContextObjectInterface o, 
        final String inpDesc)
    {
        objArray[index]  = new ObjectDetails();
        objArray[index].o = o;
        objArray[index].desc = inpDesc;
        index++;
        return index;
    }
    
    /**
     * returns object at a given index.
     * @param inpInd input index.
     * @return object, or null if invalid index.
     */
    public final ObjectDetails getObjectAtIndex(final int inpInd)
    {
        if(inpInd < 0 && inpInd > index)
        {
            CommonLogger.CMN_LOG.error(
                " Invalid Index in the CRPContext Object Array");
            return null;
        }
        return(objArray[inpInd]);
    }
    
    /**
     * returns current number of objs in this ctx.
     * @return num of objects(int).
     */
    public final int numOfObjects()
    {
        
        return index;
    }
    
    /**
     * object details.
     *
     */
    public class ObjectDetails
    {
        /**
         * object to store.
         */
        public CRPContextObjectInterface o;
        /**
         * description about the object.
         */
        public String desc;
    }
}
