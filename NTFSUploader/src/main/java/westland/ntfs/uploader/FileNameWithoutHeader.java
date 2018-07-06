package westland.ntfs.uploader;

public class FileNameWithoutHeader
{
  private long referenceToParentDirectory;
  private long cTime;
  private long aTime;
  private long mTime;
  private long rTime;
  private long allocatedSize;
  private long realSize;
  private int flags;
  private int filenameLength;
  private int namespace;
  private String name;
  private String nameAsHex;
  public FileNameWithoutHeader(byte[] header)
  {
    referenceToParentDirectory = Helper.bytesToLong(header,0,8);
    cTime = Helper.bytesToLong(header,8,8);
    aTime = Helper.bytesToLong(header,16,8);
    mTime = Helper.bytesToLong(header,24,8);
    rTime = Helper.bytesToLong(header,32,8);
    allocatedSize = Helper.bytesToLong(header,40,8);
    realSize = Helper.bytesToLong(header,48,8);
    flags = Helper.bytesToInt(header,56,4);
    filenameLength = Helper.bytesToInt(header,64,1);
    namespace = Helper.bytesToInt(header,65,1);
    name = Helper.bytesToString(header,66,(2*filenameLength),"UTF-16LE");
    nameAsHex = Helper.bytesToHexString(header,66,(2*filenameLength));

  }
  public String getName()
  {
    return name;
  }
  public String toString()
  {
    StringBuilder sb = new StringBuilder();


    sb.append("referenceToParentDirectory: ");
    sb.append(this.referenceToParentDirectory);
    sb.append("\n");
    sb.append("name: ");
    sb.append(this.name);
    sb.append("\n");
    sb.append("name as hex: ");
    sb.append(this.nameAsHex);
    return sb.toString();

  }
}
