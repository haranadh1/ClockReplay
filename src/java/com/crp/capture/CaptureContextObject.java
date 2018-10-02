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
package com.crp.capture;

import org.apache.log4j.Category;
import org.json.JSONException;
import org.json.JSONObject;

import com.crp.common.CRPContextObjectInterface;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.JSON_KEYS;
import com.crp.common.RawPacket;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.Photon;
import com.crp.interfaces.Caplet;
import com.crp.protocol.CRPProtocolException;
import com.crp.protocol.CRPTcp;
import com.crp.thread.CRPThread;

/**
 * capture context object.
 * @author hpoduri
 * this is a class to store the capture context.
 * store this object in the CRPContext.
 * too bad java dont have structures..this class is like
 * a structure.
 */
public class CaptureContextObject implements CRPContextObjectInterface
{
    /**
     * store the current db message to send over to db.
     */
    public Message currentDBMessage;
    
    /**
     * capture worker thread object.
     * save it here, as calling Thread.getCurrentThread is very xpensive.
     */
    public CRPThread crpt;
    /**
     * capture progress status.
     *
     */
    public enum CaptureProgressStatus
    {
        /**
         * in progress.
         */
        CAPTURE_IN_PROGRESS,
        /**
         * capture stopped.
         */
        CAPTURE_STOPPED,
    }
    /**
     * db photon.
     */
    public Photon dbPhoton;
    
    /**
     * caplets captured so far.
     */
    public long capletCounter;
    
    /**
     * represents capture status.
     */
    public CaptureProgressStatus cStatus;
    
    /**
     * capture engine.
     */
    public JnetPcapCaptureEngine ce;

    /**
     * store cap worker thread logger here.
     */
    public Category capLogger;
    
    /**
     * crp tcp handler object.
     */
    private CRPTcp cTCP;
    
    /**
     * constructor.
     */
    public CaptureContextObject()
    {
        capletCounter = 0;
        dbPhoton = null;
        cStatus = CaptureProgressStatus.CAPTURE_STOPPED;
        ce = new JnetPcapCaptureEngine();
        currentDBMessage = null;
        crpt = null;
        cTCP = new CRPTcp();
    }
    
    /**
     * JSON representation of this object.
     * @return JSONObject.
     * @throws JSONException on error.
     */
    public final JSONObject toJSON() throws JSONException
    {
        JSONObject ret = new JSONObject();
        ret.put(JSON_KEYS.JsonKeyStrings.JSON_CAPLETS_CAPTURED, capletCounter);
        ret.put(JSON_KEYS.JsonKeyStrings.JSON_CAPTURE_STATUS,
            cStatus.toString());
        
        return ret;
        
    }

    /**
     * check if any ctrl messages, and handle them.
     * ASSUMTION; WHILE CAPTURE IN PROGRESS, ONLY CTRL MSGS ARE READ.
     */
    public final void checkAndHandleCtrlMessages()
    {
        Message m = crpt.popCtrlMessage();
        if (m == null)
        {
            return;
        }
        else
        {
            switch (m.getMessageHeader().messageCode())
            {
                case MESSAGE_CODE.MSG_CS_STOP_CAPTURE:
                case MESSAGE_CODE.MSG_CS_CANCEL_CAPTURE:
                {
                    cStatus = CaptureProgressStatus.CAPTURE_STOPPED;
                    break;
                }

            }
        }
    }
    
    /**
     * returns next available caplet object to fill in.
     * as a side effect, it sends the pending messages to db.
     * @param paylodLen pay load length, needed to see if this
     * caplet can fit in the current message.
     * @return Caplet Object.
     * @throws EtherException  on error.
     */
    public final Caplet getNextAvailableCaplet(
        final int paylodLen) throws EtherException
    {
        if(currentDBMessage == null)
        {
            try
            {
                currentDBMessage = crpt.getMyMsgWorld().getFreeMsg(
                    GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY);
                currentDBMessage.getMessageHeader().setMessageCode(
                    MESSAGE_CODE.MSG_CS_WRITE_CAPLET_STREAM_DB);
            }
            catch (EtherException e)
            {
                capLogger.error(e.getMessage());
                return null;
            }
        }
        if( !currentDBMessage.anyRoomForObjOfVarLength(
            paylodLen))
        {
            // no space to fit in the next caplet in this msg.
            
            dbPhoton.sendMessageNoFail(currentDBMessage);
            capLogger.info("Sending caplets to db : [" 
                + String.valueOf(
                    currentDBMessage.getPayloadMBO().getActiveObjects())+ "]");
            
            while ((currentDBMessage = crpt.getMyMsgWorld().getFreeMsg(
                GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY)) == null)
            {
                //TODO : should handle this more carefully.
                Thread.yield();
                dbPhoton.getChannel().flushChannel();
                dbPhoton.getChannel().notifyReceiver();
            }
            
            assert(currentDBMessage != null);
            currentDBMessage.getMessageHeader().setMessageCode(
                MESSAGE_CODE.MSG_CS_WRITE_CAPLET_STREAM_DB);
            assert(currentDBMessage.anyRoomForObjOfVarLength(paylodLen));
           
        }
        Caplet c = (Caplet) currentDBMessage.getPayloadMBO().getObject();
        return c;
    }

    /**
     * handle network packet.
     * @param rp raw packet to be processed.
     * @throws CRPProtocolException on error.
     */
    public final void processPacket(
        final RawPacket rp) throws CRPProtocolException
    {
        if(rp.isTCP())
        {
            Message m = cTCP.handlePacket(rp);
            if(m != null)
            {
                // send the message.
                try
                {
                    dbPhoton.sendMessageNoFail(m);
                }
                catch (EtherException e)
                {
                    CaptureService.CAP_SERVICE_LOG.error(e.getMessage());
                    throw new CRPProtocolException("CRP_PROTCL_ERROR_004", null,
                        "process packet in capture context: ");
                    
                }
            }
        }
        else
        {
            // for now support only tcp.
            assert(false);
        }
    }   
}
