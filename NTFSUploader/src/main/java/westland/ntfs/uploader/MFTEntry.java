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

  HashMap<Integer,List<Attribute>> attributeMap;


  public MFTEntry(IndexConverter data, long offset)
  {
    magicNumber = Helper.bytesToString(data,offset+indexOfmagicNumber,lengthOfmagicNumber);
    offsetToUpdateSequence = Helper.bytesToInt(data,offset+indexOfoffsetToUpdateSequence,lengthOfoffsetToUpdateSequence);
    numberOfEntriesInFixupArray = Helper.bytesToInt(data,offset+indexOfnumberOfEntriesInFixupArray,lengthOfnumberOfEntriesInFixupArray);
    LSN = Helper.bytesToLong(data,offset+indexOfLSN,lengthOfLSN);
    sequenceNumber = Helper.bytesToInt(data,offset+indexOfsequenceNumber,lengthOfsequenceNumber);
    hardLinkCount = Helper.bytesToInt(data,offset+indexOfhardLinkCount,lengthOfhardLinkCount);
    offsetToFirstAttribute = Helper.bytesToInt(data,offset+indexOfoffsetToFirstAttribute,lengthOfoffsetToFirstAttribute);
    flags = Helper.bytesToInt(data,offset+indexOfflags,lengthOfflags);
    usedSize = Helper.bytesToInt(data,offset+indexOfusedSize,lengthOfusedSize);
    allocatedSizeOfEntry = Helper.bytesToInt(data,offset+indexOfallocatedSizeOfEntry,lengthOfallocatedSizeOfEntry);
    fileReferenceToTheBaseOfFileRecord = Helper.bytesToLong(data,offset+indexOffileReferenceToTheBaseOfFileRecord,lengthOffileReferenceToTheBaseOfFileRecord);
    nextAttributeID = Helper.bytesToInt(data,offset+indexOfnextAttributeID,lengthOfnextAttributeID);
    numberOfRecord = Helper.bytesToInt(data,offset+indexOfnumberOfRecord,lengthOfnumberOfRecord);
    attributeMap = new HashMap<>();
    free = false;
    parseAttributes(data, offset);
  }


  private void parseAttributes(IndexConverter data, long offset)
  {
    int offsetToNext = 0;
    boolean parseAgain = true;

    if(Helper.bytesToInt(data,this.offsetToFirstAttribute+offsetToNext+offset,4) == 0xFFFFFFFF){
      this.setFree();
    }else{
      while(parseAgain){
        Attribute attribute = null;
        try{
          attribute = Attribute.getAttribute(data,offset+offsetToNext+this.offsetToFirstAttribute);
          offsetToNext+=attribute.get_length();
          this.addAttribute(attribute);
        }catch(RuntimeException e){
          e.printStackTrace();
          parseAgain = false;
        }
        if(Helper.bytesToInt(data,offset+this.offsetToFirstAttribute+offsetToNext,4) == 0xFFFFFFFF){
          parseAgain = false;
        }

      }
    }
    if(hasFileName() && getName().toLowerCase().equals("$secure")){
      System.out.println("found secure");

      List<Attribute> roots = this.getAttribute(0x80);

      for(Attribute a : roots){
        if(a.get_attributeName() != null && a.get_attributeName().toLowerCase().equals("$sds")){
          System.out.println("I found the sds");
          Data_Attribute d = (Data_Attribute) a;
          IndexConverter ic = d.getFile();
          int index = 0;
          int wait = 0;

          new SDSStream(ic);


        }
      }
    }
  }
  public List<IndexEntryList.IndexEntry> getSubFiles()
  {
    ArrayList<IndexEntryList.IndexEntry> toReturn = new ArrayList<>();
    IndexRoot_Attribute iRoot = (IndexRoot_Attribute)getAttribute(0x90).get(0);
    toReturn.addAll(iRoot.getSubFiles());
    if(getAttribute(0xa0)!=null){
      toReturn.addAll( ((IndexAllocation_Attribute)(getAttribute(0xa0).get(0))).getSubFiles() );
    }
    return toReturn;
  }
  public void setFree()
  {
    free = true;
  }
  public int getSecurityIdentifier()
  {
    StandardInformation_Attribute si = (StandardInformation_Attribute)(getAttribute(16).get(0));
    if(si.hasSecurityID){
      return si.securityID;
    }else{
      throw new RuntimeException("does not have security Identifier");
    }
  }
  public boolean hasSecurityIdentifier()
  {
    StandardInformation_Attribute si = (StandardInformation_Attribute)(getAttribute(16).get(0));
    if(!si.hasSecurityID){
      System.out.println("lloooked");
      System.out.println(this.getName());
      System.out.println(" old? "+si.oldStandard);
      System.out.println(si.filePermissions);
      SecurityDescriptor_Attribute sd = (SecurityDescriptor_Attribute)(getAttribute(0x50).get(0));
    }
    return si.hasSecurityID;
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
    int type = toAdd.get_attributeType();
    List<Attribute> set = attributeMap.get(type);
    if(set == null){
      set = new ArrayList<>();
    }
    set.add(toAdd);
    attributeMap.put(type,set);
  }
  public List<Attribute> getAttribute(int type)
  {
    return attributeMap.get(type);
  }
  public String getName()
  {
    if(this.getAttribute(48)!=null){
      return ((FileName_Attribute)this.getAttribute(48).get(0)).getName();
    }else{
      throw new RuntimeException("Entry does not have name");
    }
  }
  public IndexConverter getFile()
  {
    List<Attribute> attributes = getAttribute(0x80);
    for(Attribute a : attributes){
      Data_Attribute d = (Data_Attribute) a;
      if(d.get_attributeName() == null){
        return d.getFile();
      }
    }
    throw new RuntimeException("could not find data attribute without name");
  }
  public byte[] getBytes()
  {
    List<Attribute> attributes = getAttribute(0x80);
    for(Attribute a : attributes){
      Data_Attribute d = (Data_Attribute) a;
      if(d.get_attributeName() == null){
        return d.getBytes();
      }
    }
    throw new RuntimeException("could not find data attribute without name");
  }
  public Data_Attribute getUnnamedData()
  {
    List<Attribute> attributes = getAttribute(0x80);
    for(Attribute a : attributes){
      Data_Attribute d = (Data_Attribute) a;
      if(d.get_attributeName() == null){
        return d;
      }
    }
    throw new RuntimeException("could not find data attribute without name");
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    String name;
    if(this.getAttribute(48)!=null){
      name = ((FileName_Attribute)this.getAttribute(48).get(0)).getName();
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
