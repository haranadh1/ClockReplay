#include <iostream>
#include <string>
#include <map>
#include "pcapEngine.h"
using namespace std;

/* pcap: Simple capture tool for network traffic 
   usage: pcap -host <ipaddress> -device <nw device> -port <port> -dir <incoming/outgoing/both> -capturePath <path to store capture file at>
*/

bool isValidParam(string nameVal,string& outField, string& outValue)
{
	int pos = nameVal.find(":");
	if(pos == -1)
	{
		cerr << "pcap parameters should be in the name:value format..!" << endl;
        return false;
	}
	outField = nameVal.substr(0,pos);
	outValue = nameVal.substr(pos+1);
	cerr << outField << " = " << outValue << endl;
	return true;
}

int parse_pcap_info(int argc,char** argv,std::map<std::string,std::string>& pcapInfo)
{
  std::string outField,outValue;
  if(argc == 1)
  {
	  cerr << "pcap: Simple capture tool for network traffic" << endl;
	  cerr << "usage: pcap host:<ipaddress> device:<nw device> port:<port> dir:<incoming/outgoing/both> capturePath:<path to store capture file at>" << endl;
	  return -1;
  }
  for(int i=1;i<argc;i++)
  {
    if(!isValidParam(argv[i],outField,outValue))
	{
	  cerr << argv[i] << " is not a valid parameter !" << endl;
	  return -1;
	}
	pcapInfo[outField] = outValue;
  }
  return 0;
}

int main(int argc,char** argv)
{
  PcapEngine peng;
  std::map<std::string,std::string> pcapInfo;

  if(parse_pcap_info(argc,argv,pcapInfo) == -1)
  {
	  exit(1);
  }
  
  int ret = peng.findAllDevices();
  cout << "findAllDevices return value = " << ret << endl;

  peng.setCaptureFilePath(pcapInfo["capturePath"]);

  ret = peng.openDevice("\\Device\\NPF_{DEBF4130-839B-4153-918B-B41C9A9CE383}");
  cout << "openDevice api return value = " << ret << endl;

  std::string filterStr = "port ";
  filterStr += pcapInfo["port"];
  peng.setFilter(filterStr.c_str());

  peng.grabPackets();
  peng.closeDevice();
  return 0;
}