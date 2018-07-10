package westland.ntfs.uploader;

import java.util.*;

public class SDSStream
{
  List<Entry> entries;

  public SDSStream(IndexConverter ic)
  {
    Entry entry;
    long index = 0;
    while((entry = new Entry(ic,index)).hash!= 0 && entry.sizeOfThisEntry != 0){
      int offsetToAdd = entry.sizeOfThisEntry;
      index+=offsetToAdd + (16-(offsetToAdd%16))%16;
      if(index%16!=0){
        System.out.println("oops");
      }
      System.out.println(entry.securityID);
    }
  }

  public class Entry
  {
    int hash;
    int securityID;
    long offsetToThisEntry;
    int sizeOfThisEntry;
    //other stuff not yet entered

    public Entry(IndexConverter ic, long offset)
    {
      hash = Helper.bytesToInt(ic,offset+0,4);
      securityID = Helper.bytesToInt(ic,offset+4,4);
      offsetToThisEntry = Helper.bytesToLong(ic,offset+8,8);
      sizeOfThisEntry = Helper.bytesToInt(ic,offset+16,4);
    }
  }
}
