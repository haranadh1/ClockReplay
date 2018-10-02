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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crp.thread.CRPThread;

/**
 * class to store all the contexts for a thread.
 * @author hpoduri
 * @version $Id$
 */
public class CRPContextGroup
{
    /**
     * variable to synchronize read/write operations.
     */
    private AtomicInteger cgStatus;
    
    /**
     * list of context objects.
     */
    private CRPContext [] crpCtxList;

    /**
     * counter for contexts in the crpCtxList.
     */
    private int ctxCounter;
    
    /**
     * constructor.
     */
    public CRPContextGroup()
    {
        crpCtxList = new CRPContext[GLOBAL_CONSTANTS.LONG_SIZE];
        cgStatus = new AtomicInteger(CRPContextReadWriteStatus.FREE);
        ctxCounter = 0;
    }
    
    /**
     * create a new context.
     * @param inpDesc description about this context.
     * @return CRPContext variable.
     * @throws CRPException on error.
     */
    public final CRPContext createContext(
        final String inpDesc) throws CRPException
    {
        CRPContext crpCtx = new CRPContext(inpDesc);
        
        int oldVal = cgStatus.get();
        int newVal = CRPContextReadWriteStatus.WRITING;
        do
        {
            if(oldVal == CRPContextReadWriteStatus.READING)
            {
                Thread.yield();
                continue;
            }
            if(oldVal == CRPContextReadWriteStatus.WRITING)
            {
                // another write pending..does not make sense.
                throw new CRPException("CRP_COMMON_ERROR_007", null);
            }             
        } while (!cgStatus.compareAndSet(oldVal, newVal));
        // look for the slot.
        for(int i = 0; i < crpCtxList.length; i++)
        {
            if(crpCtxList[i] == null)
            {
                crpCtxList[i] = crpCtx;
                break;
            }
        }
        ctxCounter++;
        oldVal = cgStatus.getAndSet(CRPContextReadWriteStatus.FREE);
        assert(oldVal == CRPContextReadWriteStatus.WRITING);
        return crpCtx;
    }
    
    /**
     * returns json representation of this class.
     * @return json object.
     * @throws CRPException on error.
     */
    public final JSONObject toJSON() throws CRPException
    {
        int oldVal = cgStatus.get();
        int newVal = CRPContextReadWriteStatus.READING;
        do
        {
            if(oldVal == CRPContextReadWriteStatus.WRITING)
            {
                Thread.yield();
                continue;
            }
            if(oldVal == CRPContextReadWriteStatus.READING)
            {
                continue;
            }             
        } while (!cgStatus.compareAndSet(oldVal, newVal));
        JSONArray  ctxArray = new JSONArray();
        JSONObject ret = new JSONObject();
        try
        {
            for (int i = 0; i < ctxCounter; i++)
            {
                JSONObject desc = new JSONObject();

                if (crpCtxList[i] != null)
                {
                    desc.put(
                        JSON_KEYS.JsonKeyStrings.JSON_CTXT_DESC,
                        crpCtxList[i].description());
                }
                JSONArray ja = new JSONArray();
                for (int j = 0; j < crpCtxList[i].numOfObjects(); j++)
                {
                    JSONObject jo1 = new JSONObject().put(crpCtxList[i]
                        .getObjectAtIndex(j).desc, crpCtxList[i]
                        .getObjectAtIndex(j).o.toString());
                    ja.put(jo1);
                }
                JSONObject agg = new JSONObject().put(
                    JSON_KEYS.JsonKeyStrings.JSON_CTXT_DETAILS,
                    new JSONArray().put(desc).put(
                        new JSONObject().put(
                            JSON_KEYS.JsonKeyStrings.JSON_OBJECT_DETAILS,
                            ja)));

                ctxArray.put(new JSONObject().put(String.valueOf(i), agg));
            }
            ret.put(JSON_KEYS.JsonKeyStrings.JSON_CTXT_LIST, ctxArray);
        }
        catch (JSONException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            throw new CRPException(
                "CRP_COMMON_ERROR_008",
                new String[]{CRPThread.getCurrentThread().getName()});
        }
        oldVal = cgStatus.getAndSet(CRPContextReadWriteStatus.FREE);
        assert(oldVal == CRPContextReadWriteStatus.READING);
        return (ret);
    }
    
    /**
     * adds object to a given crp context.
     * it is necessary to use this interface instead of directly
     * calling addobject from crp context. this makes the read/write
     * operations on the crp context class thread safe.
     * @param ctx context to which the object to be added.
     * @param o object to be added.
     * @param inpDesc description about the object to be added.
     * @throws CRPException on error.
     */
    public final void addObject(final CRPContext ctx,
        final CRPContextObjectInterface o,
        final String inpDesc) throws CRPException
    {
        int oldVal = cgStatus.get();
        int newVal = CRPContextReadWriteStatus.WRITING;
        do
        {
            if(oldVal == CRPContextReadWriteStatus.READING)
            {
                Thread.yield();
                continue;
            }
            if(oldVal == CRPContextReadWriteStatus.WRITING)
            {
                // another write pending..does not make sense.
                throw new CRPException("CRP_COMMON_ERROR_007", null);
            }             
        } while (!cgStatus.compareAndSet(oldVal, newVal));
        ctx.addObject(o, inpDesc);
        oldVal = cgStatus.getAndSet(CRPContextReadWriteStatus.FREE);
        assert(oldVal == CRPContextReadWriteStatus.WRITING);
        
    }
    /**
     * returns context at a given index.
     * @param ind ind at context is needed.
     * @return crp context object.
     */
    public final CRPContext getContextAtIndex(final int ind)
    {
        return (crpCtxList[ind]);
    }
    /**
     * returns true if empty.
     * @return true/false.
     */
    public final boolean isEmpty()
    {
        if(ctxCounter == 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * deletes the context at a given index.
     * @param i index.
     */
    public final void deleteContext(final int i)
    {
        int oldVal = cgStatus.get();
        int newVal = CRPContextReadWriteStatus.WRITING;
        do
        {
            if(oldVal != CRPContextReadWriteStatus.FREE)
            {
                Thread.yield();
                continue;
            }
                    
        } while (!cgStatus.compareAndSet(oldVal, newVal));
        crpCtxList[i] = null;
        ctxCounter--;
        
        oldVal = cgStatus.getAndSet(CRPContextReadWriteStatus.FREE);
        assert(oldVal == CRPContextReadWriteStatus.WRITING);
        
    }
    
    /**
     *represent different possible values for cgStatus.
     */
    public interface CRPContextReadWriteStatus
    {
        /**
         * represents free state.
         * reader/writer are allowed to make change
         * to this variable, only if it is in FREE state, otherwise
         * should wait.
         */
        public static final int FREE = 0;
        /**
         * reader lock.
         */
        public static final int READING = 1;
        /**
         * writer lock.
         */
        public static final int WRITING = 2;
    }

    
}
