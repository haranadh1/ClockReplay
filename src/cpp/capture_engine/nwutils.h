#ifndef _NWUTILS_H
#define _NWUTILS_H

#ifndef WIN32
  #include <sys/socket.h>
  #include <netinet/in.h>
  #include <arpa/inet.h>
#else
  #include <winsock.h>
#endif

/* 4 bytes IP address */
typedef struct ip_address{
    u_char byte1;
    u_char byte2;
    u_char byte3;
    u_char byte4;
}ip_address;

/* IPv4 header */
typedef struct ip_header{
    u_char  ver_ihl;        // Version (4 bits) + Internet header length (4 bits)
    u_char  tos;            // Type of service 
    u_short tlen;           // Total length 
    u_short identification; // Identification
    u_short flags_fo;       // Flags (3 bits) + Fragment offset (13 bits)
    u_char  ttl;            // Time to live
    u_char  proto;          // Protocol
    u_short crc;            // Header checksum
    ip_address  saddr;      // Source address
    ip_address  daddr;      // Destination address
    u_int   op_pad;         // Option + Padding
}ip_hdr;

/* UDP header*/
typedef struct udp_header{
    u_short sport;          // Source port
    u_short dport;          // Destination port
    u_short len;            // Datagram length
    u_short crc;            // Checksum
}udp_header;

typedef struct tcp_hdr {
        unsigned short source;
        unsigned short dest;
        unsigned long seq;
        unsigned long ack_seq;       
        #  if __BYTE_ORDER == __LITTLE_ENDIAN
        unsigned short res1:4;
        unsigned short doff:4;
        unsigned short fin:1;
        unsigned short syn:1;
        unsigned short rst:1;
        unsigned short psh:1;
        unsigned short ack:1;
        unsigned short urg:1;
        unsigned short res2:2;
        #  elif __BYTE_ORDER == __BIG_ENDIAN
        unsigned short doff:4;
        unsigned short res1:4;
        unsigned short res2:2;
        unsigned short urg:1;
        unsigned short ack:1;
        unsigned short psh:1;
        unsigned short rst:1;
        unsigned short syn:1;
        unsigned short fin:1;
        #  endif
        unsigned short window;       
        unsigned short check;
        unsigned short urg_ptr;
} tcp_hdr;

class NWUtils {

  public:
    NWUtils();
    ~NWUtils();

  static char* getNWAddrString(struct in_addr addr);
  static ip_hdr* getIPHeader(const u_char*);
  static tcp_hdr* getTCPHeader(const u_char*);
  static int getTCPPayloadLen(ip_hdr* ih, tcp_hdr* th);
  static u_char* getTCPPayload(tcp_hdr* th);
};

#endif
