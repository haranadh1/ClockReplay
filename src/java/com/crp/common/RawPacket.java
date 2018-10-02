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

import org.jnetpcap.packet.JPacket;

import com.crp.common.GLOBAL_ENUMS.CRP_CAPTURE_AGENT_TYPE;

/**
 * raw network packet representation.
 * @author hpoduri
 * @version $Id$
 */
public class RawPacket
{
    /**
     * exptected length of this packet.
     * this is used by higher level protocols to specify
     * the total number of bytes expected for it to finish a
     * communication between the source and target.
     */
    private int expectedLength;
    
    /**
     * true if this packet is tcp.
     */
    private boolean isTCP;
    /**
     * capture agent type.
     * type of capturing library.
     */
    private GLOBAL_ENUMS.CRP_CAPTURE_AGENT_TYPE caType;
    
    /**
     * represents the packet from the pcap or any other.
     * for now support only pcap.
     */
    private JPacket pk;

    /**
     * constructor.
     * @param inpCAType type of capturing library.
     */
    public RawPacket(final GLOBAL_ENUMS.CRP_CAPTURE_AGENT_TYPE inpCAType)
    {
        caType = inpCAType;
        pk = null;
    }
    
    /**
     * set pcap/other capturing library packet object.
     * @param inpPK pcap/other capture library packet.
     */
    public final void setPacketObject(final JPacket inpPK)
    {
        pk = inpPK;
    }
    
    /**
     * returns packet object.
     * @return JPacket (support only pcap for now).
     */
    public final JPacket getPcapPacketObject()
    {
        return pk;
    }
    
    /**
     * returns capture agent type(pcap/apache logs etc).
     * @return enum.
     */
    public final CRP_CAPTURE_AGENT_TYPE getCapAgentType()
    {
        return caType;
    }
    
    /**
     * resets this object.
     */
    public final void reset()
    {
        setExpectedLength(-1);
        pk = null;
    }

    /**
     * @param expectedLength the expectedLength to set
     */
    public final void setExpectedLength(final int expectedLength)
    {
        this.expectedLength = expectedLength;
    }

    /**
     * @return the expectedLength
     */
    public final int getExpectedLength()
    {
        return expectedLength;
    }

    /**
     * @param isTCP the isTCP to set
     */
    public final void setTCP(final boolean isTCP)
    {
        this.isTCP = isTCP;
    }

    /**
     * @return the isTCP
     */
    public final boolean isTCP()
    {
        return isTCP;
    }
    
}
