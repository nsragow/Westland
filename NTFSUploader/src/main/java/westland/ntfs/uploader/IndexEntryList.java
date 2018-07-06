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
      entries.add(entry);
      nextEntry+=entry.length;
    }
  }
  private IndexEntry parseEntry(byte[] bytes, int offset)
  {
    IndexEntry toReturn = new IndexEntry();
    toReturn.mftFileReference = Helper.bytesToLong(bytes,offset+0,6);
    System.out.println(toReturn.mftFileReference);
    toReturn.length = Helper.bytesToInt(bytes,offset+8,2);
    toReturn.lengthOfFileName_Attribute = Helper.bytesToInt(bytes,offset+10,2);
    toReturn.flags = Helper.bytesToInt(bytes,offset+12,2);
    System.out.println(toReturn.flags);

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
      System.out.println(toReturn.fileName.getName());
    }
    System.out.println();
    return toReturn;
  }
  public List<Long> getSubFiles()
  {
    System.out.println("getting entries");
    ArrayList<Long> toReturn = new ArrayList<>();
    for(IndexEntry iEntry : entries){
      toReturn.add(iEntry.mftFileReference);
    }
    return toReturn;
  }
  public class IndexEntry
  {
    private FileNameWithoutHeader fileName;
    private long mftFileReference;
    private int length;
    private int lengthOfFileName_Attribute;
    private int flags;
    private long vcn;


  }
}
