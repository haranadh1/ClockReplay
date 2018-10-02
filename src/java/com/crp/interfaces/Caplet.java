/*
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains
 * the property of ClockReplay Incorporated and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or
 * copyright law.
 * Dissemination of this information or reproduction of this
 * material is strictly forbidden unless prior written
 * permission is obtained from ClockReplay Incorporated.
 */
package com.crp.interfaces;

import java.io.IOException;

import com.crp.capture.CaptureENVForCaplet;
import com.crp.common.CommonLogger;
import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.db.DBService;
import com.crp.ether.Message;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManagerException;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * Generic caplet implementation.
 * serialize the caplet header/data into a byte stream
 * we keep the Caplet in packed state as long as we can
 * we will only deserialize it into CapletHeader when
 * required
 */

public class Caplet extends AbstractInterfaceBase
{
    /**
     * 4 byte prefixed length for payload.
     */
    public static final int PAYLOAD_PREFIX_LENGTH_PACKED_SIZE = 4;
    /**
     * caplet = CapletHeader + payload.
     */
    private CapletHeader ch;
    /**
     *  size = data + header.
     */
    private CRPString payload;

    /**
     * variable to represent caplet fill status in a db page.
     */
    private CapletFillTracker cft;
    
    /**
     * capture environment.
     * data structures specific to capture are stored in this obj.
     */
    private CaptureENVForCaplet cENV;
    
    /**
     * default constructor.
     * @param inpMBB input mbb for var length fields.
     */
    public Caplet(final MemoryBlockByteBuffer inpMBB)
    {
        ch = new CapletHeader();
        payload = new CRPString(inpMBB);
        cft = new CapletFillTracker();
        cENV = new CaptureENVForCaplet(inpMBB, payload);
    }
    /**
     * constructor.
     * @param inpCH : CapletHeader
     * @param inpData : data;
     * @param inpMBB : input memory block byte buffer, used for var data.
     *        NOTE: while unpacking a caplet, the static unpack method sets the
     *        data to the entire buffer (CapletHeader + data)
     *        However, while initializing the caplet for packing,
     *        the inpData should contain the data(payload only)
     */

    public Caplet(final CapletHeader inpCH, final byte[] inpData,
        final MemoryBlockByteBuffer inpMBB)
    {
        if(inpCH == null)
        {
            ch = new CapletHeader();
        }
        else
        {
            ch = inpCH;
        }
        payload = new CRPString(inpMBB);
        if(inpData != null)
        {
            payload.setContent(inpData);
        }
        cft = new CapletFillTracker();
        cENV = new CaptureENVForCaplet(inpMBB, payload);

    }

    /**
     * returns current caplet object size in memory.
     * @return caplet size (int)
     */
    public final int getCapletSize()
    {
        return (ch.getSize() + payload.getObjSize());
    }

    /**
     * serializes the current caplet object into byte stream.
     * @param pk : Packer object where the Caplet is folded into
     * @throws PackUnPackException on error.
     */
    public final void pack(final Packer pk) throws PackUnPackException
    {
        ch.setLength(payload.length());
        
        
        ch.pack(pk);
        payload.pack(pk);

        assert(ch.getLength() == payload.length());
    }

    /**
     * construct the current caplet object from the provided unpacker.
     * @param unpk the unpacker
     * @throws PackUnPackException on error.
     */
    public final void unpack(final UnPacker unpk) throws PackUnPackException
    {
        ch.unpack(unpk);
        payload.unpack(unpk);
        assert(ch.getLength() >= payload.length());

    }

    /**
     * setting attributes.
     * @param remoteHost
     * @param remotePort
     * @param remoteIP
     * @param ts
     * @return true / false.
     */
    public final boolean setAttr(final String remoteHost,
        final int remotePort, final String remoteIP,
        final long ts)
    {
        ch.setTimestamp(ts);
        ch.setRemoteHost(remoteHost.getBytes());
        ch.setRemoteIP(remoteIP.getBytes());
        ch.setRemotePort(remotePort); 
        return (true);
    }
    
    /**
     * setting attributes.
     * @param remoteHost
     * @param remotePort
     * @param remoteIP
     * @param ts
     * @param inpData
     */
    public final boolean setAttr(final String remoteHost,
        final int remotePort, final String remoteIP,
        final long ts, final byte[] inpData)
    {
        setAttr(remoteHost, remotePort, remoteIP, ts);
        boolean retFlag = payload.setContent(inpData);
        try
        {
            payload.getMBB().finishWritingRecord(inpData.length);
        }
        catch(MemoryManagerException e)
        {
            CommonLogger.CMN_LOG.error(e.getMessage());
            retFlag = false;
        }
        return (retFlag);
    }
    /**
     * @return the ch
     */
    public final CapletHeader getCapletHeader() 
    {
        return ch;
    }

    /**
     * @param inpPayLoad the payload to set
     */
    public final void setPayload(final byte[] inpPayLoad) 
    {
        this.payload.setContent(inpPayLoad);
    }

    /**
     * to string implementation.
     * @return ret : string representation of the class
     */
    public final String toString()
    {
        String ret = " Class : Caplet" + "\n" + this.ch + "\n payload len "
            + String.valueOf(this.payload.getObjSize());
        return ret;
    }

    @Override
    public String getMyName()
    {
        // TODO Auto-generated method stub
        return GLOBAL_CONSTANTS.CRPObjNameStrings.CAPLET_KEY;
    }

    @Override
    public int getObjSize()
    {
        return CapletHeader.getSize() + GLOBAL_CONSTANTS.INT_SIZE;
    }

    @Override
    public void resetObject(Object obj)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean anyVarFields()
    {
        return true;
    }
    @Override
    public int getPackedBufferSize()
    {
        return (ch.getPackedBufferSize() + payload.getPackedBufferSize());
    }
    /**
     * returns pay load object.
     * @return crp string.
     */
    public final CRPString getPayLoadObject()
    {
        return payload;
    }
    @Override
    public Object createObjectInstance(final MemoryBlockByteBuffer mbb)
    {
        return (new Caplet(mbb));
    }
    @Override
    public boolean anyRoomForObjOfVarLength(final int length)
    {
        assert(getMBB()!= null);
        if(getMBB().remainingSpace() >= length )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    @Override
    public int getVarLenFieldsSize()
    {
        return payload.getVarLenFieldsSize();
    }
    
    /**
     * returns caplet fill tracker object.
     * @return cft object.
     */
    public final CapletFillTracker getCFT()
    {
        return cft;
    }
    
    /**
     * reads caplet payload length from the unpacker.
     * @param unp unpacker to read from.
     * @param startingPosition starting position in the 
     * unpacker mbb, from where the caplet starts.
     * @return length.
     * @throws PackUnPackException on error.
     */
    public final int readPayloadLength(
        final UnPacker unp,
        final int startingPosition) throws PackUnPackException
    {
        int retLen = 0;
        int tempVal = unp.getCurrentPosition();
        unp.setPosition(tempVal + ch.getPackedBufferSize());
        //read integer length.
        try
        {
            retLen = unp.unpackFixedInt32();
        }
        catch (IOException e)
        {
            UnPacker.UNP_LOG.error(e.getMessage());
            throw new PackUnPackException(
                "CRP_PKUNPK_ERROR_005", null, unp.toString());
        }
        unp.setPosition(tempVal);
        return retLen;
    }
    
    /**
     * compare two caplets.
     * @param c1 caplet obj
     * @param c2 caplet ob
     * @return true/false fi c1=c2/!=
     */
    
    public static boolean compare(
        final Caplet c1, final Caplet c2)
    {
        // first compare caplet headers.
        if( (c1.getCapletHeader().getTimestamp() 
                == c2.getCapletHeader().getTimestamp()) 
                && (c1.getCapletHeader().getLength() 
                        == c2.getCapletHeader().getLength()))
        {
            return true;
        }
        else
        {
            return false;
        }         
    }
    
    /**
     * returns string representation of this object.
     * @return string.
     */
    public final String zipString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("------------------------------");
        sb.append(ch.toString());
        sb.append("\n Pay Load: \n");
        sb.append(payload.toString());
        sb.append("------------------------------");
        return sb.toString();
    }
    
    /**
     * returns true if this caplet is fragmented.
     * used only during the capture.
     * @return true/false.
     */
    public final boolean isFragmented()
    {
        if(cENV.getExpectedLength() > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    /**
     * returns CaptureENVForCaplet object.
     * @return cENV
     */
    public final CaptureENVForCaplet getCaptureENV()
    {
        return cENV;
    }
    
    /**
     * coalesce all the fragments into the payload object.
     */
    public final void coalesce()
    {
        for(int i = 0; i < cENV.getFragments().length; i++)
        {
            if(cENV.getFragments()[i].length() > 0)
            {
                // TODO : I dont think we need this method; lets revisit.
            }
        }
        
    }
    /**
     * class to represent what part of the caplet is filled in the db page. 
     */
    public class CapletFillTracker
    {    
        /**
         * offset in caplet up to where the caplet contents are added.
         * any non negative value in offset is considered.
         * -1 is used to represent full caplet to be written.
         */
        private int offset;
        
        /**
         * this variable is used to store the page appender length.
         * which is useful if a caplet is spanned across multiple pages.
         * we use it just a place holder before filling the caplet in 
         * multiple pages.
         */
        private int pageAppenderLen;
        
        /**
         * constructor.
         */
        public CapletFillTracker()
        {
            offset = -1;
            pageAppenderLen = 0;
        }
        /**
         * sets offset to the value upto which caplet's contents written.
         * @param inputOffset input offset.
         */
        public final void setOffset(final int inputOffset)
        {
            offset = inputOffset;
        }
        
        /**
         * returns offset.
         * @return returns offset.
         */
        public final int getOffset()
        {
            return offset;
        }

        /**
         * sets page appender length.
         * @param addPageHeaderLen 
         */
        public final void setPageAppenderLen(final int addPageHeaderLen)
        {
            pageAppenderLen = addPageHeaderLen;
            
        }
        
        /**
         * returns page appender length.
         * @return int.
         */
        public final int getPageAppenderLen()
        {
            return pageAppenderLen;
        }
    }

   
    @Override
    public void reset()
    {
        cft.setOffset(0);
        cft.setPageAppenderLen(0);
        payload.reset();
    }
    @Override
    public void reset(MemoryBlockByteBuffer inpMBB)
    {
        reset();
        payload.reset(inpMBB);
        
    }
    
    
    
}
