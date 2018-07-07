package westland.ntfs.uploader;

import java.util.*;

public class DataRuns
{
  List<DataRun> dataRuns;
  public DataRuns(byte[] header)
  {

    dataRuns = new ArrayList<>();
    DataRun dr = new DataRun(header,0);;
    int index = dr.f+dr.l+1;
    dataRuns.add(dr);
    while(index<header.length && (dr.f!=0 && dr.l!=0)){
      dr  = new DataRun(header,index);
      index+=(dr.f+dr.l+1);
      if((dr.f!=0 && dr.l!=0)){
        dataRuns.add(dr);
        break;
      }
    }

  }
  public IndexConverter getIndexConverter()
  {
    long[] toReturn = new long[dataRuns.size()*2];
    long previousLocation = 0;
    for(int i = 0; i < dataRuns.size(); i++){
      DataRun dr = dataRuns.get(i);
      previousLocation += Helper.vcnToBytes(dr.offsetToStartingLCNOfPreviousElement);
      toReturn[i*2] = previousLocation;
      toReturn[i*2+1] = Helper.clusterToByte(dr.lengthOfTheRun);
    }
    return new IndexConverter(toReturn);
  }

  public class DataRun
  {
    int f; //size of offset field
    int l; //size of length field
    long lengthOfTheRun;
    long offsetToStartingLCNOfPreviousElement;

    public DataRun(byte[] header,int start)
    {
      int hex = Helper.bytesToInt(header,0+start,1);

      f = (hex & 0x000000f0)>>>4;
      l = hex & 0x0000000f;
      lengthOfTheRun = Helper.bytesToLong(header,1+start,l);
      long temp = Helper.bytesToLong(header,1+l,f);
      offsetToStartingLCNOfPreviousElement = (temp<<8-f)>>(8-f);

      
    }
  }
}
