package westland.ntfs.uploader;

import java.util.*;

public class IndexEntryList
{
  List<IndexEntry> entries;
  public IndexEntryList(byte[] bytes)
  {
    System.out.println("new list");
    entries = new ArrayList<>();
    int nextEntry = 0;
    while(nextEntry < bytes.length){
      IndexEntry entry = parseEntry(bytes,nextEntry);
      if(entry == null){
        break;
      }
      entries.add(entry);
      nextEntry+=entry.length;
    }
  }
  private IndexEntry parseEntry(byte[] bytes, int offset)
  {
    System.out.println();
    System.out.println("new Index");
    IndexEntry toReturn = new IndexEntry();
    toReturn.mftFileReference = Helper.bytesToLong(bytes,offset+0,6);
    System.out.println("mftFileReference "+ toReturn.mftFileReference);

    toReturn.length = Helper.bytesToInt(bytes,offset+8,2);
    if(toReturn.length <= 16){
      return null;
    }
    System.out.println(Helper.bytesToHexString(bytes,0,toReturn.length));
    System.out.println("length "+ toReturn.length);
    toReturn.lengthOfFileName_Attribute = Helper.bytesToInt(bytes,offset+10,2);
    System.out.println("length of file name "+toReturn.lengthOfFileName_Attribute);
    toReturn.flags = Helper.bytesToInt(bytes,offset+12,2);


    if(bytes.length > offset+16+toReturn.lengthOfFileName_Attribute){
      toReturn.vcn = Helper.bytesToLong(bytes,offset+16+toReturn.lengthOfFileName_Attribute,8);
    }else{
      //todo is this good? it represents that this does not refer to something itself, but might have kids
      toReturn.vcn = -1;
    }
    byte[] bytesForFileName = new byte[toReturn.lengthOfFileName_Attribute];
    if(bytesForFileName.length == 0){
      toReturn.fileName=null;
    }else{
      for(int i = 0; i< bytesForFileName.length; i++){
        bytesForFileName[i] = bytes[i+offset+16];
      }
      toReturn.fileName = new FileNameWithoutHeader(bytesForFileName);

    }

    return toReturn;
  }
  public List<IndexEntry> getSubFiles()
  {


    return entries;
  }
  public class IndexEntry
  {
     FileNameWithoutHeader fileName;
     long mftFileReference;
     int length;
     int lengthOfFileName_Attribute;
     int flags;
     long vcn;
     public IndexEntry()
     {
     }
     public String toString()
     {
       StringBuilder sb = new StringBuilder();
       sb.append("fileName "+ fileName);
       sb.append("\n");
       sb.append("mftFileReference "+ mftFileReference);
       sb.append("\n");
       sb.append("length "+ length);
       sb.append("\n");
       sb.append("lengthOfFileName_Attribute "+ lengthOfFileName_Attribute);
       sb.append("\n");
       sb.append("flags "+ flags);
       sb.append("\n");
       sb.append("vcn "+ vcn);
       return sb.toString();
     }

  }
}
