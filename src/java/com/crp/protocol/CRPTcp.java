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

package com.crp.protocol;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.ProtocolException;

import org.jnetpcap.packet.JPacket;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;

import com.crp.capture.CaptureService;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.RawPacket;
import com.crp.ether.EtherException;
import com.crp.ether.Message;
import com.crp.interfaces.Caplet;
import com.crp.memmgr.MemoryManagerException;
import com.crp.thread.CRPThread;

/**
 * crp tcp handler.
 * @author hpoduri
 * @version $Id$
 */
public class CRPTcp extends CRPProtocol
{

    /**
     * current message, from which caplets are added to reassembler.
     */
    private Message currentMsg;
    /**
     * store the thread object here, as calling getcurrentthread is xpensive.
     */
    private CRPThread crpt;
    /**
     * multi dimensional map to store the out of band tcp packets.
     * this structure is used to reassemble the tcp packets.
     * TODO : should think of optimizing this.
     */
    private Map <Long, Map<Long, Caplet>> reassembler;
    
    /**
     * for now keep the pcap tcp object here.
     */
    private Tcp tcp;
    
    /**
     * ip pcap object.
     */
    private Ip4 ip4;
    
    /**
     * constructor.
     */
    public CRPTcp()
    {
        ip4 = new Ip4();
        tcp = new Tcp();
        reassembler = new HashMap<Long, Map<Long, Caplet>> ();
        currentMsg = null;
        crpt = CRPThread.getCurrentThread();
    }
    @Override
    public String getDesc()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName()
    {
        return GLOBAL_CONSTANTS.CRPProtocolNameStrings.TCP_PROTO;
    }

    @Override
    public Message getNextCapletMessage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message processCaplet(Caplet c)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message handlePacket(RawPacket rp) throws CRPProtocolException
    {
        JPacket pcapObj = null;
        /**
         * message to be returned.
         * only return when we have a message filled with caplets.
         */
        Message retMsg = null;
        Caplet c = null;
        
        if (rp.getCapAgentType() == GLOBAL_ENUMS.CRP_CAPTURE_AGENT_TYPE.PCAP)
        {
            pcapObj = rp.getPcapPacketObject();
            if (pcapObj.hasHeader(tcp))
            {
                if(!tcp.flags_SYN() && !tcp.flags_FIN() 
                        && (tcp.getPayloadLength() == 0))
                {
                    // it is just ACK, no data. ignore it.
                    return null;
                }
                if (pcapObj.hasHeader(ip4))
                {
                    if(currentMsg == null)
                    {
                       
                        currentMsg = getNextAvailableMsg();
                    }
                    // check to see if this is a fragment of already existing
                    // packet.
                    c = getCapletFromMap(tcp.source(), tcp.destination(),
                        ip4.sourceToInt(),
                        ip4.destinationToInt());
                    if(c == null)
                    {            
                        try
                        {
                            // first see if this message can fit this packet.
                            if(!currentMsg.anyRoomForObjOfVarLength(tcp.getPayloadLength()))
                            {
                                // no space in the current message.
                                retMsg = currentMsg;
                                currentMsg = getNextAvailableMsg();   
                            }
                            // first get a new caplet object.
                            c = getNextCapletToWrite();
                            if(c == null)
                            {
                                // no space in the current message.
                                // this case handles when the current message
                                // can fit this packet, but the message ran out
                                // of any more caplets.Theoretically this is possible.
                                retMsg = currentMsg;
                                currentMsg = getNextAvailableMsg();
                                c = getNextCapletToWrite();
                            }
                            assert(c != null);
                        }
                        catch(EtherException e)
                        {
                            CaptureService.CAP_SERVICE_LOG.error(e.getMessage());
                            throw new CRPProtocolException(
                                "CPR_PROTCL_ERROR_002", new String[] {
                                    this.getName()}, "tcp reassembly:");
                        }
                        c.getCaptureENV().setCapletMsg(currentMsg);
                        buildCaplet(c, pcapObj, rp.getExpectedLength());
                        
                    }
                    else
                    {
                        boolean isCapletComplete = false;
                        try
                        {
                            isCapletComplete = handleFragment(c,pcapObj,
                                tcp.source(), tcp.destination(),
                                ip4.sourceToInt(),
                                ip4.destinationToInt());
                        }
                        catch (MemoryManagerException e)
                        {
                            CaptureService.CAP_SERVICE_LOG.error(
                                e.getMessage());
                            throw new CRPProtocolException(
                                "CPR_PROTCL_ERROR_002", new String[] {
                                    this.getName()}, "tcp reassembly:");
                        }
                        Message cMsg = c.getCaptureENV().getEmbedMsg();
                      
                        if(isCapletComplete)
                        {  
                            // current message should not be returned,
                            // as some data may still go into it.
                            // also, if the caplet message is not the
                            // current message, it means that no more
                            // packets can go into the caplet message
                            // as we only get the next message,
                            // only if the current msg is full.
                            
                            if(cMsg != currentMsg)
                            {
                                // meaning this message is not actively being used for
                                // fragment writing.
                                if(cMsg.getExpectedLength() 
                                        == cMsg.getReceivedLength())
                                {
                                    retMsg = cMsg;
                                }
                            }
                        } 
                    }
                }
                else
                {
                    // tcp, no ip;
                    // should never be here.
                    assert(false);
                }
            }
            else
            {
                return null;
            }
        }
        else
        {
            // un supported agent for now.
            assert(false);
        }
        if(retMsg != null 
                && retMsg.getExpectedLength() == retMsg.getReceivedLength())
        {
            return retMsg;
        }
        return null;
    }
    
    /**
     * build given caplet with data from the given raw packet.
     * @param c is set with values from the raw packet.
     * @param p raw packet to read data from.
     * @param xpectedLen expected length of this caplet(if any).
     * @return Message m (return message if the message of
     *  the given caplet is full.
     * @throws CRPProtocolException on error.
     */
    private Message buildCaplet(final Caplet c, final JPacket p,
        final int xpectedLen) throws CRPProtocolException
    {
        try
        {
            c.setAttr(ip4.destination().toString(), tcp.destination(),
                ip4.destination().toString(),
                System.currentTimeMillis());
            
            if(tcp.flags_SYN())
            {
                c.getCapletHeader().setSessionStart();
            }
            if(tcp.flags_FIN()|| tcp.flags_RST())
            {
                c.getCapletHeader().setSessionClose();
            }
            
            if(xpectedLen == 0 || tcp.getPayloadLength() == 0)
            {
                return(handleFinishCaplet(c));
            }
            if(xpectedLen == tcp.getPayloadLength())
            {
               
                // this is the full packet.
                // means it is non-fragmented packet.
                // dont have to add to the map.
                // just fill it in and end of the story.
                p.getByteArray(tcp.getPayloadOffset(),
                    c.getMBB().getPoolByteArray(),
                    c.getMBB().getLocalOffset(),
                    tcp.getPayloadLength());
                
                // need to update necessary mem mgr housekeeping stuff.
                // we have to this ugly stuff, as the payload buffer is
                // filled by the pcap api.
                c.getPayLoadObject().setLength(
                    tcp.getPayloadLength());
                c.getPayLoadObject().setOffset(
                    c.getMBB().getLocalOffset());
                
                c.getMBB().setLocalOffset(
                    c.getMBB().getLocalOffset() 
                    + tcp.getPayloadLength());
              
                c.getMBB().finishWritingRecord(tcp.getPayloadLength());
                
                // in this case, we got all the packet that we need.
                return(handleFinishCaplet(c));
                
            }
            else
            {
                // new packet; should add a caplet to the reassembler.
                // this should be considered as a fragment as the expected
                // length is more than the received length
                c.getCaptureENV().setAttr(xpectedLen, tcp.seq());
                // add expected length to the message.
                c.getCaptureENV().getEmbedMsg().setExpectedLength(
                    c.getCaptureENV().getEmbedMsg().getExpectedLength()
                    + xpectedLen);
                
                boolean isCapletComplete = false;
                isCapletComplete = handleFragment(c,p,
                    tcp.source(), tcp.destination(),
                    ip4.sourceToInt(),
                    ip4.destinationToInt());
                Message cMsg = c.getCaptureENV().getEmbedMsg();
                cMsg.setReceivedLength(
                    cMsg.getReceivedLength() 
                    + tcp.getPayloadLength());
                
                // this caplet is fragmented, wait until we get all the other fragments.
                return null;
            }
        }
        catch(MemoryManagerException e)
        {
            CaptureService.CAP_SERVICE_LOG.error(e.getMessage());
            throw new CRPProtocolException("CRP_PROTCL_ERROR_003",
                null, "build caplet method:");
        }
        
    }
    
    /**
     * 
     * @param c
     * @param source
     * @param destination
     * @param sourceToInt
     * @param destinationToInt
     */
    private final boolean handleFragment(final Caplet c,
        final JPacket p,
        final int source, final int destination,
        final int sourceToInt, final int destinationToInt) throws MemoryManagerException
    {
        boolean isCapletComplete = false;
        isCapletComplete = c.getCaptureENV().addFragment(p,
            tcp.getPayloadOffset(),
            tcp.getPayloadLength(), tcp.seq());
        // add the caplet to map.
        if(!isCapletComplete)
        {
            addCapletToMap(c,
                tcp.source(), tcp.destination(),
                ip4.sourceToInt(),
                ip4.destinationToInt());
        }
        else
        {
            // the current caplet received all the expected data.
            c.getPayLoadObject().setCompleted();
            deleteCapletFromMap(c,
                tcp.source(), tcp.destination(),
                ip4.sourceToInt(),
                ip4.destinationToInt());
        }
        Message cMsg = c.getCaptureENV().getEmbedMsg();
        cMsg.setReceivedLength(
            cMsg.getReceivedLength() 
            + tcp.getPayloadLength());
        return isCapletComplete;
    }
    /**
     * retrieves the caplet object for the given key.
     * |source port + dest port|
     *  |                    |  
     *   | src ip + dest ip |
     *     |               |
     *       | caplet obj|
     * @param source source port
     * @param destination dest port
     * @param sourceToInt source ip converted to int
     * @param destinationToInt dest port converted to int
     * @return caplet object, if found; otherwise null.
     */
    private Caplet getCapletFromMap(final int source, final int destination,
        final int sourceToInt, final int destinationToInt)
    {
        long key1 = source;
        key1 = key1 << 32;
        key1 = key1 | (long)sourceToInt;
        Map <Long, Caplet> m = reassembler.get(key1);
        
        if(m == null)
        {
            return null;
        }
        else if(m.size() == 1)
        {
            return m.values().iterator().next();
        }
        long key2 = destination;
        key2 = key2 << 32;
        key2 = key2 | destinationToInt;
        
        return m.get(key2);
    }

    /**
     * add a caplet to the map.
     * should be called when we find a first fragmented tcp ip packet.
     * @param c caplet to be added to the map.
     * @param source source port
     * @param destination dest port
     * @param sourceToInt source ip as int
     * @param destinationToInt dest ip as int.     */
    private final void addCapletToMap(final Caplet c,
        final int source, final int destination,
        final int sourceToInt, final int destinationToInt)
    {
        long keyL = source;
        keyL = (long)(keyL << 32);
        long keyR = sourceToInt;
        long key = (long) (keyL|keyR);
        Map<Long, Caplet> m = reassembler.get(key);
        
        keyL = destination;
        keyL = keyL << 32;
        keyR = destinationToInt;
        long mKey = (long)(keyL | keyR);
        if(m == null)
        {
            m = new HashMap<Long, Caplet>();
            m.put(mKey, c);
        }
        reassembler.put(key, m);
    }
    
    /**
     * delete caplet from the map.
     * this needs to be called, when we receive all the fragments
     * for this caplet.
     * @param c caplet object to be deleted from the map.
     * @param source source port
     * @param destination dest port
     * @param sourceToInt source ip as int
     * @param destinationToInt dest ip as int.
     */
    private final void deleteCapletFromMap(
        final Caplet c,  final int source, final int destination,
        final int sourceToInt, final int destinationToInt)
    {
        long key1 = source;
        key1 = key1 << 32;
        key1 = key1 | sourceToInt;
        Map<Long, Caplet> m = reassembler.get(key1);
        
        long key2 = destination;
        key2 = key2 << 32;
        key2 = key2 | destinationToInt;
        if(m == null)
        {
            return;
        }
        m.remove(key2);
        if(m.size() == 0)
        {
            reassembler.remove(key1);
        }
    }
    /**
     * get a new caplet from the current msg.
     * if the current msg is null, create a caplet object
     * message pool.
     * @return Caplet object.
     * @throws EtherException on error.
     */
    private final Caplet getNextCapletToWrite() throws EtherException
    {
        Caplet c = null;
        
        
        c = (Caplet) currentMsg.getPayloadMBO().getObject();
        c.reset();
        return c;
        
    }
    
    /**
     * returns next available message to write.
     * @return Message m
     * @throws CRPProtocolException on error.
     */
    private final Message getNextAvailableMsg() throws CRPProtocolException
    {
        Message m = null;
        if(currentMsg == null)
        {
           
            try
            {
                // first time, create a message group.
                crpt.getMyMsgWorld().createCapletMsgGroupForCS(
                    crpt);
                while ( ( m = crpt
                    .getMyMsgWorld()
                    .getFreeMsg(
                        GLOBAL_CONSTANTS.CRPObjNameStrings.MSG_CAPLET_KEY
                    ) ) == null )
                {
                    Thread.yield();
                }
            }
            catch (EtherException e)
            {
                CaptureService.CAP_SERVICE_LOG.error(e.getMessage());
                throw new CRPProtocolException(
                    "CPR_PROTCL_ERROR_002", new String[] {
                        this.getName()}, "tcp reassembly:");
            }
        }
        
        assert(m != null);
        return m;
        
    }
    
    /**
     * should be called when we re assemble a full caplet from tcp.
     * @param c caplet reassembled.
     * @return Message if the message in which the given caplet is full,
     * or null.
     */
    public final Message handleFinishCaplet(final Caplet c)
    {
        Message capMsg = null;
        {  
            // current message should not be returned,
            // as some data may still go into it.
            // also, if the caplet message is not the
            // current message, it means that no more
            // packets can go into the caplet message
            // as we only get the next message,
            // only if the current msg is full.
            capMsg = c.getCaptureENV().getEmbedMsg();
            if(capMsg != currentMsg)
            {
                // meaning this message is not actively being used for
                // fragment writing.
                if(capMsg.getExpectedLength() 
                        == capMsg.getReceivedLength())
                {
                    return capMsg;
                }
            }
        }
        return null;
    }
}
