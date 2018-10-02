"""
this file implements a http client interface for crp test engine
the idea is to be able to create many http client threads, 
constantly generating the http traffic, so that the capture
agent can capture decent data.

"""

import httplib
import threading
import random
import sys
import getopt

SERVER_PORT = 8107
SERVER_URI = 'localhost:%s' %str(SERVER_PORT)
ROWS = COLS = 80

class HttpClientThread(threading.Thread):
    """    
    this class implements the http client thread functionality
    the main program can instantiate as many client threads as
    configured

    """
    
    def __init__(self, thread_counter):
        threading.Thread.__init__(self, name = "HttpClientThread_%s" %str(thread_counter))
        
    
    def run(self):
        """
        thread main function; each thread does some basic operations
                        - reserve
                        - cancel
                        - display
                        - index
        in random order
        """
        
        conn = httplib.HTTPConnection(SERVER_URI)
        
        conn.connect()
        print "connection established"
        for i in xrange(100000):
            rand = random.randint(0,2)
            if rand == 1 :
                row = random.randint(0, ROWS -1)
                col = random.randint(0, COLS -1)
                row_col = '/'+ str(row) + '_' + str(col)
                conn.request("POST", "/reserve_slot" + row_col )
            else:
                row = random.randint(0, ROWS -1)
                col = random.randint(0, COLS -1)
                row_col = '/'+ str(row) + '_' + str(col)
                conn.request("POST", "/cancel_slot" + row_col)
            res = conn.getresponse()
            print res.status, res.reason
            data =res.read()
            conn.close()
            
usage = """
    
    python web_client -p <server port number> -t < number of client threads >
ex: python web_client -p 8107 -t 4    

important information on windows xp:
------------------------------------ 
please make sure that you do the following to increase the ports on
windows xp machine. make sure you add to decimal value(not hex)
HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Tcpip\Parameters
Value name:MaxUserPort
Value data:<Enter a decimal value between 5000 and 65534 here>
Value name : TcpTimedWaitDelay

Value data: <Enter a decimal value between 30 and 240 here>
unfortunately, you should restart windows xp client after the registry 
changes :-) :-)
"""

if __name__ == '__main__' :

    e  = None
    thread_count = 0
    if(len(sys.argv)<2):
        print usage
        sys.exit()
    try:
        opts, xargs = getopt.getopt(sys.argv[1:], "hp:t:", ["help", "port=" , "threads="])
   
    except ( getopt.GetoptError, e ):
        print ( "Invalid Option(s) Specified [%s]", str( e ) )
        print usage
        sys.exit() 
    for opt, arg in opts:
        
        if opt in [ "-h", "--help" ]:
            print usage
            sys.exit()
        elif opt in [ "-p", "--port" ]:
            SERVER_PORT = int(arg)   
        elif opt in [ "-t", "--threads" ]:
            thread_count = int(arg)
        
    SERVER_URI = 'localhost:%s' %str(SERVER_PORT)
    thread_pool = []
    for i in range(thread_count):
        thread_pool.append(HttpClientThread(i))
    for i in range(thread_count):
        thread_pool[i].start()
        