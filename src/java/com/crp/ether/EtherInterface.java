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

import com.crp.memmgr.ObjectFactory;
import com.crp.pkunpk.PackUnPackException;
import com.crp.pkunpk.Packer;
import com.crp.pkunpk.UnPacker;
/**
 * interface for all the data communications in ether.
 * @author hpoduri
 * @version $Id$
 */

public interface EtherInterface extends ObjectFactory
{
    /**
     * pack object.
     * @param pk packer object
     * @throws PackUnPackException on error.
     */
    public void pack(final Packer pk) throws PackUnPackException;
    /**
     * unpack into this object.
     * @param unp unpacker object.
     * @throws PackUnPackException on error.
     */
    public void unpack(final UnPacker unp) throws PackUnPackException;

}
