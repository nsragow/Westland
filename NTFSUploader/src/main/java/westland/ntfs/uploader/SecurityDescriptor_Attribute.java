package westland.ntfs.uploader;

public class SecurityDescriptor_Attribute extends Attribute
{
  SecurityDescriptor sd;
  public SecurityDescriptor_Attribute(IndexConverter header, long offset)
  {
    super(header, offset);
    System.out.println("found security descriptor");
    if(!this.get_resident()){
      throw new RuntimeException("this should be resident");
    }
    sd = new SecurityDescriptor(header, offset+this.get_offsetToAttribute(), this.get_lengthOfAttribute());
    System.out.println(sd);
  }
}
