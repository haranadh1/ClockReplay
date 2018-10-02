#include <iostream>
#include <map>
#include <set>
#include <pcap.h>
#include "pcapEngine.h"
#include "nwutils.h"

#ifdef WIN32
#include <assert.h>
#endif

using namespace std;

static const int MAX_PACKET_SIZE = 65535;
FILE *fp;

struct pcap_tcp_seg_info {
public:
	bool operator< (const pcap_tcp_seg_info& rhs) const
	{
		if(seq < rhs.seq)
			return true;
		return false;
	};
	unsigned long seq;
	u_char* data;
	int len;
};
struct pcap_packet_info {
	unsigned long sseq;
	unsigned long sack;
	int slen;
	unsigned long sseq_offset;
	unsigned long sack_offset;
	unsigned long rseq;
	unsigned long rack;
	int rlen;
	unsigned long next_resp_seq;
	std::set<pcap_tcp_seg_info>* segList;
};

class capture_hdr {
public:
	capture_hdr(int sport,int dport)
	{
		this->sport = sport;
		this->dport = dport;
	}
	int sport;
	int dport;
};

void dump_seglist(int port, std::map<int,pcap_packet_info>& packet_map)
{
	if(!packet_map[port].segList) return;

	std::set<pcap_tcp_seg_info>::iterator siter = packet_map[port].segList->begin();
	for(;siter != packet_map[port].segList->end();siter++)
	{
		cout << (*siter).seq << " ";
	}
	cout << endl;
}
void write_payload_to_file(u_char* data,int len,FILE* fp)
{
	fwrite(data,sizeof(u_char),len,fp);
}

void write_capture_hdr_to_file(capture_hdr capHdr,FILE* fp)
{
	fprintf(fp,"SRC:%d\r\nDEST:%d\r\n",capHdr.sport,capHdr.dport);
}

void analyze_tcp_segments_for_reassembly(int port,unsigned long seq, unsigned long ack, u_char* data,int len, int direction)
{
   pcap_packet_info pinfo;
   static std::map<int,pcap_packet_info> packet_map;

   if(direction == 0)
   {
	   std::map<int,pcap_packet_info>::iterator pack_iter = packet_map.find(port);
	   if(pack_iter != packet_map.end())
	   {
		   delete (*pack_iter).second.segList;
	   }
	   pinfo.sseq_offset = seq;
	   pinfo.sack_offset = ack;
	   
	   std::set<pcap_tcp_seg_info> *segList = new std::set<pcap_tcp_seg_info>;
	   pinfo.segList = segList;

	   pinfo.sseq = seq - pinfo.sseq_offset + 1;
	   pinfo.sack = ack - pinfo.sack_offset + 1;
	   pinfo.slen = len;
	   pinfo.next_resp_seq = 1;
	   packet_map[port] = pinfo;
	   printf("(seq = %d, ack = %d, len = %d,next_seq = %d)\n",pinfo.sseq,pinfo.sack,len,pinfo.next_resp_seq);

	   capture_hdr capHdr(port,80);
	   write_capture_hdr_to_file(capHdr,fp);
	   write_payload_to_file(data,len,fp);
   }
   else
   {
	   unsigned long pseudo_seq = 0;
	   unsigned long pseudo_ack = 0;
	   std::map<int,pcap_packet_info>::iterator pack_iter = packet_map.find(port);
	   if(pack_iter != packet_map.end())
	   {
		   pseudo_seq = seq - (*pack_iter).second.sack_offset + 1;
		   pseudo_ack = ack - (*pack_iter).second.sseq_offset + 1;
	   }
		
	   (*pack_iter).second.rseq = pseudo_seq;
	   (*pack_iter).second.rack = pseudo_ack;
	   (*pack_iter).second.rlen += len;
	   
	   pcap_tcp_seg_info seginfo;
	   // if incoming sequence is the expected one, dump it to file
	   // also update the expected sequence # and keep dumping till
	   // end of list or we dont have the expected sequence #
	   if( (*pack_iter).second.next_resp_seq == pseudo_seq)
	   {
		   if(pseudo_seq == 1)
		   {
			   capture_hdr capHdr(80,port);
			   write_capture_hdr_to_file(capHdr,fp);
		   }
			//dump the incoming seg
		   printf("Incoming matched (seq = %u, ack = %u, len = %d)\n",pseudo_seq,pseudo_ack,len);
		   write_payload_to_file(data,len,fp);

		   // loop thru seglist
		   std::set<pcap_tcp_seg_info>::iterator segiter;
		   (*pack_iter).second.next_resp_seq = (*pack_iter).second.next_resp_seq + len;
		   for(segiter = (*pack_iter).second.segList->begin();segiter != (*pack_iter).second.segList->end();)
		   {
				if((*pack_iter).second.next_resp_seq == (*segiter).seq)
				{
					// dump and free block
					printf("Freed block (seq = %u, len = %d)\n",(*segiter).seq,len);
					write_payload_to_file((*segiter).data,(*segiter).len,fp);
					(*pack_iter).second.next_resp_seq = (*pack_iter).second.next_resp_seq + (*segiter).len;

					delete [] (*segiter).data;
					(*pack_iter).second.segList->erase(segiter);
					segiter = (*pack_iter).second.segList->begin();
				}
				else
					break;
		   }
	   }
	   else
	   {
			seginfo.data = new u_char[len];
			memcpy(seginfo.data,data,len);
			seginfo.len = len;
			seginfo.seq = pseudo_seq;

			(*pack_iter).second.segList->insert(seginfo);
			printf("Incoming not matched (seq = %u, ack = %u, len = %d)\n",pseudo_seq,pseudo_ack,len);
	   }
	   
	   //dump_seglist(port,packet_map);
   }
}

void tcp_packet_handler(u_char *param, const struct pcap_pkthdr *header, const u_char *pkt_data)
{
	struct tm ltime;
    char timestr[16];
    ip_hdr *ih;
    tcp_hdr *th;
    u_short sport,dport;
    time_t local_tv_sec;


    /* convert the timestamp to readable format */
    local_tv_sec = header->ts.tv_sec;
    localtime_s(&ltime, &local_tv_sec);
    strftime( timestr, sizeof timestr, "%H:%M:%S", &ltime);

    /* retrieve the position of the ip header */
	ih = NWUtils::getIPHeader(pkt_data);

    /* retrieve the position of the tcp header */
	th = NWUtils::getTCPHeader((const u_char*)ih);

    /* convert from network byte order to host byte order */
    sport = ntohs( th->source );
    dport = ntohs( th->dest );

	int tcp_payload_len = NWUtils::getTCPPayloadLen(ih,th);

	if(tcp_payload_len > 0)
	{
		/* print timestamp and length of the packet */
    //printf("%s.%.6d len:%d ", timestr, header->ts.tv_usec, header->len);
	  /* print ip addresses and tcp ports */
		
    printf("%d.%d.%d.%d.%d -> %d.%d.%d.%d.%d",
        ih->saddr.byte1,
        ih->saddr.byte2,
        ih->saddr.byte3,
        ih->saddr.byte4,
        sport,
        ih->daddr.byte1,
        ih->daddr.byte2,
        ih->daddr.byte3,
        ih->daddr.byte4,
        dport);

	  /* get tcp payload ptr */
	u_char* app_data = NWUtils::getTCPPayload(th);
	  
	  /* tcp segment reassembly checks */
	  int direction;
	  int port;
	  if(dport == 80)
	  {
		  port = sport;
		  direction = 0;
	  }
	  else
	  if(sport == 80)
	  {
		  port = dport;
		  direction = 1;
	  }
	  if(sport == 80 || dport == 80)
	  analyze_tcp_segments_for_reassembly(port,ntohl(th->seq),ntohl(th->ack_seq),app_data,tcp_payload_len,direction);
	}
	/*
	else
		printf("This is a control packet...\n");
	*/
}

void packet_dumper(u_char *dumpfile,const struct pcap_pkthdr* pkthdr,const u_char* packet)
{
  /* save the packet on the dump file */
    pcap_dump(dumpfile, pkthdr, packet);
}
void my_callback(u_char *arg,const struct pcap_pkthdr* pkthdr,const u_char* packet)
{
  cout << "Grabbed packet of length = " << pkthdr->len << endl;
}

PcapEngine::PcapEngine()
: currentDeviceName_(0),
  devHd_(0)
{
  errbuf_ = new char[PCAP_ERRBUF_SIZE];
}

void PcapEngine::setCaptureFilePath(std::string path)
{
  capFilePath_ = path;
  fp = fopen(capFilePath_.c_str(),"wb+");
}

PcapEngine::~PcapEngine()
{
  if(errbuf_)
    delete [] errbuf_;
  fclose(fp);
}

int PcapEngine::openDumpFile(char* dmpFile)
{
  /* Open the dump file */
    dumpfile_ = pcap_dump_open(devHd_, dmpFile);
	if(dumpfile_ == NULL)
    {
        cerr << "Error opening output file" << endl;
        return -1;
    }
	return 0;
}

char* PcapEngine::findDefaultDevice()
{
  currentDeviceName_ = pcap_lookupdev(errbuf_);

  if(currentDeviceName_ == NULL)
  {
    cerr << errbuf_ << endl;
  }
  
  return currentDeviceName_;
}

int PcapEngine::findAllDevices()
{
  pcap_if_t *alldevs, *devicePtr;

  if (pcap_findalldevs(&alldevs, errbuf_) == -1)
  {
     cerr << "Error in pcap_findalldevs: " << errbuf_ << endl;
     return -1;
  }

  int interfaceCnt=0;
  cout << "Interfaces: " << endl;
  for(devicePtr=alldevs; devicePtr != NULL; devicePtr=devicePtr->next)
  {
    cout << ++interfaceCnt << ". " << devicePtr->name << "(" << devicePtr->description << ")" << endl;
	deviceNameList_.push_back(devicePtr->name); // do we need to copy the names rather than the ptrs
  }

  return 0;
}

int PcapEngine::getDeviceAddrAndMask()
{
  bpf_u_int32 maskp;

  assert(currentDeviceName_);
  int ret = pcap_lookupnet(currentDeviceName_,&netp_,&maskp,errbuf_);
  if(ret == -1)
  {
    cerr << errbuf_ << endl;
  }

  // print device address
  struct in_addr addr;
  addr.s_addr = netp_;
  char* addrStr = NWUtils::getNWAddrString(addr);
  cout << "Device address = " << addrStr << endl;

  return ret;
}

int PcapEngine::openDevice(char* deviceName)
{
  int promisMode = 0;
  int readTimeout = -1;

  assert(deviceName);
  currentDeviceName_ = deviceName;

  devHd_ = pcap_open_live(currentDeviceName_,
                       MAX_PACKET_SIZE,
                       promisMode,
                       readTimeout,errbuf_);
  if(devHd_== NULL)
  {
    cerr << errbuf_ << endl;
    return -1;
  }

  return 0;
}

const u_char* PcapEngine::grabPacket()
{
  assert(devHd_);
  const u_char* packet = pcap_next(devHd_, &pcapHdr_);
  if(packet == NULL)
  {
    cerr << "Failed to grab packet" << endl;
  }
  
  return packet;
}

int PcapEngine::grabPackets(int count)
{
  //int ret = pcap_loop(devHd_, count, packet_dumper, (unsigned char *)dumpfile_);
  int ret = pcap_loop(devHd_, count, tcp_packet_handler, NULL);
  if(ret == -1)
    cerr << "Error while capturing packets !" << endl;
  else
  if(ret == -2)
    cerr << "Capture ended without any packets !" << endl;

  return ret;
}

void PcapEngine::closeDevice()
{
  pcap_close(devHd_);
}

int PcapEngine::setFilter(const char* filter)
{
  int ret = buildFilter(filter);
  if(ret == -1)
    return ret;

  ret = applyFilter();
  return ret;
}

int PcapEngine::buildFilter(const char* filter)
{
  assert(devHd_);
  int ret = pcap_compile(devHd_, &fp_, filter, 0, netp_);
  if(ret == -1)
    cerr << "Error calling pcap_compile" << endl;
  
   return ret;
}

int PcapEngine::applyFilter()
{
  assert(devHd_);
  int ret = pcap_setfilter(devHd_, &fp_);
  if(ret == -1)
    cerr << "Error applying filter" << endl;

  return ret;
}

void PcapEngine::freeDeviceList(pcap_if_t* alldevs)
{
	pcap_freealldevs(alldevs);
}

void PcapEngine::dumpDeviceInfo()
{

}
