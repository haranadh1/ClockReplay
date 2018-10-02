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
/**
 * class that represents a generic ether data.
 * @author hpoduri
 * @version $Id$
 */

public class EtherData
{
    /**
     * type of ether data.
     */
    public static enum EtherDataType
    {
        /**
         * If the data is of message type.
         */
        MESSAGE_TYPE,
        /**
         * if the data is none.
         */
        NONE_TYPE,
        /**
         * if the data is string data.
         */
        STRING_TYPE,
    }
    /**
     * photon object corresponds to ether data.
     * if not applicable, null.
     */
    private Photon ph;
    
    /**
     * ether data type.
     */
    private EtherDataType edt;
    /**
     * ether data.
     */
    private Object data;
    /**
     * constructor.
     * @param inpEdt input ether data type.
     * @param inpPH photon corresponds to this ether data object.
     */
    public EtherData( final EtherDataType inpEdt, 
        final Photon inpPH)
    {
        edt = inpEdt;
        data = null;
        ph = inpPH;
    }
    /**
     * sets message ether data.
     * @param m message. 
     */
    public final void setData(final Message m)
    {
        data = m;
    }
    /**
     * sets photon ether data.
     * @param p input photon object.
     */
    public final void setData(final Photon p)
    {
        data = p;
    }  
    /**
     * returns ether data type.
     * @return edt.
     */
    public final EtherDataType getType()
    {
        return edt;
    }
    /**
     * returns data.
     * @return returns data, should cast it
     * based on the edt.
     */
    public final Object getData()
    {
        return data;
    }
    /**
     * returns photon for this ether data object.
     * @return photon.
     */
    public final Photon getPhoton()
    {
        return ph;
    }
}
