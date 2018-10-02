"""
 **
 * Copyright (C) 2011, ClockReplay, Inc. All Rights Reserved. NOTICE: All
 * information contained herein is, and remains the property of ClockReplay
 * Incorporated and its suppliers, if any. The intellectual and technical
 * concepts contained herein are proprietary to ClockReplay Incorporated and its
 * suppliers and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from ClockReplay Incorporated.
 *
 """
 
from com.crp.process import ProcessMain
from com.crp.common import CRPServiceInfo 
from com.crp.common import GLOBAL_ENUMS
from com.crp.common import CRPTailorMadeConfigs
from com.crp.process import PROCESS_GLOBALS
from com.crp.capture import CaptureService
 
def create_crp_process():
     """
     creates a crp process data structures and initializes service handler
     
     """
     
     crpt = CRPTailorMadeConfigs()
     config = crpt.getAllInOneConfig()
     ProcessMain.initServices(config)
     