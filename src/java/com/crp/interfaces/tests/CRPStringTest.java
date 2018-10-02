package com.crp.interfaces.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.crp.common.CRErrorHandling;
import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.interfaces.CRPString;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryBlockGroup;
import com.crp.memmgr.MemoryBlockObjectPool;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.ObjectFactory;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;
import com.crp.process.PROCESS_GLOBALS;

public class CRPStringTest
{

    @Before
    public void setUp() throws Exception
    {
        CRErrorHandling.load();
        MemoryManager.MEM_LOG.info(" Memory Manager Initiated");
        MemoryManager.MEM_LOG.error(" test error stream of the log");
       
        // register service first.
        PROCESS_GLOBALS.addService(new CRPService(
            new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
                "localhost",
                "service for capture",
                8085,
                0)));
        PROCESS_GLOBALS.addService(new CRPService(
            new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE,
                "localhost",
                "service for db",
                8086,
                0)));
        PROCESS_GLOBALS.addService(new CRPService(
            new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.REPLAY_SERVICE,
                "localhost",
                "service for replay",
                8085,
                0)));

        try
        {
            MemoryManager.init(100*GLOBAL_CONSTANTS.MB);
            MemoryManager.registerThread((short) 0);
            
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());

        }
    
    }

    @Test
    public void testPackUnPack()
    {
    // create a MemoryBlockGroup.
        
        MemoryBlockGroup mbg = new MemoryBlockGroup(4);
        // create a memory block first.
        MemoryBlockObjectPool<CRPString> mbo = null;
        MemoryBlockByteBuffer mbb = null;
        MemoryBlockByteBuffer mbbPack = null;
        
        try
        {
            mbb = (MemoryBlockByteBuffer)MemoryManager.createMemoryBlock(
                (short)0, 1*GLOBAL_CONSTANTS.MB,
                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER, null,
                MemoryManager.MemoryAllocationType.JAVA_BYTE_ARRAY, null);
            ObjectFactory of = new CRPString(null);
            
            mbo = (MemoryBlockObjectPool<CRPString>)MemoryManager
                .createMemoryBlock(
                    (short) 0,
                    1*GLOBAL_CONSTANTS.MB,
                    MemoryManager.MemoryBlockType.MEMORY_BLOCK_OBJECT_POOL,
                    of,
                    MemoryManager.MemoryAllocationType.NOT_VALID, null);
            mbo.setMBB(mbb);
            
            mbbPack = (MemoryBlockByteBuffer)MemoryManager.createMemoryBlock(
                (short)0, 1*GLOBAL_CONSTANTS.MB,
                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER, null,
                MemoryManager.MemoryAllocationType.JAVA_BYTE_ARRAY, null);
            
           
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        CRPString s1 = (CRPString) mbo.getObject();
        s1.setContent("JUST LIKE THAT");
        CRPString s3 = (CRPString) mbo.getObject();
        s3.setContent("INPUT  STRING 1");
        
        CRPString s2 = (CRPString) mbo.getObject();
        CRPString s4 = (CRPString) mbo.getObject();
        
        Packer pk= new Packer(mbbPack);
        try
        {
            s1.pack(pk);
            s3.pack(pk);
            
            mbbPack.readyToRead();
            UnPacker unpk = new UnPacker(mbbPack);
            int offset = mbbPack.getCurOffset();
            s2.unpack(unpk);
            s4.unpack(unpk);
            System.out.println(" o/p string :  " + s2.toString());
            System.out.println(" o/p string1: " + s4.toString());
        }
        catch (PackUnPackException e)
        {
            System.out.print("error : " + e.getMessage());
        }
        assertEquals("Success", s1, s2);
        assertEquals("Success", s3, s4);
    }

    @Test
    public void testUnpack()
    {
        fail("Not yet implemented");
    }

}
