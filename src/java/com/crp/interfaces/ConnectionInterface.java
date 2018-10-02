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

package com.crp.interfaces;

import java.io.IOException;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_ENUMS.SERVICE_CATALOG;
import com.crp.ether.Message;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * class for connection interface.
 * this is the first object sent over the wire
 * for the connection establishment.
 * @author hpoduri
 * @version $Id$
 */
public class ConnectionInterface extends AbstractInterfaceBase
{

    /**
     * source service.
     */
    private SERVICE_CATALOG  srcService;
    /**
     * dest service.
     */
    private SERVICE_CATALOG destService;
    
    /**
     * source thread id.
     */
    private short srcThrdId;
    
    /**
     * dest thread id.
     */
    private short destThrdId;
    
    /**
     * default constructor.
     */
    public ConnectionInterface()
    {
        srcService = GLOBAL_ENUMS.SERVICE_CATALOG.INVALID_HANDLER_SERVICE;
        destService = GLOBAL_ENUMS.SERVICE_CATALOG.INVALID_HANDLER_SERVICE;
        srcThrdId = GLOBAL_CONSTANTS.INVALID_THREAD_ID;
        destThrdId = GLOBAL_CONSTANTS.INVALID_THREAD_ID;
    }
    /**
     * returns source service.
     * @return enum service.
     */
    public final SERVICE_CATALOG getSrcService()
    {
        return srcService;
    }
    /**
     * returns dest service.
     * @return enum value.
     */
    public final SERVICE_CATALOG getDestService()
    {
        return destService;
    }
    
    /**
     * set services.
     * @param inpSrc source service.
     * @param destSrc dest service.
     */
    public final void setSrcAndDestServices(final SERVICE_CATALOG inpSrc,
        final SERVICE_CATALOG destSrc)
    {
        srcService = inpSrc;
        destService = destSrc;
    }
    
    /**
     * set thread ids. 
     * @param inpSrc src thread id.
     * @param inpDest dest thread id.
     */
    public final void setSrcAndDestThreads(final short inpSrc,
        final short inpDest)
    {
        srcThrdId = inpSrc;
        destThrdId = inpDest;
    }
    
    @Override
    public Object createObjectInstance(MemoryBlockByteBuffer mbb)
    {   
        return (new ConnectionInterface());
    }

    @Override
    public String getMyName()
    {
        return GLOBAL_CONSTANTS.CRPObjNameStrings.CONNECTION_KEY;
    }

    @Override
    public int getObjSize()
    {        
        return GLOBAL_CONSTANTS.INT_SIZE*2;
    }

    @Override
    public void pack(final Packer pk) throws PackUnPackException
    {
        try
        {
            pk.packFixedInt32(srcService.ordinal());
            pk.packFixedInt32(destService.ordinal());
        }
        catch (IOException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw new PackUnPackException(
                "CRP_PKUNPK_ERROR_006", null, pk.toString());
        }

    }

    @Override
    public void resetObject(Object obj)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void unpack(final UnPacker unp) throws PackUnPackException
    {
        try
        {
            int intVal = unp.unpackFixedInt32();
            
            if((intVal > SERVICE_CATALOG.MAX_HANDLER_SERVICE.ordinal()) 
                    || (
                    intVal < SERVICE_CATALOG.INVALID_HANDLER_SERVICE.ordinal()))
            {
                UnPacker.UNP_LOG.error(
                    "Invalid value for enum : " + String.valueOf(intVal));
                throw new PackUnPackException ("CRP_PKUNPK_ERROR_005", null,
                    getMyName());
            }
            this.srcService = SERVICE_CATALOG.getEnumFromInt(intVal);
            intVal = unp.unpackFixedInt32();
            if((intVal > SERVICE_CATALOG.MAX_HANDLER_SERVICE.ordinal()) 
                    || (
                    intVal < SERVICE_CATALOG.INVALID_HANDLER_SERVICE.ordinal()))
            {
                UnPacker.UNP_LOG.error(
                    "Invalid value for enum : " + String.valueOf(intVal));
                throw new PackUnPackException ("CRP_PKUNPK_ERROR_005", null,
                    getMyName());
            }
            this.destService = SERVICE_CATALOG.getEnumFromInt(intVal);
               
        }
        catch (IOException e)
        {
            UnPacker.UNP_LOG.error(e.getMessage());
            throw new PackUnPackException ("CRP_PKUNPK_ERROR_005", null,
                getMyName());
        }

    }
    @Override
    public boolean anyVarFields()
    {
        return false;
    }

    /**
     * returns source thread id.
     * @return short.
     */
    public final short getSrcThrdID()
    {
        return srcThrdId;
    }
    @Override
    public int getPackedBufferSize()
    {
        return GLOBAL_CONSTANTS.INT_SIZE*2;
    }
    @Override
    public boolean anyRoomForObjOfVarLength(int length)
    {
        
        return false;
    }
    @Override
    public int getVarLenFieldsSize()
    {
        
        return -1;
    }
    @Override
    public void reset()
    {
        srcService = GLOBAL_ENUMS.SERVICE_CATALOG.INVALID_HANDLER_SERVICE;
        destService = GLOBAL_ENUMS.SERVICE_CATALOG.INVALID_HANDLER_SERVICE;
        
    }
    @Override
    public void reset(MemoryBlockByteBuffer inpMBB)
    {
        // TODO Auto-generated method stub
        
    }
}
