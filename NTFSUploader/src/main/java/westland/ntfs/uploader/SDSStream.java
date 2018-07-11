package westland.ntfs.uploader;

import java.util.*;

public class SDSStream
{
  Map<Long,SecurityDescriptor> offsetToEntry;

  public SDSStream(IndexConverter ic)
  {
    offsetToEntry = new HashMap<>();
    Entry entry;
    long index = 0;
    while((entry = new Entry(ic,index)).hash!= 0 && entry.sizeOfThisEntry != 0){ //todo this is not the way to do this
      offsetToEntry.put(index,entry.sd);
      int offsetToAdd = entry.sizeOfThisEntry;
      index+=offsetToAdd + (16-(offsetToAdd%16))%16;
      if(index%16!=0){
        System.out.println("oops");
      }

    }
    SecurityInfo.offsetToEntry = this.offsetToEntry;
  }

  public class Entry
  {
    int hash;
    int securityID;
    long offsetToThisEntry;
    int sizeOfThisEntry;
    SecurityDescriptor sd;
    //other stuff not yet entered

    public Entry(IndexConverter ic, long offset)
    {
      hash = Helper.bytesToInt(ic,offset+0,4);
      securityID = Helper.bytesToInt(ic,offset+4,4);
      offsetToThisEntry = Helper.bytesToLong(ic,offset+8,8);
      sizeOfThisEntry = Helper.bytesToInt(ic,offset+16,4);
      sd = new SecurityDescriptor(ic,offset+20,sizeOfThisEntry-20);
    }
  }
}
