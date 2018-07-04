package westland.ntfs.uploader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Helper
{
  public static int bytesToInt(byte[] bytes, int offset, int length)
  {

    byte[] address = new byte[4];
    int i = 0;
    for(; i < length; i++){
      address[i] = bytes[i+offset];

    }
    for(; i < 4; i++){
      address[i] = 0;

    }

    ByteBuffer bb = ByteBuffer.wrap(address);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    return bb.getInt();


  }
  public static long bytesToLong(byte[] bytes, int offset, int length)
  {

    byte[] address = new byte[8];
    int i = 0;
    for(; i < length; i++){
      address[i] = bytes[i+offset];

    }
    for(; i < 8; i++){
      address[i] = 0;

    }

    ByteBuffer bb = ByteBuffer.wrap(address);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    return bb.getLong();


  }
  public static String bytesToString(byte[] bytes, int offset, int length)
  {

    byte[] address = new byte[length];
    int i = 0;
    for(; i < length; i++){
      address[i] = bytes[i+offset];

    }

    return new String(address);


  }
  public static String bytesToString(byte[] bytes, int offset, int length, String charset)
  {

    byte[] address = new byte[length];
    int i = 0;
    for(; i < length; i++){
      address[i] = bytes[i+offset];

    }
    String toReturn;
    try{
      toReturn = new String(address,charset);
    }catch(Exception e){
      throw new RuntimeException("charset not supported");
    }
    return toReturn;


  }
  public static String bytesToHexString(byte[] a, int offset, int length)
  {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < length; i++){
      sb.append(String.format("%02x", a[i+offset]));

    }
    return sb.toString();
  }
}
