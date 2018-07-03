package westland.ntfs.uploader;

import java.io.*;

public class Main
{
  public static void main(String[] args) throws Exception
  {
    byte[] bytes = {0,4,0};
    System.out.println(Helper.bytesToLong(bytes,1,2));
    VHDReader parser = new VHDReader("./src/main/resources/fixed.vhd");
    System.out.println(parser.getStartOfPartition());



  }
}
