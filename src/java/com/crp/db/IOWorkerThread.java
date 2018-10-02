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

package com.crp.db;

import org.apache.log4j.Category;

import com.crp.common.CRPException;
import com.crp.common.CRPLogger;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.MessageHandlerCallBack;
import com.crp.ether.MessageHolder;
import com.crp.ether.Message.MessageStatus;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManager;
import com.crp.memmgr.MemoryManagerException;
import com.crp.memmgr.MemoryManager.MemoryAllocationType;
import com.crp.memmgr.MemoryManager.MemoryBlockType;
import com.crp.pkunpk.Packer;
import com.crp.thread.CRPThread;
import com.crp.thread.CRPThreadException;
import com.crp.thread.CRPThread.CRPThreadStatus;

/**
 * IO Thread, handles all the IO write/reads.
 * @author hpoduri
 * @version $Id$
 */
public class IOWorkerThread implements Runnable, MessageHandlerCallBack
{ 
    /**
     * used for packing the db pages into byte array.
     */
    private MemoryBlockByteBuffer packMBB;
    
    /**
     * file manager handles all file operations.
     */
    private FileManager fm;
    
    /**
     * create a separate logger for ConnectionService.
     */
    public static final Category IO_LOG = 
        CRPLogger.initializeLogger(
            "com.crp.db.IOWorkerThread");
    
    @Override
    public void run()
    {
        packMBB = null;
        CRPThread crpt = (CRPThread)Thread.currentThread();
        String galoreRoot = System.getenv(GLOBAL_CONSTANTS.CRP_ENV);
        String fileSep = System.getProperty("file.separator");
        String crpDBFilePath = 
            galoreRoot + fileSep + "db" + fileSep;
        fm = new FileManager(crpDBFilePath);
        assert(crpt != null);
        try
        {
            crpt.initMsgPools();
        }
        catch (CRPThreadException e)
        {
            IO_LOG.error(e.getMessage());
            crpt.setCRPThreadStatus(CRPThreadStatus.KILLED);
            return;
        }
        crpt.setCRPThreadStatus(CRPThreadStatus.INIT);
        
        {
            IO_LOG.info("IO Worker Thread id : "
                + String.valueOf(crpt.getCRPThreadID() + " started"));
            
        }
        while (true)
        {
            //check control messages.   
            //check non-control messages.
            // --- process non-control messages if any.
            
            // if no messages
            // yield for some iterations...
            // after some iterations do sleep for ever.
            // wake up thru interrupt by the process main thread.
            
            // testing....
            Message m = null;
            
            try 
            {
                if(crpt.shutdownInProgress())
                {
                    IO_LOG.info("Thread ID : " 
                        + String.valueOf(CRPThread.getCurrentThreadID()
                            + " finished for service: " 
                            + crpt.getMyService().getServiceInfo().getName().toString()));
                    crpt.shutdownCompleted();
                    break;
                }
                try
                {
                    MessageHolder mh = null;
                    mh = crpt.getMessageCollector().checkForNewMessages();
                    int curNumOfMsgs = mh.size();
                    for (int i = 0; i < curNumOfMsgs; i++)
                    {
                        m = mh.getNextMessage(i);
                        if(m != null)
                        {
                            handleMessage(m);
                        }
                        else
                        {
                            IO_LOG.info(" null msg received in io mgr" + mh.zipString());
                        }
                    }
                    // mh.reset();
                }
                catch (Exception e)
                {
                    m.updateStatus(MessageStatus.AVAILABLE);
                    IO_LOG.error(e.toString());
                    IO_LOG.error(e.getMessage());
                    IO_LOG.error(CRPException.getPrintStack(e));
                    crpt.finishProcessingRequest();
                    // send  a error message back.
                    Message errMsg = crpt.getMyMsgWorld().getFreeMsg(
                        GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY);
                    errMsg.getMessageHeader().setMessageCode(
                        MESSAGE_CODE.MSG_FAIL);
                    if( m != null )
                    {
                        m.getPhoton().sendMessageNoFail(errMsg);
                    }
                }
                
            }
            catch (Exception e)
            {
                DBService.DB_SERVICE_LOG.error(e.getMessage());
                crpt.setCRPThreadStatus(CRPThreadStatus.KILLED);
                break;
                
            }
        }      
        
    }

    @Override
    public Message handleMessage(Message m) throws EtherException
    {
        CRPThread crpt = CRPThread.getCurrentThread();

        switch(m.getMessageHeader().messageCode())
        {
            case MESSAGE_CODE.MSG_DB_PAGE_TO_FILE_IO:
            {
                try
                {
                    if(!fm.isInitialized())
                    {
                        fm.init(FileIO.FileOpenFlag.OPEN_FOR_WRITE);
                    }
                }
                catch(DBException e)
                {
                    IO_LOG.error(e.getMessage());
                    throw new EtherException("CRP_EHTERM_ERROR_017",
                        new String[] {
                            String.valueOf(m.getMessageHeader().messageCode())
                        });
                }
                
                try
                {
                    IO_LOG.info(" (m)FILE IO: WROTE " + m.toString());
                    assert(m.getPackerForObjs() != null);
                    fm.writeToFile(
                        m.getPackerForObjs().getMBB().getPoolByteArray(),
                        m.getPackerForObjs().getMBB().getCurOffset());
                }
                catch(DBException e)
                {
                    IO_LOG.error(e.getMessage());
                    throw new EtherException("CRP_EHTERM_ERROR_017",
                        new String[] {
                            String.valueOf(m.getMessageHeader().messageCode())
                        });
                }
                break;
            }
            case MESSAGE_CODE.MSG_REQ_DB_SEQ_READ_FROM_IO:
            {
                handleSeqReadFromDB(m);
                break;
            }
            case MESSAGE_CODE.MSG_SHUTDOWN:
            {
                IO_LOG.info(" shutdown message received: ");

                if(fm.isInitialized())
                {
                    // wait for the close message.
                    m.dontConsiderForProcessing();
                    crpt.getMessageCollector().getMessageHolder().addMessage(m);
                    break;
                }
                else
                {
                    m.considerForProcessing();
                    crpt.startShutdown();
                    // let it fall thru.
                }
            }
            case MESSAGE_CODE.MSG_CLOSE_CONNECTION:
            {
                IO_LOG.info(" close connection message received: ");
                try
                {
                    if(fm.isInitialized())
                    {
                        fm.closeFile();
                    }
                }
                catch(DBException e)
                {
                    IO_LOG.error(e.getMessage());
                    throw new EtherException("CRP_EHTERM_ERROR_017",
                        new String[] {
                            String.valueOf(m.getMessageHeader().messageCode())
                        });
                }
                break;
            }
        }
        m.updateStatus(MessageStatus.AVAILABLE);
        return null;
    }

    /**
     * handles seq db read req from db.
     * @param m message object 
     * @throws EtherException on error.
     */
    private final void handleSeqReadFromDB(
        final Message m) throws EtherException
    {
        
        try
        {
            if(!fm.isInitialized())
            {
                fm.init(FileIO.FileOpenFlag.OPEN_FOR_READ);
            }
        }
        catch(DBException e)
        {
            IO_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_017",
                new String[] {
                    String.valueOf(m.getMessageHeader().messageCode())
                });
        }
        CRPThread crpt = CRPThread.getCurrentThread();
        Message dbMsg = null;
        
        while( (dbMsg = crpt.getMyMsgWorld().getFreeMsg(
            GLOBAL_CONSTANTS.CRPObjNameStrings.MESSAGE_KEY)) == null)
        {
            m.getPhoton().getChannel().flushChannel();
            m.getPhoton().getChannel().notifyReceiver();
            Thread.yield();
        }
        
        // create a memory block and attach to a message.
        try
        {
            if (dbMsg.getPackerForObjs() == null)
            {
                packMBB = (MemoryBlockByteBuffer) MemoryManager
                    .createMemoryBlock(
                        CRPThread.getCurrentThreadID(),
                        GLOBAL_CONSTANTS.MAX_DB_PAGES_PER_IO
                            * GLOBAL_CONSTANTS.DB_PAGE_SIZE,
                        MemoryBlockType.MEMORY_BLOCK_BYTE_BUFFER,
                        null,
                        MemoryAllocationType.JAVA_BYTE_ARRAY,
                        "IOThread: packer object for IO reads");
                Packer p = new Packer(packMBB);
                dbMsg.setPackerForObjs(p);
            }
        }
        catch (MemoryManagerException e)
        {
            IO_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_017",
                new String[] {
                    String.valueOf(m.getMessageHeader().messageCode())
                });
        }
   
        int retLen = 0;
        try
        {
            packMBB.reset();
            retLen = fm.readFromFile(packMBB.getPoolByteArray());
        }
        catch (DBException e)
        {
            IO_LOG.error(e.getMessage());
            throw new EtherException("CRP_EHTERM_ERROR_017",
                new String[] {
                    String.valueOf(m.getMessageHeader().messageCode())
                });
        }
        if(retLen > 0)
        {
            
            try
            {
                // trick to make it look like packing.
                packMBB.setLocalOffset(retLen);
                packMBB.finishWritingRecord(retLen);
            }
            catch (MemoryManagerException e)
            {
                IO_LOG.error(e.getMessage());
                throw new EtherException("CRP_EHTERM_ERROR_017",
                    new String[] {
                        String.valueOf(m.getMessageHeader().messageCode())
                    });
            }
            
            dbMsg.getMessageHeader().setMessageCode(
                MESSAGE_CODE.MSG_RES_IO_PAGE_SEQ_READ_TO_DB);
        }
        else
        {
            dbMsg.getMessageHeader().setMessageCode(
                MESSAGE_CODE.MSG_END_OF_DATA);
        } 
        m.getPhoton().sendMessageNoFail(dbMsg);
              
    }
}