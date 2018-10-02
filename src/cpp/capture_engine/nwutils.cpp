#include <stdio.h>
#include <errno.h>
#include "nwutils.h"

static const int ETHER_HDR_LENGTH = 14;

NWUtils::NWUtils()
{
}

NWUtils::~NWUtils()
{
}

char* NWUtils::getNWAddrString(struct in_addr addr)
{
  char* addrStr = inet_ntoa(addr);
  if(addrStr == NULL)
  {
    perror("inet_ntoa");
  }
  return addrStr;
}

ip_hdr* NWUtils::getIPHeader(const u_char* eth_frame)
{
	return (ip_hdr *) (eth_frame + ETHER_HDR_LENGTH);
}

tcp_hdr* NWUtils::getTCPHeader(const u_char* ip_packet)
{
	u_int ip_len = (((ip_hdr*)ip_packet)->ver_ihl & 0xf) * 4;
	return (tcp_hdr *) ((u_char*)ip_packet + ip_len);
}

int NWUtils::getTCPPayloadLen(ip_hdr* ih, tcp_hdr* th)
{
	int ip_packet_len = ntohs(ih->tlen);
	int ip_hdr_len = (ih->ver_ihl & 0xf) * 4;
	int tcp_hdr_len = th->doff*4;
	return ( ip_packet_len - ( ip_hdr_len + tcp_hdr_len ) );
}

u_char* NWUtils::getTCPPayload(tcp_hdr* th)
{
	return ( (u_char*)((u_char*)th + th->doff*4) );
}