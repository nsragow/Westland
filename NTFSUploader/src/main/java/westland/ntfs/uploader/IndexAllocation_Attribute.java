package westland.ntfs.uploader;

import java.util.*;

public class IndexAllocation_Attribute extends Attribute
{
  DataRuns dRuns;
  INDX indx;
  byte[] storage;
  INDXSDH indxsdh;
  public IndexAllocation_Attribute(byte[] header)
  {
    super(header);
    storage = header;
    byte[] forDataRun = new byte[header.length-get_offsetToDataRuns()];
    for(int i = 0; i < forDataRun.length; i++){
      forDataRun[i] = header[i+get_offsetToDataRuns()];
    }
    dRuns = new DataRuns(forDataRun);
    IndexConverter indices = dRuns.getIndexConverter();
    indices.map();

    if(this.get_attributeName().toLowerCase().equals("$i30")){
      indx = new INDX(indices);
    }else if(this.get_attributeName().toLowerCase().equals("$sdh")){
      indxsdh = new INDXSDH(indices);
    }




  }
  public List<IndexEntryList.IndexEntry> getSubFiles()
  {
    return indx.getSubFiles();
  }
}
