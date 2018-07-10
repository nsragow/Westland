package westland.ntfs.uploader;

import java.util.*;

public class INDX
{
  String magicNumber;
  int offsetToFixupArray;
  int numberOfEntriesInFixupArray;
  long sequenceNumber;
  long vcn;
  NodeHeader nodeHeader;
  IndexEntryList entries;

  public INDX(byte[] indx)
  {

    magicNumber = Helper.bytesToString(indx,0,4);
    if(!magicNumber.toLowerCase().equals("indx")){
      throw new RuntimeException("indx did not contain magic number indx");
    }
    offsetToFixupArray = Helper.bytesToInt(indx,4,2);
    numberOfEntriesInFixupArray = Helper.bytesToInt(indx,6,2);
    sequenceNumber = Helper.bytesToLong(indx,8,8);
    vcn = Helper.bytesToLong(indx,16,8);
    byte[] forObjects = new byte[16];
    for(int i = 0; i < forObjects.length; i++){
      forObjects[i] = indx[i+24];
    }
    nodeHeader = new NodeHeader(forObjects);

    forObjects = new byte[nodeHeader.offsetToEndOfAllocatedSpace-nodeHeader.offsetToFirstIndex];
    for(int i = 0; i < forObjects.length; i++){
      forObjects[i] = indx[i+24];
    }
    entries = new IndexEntryList(forObjects);
  }
  public INDX(IndexConverter indx)
  {

    indx.map();

    magicNumber = Helper.bytesToString(indx,0,4);
    if(!magicNumber.toLowerCase().equals("indx")){
      throw new RuntimeException("indx did not contain magic number indx");
    }
    offsetToFixupArray = Helper.bytesToInt(indx,4,2);
    numberOfEntriesInFixupArray = Helper.bytesToInt(indx,6,2);
    sequenceNumber = Helper.bytesToLong(indx,8,8);
    vcn = Helper.bytesToLong(indx,16,8);
    byte[] forObjects = new byte[16];
    for(int i = 0; i < forObjects.length; i++){
      forObjects[i] = indx.get(i+24);
    }
    nodeHeader = new NodeHeader(forObjects);

    forObjects = new byte[nodeHeader.offsetToEndOfUsedSpace-nodeHeader.offsetToFirstIndex];
    for(int i = 0; i < forObjects.length; i++){
      forObjects[i] = indx.get(i+nodeHeader.offsetToFirstIndex+24);
    }
    entries = new IndexEntryList(forObjects);
  }
  public INDX(IndexConverter indx, String type)
  {

    indx.map();

    magicNumber = Helper.bytesToString(indx,0,4);
    if(!magicNumber.toLowerCase().equals("indx")){
      throw new RuntimeException("indx did not contain magic number indx");
    }
    offsetToFixupArray = Helper.bytesToInt(indx,4,2);
    numberOfEntriesInFixupArray = Helper.bytesToInt(indx,6,2);
    sequenceNumber = Helper.bytesToLong(indx,8,8);
    vcn = Helper.bytesToLong(indx,16,8);
    byte[] forObjects = new byte[16];
    for(int i = 0; i < forObjects.length; i++){
      forObjects[i] = indx.get(i+24);
    }
    nodeHeader = new NodeHeader(forObjects);

    forObjects = new byte[nodeHeader.offsetToEndOfUsedSpace-nodeHeader.offsetToFirstIndex];
    for(int i = 0; i < forObjects.length; i++){
      forObjects[i] = indx.get(i+nodeHeader.offsetToFirstIndex+24);
    }
    if(type.toLowerCase().equals("$sdh")){
      while(indx.hasNext()){
        System.out.print(Helper.bytesToHexString(new byte[]{indx.next()},0,1));
      }
      System.out.println();
    }
  }

  public List<IndexEntryList.IndexEntry> getSubFiles()
  {
    return entries.getSubFiles();
  }
}
