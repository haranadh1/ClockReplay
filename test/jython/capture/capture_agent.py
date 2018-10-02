
"""
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
 
"""
this file implements a sample capture agent
captures web server traffic.

"""

from com.crp.common import GLOBAL_ENUMS, CRPServiceInfo
from com.crp.ether import Photon, NetworkChannel,  MessageWorld, MESSAGE_CODE, EtherException
from com.crp.interfaces import ConnectionInterface

from com.crp.thread import CRPThread, CRPThreadPoolManager
from com.crp.common import GLOBAL_CONSTANTS
from com.crp.process import ProcessMain

