package westland.ntfs.uploader;

import java.util.*;

public class IndexAllocation_Attribute extends Attribute
{
  DataRuns dRuns;
  INDX indx;
  INDXSDH indxsdh;
  
  public IndexAllocation_Attribute(IndexConverter header,long offset)
  {
    super(header,offset);
    byte[] forDataRun = new byte[(int)(header.sizeInLong()-get_offsetToDataRuns()-offset)];
    for(int i = 0; i < forDataRun.length; i++){
      forDataRun[i] = header.get(i+get_offsetToDataRuns()+offset);
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
