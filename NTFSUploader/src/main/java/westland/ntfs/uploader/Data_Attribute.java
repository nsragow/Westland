package westland.ntfs.uploader;

public class Data_Attribute extends Attribute
{
  DataRuns dr;
  byte[] bytes;
  
  public Data_Attribute(IndexConverter header, long offset)
  {
    super(header, offset);

    if(!this.get_resident()){
      byte[] forDataRun = new byte[(int)(header.sizeInLong() - this.get_offsetToDataRuns())-(int) offset];
      for(int i = 0; i < forDataRun.length; i++){
        forDataRun[i] = header.get(i+this.get_offsetToDataRuns()+offset);
      }
      dr = new DataRuns(forDataRun);

    }else{
      byte[] forDataRun = new byte[(int)(header.sizeInLong() - this.get_offsetToAttribute())-(int) offset];
      for(int i = 0; i < forDataRun.length; i++){
        forDataRun[i] = header.get(i+this.get_offsetToAttribute()+offset);
      }
      bytes = forDataRun;

    }
  }
  public IndexConverter getFile()
  {
    IndexConverter ic;

      ic = dr.getIndexConverter();
      ic.map();
    return ic;
  }
  public byte[] getBytes()
  {
    return bytes;
  }
}
