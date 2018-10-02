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

package com.crp.ether;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;

import com.crp.capture.CaptureService;
import com.crp.common.CRPLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.interfaces.CRPString;
import com.crp.interfaces.Caplet;
import com.crp.interfaces.ConnectionInterface;
import com.crp.memmgr.ObjectFactory;
import com.crp.thread.CRPThread;

/**
 * class to represent all the message assemblers.
 * every thread contains one instance of this class, using
 * which we can create many types of message assemblers.
 * @author hpoduri
 * @version $Id$
 */
public class MessageWorld
{
    /**
     * create a separate logger for MessageWorld.
     */
    public static final Category MSG_WORLD_LOG = CRPLogger
                                             .initializeLogger("com.crp.ether.MessageWorld");
    /**
     * store the array of message groups mapped to the object name.
     * the key to the map is the name of the object defined as per
     * getMyName method from ObjectFactory.
     * ex: if a Message has an object embedded in it the key would be,
     * Message.getMyName + "_" + object.getMyName.
     */
    private Map<String, MessageGroup> msgGrpMap 
        = new HashMap<String, MessageGroup>();
   
    /**
     * these are the common message group pools get created for 
     * every thread.
     * any thread specific stuff should be created later as
     * required.
     * @throws EtherException on error.
     */
    public final void createCommonMsgGroupPools() throws EtherException
    {
        /**
         * create message pool, without objects.
         */
        ObjectFactory ofm = new Message(MESSAGE_CODE.INVALID_MESSAGE);
        createMsgGroupPool(ofm, 10, -1);
        
        /**
         * create CRPString Message Group Pool.
         */
        ObjectFactory ofs = new CRPString(null);
        createMsgGroupPool(ofs, 10, GLOBAL_CONSTANTS.MB);
        
        /**
         * create connection message group pool.
         */
        ObjectFactory ofc = new ConnectionInterface();
        createMsgGroupPool(ofc, 10, -1);
    }
    
    /**
     * convenience method to create a msg group pool.
     * @param ofs object factory instance.
     * @param numObjs number of objects to be created.
     * @param inpBlockSize block size for the memory block byte buffer. 
     * @throws EtherException 
     */
    public final void createMsgGroupPool(final ObjectFactory ofs,
        final int numObjs,
        final int inpBlockSize) throws EtherException
    {
        // first create Message Group.
        ObjectFactory ofm = new Message(MESSAGE_CODE.INVALID_MESSAGE);
        
        MessageGroup mg = null;
        if (ofm.getMyName() == ofs.getMyName())
        {
            // for message group , we should always pass number of messages.
            // this is a special case as we dont have objects embedded in msg.
            mg = new MessageGroup(numObjs);
            //means it is only for message type and no object embedded.
            msgGrpMap.put(ofm.getMyName(), mg);
        }
        else
        {
            //TODO : how many msgs should we need??
            mg = new MessageGroup(4);
            mg.initMsgs(ofs, numObjs, inpBlockSize); 
            msgGrpMap.put(ofm.getMyName() + "_" + ofs.getMyName(), mg);
        }
    }
    
    /**
     * get next available message for the given key.
     * @param key string for which the message is required.
     * @return message.
     * @throws EtherException on error.
     */
    public final Message getFreeMsg(final String key) throws EtherException
    {
        MessageGroup mg = msgGrpMap.get(key);
        if(mg == null)
        {
            return null;
        }
        Message m = mg.getNextAvailableMsg();
        if(m != null && m.getPayloadMBO() != null)
        {
            m.getPayloadMBO().reset();
        }
        return (m);
    }

    /**
     * get next available message for the given key.
     * however, it wont return null, when a message is not available.
     * it loops through until it gets one.
     * @param key string for which the message is required.
     * @return message.
     * @throws EtherException on error.
     */
    public final Message getFreeMsgNoFail(
        final String key) throws EtherException
    {
        Message m = null;
        while ((m = getFreeMsg(key)) == null)
        {
           
            {
                //m.getPhoton().getChannel().flushChannel();
                //m.getPhoton().getChannel().notifyReceiver();
                Thread.yield();
            }
        }
        return m;
    }
    
    /**
     * create a caplet object message for the given thread.
     * @param crpt thread, that needs caplet objects.
     * @throws EtherException on error.
     */
    public final void createCapletMsgGroupForCS(
        final CRPThread crpt) throws EtherException
    {
        // create a message pool for caplet streaming from capture service
        // to db service.
        ObjectFactory ofcap = new Caplet(null);
        if (crpt.getMyMsgWorld().getFreeMsg(
            GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY) == null)
        {
            throw new EtherException("CPR_ETHERM_ERROR_022", null);
        }
        crpt.getMyMsgWorld().createMsgGroupPool(
            ofcap,
            20 * GLOBAL_CONSTANTS.KB,
            GLOBAL_CONSTANTS.CAPLET_MEMORY_BLOCK_SIZE);
       
    }
    /**
     * returns msg group map.
     * @return map object.
     */
    public final Map<String, MessageGroup> getMsgGrpMap()
    {
        return msgGrpMap;
    }
}
