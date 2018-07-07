package westland.ntfs.uploader;

import java.util.*;

public class IndexRoot_Attribute extends Attribute
{
  int attributeType;
  boolean fileName;
  int collationRule;
  int sizeOfIndexAllocationEntry;
  int clustersPerIndexRecord;

  NodeHeader nodeHeader;


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
    byte[] forNodeHeader = new byte[16];
    for(int i = 0; i<forNodeHeader.length;i++){
      forNodeHeader[i] = header[offset+16+i];
    }
    nodeHeader = new NodeHeader(forNodeHeader);

    byte[] forIndexList = new byte[nodeHeader.offsetToEndOfAllocatedSpace-nodeHeader.offsetToFirstIndex];
    for(int i = 0; i < forIndexList.length; i++){
      forIndexList[i] = header[offset+16+nodeHeader.offsetToFirstIndex+i];
    }
    if(fileName && get_resident()){


      indexEntryList = new IndexEntryList(forIndexList);
    }



  }
  public List<IndexEntryList.IndexEntry> getSubFiles()
  {
    return indexEntryList.getSubFiles();
  }
}
