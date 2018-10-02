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
package com.crp.common.tests;

import java.util.Random;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.ether.MessageWorld;
import com.crp.interfaces.Caplet;
import com.crp.interfaces.TestCaplet;
import com.crp.memmgr.MemoryBlockObjectPool;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.memmgr.MemoryManager.MemoryBlockType;
import com.crp.thread.CRPThread;

/**
 * generates random data caplets.
 * @author hpoduri
 */
public class TestCapletGenerator
{

    private  MemoryBlockObjectPool<?> capMBO = null;
    
    public final int getSize()
    {
        return capMBO.getNumOfObjects();
    }
    public final void init(int maxCaplets)
    {
        ObjectFactory of = new Caplet(null);
        
        int inpBlockSize = maxCaplets * of.getObjSize();
        try
        {
            // create memory block for the object
            
            capMBO = MemoryManager.createObjectPoolMemoryBlock(
                CRPThread.getCurrentThreadID(),
                maxCaplets,
                20 * GLOBAL_CONSTANTS.MB,
                MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL,
                of, of.anyVarFields());
        }
        catch (MemoryManagerException e)
        {
            MessageWorld.MSG_WORLD_LOG.error(e.getMessage());
            
        }
        
    }
    public final Caplet getNextCaplet()
    {
        Caplet c = (Caplet) capMBO.getObject();
        if (c == null)
            return null;
        
        if (c.setAttr(
            generateString(
                generateInt(GLOBAL_CONSTANTS.HOST_SIZE - 1)+1).toString(),
            generatePort(), generateIP(),
            System.currentTimeMillis(), generateString(generateInt(GLOBAL_CONSTANTS.MB -1 )+1)))
        {
            return c;
        }
        else
        {
            return null;
        }
        
    }
    
    public static final TestCaplet getNextTestCaplet()
    {
        TestCaplet tc= new TestCaplet(generateString(
                generateInt(GLOBAL_CONSTANTS.HOST_SIZE - 1)+1).toString(),
                generateIP(),
            generatePort(), 
            System.currentTimeMillis(),
            generateJavaString(generateInt(40*GLOBAL_CONSTANTS.KB)+1));
        return tc;
    }
    public static int generatePort()
    {
        Random rn = new Random();
        return ( 4000 + rn.nextInt(4000));
    }
    public static int generateInt(int max)
    {
        Random rn = new Random();
        return rn.nextInt(max);
    }
    public static String generateIP()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(generateInt(128)));
        sb.append(".");
        sb.append(String.valueOf(generateInt(128)));
        sb.append(".");
        sb.append(String.valueOf(generateInt(128)));
        sb.append(".");
        sb.append(String.valueOf(generateInt(128)));
        return sb.toString();
    }
    public static byte [] generateString(int length)
    {
        Random rn = new Random();
        byte [] b = new byte [length];
        rn.nextBytes(b);
        
        return b;
    }
    public static String generateJavaString(int length)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0 ; i < length/16; i++)
        {
            sb.append(generateIP().toString());
        }
        return sb.toString();
    }
    public MemoryBlockObjectPool<?> getMBO()
    {
        // TODO Auto-generated method stub
        return capMBO;
    }
}
