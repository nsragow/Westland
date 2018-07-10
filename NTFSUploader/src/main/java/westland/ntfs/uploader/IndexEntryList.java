package westland.ntfs.uploader;

import java.util.*;

public class IndexEntryList
{
  List<IndexEntry> entries;
  public IndexEntryList(byte[] bytes)
  {
    entries = new ArrayList<>();
    int nextEntry = 0;
    while(nextEntry < bytes.length){
      IndexEntry entry = parseEntry(bytes,nextEntry);
      if(entry.lastIndexEntry){
        if(entry.hasKids){
          entries.add(entry);
          nextEntry+=entry.length;
        }
        break;
      }
      entries.add(entry);
      nextEntry+=entry.length;
    }
  }
  private IndexEntry parseEntry(byte[] bytes, int offset)
  {

    IndexEntry toReturn = new IndexEntry();
    toReturn.mftFileReference = Helper.bytesToLong(bytes,offset+0,6);

    toReturn.length = Helper.bytesToInt(bytes,offset+8,2);
    toReturn.lengthOfFileName_Attribute = Helper.bytesToInt(bytes,offset+10,2);
    toReturn.flags = Helper.bytesToInt(bytes,offset+12,2);
    toReturn.hasKids = (toReturn.flags & 1) == 1;
    toReturn.lastIndexEntry = (toReturn.flags & 2) == 2;
    if(toReturn.lastIndexEntry && !toReturn.hasKids){

      return toReturn;
    }



    if(bytes.length > offset+16+toReturn.lengthOfFileName_Attribute){
      toReturn.vcn = Helper.bytesToLong(bytes,offset+16+toReturn.lengthOfFileName_Attribute,8);
    }else{
      //todo is this good? it represents that this does not refer to something itself, but might have kids
      toReturn.vcn = -1;
    }
    if(toReturn.lastIndexEntry){
      return toReturn;
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
     boolean lastIndexEntry;
     boolean hasKids;
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
