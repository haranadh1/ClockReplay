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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.JPacketHandler;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.network.Ip6;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Http.Request;
import org.jnetpcap.protocol.tcpip.Http.Response;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.JSON_KEYS;
import com.crp.common.RawPacket;
import com.crp.ether.EtherException;
import com.crp.interfaces.Caplet;
import com.crp.memmgr.MemoryManagerException;
import com.crp.protocol.CRPProtocolException;
/**
 * capture agent based on jnet pcap library.
 * @author hpoduri
 * @version $Id$
 */
public class JnetPcapCaptureEngine extends CaptureAgentBase
{
    
    /**
     * pcap object.
     */
    private Pcap pcapObj;
    
    /** ethernet. */
    private Ethernet ethernet;

    /** The ip4. */
    private Ip4 ip4;

    /** The ip6. */
    private Ip6 ip6;
    
    /** http */
    private Http http;
    
    /**
     * tcp.
     */
    private Tcp tcp;
    /**
     * capture filter.
     */
    private PcapBpfProgram filter;
    
    /**
     * packet handler object.
     * nextPacket method in this packets is called for every
     * ethernet packet received by pcap.
     */
    private JPacketHandler<CaptureContextObject> handler;
    /**
     * constructor.
     */
    public JnetPcapCaptureEngine()
    {
        pcapObj = null;
        ethernet = new Ethernet();
        ip4 = new Ip4();
        ip6 = new Ip6();
        filter = new PcapBpfProgram();
        http = new Http();
        tcp = new Tcp();
        handler = null;
    }
    /**
     * returns all the list of network interfaces.
     * @return json string.
     * @throws CaptureEngineException on error.
     */
    public final JSONObject listAllNetworkInterfaces()
        throws CaptureEngineException
    {
        List<PcapIf> allDevs = new ArrayList<PcapIf>();
        StringBuilder errBuf = new StringBuilder();
        int retCode = Pcap.findAllDevs(allDevs, errBuf);
        
        if(retCode == Pcap.ERROR)
        {
            throw new CaptureEngineException("CRP_CPTURE_ERROR_001",
                new String[] {errBuf.toString()});
        }
        if(allDevs.size() == 0)
        {
            throw new CaptureEngineException("CRP_CPTURE_ERROR_002", null);
        }
        // create a new json object
        JSONObject jo = new JSONObject();
        JSONArray devArray = new JSONArray();

        for(int i = 0; i < allDevs.size(); i++)
        {
            JSONArray temp = new JSONArray();
            JSONObject device = new JSONObject();
            try
            {
                temp.put(new JSONObject().put( 
                    JSON_KEYS.JsonKeyStrings.JSON_CAP_NETWORK_INT_NAME,
                    allDevs.get(i).getName()));
                temp.put(
                    new JSONObject().put(
                        JSON_KEYS.JsonKeyStrings.JSON_CAP_NETWORK_INT_DESC,
                        allDevs.get(i).getDescription()));
               
                device.put(
                    JSON_KEYS.JsonKeyStrings.JSON_CAP_NETWORK_INT_DETAILS,
                    temp);
            }
            catch (JSONException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            devArray.put(device);
        }
        try
        {
            jo.put(JSON_KEYS.JsonKeyStrings.JSON_CAP_LIST_DEVICES, devArray);
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jo;
    }
    
    /**
     * setup capture for the device given.
     * @param device device name, as shown in list of devices.
     * @throws CaptureEngineException on error.
     */
    public final void setupCapture(
        final String device) throws CaptureEngineException
    {
        StringBuilder errBuf = new StringBuilder();
        pcapObj = Pcap.openLive(device,
            64*GLOBAL_CONSTANTS.KB, Pcap.MODE_PROMISCUOUS,
            0, errBuf);
        
        if(pcapObj == null)
        {
            throw new CaptureEngineException("CRP_CPTURE_ERROR_003",
                new String[]{errBuf.toString()});
        }
        
        // handle filters if any.
        String filterString = "tcp port 80";
        int r = pcapObj.compile(filter, filterString, 0, 0);

        if(r == Pcap.ERROR)
        {
            throw new CaptureEngineException("CRP_CPTURE_ERROR_004", 
                new String[] {filterString, pcapObj.getErr()});
        }
        pcapObj.setFilter(filter); 
        // lets do the packet handler here.
        
        /**
         * packet handler implementation.
         */
        handler = new JPacketHandler<CaptureContextObject>()
        {
            public void nextPacket(final JPacket p,
                final CaptureContextObject cco)
            {
                System.out.println(p.toString());
                if(!p.hasHeader(ethernet))
                {
                    return;
                }
                if(p.hasHeader(tcp))
                {
                    RawPacket rp = new RawPacket(
                        GLOBAL_ENUMS.CRP_CAPTURE_AGENT_TYPE.PCAP);
                    rp.setTCP(true);
                    rp.setPacketObject(p);
                    /**
                     * business of expectedLength?
                     * if the content length is known(usually the higher
                     * protocol, like http) ,
                     * read the content length from the packet and set it.
                     * if the content length is not set by the upper level
                     * protocol,
                     * then set the content length to the tcp.payLoadLength().
                     * if it is just a tcp packet, then it must be a fragment of
                     * the previous packet(or tcp status message);
                     * in this case set the expectedLength to -1.
                     * you should know that all the tcp status packets, should
                     * also have the expectedLength set to -1.
                     */
                    if (p.hasHeader(http))
                    {
                        if(http.hasField(Request.Content_Length))
                        {
                            rp.setExpectedLength(Integer.parseInt(
                                http.fieldValue(Request.Content_Length)));
                        }
                        else if(http.hasField(Response.Content_Length))
                        {
                            rp.setExpectedLength(Integer.parseInt(
                                http.fieldValue(Response.Content_Length)));
                        }
                        else
                        {
                            rp.setExpectedLength(tcp.getPayloadLength());
                        }
                    }
                    else
                    {
                        rp.setExpectedLength(-1);
                    }
                    try
                    {
                        cco.processPacket(rp);
                    }
                    catch (CRPProtocolException e)
                    {
                        CaptureService.CAP_SERVICE_LOG.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
                
            }
        };
    }
    
    /**
     * starts capture.
     * @param cco capture context object.
     */
    public final void startCapture(final CaptureContextObject cco)
    {
        pcapObj.loop(Pcap.LOOP_INFINITE, handler, cco);
    }
    

}
