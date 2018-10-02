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

import org.jnetpcap.packet.JPacket;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Http.Request;

import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.RawPacket;
import com.crp.ether.Message;
import com.crp.interfaces.Caplet;

/**
 * crp implmentation of http prototocl.
 * @author hpoduri
 * @version $Id$
 */
public class CRPHttp extends CRPProtocol
{

    /**
     * crp implementation of tcp.
     */
    private CRPTcp crpTcp;
    
    /**
     * pcap http object. keep it here for now.
     * we can generalize it later.
     */
    private Http http;
    
    /**
     * constructor.
     */
    public CRPHttp()
    {
        http = new Http();
        crpTcp = new CRPTcp();
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message getNextCapletMessage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Message handlePacket(RawPacket rp) throws CRPProtocolException
    {
        JPacket pcapObj= null;
        Message m = null;
        
        if(rp.getCapAgentType() == GLOBAL_ENUMS.CRP_CAPTURE_AGENT_TYPE.PCAP)
        {
            pcapObj = rp.getPcapPacketObject();
            if(pcapObj.hasHeader(http))
            {
                // check if it has content-length field
                if(http.hasField(Request.Content_Length))
                {
                    int cLen = Integer.parseInt(
                        http.fieldValue(Request.Content_Length));
                    rp.setExpectedLength(cLen);
                    m = crpTcp.handlePacket(rp);
                }
                else
                {
                    m = crpTcp.handlePacket(rp);
                }
            }
        }
        else
        {
            m = crpTcp.handlePacket(rp);
        }
        return m;
    }

    @Override
    public Message processCaplet(Caplet c) throws CRPProtocolException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
