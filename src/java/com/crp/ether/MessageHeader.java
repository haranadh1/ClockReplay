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

import java.io.IOException;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.PackUnPackInterface;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;

/**
 * Message Header class.
 * @author hpoduri
 * @version $Id$
 */
public class MessageHeader implements PackUnPackInterface
{
    /**
     * message category definition. 
     */
    public enum MessageCategory 
    {
        SHORT,
        NORMAL,
    }
    /**
     * message category.
     */
    private MessageCategory mc;
    /**
     * message type for the message.
     */
    private int messageCode;
    /**
     * number of objects in this message.
     */
    private int size;
    
    /**
     * constructor.
     * @param inpMsgCode input message type.
     * @param inpMc Message Category(SHORT/NORMAL)
     */
    public MessageHeader( final int inpMsgCode, final MessageCategory inpMc )
    {
        assert(inpMsgCode != MESSAGE_CODE.INVALID_MESSAGE);
        messageCode = inpMsgCode;
        size = -1;
        mc = inpMc;
    }
    /**
     * constructor.
     * @param inpMsgType input message type.
     * @param inpLength length of the message(including header)
     */
    public MessageHeader( final int inpMsgType, final int inpLength )
    {
        messageCode = inpMsgType;
        size = inpLength;
    }
    /**
     * set message size.
     * @param inpLen number of objects in the payload.
     */
    public final void setSize( final int inpLen )
    {
        assert(inpLen >=0);
        size = inpLen;
    }
    /**
     * returns message type.
     * @return message type.
     */
    public final int messageCode()
    {
        assert(messageCode != MESSAGE_CODE.INVALID_MESSAGE);
        return messageCode;
    }
    
    /**
     * returns message length(number of objects).
     * @return length.
     */
    public final int size()
    {
        assert(size != -1);
        return size;
    }
    
    /**
     * serializes the message header into a byte stream and returns the same.
     * @param pk Packer object into which the message header is to be
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
            pk.packFixedInt32(this.messageCode);
            pk.packFixedInt32(this.size);
        }
        catch(IOException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_014",
                null, this.zipString());
        }
    }
    
   
    /**
     * sets message code.
     */
    public final void setMessageCode(final int inpMC)
    {
        messageCode = inpMC;
    }
    @Override
    public int getPackedBufferSize()
    {
        // TODO Auto-generated method stub
        return (GLOBAL_CONSTANTS.INT_SIZE * 2);
    }
    @Override
    public void unpack(final UnPacker unp) throws PackUnPackException
    {
        try
        {
            this.messageCode = unp.unpackFixedInt32();
            this.size = unp.unpackFixedInt32();
        }
        catch(IOException e)
        {
            Packer.PCK_LOG.error(e.getMessage());
            throw new PackUnPackException("CRP_PKUNPK_ERROR_015",
                null, this.zipString());
        }   
    }
    
    /**
     * returns a string.
     * @return string rep of this class.
     */
    public final String zipString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("mc = " + this.messageCode);
        sb.append("length = " + this.size);
        return sb.toString();
    }
}
