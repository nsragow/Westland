package westland.ntfs.uploader;

public class StandardInformation_Attribute extends Attribute
{
  int securityID;
  boolean hasSecurityID;
  boolean oldStandard;
  String filePermissions;
  public StandardInformation_Attribute(IndexConverter header, long offset)
  {
    super(header, offset);
    if(get_lengthOfAttribute()<0x30+1){
      hasSecurityID = false;
      oldStandard = true;
    }else{
      securityID = Helper.bytesToInt(header,offset+get_offsetToAttribute()+0x34,4);
      oldStandard = false;
      if(securityID == 0){
        hasSecurityID = false;
      }else{
        hasSecurityID = true;
      }
    }
    filePermissions = Helper.bytesToHexString(header,offset+get_offsetToAttribute()+32,4);
    System.out.println("filePermissions = "+ filePermissions);

  }
}
