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

    int offsetToEntry = 0;
    int i = 0;

    MFTEntry nextEntry;
    while(i <12){
      nextEntry = getEntry(startOfNtfsPartition+ntfs.relativeByteOfMFT(),i);

      if(!nextEntry.isFree() && nextEntry.hasFileName()){

        ntfs.addEntry(nextEntry);
        if(nextEntry.hasFileName()){
          if(nextEntry.getName().toLowerCase().equals(".")){

            System.out.println("found dot");
            recurseThroughFileStructure(startOfNtfsPartition+ntfs.relativeByteOfMFT(), nextEntry);
            System.out.println("done with dot");

          }
        }
      }
      i++;
    }



  }
  private void recurseThroughFileStructure(long offset, MFTEntry entry) throws Exception
  {

    System.out.println(entry.getName());
    if(entry.isDirectory()){
      for(long l : entry.getSubFiles()){
        recurseThroughFileStructure(offset,getEntry(offset,l));
      }
    }
  }
  private MFTEntry getEntry(long offset, long index) throws Exception
  {
    byte[] bytes = new byte[1024];
    vhd.seek(offset+index*1024);

    vhd.read(bytes);
    return new MFTEntry(bytes);
  }
}
//todo, are we dealing with signed or insigned integer values
//todo make this memory mapped so it fits when working with big Computers
