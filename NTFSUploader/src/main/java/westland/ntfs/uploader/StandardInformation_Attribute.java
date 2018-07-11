package westland.ntfs.uploader;

public class StandardInformation_Attribute extends Attribute
{
  int securityID;
  boolean hasSecurityID;
  public StandardInformation_Attribute(byte[] header)
  {
    super(header);
    if(get_lengthOfAttribute()<0x30+1){
      hasSecurityID = false;
    }else{
      securityID = Helper.bytesToInt(header,get_offsetToAttribute()+0x34,4);
      if(securityID == 0){
        hasSecurityID = false;
      }else{
        hasSecurityID = true;
      }
    }
  }
}
