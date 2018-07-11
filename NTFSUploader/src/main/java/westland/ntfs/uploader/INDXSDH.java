package westland.ntfs.uploader;

import java.util.*;

public class INDXSDH
{
  String magicNumber;
  int offsetToFixupArray;
  int numberOfEntriesInFixupArray;
  long sequenceNumber;
  long vcn;
  int offsetToIndexEntries;
  int sizeOfEntries;
  int allocatedSizeOfEntries;
  boolean hasChildren;
  List<Entry> entries;

  Map<Integer,Long> identifierToOffset;

  public INDXSDH(IndexConverter indx)
  {

    indx.map();
    identifierToOffset = new HashMap<>();
    magicNumber = Helper.bytesToString(indx,0,4);
    if(!magicNumber.toLowerCase().equals("indx")){
      throw new RuntimeException("indx did not contain magic number indx");
    }
    offsetToFixupArray = Helper.bytesToInt(indx,4,2);
    numberOfEntriesInFixupArray = Helper.bytesToInt(indx,6,2);
    sequenceNumber = Helper.bytesToLong(indx,8,8);
    vcn = Helper.bytesToLong(indx,16,8);
    offsetToIndexEntries = Helper.bytesToInt(indx,0x18,4);
    sizeOfEntries = Helper.bytesToInt(indx,0x1c,4);
    allocatedSizeOfEntries = Helper.bytesToInt(indx,0x20,4);

    entries = new ArrayList<>();

    int index = 0x18 + offsetToIndexEntries;

    while(indx.has(index) && indx.get(index) == 0x18){
      Entry entry = new Entry();
      entry.offsetToData = Helper.bytesToInt(indx,index+0,2);
      entry.sizeOfData = Helper.bytesToInt(indx,index+0x02,2);
      entry.sizeOfIndexEntry = Helper.bytesToInt(indx,index+0x08,2);
      entry.sizeOfIndexKey = Helper.bytesToInt(indx,index+0x0a,2);
      entry.flags = Helper.bytesToInt(indx,index+0x0c,2);
      entry.HSDKey = Helper.bytesToInt(indx,index+0x10,4);
      entry.SIDKey = Helper.bytesToInt(indx,index+0x14,4);
      entry.HSDData = Helper.bytesToInt(indx,index+0x18,4);
      entry.SIDData = Helper.bytesToInt(indx,index+0x1c,4);
      entry.offsetToSD = Helper.bytesToLong(indx,index+0x20,8);
      entry.sizeOfSD = Helper.bytesToInt(indx,index+0x28,4);


      identifierToOffset.put(entry.SIDKey,entry.offsetToSD);
      if(entry.offsetToData!=0x18){
        System.out.println("value is wrong");
      }
      if(entry.sizeOfData!=0x14){
        System.out.println("value is wrong");
      }
      if(entry.sizeOfIndexEntry!=0x30){
        System.out.println("value is wrong");
      }
      if(entry.sizeOfIndexKey!=0x08){
        System.out.println("value is wrong");
      }

      index+=48;
      entries.add(entry);

    }
    SecurityInfo.identifierToOffset = this.identifierToOffset;


  }

  public class Entry
  {
    int offsetToData;
    int sizeOfData;
    int sizeOfIndexEntry;
    int sizeOfIndexKey;
    int flags;
    int HSDKey;
    int SIDKey;
    int HSDData;
    int SIDData;
    long offsetToSD;
    int sizeOfSD;
  }



}
