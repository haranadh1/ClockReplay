package com.crp.capture;

import java.util.BitSet;

import org.jnetpcap.packet.JPacket;

import com.crp.common.GLOBAL_CONSTANTS;
import com.crp.ether.Message;
import com.crp.interfaces.CRPString;
import com.crp.memmgr.MemoryBlockByteBuffer;
import com.crp.memmgr.MemoryManagerException;

/**
 * class to contain all the necessary info used only during the capture.
 * should not use this class by any other module other than capture.
 *
 */
public class CaptureENVForCaplet
{

    /**
     * msg object, in which this caplet object is in.
     */
    private Message     embeddedMsg;

    /**
     * expected length of this caplet.
     */
    private int         expectedLength;

    /**
     * received length so far.
     * should always be < expectedLength.
     */
    private int         receivedLength;
    
    /**
     * initial sequence number, in case of tcp.
     */
    private long         isn;
    
    /**
     * store all the fragments in one crp string.
     * this should ideally be set to the crp string object
     * in the caplet object.
     */
    private CRPString fragString;
    
    /**
     * bit map index for the fragments.
     * size of bit map index = 
     * expectedLength/(512:LOBAL_CONSTANTS.MAX_FRAGMENT_SIZE).
     * if a fragments data is received, then the
     * corresponding bit is set.
     */
    private BitSet    fragBitMap;
    /**
     * during capture, data may come in fragments. this array stores all the
     * packets indexed by isn + <array index>
     */
    private CRPString[] fragments;

    /**
     * number of fragments.
     */
    private int         numFragments;

    /**
     * constructor.
     */
    public CaptureENVForCaplet(final MemoryBlockByteBuffer inpMBB,
        final CRPString inpStr)
    {
        embeddedMsg = null;
        isn = -1;
        expectedLength = -1;
        fragments = new CRPString[GLOBAL_CONSTANTS.MAX_FRAGMENTS_IN_A_CAPLET];
        fragString = inpStr;
        /**
         * create crp string objects.
         *
        for(int i = 0; i < GLOBAL_CONSTANTS.MAX_FRAGMENTS_IN_A_CAPLET; i++)
        {
            fragments[i] = new CRPString(inpMBB);
        }
        */
        numFragments = 0;
        /**
         * One bit is enough for each fragment.
         * that is we can support
         * GLOBAL_CONSTANTS.MAX_FRAGMENTS_IN_A_CAPLET * 8 * MAX_FRAGMENT_SIZE
         * size of http payload.
         */
        fragBitMap = new BitSet(GLOBAL_CONSTANTS.MAX_FRAGMENTS_IN_A_CAPLET * 8);
    }

    /**
     * set the message in which this caplet is in.
     * @param inpMsg message object.
     */
    public final void setCapletMsg(final Message inpMsg)
    {
        embeddedMsg = inpMsg;
    }

    /**
     * set attributes.
     * @param expLen expected length of this caplet.
     * @param inpISN initial sequence number if any.
     * @throws MemoryManagerException on error.
     */
    public final void setAttr(final int expLen,
        final long inpISN) throws MemoryManagerException
    {
        assert(fragString != null);
        expectedLength = expLen;
        isn = inpISN;
        // here is the trick.
        // we dont really write the expLen bytes in the buffer, however
        // we create space of length expLen. store the local offset,
        // and also the length in the CRPString object.
        // dont forget to set the flag as PARTIAL_DATA.
        fragString.setOffset(fragString.getMBB().getLocalOffset());
        fragString.setLength(expLen);
        fragString.getMBB().setLocalOffset(fragString.getMBB().getLocalOffset() + expLen);
        fragString.getMBB().finishWritingRecord(expLen);
        fragString.setPartialData();
    }
    
    /**
     * returns expected length.
     * @return int.
     */
    public final int getExpectedLength()
    {
        return expectedLength;
    }
    
    /**
     * received length so far.
     * @return int.
     */
    public final int getReceivedLength()
    {
        return receivedLength;
    }
    /**
     * reset env.
     */
    public final void reset()
    {
        isn = -1;
        expectedLength = -1;
        receivedLength = -1;
        numFragments = 0;
    }
    
    /**
     * adds a fragment to this caplet.
     * @param p input packet to be added.(JPacket for pcap)
     * @param plOffset offset in the packet to be considered for copy.
     * @param plLen payload length.
     * @param seq sequence number of this fragment in the packet.(tcp seq).
     * @throws MemoryManagerException on error.
     * @return true if we received expected size of the packet, or false.
     */
    public final boolean addFragment(final JPacket p,
        final int plOffset, final int plLen,
        final long seq) throws MemoryManagerException
    {
        int offset = (int) (seq - isn);
        
        int bitIndex = 0;
        if(offset != 0)
        {
            bitIndex = expectedLength / offset;
        }
        // first check if this fragment is already received.
        if(fragBitMap.get(bitIndex))
        {
            // we received this packet already.
            p.getByteArray(plOffset, fragString.getMBB().getPoolByteArray(),
                fragString.getOffset(),
                plLen);
            // dont update the received length.
            return false;
        }
        p.getByteArray(plOffset, fragString.getMBB().getPoolByteArray(),
            fragString.getOffset(),
            plLen);
        fragBitMap.set(bitIndex);
        receivedLength += plLen;
        if(receivedLength == expectedLength)
        {
            return true;
        }
        if(receivedLength > expectedLength)
        {
            CaptureService.CAP_SERVICE_LOG.error(
                " capture packet: Received Length: "
                + String.valueOf(receivedLength) 
                + " is greater than expected length: "
                + String.valueOf(expectedLength));
            // something terribly wrong.
            assert(false);
            
        }
        return false;
        /**
         * CRPString fragment = fragments[numFragments++];
        
        fragment.setFragmentOffset(seq);
        p.getByteArray(plOffset,
            fragment.getMBB().getPoolByteArray(),
            fragment.getMBB().getLocalOffset(),
            plLen);
        
        // need to update necessary mem mgr housekeeping stuff.
        // we have to this ugly stuff, as the payload buffer is
        // filled by the pcap api.
        fragment.setLength(
            plLen);
        fragment.setOffset(
            fragment.getMBB().getLocalOffset());
        
        fragment.getMBB().setLocalOffset(
            fragment.getMBB().getLocalOffset() 
            + plLen);
      
        fragment.getMBB().finishWritingRecord(plLen); */
    }
    
    /**
     * return fragments array.
     * @return CRPString [].
     */
    public final CRPString [] getFragments()
    {
        return fragments;
    }
    
    /**
     * returns message that this caplet is in.
     * @return Message object.
     */
    public final Message getEmbedMsg()
    {
        return embeddedMsg;
    }
}