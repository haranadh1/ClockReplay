"""
This is my first ever file created for Galore Project; things can change a lot
from this point onwards. Lets see how this journey goes :-)

"""

"""
This file has methods that handle the http requests from client
standard python wsgi is used to serve requests from http clients.

"""

from wsgiref.simple_server import make_server
import random
import sys
import getopt
import os
import socket

DB_HOST = 'localhost'
DB_PORT = 9600


URI_ROOT = "/"
SERVER_PORT = 8107
SERVER_URI = "http://localhost:%s" %str(SERVER_PORT)

RESERVE_PREFIX = URI_ROOT + "reserve_slot"
CANCEL_PREFIX  = URI_ROOT + "cancel_slot"
DISPLAY_PREFIX = URI_ROOT + "display_matrix"
CLIENT_PREFIX  = URI_ROOT + "client"
ENVIRON_PREFIX = URI_ROOT + "environment"

fifo_ds = 0
RESERVE_SERVICE = "Reserve Service"
CANCEL_SERVICE  = "Cancel Service"
DISPLAY_SERVICE = "Display Service"
CLIENT_SERVICE  = "Client"
ENVIRON_SERVICE = "Environment Variables"

ROWS = 80
COLS = 80
reservation_matrix = [[0 for col in range(COLS)] for row in range(ROWS)]

class MySocket:
    """
    a wrapper class on top of python socket interface
    
    """
    
    def __init__(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    def connect(self, host, port):
        """
        connects to given host and port
        @param host: hostname 
        @type host : string
        
        @param port: port number the server listening on
        @type port : number
         
        """
        self.sock.connect ((host,port))
    
    def send(self, msg, msglen):
        """
        send data to the socket
        @param msg: data to be sent
        @type msg: string
        @param msglen: length of data
        @type msglen : number
        
        """
        
        totalsent = 0
        while totalsent < msglen:
            sent = self.sock.send(msg[totalsent:])
            if sent == 0:
                print ("socket connection broken")
            totalsent = totalsent + sent

    def myreceive(self):
        """
        receive the data buffer 
        """
        
        msg = ''
        msglen = 128
        while len(msg) < msglen:
            chunk = self.sock.recv(msglen-len(msg))
            if chunk == '':
                print ("socket connection broken")
            msg = msg + chunk
        return msg

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
                                       description="Environment Variables")
                }
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
    
    start_response(status, response_headers)
    
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

    #start the wsgi server
    
    wsgi_server = make_server('localhost', SERVER_PORT, application )    
    #handle requests
    wsgi_server.serve_forever()
        
# done deal.