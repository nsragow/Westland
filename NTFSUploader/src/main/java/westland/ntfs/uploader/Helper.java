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
}
