package westland.ntfs.uploader;

import java.util.*;

public class MFTEntry
{
  final int indexOfmagicNumber = 0;
  final int lengthOfmagicNumber = 4;
  final int indexOfoffsetToUpdateSequence = 4;
  final int lengthOfoffsetToUpdateSequence = 2;
  final int indexOfnumberOfEntriesInFixupArray = 6;
  final int lengthOfnumberOfEntriesInFixupArray = 2;
  final int indexOfLSN = 8;
  final int lengthOfLSN = 8;
  final int indexOfsequenceNumber = 16;
  final int lengthOfsequenceNumber = 2;
  final int indexOfhardLinkCount = 18;
  final int lengthOfhardLinkCount = 2;
  final int indexOfoffsetToFirstAttribute = 20;
  final int lengthOfoffsetToFirstAttribute = 2;
  final int indexOfflags = 22;
  final int lengthOfflags = 2;
  final int indexOfusedSize = 24;
  final int lengthOfusedSize = 4;
  final int indexOfallocatedSizeOfEntry = 28;
  final int lengthOfallocatedSizeOfEntry = 4;
  final int indexOffileReferenceToTheBaseOfFileRecord = 32;
  final int lengthOffileReferenceToTheBaseOfFileRecord = 8;
  final int indexOfnextAttributeID = 40;
  final int lengthOfnextAttributeID = 2;
  final int indexOfnumberOfRecord = 44;
  final int lengthOfnumberOfRecord = 4;

  String magicNumber;//deal with baad instead of file
  int offsetToUpdateSequence;
  int numberOfEntriesInFixupArray;
  long LSN;
  int sequenceNumber;
  int hardLinkCount;
  int offsetToFirstAttribute;
  int flags;
  int usedSize;
  int allocatedSizeOfEntry;
  long fileReferenceToTheBaseOfFileRecord;
  int nextAttributeID;
  int numberOfRecord;

  boolean free;

  HashMap<Integer,Attribute> attributeMap;

  public MFTEntry(byte[] data)
  {
    magicNumber = Helper.bytesToString(data,indexOfmagicNumber,lengthOfmagicNumber);
    offsetToUpdateSequence = Helper.bytesToInt(data,indexOfoffsetToUpdateSequence,lengthOfoffsetToUpdateSequence);
    numberOfEntriesInFixupArray = Helper.bytesToInt(data,indexOfnumberOfEntriesInFixupArray,lengthOfnumberOfEntriesInFixupArray);
    LSN = Helper.bytesToLong(data,indexOfLSN,lengthOfLSN);
    sequenceNumber = Helper.bytesToInt(data,indexOfsequenceNumber,lengthOfsequenceNumber);
    hardLinkCount = Helper.bytesToInt(data,indexOfhardLinkCount,lengthOfhardLinkCount);
    offsetToFirstAttribute = Helper.bytesToInt(data,indexOfoffsetToFirstAttribute,lengthOfoffsetToFirstAttribute);
    flags = Helper.bytesToInt(data,indexOfflags,lengthOfflags);
    usedSize = Helper.bytesToInt(data,indexOfusedSize,lengthOfusedSize);
    allocatedSizeOfEntry = Helper.bytesToInt(data,indexOfallocatedSizeOfEntry,lengthOfallocatedSizeOfEntry);
    fileReferenceToTheBaseOfFileRecord = Helper.bytesToLong(data,indexOffileReferenceToTheBaseOfFileRecord,lengthOffileReferenceToTheBaseOfFileRecord);
    nextAttributeID = Helper.bytesToInt(data,indexOfnextAttributeID,lengthOfnextAttributeID);
    numberOfRecord = Helper.bytesToInt(data,indexOfnumberOfRecord,lengthOfnumberOfRecord);
    attributeMap = new HashMap<>();
    free = false;
    parseAttributes(data);
  }

  private void parseAttributes(byte[] data)
  {
    int offsetToNext = 0;
    boolean parseAgain = true;

    if(Helper.bytesToInt(data,this.offsetToFirstAttribute+offsetToNext,4) == 0xFFFFFFFF){
      this.setFree();
    }else{
      byte[] bytes;
      while(parseAgain){
        bytes = new byte[64];
        for(int i = 0; i < bytes.length; i++){
          bytes[i] = data[this.offsetToFirstAttribute+offsetToNext+i];
        }
        bytes = new byte[Attribute.getAttributesLength(bytes)];

        for(int i = 0; i < bytes.length; i++){
          bytes[i] = data[this.offsetToFirstAttribute+offsetToNext+i];
        }
        Attribute attribute = null;
        try{
          attribute = Attribute.getAttribute(bytes);
          offsetToNext+=attribute.get_length();
          this.addAttribute(attribute);
        }catch(RuntimeException e){
          e.printStackTrace();
          parseAgain = false;
        }
        if(Helper.bytesToInt(data,this.offsetToFirstAttribute+offsetToNext,4) == 0xFFFFFFFF){
          parseAgain = false;
        }

      }
    }
  }

  public void setFree()
  {
    free = true;
  }
  public boolean isFree()
  {
    return free;
  }
  public boolean hasFileName()
  {
    return this.getAttribute(48)!=null;
  }
  public boolean isDirectory()
  {

    return (flags&0x00000002) == 2;
  }

  public void addAttribute(Attribute toAdd)
  {
    attributeMap.put(toAdd.get_attributeType(),toAdd);
  }
  public Attribute getAttribute(int type)
  {
    return attributeMap.get(type);
  }
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    String name;
    if(this.getAttribute(48)!=null){
      name = ((FileName_Attribute)this.getAttribute(48)).getName();
    }else{
      name = "FileName not found";
    }
    sb.append("-MFT_ENTRY-");
    sb.append("\n");
    if(free){
      sb.append("Entry is free");
    }else{
      sb.append("file name: ");
      sb.append(name);
      sb.append("\n");
      sb.append("magic number string: ");
      sb.append(magicNumber);
      sb.append("\n");
      sb.append("Directory: ");
      sb.append(isDirectory());
    }
    return sb.toString();
  }


}
