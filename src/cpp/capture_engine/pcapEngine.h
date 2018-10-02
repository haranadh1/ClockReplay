#ifndef _PCAP_ENGINE_H
#define _PCAP_ENGINE_H

#include <vector>
#include <pcap.h>

class PcapEngine
{
  public:
    PcapEngine();

    ~PcapEngine();

    int openDevice(char* deviceName);
    char* findDefaultDevice();
	int findAllDevices();
    int getDeviceAddrAndMask();
    const u_char* grabPacket();
    int grabPackets(int count=-1);
    void closeDevice();
    void dumpDeviceInfo();
    int setFilter(const char* filter);
	int openDumpFile(char* dmpFile);
	void setCaptureFilePath(std::string path);
  
  private:
    int buildFilter(const char* filter);
    int applyFilter();
	void freeDeviceList(pcap_if_t*);

    char* errbuf_;
    char* currentDeviceName_;
	std::vector<char*> deviceNameList_;

	std::string capFilePath_;

    pcap_t* devHd_;
    pcap_pkthdr pcapHdr_;
    bpf_u_int32 netp_;
    struct bpf_program fp_;
	pcap_t* adhandle_;
	pcap_dumper_t* dumpfile_;

};
#endif  
