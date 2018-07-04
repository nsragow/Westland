package westland.ntfs.uploader;

public class FileName_Attribute extends Attribute
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
  public FileName_Attribute(byte[] header)
  {
    super(header);
    if(!this.get_resident()){
      throw new AttributeException("this FileName_Attribute is not resident, should not be possible");
    }
    int offset = this.get_offsetToAttribute();
    referenceToParentDirectory = Helper.bytesToLong(header,offset+0,8);
    cTime = Helper.bytesToLong(header,offset+8,8);
    aTime = Helper.bytesToLong(header,offset+16,8);
    mTime = Helper.bytesToLong(header,offset+24,8);
    rTime = Helper.bytesToLong(header,offset+32,8);
    allocatedSize = Helper.bytesToLong(header,offset+40,8);
    realSize = Helper.bytesToLong(header,offset+48,8);
    flags = Helper.bytesToInt(header,offset+56,4);
    filenameLength = Helper.bytesToInt(header,offset+64,1);
    namespace = Helper.bytesToInt(header,offset+65,1);
    name = Helper.bytesToString(header,offset+66,(2*filenameLength),"UTF-16LE");
    nameAsHex = Helper.bytesToHexString(header,offset+66,(2*filenameLength));

  }
  public String getName()
  {
    return name;
  }
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString());
    sb.append("\n");
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
