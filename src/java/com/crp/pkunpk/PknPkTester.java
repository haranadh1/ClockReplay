/*
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

/* this file implements the methods to pack the standard data types */


package com.crp.pkunpk;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import com.crp.common.CRErrorHandling;
import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.MessageWorld;
import com.crp.interfaces.Caplet;
import com.crp.interfaces.CapletHeader;
import com.crp.memmgr.MemoryBlock;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.process.PROCESS_GLOBALS;
import com.crp.process.ProcessMain;

/**
 * class with methods to test pack/unpack interface.
 * @author hpoduri
 *
 */
public final class PknPkTester 
{
     /**
      * * just static methods; make the constructor private.
      */
    private PknPkTester()
    {
        // Do Nothing
    }
	/**
	 * test very simple pack/unpack.
	 */
    private static void testSimple()
    {
        // take a simple class say, Caplet ( how we store/send data )
        /**
         * consider the following format { ts : long capid: long req : String
         * res : byte []
         */
        int size = 1024;
        Packer p = new Packer(size);
        try
        {
            // Pack the buffer
            p.packUInt64(18888888888L);
            p.packUInt64(134444);
            p.packString("HTTP GET STATUS_CHECK ");
            byte[] ba = new byte[]{'a', 'b', 'c', 'd'};
            p.packBytes(ba);
            byte[] byteBuffer = p.getBuffer();
            UnPacker up = new UnPacker(byteBuffer, byteBuffer.length);
            // print the fields as you unpack
            System.out.println(" ts : " + up.unpackUInt64());
            System.out.println(" capid: " + up.unpackUInt64());
            System.out.println(" req:   " + up.unpackString());
            System.out.println(" res:   " + up.unpackBytes().toString());
        }
        catch (IOException e)
        {
            System.out.println(" exception " + e.getStackTrace());
        }
    }
	/**
	 * test pack/unpack with caplet obejct.
	 */
    private static void testCapletPunP()
    {
        // Test the pack/unpack interface with the caplet objects
        try
        {
            URL yahoo = new URL("http://www.yahoo.com");
            URLConnection urlc = yahoo.openConnection();
            byte[] byteBuffer = urlc.getInputStream().toString().getBytes();
            // construct a caplet object from stream
            // First construct a caplet header
            CapletHeader ch = new CapletHeader("yahoo.com", "192.0.0.1", 7067,System
                .currentTimeMillis(), -1);
            Caplet res = new Caplet(ch, byteBuffer, null);
            System.out.println(" Orig Caplet Object " + res.toString());
            // pack the caplet now
            // size should always be more than the buffer size
            // In some cases it might overflow. this would not be
            // the case once we have the memory manager
            Packer pk = new Packer(res.getCapletSize() + 120);
            res.pack(pk);
            Caplet resUnpk = new Caplet(ch, byteBuffer, null);
            byte [] bb = new byte [1024*1024];
            UnPacker unpk = new UnPacker(bb, bb.length);
            resUnpk.unpack(
                unpk);
            System.out.println(" Caplet after Unpk " + resUnpk.toString());
            // Now that we packed the Caplet object, we try to
            // unpack it and see the contents
        }
        catch (Exception e)
        {
            System.out.println(" Exception " + e.getStackTrace());
        }
    }


	/**
	 * test punp with memory block interface.
	 */
    private static void testPunPWithMemoryBlockInterface()
    {
        System.out.println(GLOBAL_UTILS.getCurrentMethod() + "-----------");
        //create all the stuff that needs to create memory block.
        // first register/create a new service
        // register service first.
        PROCESS_GLOBALS.addService(new CRPService(
            new CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE, "localhost", "service for capture", 9090, 1)));
        
        //register current thread and init.
        try
        {
            MemoryManager.registerThread((short) 0);
            MemoryManager.init(100*GLOBAL_CONSTANTS.MB);
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        MemoryBlockByteBuffer mb = null;
        try
        {
            int size = 1 * GLOBAL_CONSTANTS.MB;
            mb = (MemoryBlockByteBuffer)MemoryManager
                .createMemoryBlock(
                    (short) 0,
                    size,
                    MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                    "for testing");
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        // now we can pack/unpack using this memory block.

     // Test the pack/unpack interface with the caplet objects
        try
        {
            URL yahoo = new URL("http://www.yahoo.com");
            URLConnection urlc = yahoo.openConnection();
            byte[] byteBuffer = urlc.getInputStream().toString().getBytes();
            // construct a caplet object from stream
            // First construct a caplet header
            CapletHeader ch = new CapletHeader("yahoo.com", "192.0.0.1", 1000,System
                .currentTimeMillis(), 1000);
            Caplet res = new Caplet(ch, byteBuffer, null);
            System.out.println(" Orig Caplet Object " + res.toString());
            // pack the caplet now
            // size should always be more than the buffer size
            // In some cases it might overflow. this would not be
            // the case once we have the memory manager
            Packer pk = new Packer(mb);
            res.pack(pk);
            System.out.println(" offset before packing : " + String.valueOf(mb.getCurOffset()));
            
            // pack another object to same memory block.
            URL google = new URL("http://www.google.com");
            URLConnection urlg = google.openConnection();
            byte[] byteBufferg = urlg.getInputStream().toString().getBytes();
            // construct a caplet object from stream
            // First construct a caplet header
            CapletHeader chg = new CapletHeader("google.com", "192.0.0.1", 1000, System
                .currentTimeMillis(), 1000);
            Caplet resg = new Caplet(chg, byteBufferg, null);
            System.out.println(" Orig Caplet Object " + resg.toString());
            resg.pack(pk);
            System.out.println(" offset before packing : " + String.valueOf(mb.getCurOffset()));
            mb.resetOffset();
            UnPacker unp = new UnPacker(mb);
            Caplet resUnpk = new Caplet(chg, byteBufferg, null);
            resUnpk.unpack(unp);
            System.out.println(" Caplet after Unpk " + resUnpk.toString());
            Caplet resUnpkg = new Caplet(chg, byteBufferg, null);
            resUnpkg.unpack(unp);
            System.out.println(" Caplet after Unpk " + resUnpkg.toString());
            
            // Now that we packed the Caplet object, we try to
            // unpack it and see the contents
        }
        catch (Exception e)
        {
            System.out.println(" Exception " + e.getStackTrace());
        }
    }
    private static void testMessagePunP()
    {
        // do the initial setup of creating a dummy service and 
        // init mem mgr.
        ProcessMain.createDummyService();
      //register current thread and init.
        try
        {
            MemoryManager.registerThread((short) 0);
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        MemoryBlockByteBuffer mbPck = null;
        MemoryBlockByteBuffer mbUnPck = null;
        try
        {
            int size = 1 * GLOBAL_CONSTANTS.MB;
            mbPck = (MemoryBlockByteBuffer)MemoryManager
                .createMemoryBlock(
                    (short) 0,
                    size,
                    MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                    "for testing");
            mbUnPck = (MemoryBlockByteBuffer)MemoryManager
            .createMemoryBlock(
                (short) 0,
                size,
                MemoryManager.MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                "for testing");
        }
        catch (MemoryManagerException e)
        {
            System.out.println(e.getMessage());
        }
        Message m = new Message(MESSAGE_CODE.MSG_REMOTE_NEW_CONNECTION);
        Message m1 = new Message(MESSAGE_CODE.INVALID_MESSAGE);
        Packer pk = new Packer(mbPck);
        
        try
        {
            m.pack(pk);
            mbPck.resetOffset();
            UnPacker upk = new UnPacker(mbPck);
            int length = -1;
            int mc = MESSAGE_CODE.INVALID_MESSAGE;
            
            length = upk.unpackWireHeaderLength();
            mc = upk.unpackWireHeaderMessageCode();
            m1.unpack(upk);
            
            System.out.println(" message code : " + String.valueOf(m1.getMessageHeader().messageCode()));
           
        }
        catch(PackUnPackException e)
        {
            MessageWorld.MSG_WORLD_LOG.error(e.getMessage());
        }
    }
    public static void testPackUnPackEvents()
    {
        
    }
	/**
	 * @param args string argument list.
	 */
    public static void main(final String[] args)
    {
        //load the error messages.
        CRErrorHandling.load();
       // testSimple();
      //  testCapletPunP();
       // testPunPWithMemoryBlockInterface();
       // testPackUnPackEvents();
        testMessagePunP();
    }
   

}
