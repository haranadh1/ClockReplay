"""
sample admin service for galore. 
"""

import time
import cmd
from java.lang import Runnable
from java.lang import Thread

from com.crp.common import GLOBAL_ENUMS, CRPServiceInfo
from com.crp.ether import Photon, NetworkChannel,  MessageWorld, MESSAGE_CODE, EtherException
from com.crp.interfaces import ConnectionInterface

from com.crp.thread import CRPThread, CRPThreadPoolManager
from com.crp.common import GLOBAL_CONSTANTS
from com.crp.process import ProcessMain

class ServiceThread(Runnable):
    """
    crp thread, needed to run all the crp methods.
    
    """
    
    def run(self):
        t = CRPThreadPoolManager.getCRPThread(0)
        t.initMsgPools()
        while (True):
            AdminService().cmdloop()
        
class AdminService(cmd.Cmd):
    """
    class for admin service command line interpreter.

    """
    
    prompt = 'crp_admin>'
    
    def do_prompt(self, line):
        """
        change the default command line prompt
        
        """
        self.prompt = line + '>'
        
    def do_help1(self, line):
        """
        admin_service.py <command> <args>
        for any command help, 
        admin_service.py <command> <help>
        
        """
        
        print line
        
    def do_exit(self, line):
        """
        exit from the shell
        
        """
        return True
    
    def do_connect(self, line):
        """
        connect to any service in CRP.
        connect <service_name>
        service_name can be CAPTURE|REPLAY|DB
        """
        
        ph = Photon(GLOBAL_ENUMS.ETHER_ELEMENT_TYPE.CLIENT, GLOBAL_ENUMS.ETHER_COMMUNICATION_TYPE.REMOTE)
        ph.initializeClient()
        
        t = CRPThreadPoolManager.getCRPThread(0)
        mw = t.getMyMsgWorld()
        m = mw.getFreeMsg(GLOBAL_CONSTANTS.StaticStrings.MSG_REMOTE_CONNECTION_KEY)
        
        ci =  (m.getPayLoadObject())
        ci.setSrcAndDestServices(GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE, GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE)
        csi = CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,"localhost","service handler service",
                             GLOBAL_CONSTANTS.DEFAULT_SERVICE_HANDLER_PORT,0)
        try:
            ph.connect(csi)   
            m.getMessageHeader().setMessageCode(MESSAGE_CODE.MSG_REMOTE_NEW_CONNECTION)
            ph.bsendMessage(m)
            ph.close()
        except EtherException, e:
            print e
            return
        print ph.toString()
        print ci.toString()
        
if __name__ == '__main__':
    ProcessMain().createDummyService()
    t = CRPThreadPoolManager.createNewThread(GLOBAL_ENUMS.SERVICE_CATALOG.DUMMY_HANDLER_SERVICE,
            ServiceThread(),GLOBAL_ENUMS.THREAD_TYPE.SERVICE_MAIN_THREAD, GLOBAL_CONSTANTS.PROCESS_MAIN_THREAD_ID)
    t.start()
    time.sleep(1000000)    
