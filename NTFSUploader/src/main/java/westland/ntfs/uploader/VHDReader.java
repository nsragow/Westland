package westland.ntfs.uploader;

import java.io.RandomAccessFile;
import java.io.IOException;
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



  private static RandomAccessFile vhd;
  private int LBA;
  private long startOfNtfsPartition;
  private NTFS ntfs;


  public VHDReader(String path) throws Exception
  {
    vhd = new RandomAccessFile(path,"r");
    parseMBR();
    initializeNTFS();
  }
  public static MappedByteBuffer map(long start, long length)
  {
    try{
      return vhd.getChannel().map(FileChannel.MapMode.READ_ONLY,start,length);
    }catch(IOException e){
      throw new RuntimeException(e);
    }

  }
  public long getStartOfPartition()
  {
    return startOfNtfsPartition;
  }
  private void parseMBR() throws Exception
  {
    byte[] bytes = new byte[512];
    vhd.read(bytes);
    for(int index : PARTITION_INDEXES){
      if(((int) bytes[index+ID_INDEX]) == NTFS_ID){

        startOfNtfsPartition = Helper.bytesToLong(bytes,index+LBA_INDEX,4)*512l;
        Helper.NTFS_START =  startOfNtfsPartition;
        System.out.println(Helper.NTFS_START);
      }
    }
  }
  private void initializeNTFS() throws Exception
  {
    byte[] bytes = new byte[512];

    vhd.seek(startOfNtfsPartition);
    vhd.read(bytes);
    ntfs = new NTFS(bytes);


    Helper.LCN_MULTIPLE = ntfs.BYTES_PER_SECTOR*ntfs.SECTORS_PER_CLUSTER;

    int offsetToEntry = 0;
    int i = 0;

    MFTEntry nextEntry;
    MFTEntry root = null;
    while(i <12){

      nextEntry = getEntry(startOfNtfsPartition+ntfs.relativeByteOfMFT(),i);
      if(!nextEntry.isFree() && nextEntry.hasFileName()){

        ntfs.addEntry(nextEntry);
        if(nextEntry.hasFileName()){
          if(nextEntry.getName().toLowerCase().equals(".")){
            root = nextEntry;
          }
        }
      }
      i++;
    }
    recurseThroughFileStructure(startOfNtfsPartition+ntfs.relativeByteOfMFT(), root);



  }
  private void recurseThroughFileStructure(long offset, MFTEntry entry) throws Exception
  {
    if(entry.hasFileName()){
      //System.out.println(entry.getName()+ " contains: ");
    }else{
      //System.out.println("entry has no name");
    }
    if(entry.hasSecurityIdentifier()){
      //System.out.println("getSecurityIdentifier is " + entry.getSecurityIdentifier());
      //System.out.println(SecurityInfo.getSecurityDescriptorforSIdentifier(entry.getSecurityIdentifier()).toString());
    }else{
      //System.out.println("no security id");
    }
    if(entry.isDirectory()){
      for(IndexEntryList.IndexEntry l : entry.getSubFiles()){
        if(l.fileName == null){
          //System.out.println(" file name is null ");
          //System.out.print(l.toString()+ " ");
        }else{
          //System.out.println(" File Name "+ l.fileName.getName());

        }
        //System.out.println("MFT Reference " + l.mftFileReference);

      }
      System.out.println();
      for(IndexEntryList.IndexEntry l : entry.getSubFiles()){
        if(l.fileName!=null&&!l.fileName.getName().equals(".")){
          recurseThroughFileStructure(offset,getEntry(offset,l.mftFileReference));

        }
      }
    }
  }
  private MFTEntry getEntry(long offset, long index) throws Exception
  {


    IndexConverter ic = new IndexConverter(new long[]{(offset+index*1024),1024});
    ic.map();
    return new MFTEntry(ic,0);
  }
}
//todo, are we dealing with signed or insigned integer values
//todo make this memory mapped so it fits when working with big Computers
