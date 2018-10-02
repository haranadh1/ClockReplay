package com.crp.protocol;

import com.crp.common.RawPacket;
import com.crp.ether.Message;
import com.crp.interfaces.Caplet;

/**
 * interface to create a protocol in CRP.
 * @author hpoduri
 * @version $Id$
 */
public abstract class CRPProtocol
{
    /**
     * returns name of this protocol.
     * ideally this should be overridden by the protocol implementations.
     * @return name of this protocol.
     */
    public abstract String getName();
    
    /**
     * one line description about this protocol.
     * @return String.
     */
    public abstract String getDesc();

    /**
     * raw packet handler.
     * only the internal protocols need to implement this method.
     * @param rp raw packet object.
     * @return Message object(if a message object is full).
     * @throws CRPProtocolException on error.
     */
    public abstract Message handlePacket(final RawPacket rp)
        throws CRPProtocolException;
    
    /**
     * process a caplet. it can change the caplet data too.
     * @param c caplet to be processed.
     * @return Message object, if a message is filled with caplets
     * or null.
     * @throws CRPProtocolException on error.
     */
    public abstract Message processCaplet(
        final Caplet c) throws CRPProtocolException;
    
    /**
     * high level method that returns caplets with a msg object.
     * @return Message object, contains a list of caplets.
     */
    public abstract Message getNextCapletMessage();
}
