package westland.ntfs.uploader;

public class Data_Attribute extends Attribute
{
  DataRuns dr;
  byte[] bytes;
  public Data_Attribute(byte[] header)
  {
    super(header);

    if(!this.get_resident()){
      byte[] forDataRun = new byte[header.length - this.get_offsetToDataRuns()];
      for(int i = 0; i < forDataRun.length; i++){
        forDataRun[i] = header[i+this.get_offsetToDataRuns()];
      }
      dr = new DataRuns(forDataRun);

    }else{
      byte[] forDataRun = new byte[header.length - this.get_offsetToAttribute()];
      for(int i = 0; i < forDataRun.length; i++){
        forDataRun[i] = header[i+this.get_offsetToAttribute()];
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
