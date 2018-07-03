package westland.ntfs.uploader;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VHDReader
{
  private static final int[] PARTITION_INDEXES = {446,462,478,494};
  private static final int NTFS_ID = 7;
  private static final int ID_INDEX = 4;
  private static final int LBA_INDEX = 8;



  private RandomAccessFile vhd;
  private int LBA;
  private int startOfNtfsPartition;
  private NTFS ntfs;


  public VHDReader(String path) throws Exception
  {
    vhd = new RandomAccessFile(path,"r");
    parseMBR();
    initializeNTFS();
  }
  public int getStartOfPartition()
  {
    return startOfNtfsPartition;
  }
  private void parseMBR() throws Exception
  {
    byte[] bytes = new byte[512];
    vhd.read(bytes);
    for(int index : PARTITION_INDEXES){
      if(((int) bytes[index+ID_INDEX]) == NTFS_ID){

        startOfNtfsPartition = Helper.bytesToInt(bytes,index+LBA_INDEX,4)*512;
      }
    }
  }
  private void initializeNTFS() throws Exception
  {
    byte[] bytes = new byte[512];

    vhd.seek(startOfNtfsPartition);
    vhd.read(bytes);
    ntfs = new NTFS(bytes);
    System.out.println("BYTES_PER_SECTOR: "+ ntfs.BYTES_PER_SECTOR);
    System.out.println("SECTORS_PER_CLUSTER: "+ ntfs.SECTORS_PER_CLUSTER);
    System.out.println("RESERVED_SECTORS: "+ ntfs.RESERVED_SECTORS);
    System.out.println("ALWAYS_ZERO: "+ ntfs.ALWAYS_ZERO);
    System.out.println("MEDIA_DESCRIPTOR: "+ ntfs.MEDIA_DESCRIPTOR);
    System.out.println("SECOND_ALWAYS_ZERO: "+ ntfs.SECOND_ALWAYS_ZERO);
    System.out.println("SECTORS_PER_TRACK: "+ ntfs.SECTORS_PER_TRACK);
    System.out.println("NUMBER_OF_HEADS: "+ ntfs.NUMBER_OF_HEADS);
    System.out.println("HIDDEN_SECTORS: "+ ntfs.HIDDEN_SECTORS);
    System.out.println("TOTAL_SECTORS: "+ ntfs.TOTAL_SECTORS);
    System.out.println("MFT_CLUSTER: "+ ntfs.MFT_CLUSTER);
    System.out.println("MFT_MIRROR_CLUSTER: "+ ntfs.MFT_MIRROR_CLUSTER);
    System.out.println("CLUSTERS_PER_FILE_RECORD_SEGMENT: "+ ntfs.CLUSTERS_PER_FILE_RECORD_SEGMENT);
    System.out.println("CLUSTERS_PER_INDEX_BUFFER: "+ ntfs.CLUSTERS_PER_INDEX_BUFFER);
    System.out.println("VOLUME_SERIAL_NUMBER: "+ ntfs.VOLUME_SERIAL_NUMBER);
    System.out.println("CHECKSUM: "+ ntfs.CHECKSUM);
    System.out.println();
    int offsetToEntry = 0;
    int i = 0;
    while(i < 10){
      i++;
      bytes = new byte[48];
      vhd.seek(startOfNtfsPartition+ntfs.relativeByteOfMFT()+offsetToEntry);
      vhd.read(bytes);
      MFTEntry entryOne = new MFTEntry(bytes);
      offsetToEntry+=entryOne.allocatedSizeOfEntry;

      System.out.println("magicNumber: "+entryOne.magicNumber);
      System.out.println("offsetToUpdateSequence: "+entryOne.offsetToUpdateSequence);
      System.out.println("numberOfEntriesInFixupArray: "+entryOne.numberOfEntriesInFixupArray);
      System.out.println("LSN: "+entryOne.LSN);
      System.out.println("sequenceNumber: "+entryOne.sequenceNumber);
      System.out.println("hardLinkCount: "+entryOne.hardLinkCount);
      System.out.println("offsetToFirstAttribute: "+entryOne.offsetToFirstAttribute);
      System.out.println("flags: "+entryOne.flags);
      System.out.println("usedSize: "+entryOne.usedSize);
      System.out.println("allocatedSizeOfEntry: "+entryOne.allocatedSizeOfEntry);
      System.out.println("fileReferenceToTheBaseOfFileRecord: "+entryOne.fileReferenceToTheBaseOfFileRecord);
      System.out.println("nextAttributeID: "+entryOne.nextAttributeID);
      System.out.println("numberOfRecord: "+entryOne.numberOfRecord);
      System.out.println();
      int offsetToNext = 0;
      boolean parseAgain = true;
      while(parseAgain){
        System.out.println("about to parse "+(entryOne.offsetToFirstAttribute+offsetToNext));
        bytes = new byte[64];
        System.out.println();
        vhd.seek(startOfNtfsPartition+ntfs.relativeByteOfMFT()+entryOne.offsetToFirstAttribute+offsetToNext);
        vhd.read(bytes);
        Attribute attribute = null;
        try{
          attribute = Attribute.getAttribute(bytes);
          System.out.println(attribute.toString());
          offsetToNext+=attribute.get_length();
        }catch(RuntimeException e){
          parseAgain = false;
        }
        System.out.println();


      }


    }


  }
}
//todo, are we dealing with signed or insigned integer values
//todo make this memory mapped so it fits when working with big Computers
