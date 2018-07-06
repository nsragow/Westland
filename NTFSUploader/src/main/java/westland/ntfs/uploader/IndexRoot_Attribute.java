package westland.ntfs.uploader;

import java.util.*;

public class IndexRoot_Attribute extends Attribute
{
  int attributeType;
  boolean fileName;
  int collationRule;
  int sizeOfIndexAllocationEntry;
  int clustersPerIndexRecord;

  int offsetToFirstIndex;
  int offsetToEndOfUsedSpace;
  int offsetToEndOfAllocatedSpace;
  boolean indexAllocationNeeded;

  IndexEntryList indexEntryList;
  public IndexRoot_Attribute(byte[] header)
  {
    super(header);
    
    if(!this.get_resident()){
      throw new AttributeException("this IndexRoot_Attribute is not resident, should not be possible");
    }
    int offset = this.get_offsetToAttribute();

    attributeType = Helper.bytesToInt(header,offset+0,4);
    fileName = attributeType == 48;
    collationRule = Helper.bytesToInt(header,offset+4,4);
    sizeOfIndexAllocationEntry = Helper.bytesToInt(header,offset+8,4);
    clustersPerIndexRecord = Helper.bytesToInt(header,offset+12,1);
    offsetToFirstIndex = Helper.bytesToInt(header,offset+16,4);
    offsetToEndOfUsedSpace = Helper.bytesToInt(header,offset+20,4);
    offsetToEndOfAllocatedSpace = Helper.bytesToInt(header,offset+24,4);
    indexAllocationNeeded = header[offset+28] == 1;

    byte[] forIndexList = new byte[offsetToEndOfAllocatedSpace-offsetToFirstIndex];
    for(int i = 0; i < forIndexList.length; i++){
      forIndexList[i] = header[offset+16+offsetToFirstIndex+i];
    }
    if(fileName && get_resident()){


      indexEntryList = new IndexEntryList(forIndexList);
    }



  }
  public List<Long> getSubFiles()
  {
    return indexEntryList.getSubFiles();
  }
}
