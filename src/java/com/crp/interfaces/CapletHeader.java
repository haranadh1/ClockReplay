/**
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

package com.crp.interfaces;
import java.io.IOException;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.db.DBService;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.PackUnPackInterface;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * this file implements the generic caplet header.
 *
 */

public class CapletHeader implements PackUnPackInterface
{

	/**
	 * In case we want to use the java serialization, we should have
	 * a mechanism of detecting the changes in the class layout that
	 * might affect the deserialization.
	 */
    private static final long serialVersionUID = 1L;

    public static final int   CAPLET_HEADER_SIZE = 20;

    /**
     * remote machine name.
     */
    private byte[]         remoteHost;
    /**
     * remote IP host name.
     */
    private byte[]         remoteIP;
    /**
     * remote port.
     */
    private int               remotePort;

    /**
     * long is used for timestamp. we represent the timestamp as long variable
     * and java dont have typedef :-(
     */
    private long              ts;

    /**
     * length of the caplet.
     */
    private int               length;
	
    /**
     * Caplet status.
     * each bit represents a status.
     */
    private byte cStatus;
    
    /**
     * possible status fields.
     */
    /**
     * invalid status.
     */
    public static final byte INVALID_STATUS = (byte) 0x00;
    /**
     * Reps request caplet.
     */
    public static final byte INCOMPLETE_CAP = (byte) 0x01;
    /**
     * reps read request.
     */
    public static final byte READ_REQ = (byte) 0x02;
    /**
     * reps write request.
     */
    public static final byte WRITE_REQ     = (byte) 0x04;
    /**
     * reps response caplet.
     */
    public static final byte RESPONSE_CAP = (byte) 0x08;
    
    /**
     * represents tcp session start.
     */
    public static final byte TCP_START = (byte) 0x10;
    
    /**
     * represents tcp session close.
     */
    public static final byte TCP_CLOSE = (byte) 0x20;
    
	/**
	 * indicates if a caplet's payload is spilled over multiple pages.
	 * @author hpoduri
	 *
	 */
    private enum CapletFillStatus
    {
        /**
         * invalid entry.
         */
        INVALID,
        /**
         * a caplet can fit in a db page.
         */
        COMPLETE,
        /**
         * indicates if caplet is carried over to next page.
         */
        PARTIAL;
        
        /**
         * Method to map int to enum.
         * @param arg integer to be converted to enum
         * @return enum.
         */
        public  static CapletFillStatus intToCFS(final int arg)
        {
            if(arg == CapletFillStatus.COMPLETE.ordinal())
            {
                return CapletFillStatus.COMPLETE;
            }
            else if( arg == CapletFillStatus.PARTIAL.ordinal())
            {
                return CapletFillStatus.PARTIAL;
            }
            else
            {
                return CapletFillStatus.INVALID;
            }
        }
    }
	
	/**
	 * caplet fill status.
	 */
    private CapletFillStatus cfs;  
 
	/**
	 * there are always some potential risks need to be considered
	 * while adding new variable here;
	 * should make sure that serialize and deserialize
	 * conforming to that change.
	 * It is always better to add a variable in the end as opposed to
	 * adding it in the middle;
	 */

	/**
	 * constructor for caplet header.
	 * @param inpRH  : remote hostname
	 * @param inpRIP : remote IP
	 * @param inpTS  : timestamp in milli/nano seconds
	 * @param inpRP  : remote port
	 * @param len    : length of caplet
	 */
    public CapletHeader(final String inpRH,
        final String inpRIP, final int inpRP,
        final long inpTS, final int len)
    {
        this.ts = inpTS;
        
        this.remoteHost = new byte [GLOBAL_CONSTANTS.HOST_SIZE];
        this.remoteIP = new byte [GLOBAL_CONSTANTS.IP_SIZE];
        this.remotePort = inpRP;
        this.length = len;
        cfs = CapletFillStatus.COMPLETE;
        cStatus = CapletHeader.INVALID_STATUS;
    }

    /**
     * another way of instantiating the caplet header.
     */
    public CapletHeader()
    {
        this.ts = -1;
        
        this.remoteHost = new byte [GLOBAL_CONSTANTS.HOST_SIZE];
        
        this.remoteIP = new byte [GLOBAL_CONSTANTS.IP_SIZE];
        cfs = CapletFillStatus.COMPLETE;
        cStatus = CapletHeader.INVALID_STATUS;
    }
	/**
	 * get the size of the CapletHeader instance.
	 * @return size of the CapletHeader(int)
	 */
    public static final int getSize()
    {
        return (GLOBAL_CONSTANTS.HOST_SIZE
                + GLOBAL_CONSTANTS.IP_SIZE + GLOBAL_CONSTANTS.INT_SIZE
            + GLOBAL_CONSTANTS.LONG_SIZE 
            + GLOBAL_CONSTANTS.INT_SIZE + GLOBAL_CONSTANTS.INT_SIZE);
    }
	
	/**
	 * serializes the caplet header into a byte stream and returns the same.
	 * @param pk Packer object into which the caplet header is to be
	 * packed into.
	 * the caller of this method should make sure that the packer is set
	 * to correct size, this pack method assumes enough size exists in
	 * Packer object
	 * @throws PackUnPackException on error.
	 */
    public final void pack(final Packer pk) throws PackUnPackException
    {
        try
        {
            //DBService.DB_SERVICE_LOG.info(" packing caplet payload len: " 
            //    + String.valueOf(length));
            pk.packFixedInt32(length);
            pk.packBytesFixedWidth(remoteHost);
            
            pk.packBytesFixedWidth(remoteIP);
            pk.packFixedInt32(remotePort);
            pk.packByte(cStatus);
            pk.packFixedInt32(cfs.ordinal());
            pk.packFixedInt64(ts);

        }
        catch (IOException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_010", null,
                this.toString());
        }
    }
	/**
	 * Unpack CapletHeader from given UnPacker object.
	 * @param unp UnPacker object from which ch should be unpacked
	 * @throws PackUnPackException on error.
	 */
    public final void unpack(final UnPacker unp) throws PackUnPackException
    {
        try
        {
            length = unp.unpackFixedInt32();
            //DBService.DB_SERVICE_LOG.info(" unpacked caplet payload size: " 
            //    + String.valueOf(length));
            unp.unpackBytesFixedWidth(remoteHost,
                GLOBAL_CONSTANTS.HOST_SIZE,  0);
            unp.unpackBytesFixedWidth(remoteIP, GLOBAL_CONSTANTS.IP_SIZE,  0);
            remotePort = unp.unpackFixedInt32();
            cStatus = unp.unpackByte();

            cfs = CapletFillStatus.intToCFS(unp.unpackFixedInt32());
            ts = unp.unpackFixedInt64();
        }
        catch (IOException e)
        {
            UnPacker.UNP_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_011", null,
                this.toString());
        }
    }

    /**
     * @return the remoteHost
     */
    public final String getRemoteHost() 
    {
        return remoteHost.toString();
    }

    /**
     * @param inpRHost the remoteHost to set
     */
    public final void setRemoteHost(final byte[] inpRHost) 
    {
        assert(inpRHost.length <= GLOBAL_CONSTANTS.HOST_SIZE);
        int len = 0;
        if(inpRHost.length < GLOBAL_CONSTANTS.HOST_SIZE)
        {
            len = inpRHost.length;
        }
        else
        {
            len = GLOBAL_CONSTANTS.HOST_SIZE;
        }
        System.arraycopy(inpRHost, 0, remoteHost, 0, len);

    }

    /**
     * @param inpRIP the remoteIP to set
     */
    public final void setRemoteIP(final byte[] inpRIP) 
    {
        assert(inpRIP.length <= GLOBAL_CONSTANTS.IP_SIZE);
        int len = 0;
        if(inpRIP.length < GLOBAL_CONSTANTS.IP_SIZE)
        {
            len = inpRIP.length;
        }
        else
        {
            len = GLOBAL_CONSTANTS.IP_SIZE;
        }
        System.arraycopy(inpRIP, 0, remoteIP, 0, len);
       
    }

    /**
     * @return the remotePort
     */
    public final int getRemotePort() 
    {
        return remotePort;
    }

    /**
     * @param remotePort the remotePort to set
     */
    public final void setRemotePort(final int remotePort)
    {
        this.remotePort = remotePort;
    }

    /**
     * @return the ts
     */
    public final long getTimestamp() 
    {
        return ts;
    }

    /**
     * @param ts the ts to set
     */
    public final void setTimestamp(final long ts) 
    {
        this.ts = ts;
    }

    /**
     * @return the length
     */
    public final int getLength() 
    {
        return length;
    }

    /**
     * @param length the length to set
     */
    public final void setLength(final int length) 
    {
        this.length = length;
    }

    /**
     * @return the status
     */
    public final CapletFillStatus getStatus() 
    {
        return cfs;
    }

    /**
     * @param inpCFS the status to set
     */
    public final void setCapletFillStatus(final CapletFillStatus inpCFS) 
    {
        this.cfs = inpCFS;
    }
    
	/**
	 * tostring implementation.
	 * @return ret : string representation of the class
	 */
    public final String toString()
    {
        String ret = " Class : CapletHeader" + "\nremote host = "
            + this.remoteHost.toString() 
            + "\n remote ip " + this.remoteIP.toString()
            + "\n timestamp " + Long.toString(this.ts);
        return ret;
    }

    /**
     * makes the current caplet of type request.
     */
    public final void setReadRequestType()
    {
        cStatus = (byte) (cStatus | CapletHeader.READ_REQ);   
    }
    /**
     * makes the current caplet of type response.
     */
    public final void setResponseType()
    {
        cStatus = (byte) (cStatus | CapletHeader.RESPONSE_CAP);   
    }
    /**
     * makes the current caplet as read operation.
     */
    public final void setReadType()
    {
        cStatus = (byte) (cStatus | CapletHeader.READ_REQ);
    }
    /**
     * makes the current caplet as write operation.
     */
    public final void setWriteType()
    {
        cStatus = (byte) (cStatus | CapletHeader.WRITE_REQ);
    }
    
    /**
     * makes the tcp/or higher level protocol session start.
     */
    public final void setSessionStart()
    {
        cStatus = (byte) (cStatus | CapletHeader.TCP_START);
    }
    
    /**
     * session close.
     */
    public final void setSessionClose()
    {
        cStatus = (byte) (cStatus | CapletHeader.TCP_CLOSE);
    }

    public final void setIncompleteCaplet()
    {
        cStatus = (byte) (cStatus | CapletHeader.INCOMPLETE_CAP);
    }
    @Override
    public int getPackedBufferSize()
    {
        //count all the members size.
        return (remoteHost.length
                + remoteIP.length + GLOBAL_CONSTANTS.INT_SIZE
            + GLOBAL_CONSTANTS.LONG_SIZE 
            + GLOBAL_CONSTANTS.INT_SIZE + GLOBAL_CONSTANTS.INT_SIZE + 1 );
        // 1: is for the caplet status byte.
    }
    
}
