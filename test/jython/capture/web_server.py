from __future__ import with_statement

"""
This is my first ever file created for Galore Project; things can change a lot
from this point onwards. Lets see how this journey goes :-)

"""

"""
This file has methods that handle the http requests from client
standard python wsgi is used to serve requests from http clients.

"""
from wsgiref.util import request_uri
from wsgiref.simple_server import make_server
from wsgiref.simple_server import WSGIServer, WSGIRequestHandler
from com.crp.common import CRPServiceInfo 
from time import sleep
from datetime import datetime
from BaseHTTPServer import BaseHTTPRequestHandler
import Queue

import random
import sys
import getopt
import os
import socket
from threading import Lock

#java imports
from crp_process_main import create_crp_process
from com.crp.interfaces import TestCaplet
from com.crp.process import PROCESS_GLOBALS 
from com.crp.common import GLOBAL_ENUMS
from com.crp.ether import Photon, Message, MESSAGE_CODE
from com.crp.ether import PhotonPool


DB_HOST = 'localhost'
DB_PORT = 9600


URI_ROOT = "/"
SERVER_PORT = None
SERVER_URI = None 
ph = None

RESERVE_PREFIX = URI_ROOT + "reserve_slot"
CANCEL_PREFIX  = URI_ROOT + "cancel_slot"
DISPLAY_PREFIX = URI_ROOT + "display_matrix"
CLIENT_PREFIX  = URI_ROOT + "client"
ENVIRON_PREFIX = URI_ROOT + "environment"
START_CAPTURE_PREFIX = URI_ROOT + "start_capture"
STOP_CAPTURE_PREFIX = URI_ROOT + "stop_capture"
MEM_MGR_PREFIX = URI_ROOT + "mem_mgr"
DB_VIEWER_PREFIX = URI_ROOT + "db_viewer"
CRP_THREADS_PREFIX = URI_ROOT + "crp_threads"

CAPTURE_IN_PROGRESS=0

fifo_ds = 0
RESERVE_SERVICE = "Reserve Service"
CANCEL_SERVICE  = "Cancel Service"
DISPLAY_SERVICE = "Display Service"
CLIENT_SERVICE  = "Client"
ENVIRON_SERVICE = "Environment Variables"
START_CAPTURE   = "Start Capture"
STOP_CAPTURE    = "Stop Capture"
MEM_MGR_SERVICE = "Memory Manager Service"
DB_VIEWER_SERVICE = "DB Viewer Service"
CRP_THREADS_SERVICE = "CRP Threads Service"

ROWS = 80
COLS = 80
reservation_matrix = [[0 for col in range(COLS)] for row in range(ROWS)]

REMOTE_PORT = None

global_lock = Lock()

portQueue = Queue.Queue()


class MyHTTPServer(WSGIServer):
    """
        request handler.
    """
    
    def handle_request(self):
        """Handle one request, without blocking.
            override this method to get the client port.
            I assume that select.select has returned that the socket is
            readable before this function was called, so there should be
            no risk of blocking in get_request().
        """
        global REMOTE_PORT
        try:
            request, client_address = self.get_request()
            
            portQueue.put(client_address[1])
            
            REMOTE_PORT  = client_address[1]
        except socket.error:
            return
        if self.verify_request(request, client_address):
            try:
                self.process_request(request, client_address)
            except:
                self.handle_error(request, client_address)
                self.shutdown_request(request)

def view_crp_threads(environ):
    caps = CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,
                "localhost",
                "capture handler service",
                9101, 0)
    m = Message(MESSAGE_CODE.MSG_CRP_THREADS_DUMP_INFO)
    ph = PhotonPool.sendQuickMessage(caps, m)
    while(True):
        m = ph.brecvMessage()
        if m is None:
            continue
        else:
            break
    
    ret =  m.getPayloadMBO().getObjectAtIndex(0).toString()
    m.updateStatus(Message.MessageStatus.AVAILABLE);
    return ret

def dump_memmgr_info(environ):
    caps = CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.SERVICE_HANDLER_SERVICE,
                "localhost",
                "capture handler service",
                9101, 0)
    m = Message(MESSAGE_CODE.MSG_MEMMGR_DUMP_INFO)
    ph = PhotonPool.sendQuickMessage(caps, m)
    while(True):
        m = ph.brecvMessage()
        if m is None:
            continue
        else:
            break
    
    ret =  m.getPayloadMBO().getObjectAtIndex(0).toString()
    m.updateStatus(Message.MessageStatus.AVAILABLE);
    return ret
    
def start_capture(environ):
    """
    @param environ: http request environment
    @type environ: dictionary
    
    @return: body of the result html page
    @rtype: dictionary of name value pairs to be 
            sent back to client
    """
    
    global CAPTURE_IN_PROGRESS
    with global_lock:
        CAPTURE_IN_PROGRESS = 1
    ret = {"capture progress" : "capture is currently in progress"}
    return ret

def stop_capture(environ):
    """
    @param environ: http request environment
    @type environ: dictionary
    
    @return: body of the result html page
    @rtype: dictionary of name value pairs to be 
            sent back to client
    """
    
    global CAPTURE_IN_PROGRESS
    
    with global_lock:
        if(CAPTURE_IN_PROGRESS == 1):
            print "closing cap service photon connection"
            try:
                PhotonPool.closePhoton(ph, True)
            except EtherException, e:
                print str(e)
                    
        CAPTURE_IN_PROGRESS = 0
    ret = {"capture progress" : "capture stopped"}
    return ret
    
def view_db(environ):
    """
    @param environ: http request environment
    @type environ: dictionary
    
    @return: body of the result html page
    @rtype: dictionary of name value pairs to be 
            sent back to client
    """
    
    # connect to capture service.
    caps = CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.DB_HANDLER_SERVICE,
                "localhost",
                "capture handler service",
                9101, 0)
    dbPH = PhotonPool.createPhoton(caps, True)
    
    m = Message(MESSAGE_CODE.MSG_REQ_CLIENT_CAPLET_STREAM_DB)
    
    dbPH.sendMessageNoFail(m)
    dbPH.getChannel().flushChannel()
    while(True):
        m = dbPH.brecvMessage()
        if( m is None):
            continue
        else:
            break
    
    counter = 0
    li = []
    while (True):
        c = m.getPayloadMBO().getObjectAtIndex(counter)
        counter = counter + 1
        li.append(c.zipString())
        if(counter > m.getPayloadMBO().getActiveObjects()):
            break
    ret = "<br>________________________________________________________________________<br>".join(li)        
    return ret
   
def list_services(environ):
    """
    lists all the services under the root
    @param environ: http request environment
    @type environ: dictionary
    
    @return: body of the result html page
    @rtype: dictionary of name value pairs to be 
            sent back to client
    """
    
    # static list of all the services represented in a dictionary
    
    cap_progress = {}
    
    uri_list = {
                RESERVE_SERVICE : dict(uri= SERVER_URI + RESERVE_PREFIX, 
                                       description="Reserve a Slot"),
                CANCEL_SERVICE  : dict(uri= SERVER_URI + CANCEL_PREFIX,
                                       description="Cancel a Slot"),
                DISPLAY_SERVICE : dict(uri=SERVER_URI + DISPLAY_PREFIX,
                                       description="Display Matrix"),
                CLIENT_SERVICE  : dict(uri = SERVER_URI + CLIENT_PREFIX,
                                       description="Client"),
                ENVIRON_SERVICE : dict(uri = SERVER_URI + ENVIRON_PREFIX,
                                       description="Environment Variables"),
                MEM_MGR_SERVICE: dict(uri = SERVER_URI + MEM_MGR_PREFIX,
                                      description = "Memory Manager Info Service"),
                DB_VIEWER_SERVICE: dict(uri = SERVER_URI + DB_VIEWER_PREFIX,
                                        description = "DB Viewer"),
                CRP_THREADS_SERVICE: dict(uri = SERVER_URI + CRP_THREADS_PREFIX,
                                        description = "CRP Threads Viewer")
                }
    
    if(CAPTURE_IN_PROGRESS == 0):
        uri_list[START_CAPTURE] = dict(uri = SERVER_URI + START_CAPTURE_PREFIX,
                                       description="start capturing load")
    else:
        uri_list[STOP_CAPTURE] = dict(uri = SERVER_URI + STOP_CAPTURE_PREFIX,
                                       description="stop capturing load")            
    return uri_list 

def environ_service(environ):
    """
    prints environment variables
    @param: environ -http environ object
    @type : environ -dictionary
    @return: string of dict values
    
    """
    
    env_list = ['%s:%s' %(key,value) for key,value in sorted(environ.items())]
    return "<br>".join(env_list)
    
def reserve_service(environ):
    """
    reserves a slot 
    @param: environ -http environ object
    @type : environ -dictionary
    @return: dictionary of row/col values
    
    """
    
    row_col = 0
    row = col = 0
    path = environ['PATH_INFO']
    if (environ['REQUEST_METHOD'] == 'POST'):
        row_col = path[len(RESERVE_PREFIX)+1:]
        rowcol = row_col.split('_')
        row = int(rowcol[0])
        col = int(rowcol[1])
    else:    
        row = random.randint(0, ROWS -1)
        col = random.randint(0, COLS -1)
    reservation_matrix[row][col] = reservation_matrix[row][col] + 1
    body = { 'row': row, 'col': col}
    return body

def cancel_service(environ):
    """
    cancels a slot that was already acquired by
    reserve_service
    @param: environ -http environ object
    @type : environ -dictionary
    @return: dictionary of row/col values
    
    """
    row_col = 0
    row = col = 0
    path = environ['PATH_INFO']
    
    """
    the way it works with POST: 
    we get a post request from client with 
    row_col value appended to uri, we parse
    it to get corresponding row,col pairs, very
    simple but works for now
    """
    
    if (environ['REQUEST_METHOD'] == 'POST'):
        row_col = path[len(CANCEL_PREFIX)+1:]
        print ("path: %s , row_col: %s" %(path,row_col))
        rowcol = row_col.split('_')
        row = int(rowcol[0])
        col = int(rowcol[1])
    else:    
        row = random.randint(0, ROWS -1)
        col = random.randint(0, COLS -1)
    
    if reservation_matrix[row][col] > 0:
        reservation_matrix[row][col] = reservation_matrix[row][col] - 1
    body = { 'row': row, 'col': col}
    return body
    
def dict_to_html(inp_dict, indent = 0):
    """
    converts the data into neatly organized html.
    @param inp_dict: dictionary to be converted to html
    @type dictionary
    
    @return: html string
    @rtype: string
    """
    
    htmlLines = []
    htmlLines.append('<ul>\n')
    for k,v in inp_dict.iteritems():
        if isinstance(v, dict):
            htmlLines.append('<li>'+ k+ ':')
            htmlLines.append(dict_to_html(v))
            htmlLines.append('</li>')
        else:
            if( k == 'uri'):
                htmlLines.append('<li>' + k + '  : ' + '<a href=' + str(v) + '>' + str(v) + '</a></li>' )
            else:
                htmlLines.append('<li>'+ k+ ':'+ str(v)+ '</li>')
    htmlLines.append('</ul>\n')
    return '\n'.join(htmlLines)
    html_output = '\n'.join(htmlLines)
    return html_output
            
def display_service(environ):
    """
    displays the current reservation matrix
    @param: environ -http environ object
    @type : environ -dictionary
    
    """
    
    html_output = []
    html_output.append('<h1> Reservation Matrix </h1>')
    html_output.append('<table border =1, align=center>')
    
    style = 'background-color:yellow'
    for i in range(ROWS):
        html_output.append('<tr>')
        for j in range(COLS):
            style = 'background-color:yellow'
            if reservation_matrix[i][j] == 1 :
                style = 'background-color:red'
            elif reservation_matrix[i][j] == 2 :
                style = 'background-color:blue'
            elif reservation_matrix[i][j] == 3 :
                style = 'background-color:green'
            elif reservation_matrix[i][j] == 4 :
                style = 'background-color:aqua'
            elif reservation_matrix[i][j] == 5 :
                style = 'background-color:brown'
            elif reservation_matrix[i][j] == 6 :
                style = 'background-color:indigo'
            elif reservation_matrix[i][j] == 7 :
                style = 'background-color:grey'
            elif reservation_matrix[i][j] == 8 :
                style = 'background-color:magenta'
            elif reservation_matrix[i][j] == 9 :
                style = 'background-color:olive'
            html_output.append('<td style=' + style + '>' + str(reservation_matrix[i][j]) + '</td>')
        html_output.append('</tr>')
    html_output.append('</table>')
    
    return "\n".join(html_output)


handlers = {
            URI_ROOT:       {
                             'GET' : list_services
                             },
            RESERVE_PREFIX: {
                             'GET' : reserve_service,
                             'POST': reserve_service,
                             },
            CANCEL_PREFIX: {
                            'GET' : cancel_service,
                            'POST': cancel_service
                            },
            DISPLAY_PREFIX: {
                             'GET' : display_service
                             },
            ENVIRON_PREFIX:  {
                              'GET': environ_service
                              },
            START_CAPTURE_PREFIX: {
                            'GET': start_capture
                            },
            STOP_CAPTURE_PREFIX: {
                           'GET': stop_capture
                           },
            MEM_MGR_PREFIX: {
                           'GET': dump_memmgr_info
                           },
            DB_VIEWER_PREFIX: {
                            'GET': view_db
                            },
            CRP_THREADS_PREFIX: {
                            'GET': view_crp_threads
                            }
            }

html_template = """

<html>
    <head>
        <title> 
            Clock Replay 
        </title>
    </head>
    <body> 
        <h1 style="text-align:center">
            Clock Replay Test Engine
        </h1>
        <p>
        %s
        </p>
    </body>
</html>
"""

def application ( environ, start_response ):
    
    """
    @param environ: 
    @type  environ:
        
    @param start_response:
    @type start_response:
        
    @return : None
    
    """
    
#    write something back to the client; for now write the environment as is

    # stream the request to remote db
    
    # s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # s.connect(DB_HOST, DB_PORT)
    # s.send('\n'.join(['%s:%s' %(key,value) for key,value in environ.items()]))
    qs = environ['QUERY_STRING']
    path = environ['PATH_INFO']
    path_split = path.split('/')
    if(len(path_split) > 2):
        path = '/'+path_split[1]
    method = environ['REQUEST_METHOD']
    
    status = "200 OK"
    body = " "
    
    ts = datetime.now()
    # construct the request.
    request = []
    request.append(environ['REQUEST_METHOD'] + ' ' + environ['PATH_INFO'] + ' ' + environ['SERVER_PROTOCOL'])
    request.append('Host:' + environ['HTTP_HOST'])
    if(environ.has_key('HTTP_CONNECTION')):
       request.append('Connection:' + environ['HTTP_CONNECTION'])
    if(environ.has_key('HTTP_USER_AGENT')):   
        request.append('User-Agent:' + environ['HTTP_USER_AGENT'])
    if(environ.has_key('HTTP_ACCEPT')):    
        request.append('Accept:' + environ['HTTP_ACCEPT'])
    if(environ.has_key('HTTP_ACCEPT_ENCODING')):    
        request.append('Accept-Encoding:' + environ['HTTP_ACCEPT_ENCODING'])
    if(environ.has_key('HTTP_ACCEPT_LANGUAGE')):    
        request.append('Accept-Language:' + environ['HTTP_ACCEPT_LANGUAGE'])
    if(environ.has_key('HTTP_ACCEPT_ENCODING')):    
        request.append('Accept-Encoding:' + environ['HTTP_ACCEPT_ENCODING'])
        
    request_string = '\n'.join(request)
    rPort = portQueue.get()
    testCap = TestCaplet(environ['REMOTE_ADDR'], environ['REMOTE_ADDR'], rPort,
                          str(ts), request_string)
    m = Message(MESSAGE_CODE.MSG_TEST_CAPURE_AGENT)

    m.setForTesting()
    m.setTestObject(testCap)
    
    with global_lock:
        if(CAPTURE_IN_PROGRESS == 1):
            ph.bsendMessage(m)
            
    if handlers.has_key(path):
        handler_dict = handlers[path]
        if handler_dict.has_key(method):
            handler = handler_dict[method]
            body = handler(environ)
        else:
            status ="400 bad request"
        
    if isinstance(body,dict):
        response_body = html_template %dict_to_html(body)
    else:
        response_body = html_template %str(body)
    
    response_headers = [('ContentType', 'text/plain'), 
                      ('Content-Length', str(len(response_body))) ]
    
    response_headers_str = "\n".join([ ":".join(key) for key in response_headers ])
    
    total_response = response_headers_str + '\n' + response_body
    start_response(status, response_headers)
    
    socket = environ['wsgi.input']
    ts = datetime.now()
    testCap1 = TestCaplet(environ['REMOTE_ADDR'], environ['REMOTE_ADDR'], rPort,
                          str(ts), total_response)
    m = Message(MESSAGE_CODE.MSG_TEST_CAPURE_AGENT)
    m.setForTesting()
    m.setTestObject(testCap1)
    
    with global_lock:
        if(CAPTURE_IN_PROGRESS == 1):
            ph.bsendMessage(m)
    # write test caplet to crp thread.
    return [response_body]

def Usage(): 
    """
    python web_server.py '-p' <port(required)> 
        
    the webserver would listen to the port mentioned
    serve requests for reserve_service/cancel_service in
    GET/POST methods. 
    If the request method is is GET then the server would use
    some random number to reserve/cancel service. 
    If the request method is POST, then the server would use
    http://localhost:8107/reserve_service/12_34 - row=12 col =34
    row col given by the client
    
    sample user: 
    python web_server.py -p 8107
    browser:
    http://localhost:8107
    If you are using the python client, make sure that you mention
    same port for the client to connect to.
    
    """    
    print Usage.__doc__


if __name__ == '__main__' :

    e = None 
    if len(sys.argv) == 1:
        Usage()
        sys.exit()
    try:
        opts, xargs = getopt.getopt(sys.argv[1:], "hp:", ["help", "port=" ])
   
    except ( getopt.GetoptError, e ):
        print ( "Invalid Option(s) Specified [%s]", str( e ) )
        Usage()
        sys.exit() 
    for opt, arg in opts:
        
        if opt in [ "-h", "--help" ]:
            Usage()
            sys.exit()
            
        elif opt in [ "-p", "--port" ]:
            SERVER_PORT = int(arg)
            SERVER_URI = "http://localhost:%s" %str(SERVER_PORT)

    #start crp process related data strctures.
    create_crp_process()
    
    # connect to capture service.
    caps = CRPServiceInfo(GLOBAL_ENUMS.SERVICE_CATALOG.CAPTURE_SERVICE,
                "localhost",
                "capture handler service",
                9101, 0)
    sleep(7)
    ph = PhotonPool.createPhoton(caps, True)
    #start the wsgi server
    
    wsgi_server = make_server('localhost', SERVER_PORT, application, MyHTTPServer, WSGIRequestHandler )
    
    # do the necessary initialization stuff for capture service here.
    
    #handle requests
    wsgi_server.serve_forever()
    
        
# done deal.