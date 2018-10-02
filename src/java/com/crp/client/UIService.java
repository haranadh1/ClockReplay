package com.crp.client;

import com.crp.common.CRPConfig;
import com.crp.common.CRPException;
import com.crp.common.CRPService;
import com.crp.common.CRPServiceInfo;
import com.crp.common.CRPTailorMadeConfigs;
import com.crp.common.GLOBAL_ENUMS;
import com.crp.common.GLOBAL_UTILS;
import com.crp.ether.EtherException;
import com.crp.ether.MESSAGE_CODE;
import com.crp.ether.Message;
import com.crp.ether.Photon;
import com.crp.ether.PhotonPool;
import com.crp.ether.Message.MessageStatus;
import com.crp.interfaces.CRPString;
import com.crp.process.ProcessMain;

/**
 * class to handle all the requests from UI.
 * @author hpoduri
 * @version $Id$
 */
public class UIService extends CRPService
{

    /**
     * photon to connect to remote service.
     */
    private Photon servicePH;
    
    /**
     * db photon.
     */
    private Photon dbPH;
    
    /**
     * photon to connect to capture service.
     */
    private Photon capPH;
    
    /**
     * service handler service info object.
     */
    private CRPServiceInfo crps;
    
    /**
     * constructor.
     * @param inpSi input service info object.
     */
    public UIService(final CRPServiceInfo inpSi)
    {
        super(inpSi);
        dbPH = null;
        servicePH = null;
        capPH = null;
        
        // hard code the service info object for now.
        //TODO : 
        crps = new CRPServiceInfo(
            GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,
            "localhost",
            "db handler service",
            9100, 0);
        crps.forceRemote();
    }
    
    /**
     * initialize necessary crp data structures for non-crp, ui only thread.
     * @throws CRPException on error.
     */
    public final void initForUIServiceOnlyThread() throws CRPException
    {
        CRPTailorMadeConfigs crpt = new CRPTailorMadeConfigs();
        CRPConfig conf = crpt.getUIOnlyConfig();
        ProcessMain.initServices(conf, false);
        // clean up the external thread context if any.
        GLOBAL_UTILS.cleanUpExternalThreadContext();
        GLOBAL_UTILS.CRPifyJavaThread();
    }

    /**
     * returns crp db metadata.
     * @return json string
     * @throws CRPException on error.
     */
    public final String getDBMetadataJsonString() throws CRPException
    {
        CRPServiceInfo dbs = new CRPServiceInfo(
            GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE,
            "localhost",
            "db handler service",
            9100, 0);
        crps.forceRemote();
        if(dbPH == null)
        {
            dbPH = PhotonPool.createPhoton(dbs, false);
        }
        Message m = new Message(MESSAGE_CODE.MSG_REQ_CRP_DB_METADATA);
        
        
        dbPH.sendMessageNoFail(m);

        while ((m = dbPH.brecvMessage()) == null)
        {
            ;
        }
        CRPString crpstring = (CRPString)(m.getPayloadMBO().getObjectAtIndex(0));
        String retString = crpstring.toString();
        m.updateStatus(MessageStatus.AVAILABLE);
        return retString;
    }
    
    /**
     * returns memory manager json string from crp process.
     * @return string representation of mem mgr info json.
     * @throws CRPException on error.
     */
    public final String getMemMgrInfoJsonString() throws CRPException
    {
        if(servicePH == null)
        {
            servicePH = PhotonPool.createShortCircuitedPhoton(crps);
        }
        Message m = new Message(MESSAGE_CODE.MSG_REQ_MEMMGR_INFO);
        
        servicePH.sendMessageNoFail(m);

        while ((m = servicePH.brecvMessage()) == null)
        {
            ;
        }
        CRPString crpstring = (CRPString)(m.getPayloadMBO().getObjectAtIndex(0));
        return crpstring.toString();
    }
    
    /**
     * returns memory manager json string from crp process.
     * @return string representation of mem mgr info json.
     * @throws CRPException on error.
     */
    public final String getThreadPoolInfoJsonString() throws CRPException
    {
               
        if(servicePH == null)
        {
            servicePH = PhotonPool.createShortCircuitedPhoton(crps);
        }
        Message m = new Message(MESSAGE_CODE.MSG_REQ_CRP_THREADS_INFO);
        
        servicePH.sendMessageNoFail(m);

        while ((m = servicePH.brecvMessage()) == null)
        {
            ;
        }
        CRPString crpstring = (CRPString)(m.getPayloadMBO().getObjectAtIndex(0));
        return crpstring.toString();
    }
    
    /**
     * send message to start capture.
     * @throws CRPException on error.
     */
    public final void startCapture() throws CRPException
    {
        if(capPH == null)
        {
            CRPServiceInfo crps = new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
                "localhost",
                "db handler service",
                9100, 0);
            crps.forceRemote();
            
            // create a new photon to connect to capture service.
            capPH = PhotonPool.createPhoton(crps, false);
            Message m = new Message(MESSAGE_CODE.MSG_CS_START_CAPTURE);
            capPH.sendMessageNoFail(m);
            PhotonPool.returnPhotonToPool(capPH);
        }
    }
    
    /**
     * sends stop capture message.
     * @throws CRPException on error.
     */
    public final void stopCapture() throws CRPException
    {
        if(capPH == null)
        {
            CRPServiceInfo crps = new CRPServiceInfo(
                GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
                "localhost",
                "db handler service",
                9100, 0);
            crps.forceRemote();
            
            // create a new photon to connect to capture service.
            capPH = PhotonPool.createPhoton(crps, false);
            Message m = new Message(MESSAGE_CODE.MSG_CS_STOP_CAPTURE);
            capPH.sendMessageNoFail(m);
            PhotonPool.returnPhotonToPool(capPH);
        }
    }
    
    /**
     * shuts down the crp process(services).
     * @throws CRPException on error.
     */
    public final void shutdownCRPProcess() throws CRPException
    {
        if(servicePH == null)
        {
            servicePH = PhotonPool.createShortCircuitedPhoton(crps);
        }
        Message m = new Message(MESSAGE_CODE.MSG_SHUTDOWN);
        
        servicePH.sendMessageNoFail(m);
    }
}
