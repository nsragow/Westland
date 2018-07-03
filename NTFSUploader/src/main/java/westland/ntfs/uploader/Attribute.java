package westland.ntfs.uploader;


public class Attribute
{
  int attributeType;
  int length;
  boolean resident;
  int nameLength;
  int offsetToName;
  int flags;
  int attributeID;

  //resident
  int lengthOfAttribute;
  int offsetToAttribute;
  int indexedFlag;

  //non-resident
  long startingVCN;
  long lastVCN;
  int offsetToDataRuns;
  int compressionUnitSize;
  long allocatedSizeOfAttribute;
  long realSizeOfAttribute;
  long initializedDataSizeOfStream;
  public int get_attributeType()
  {
    return attributeType;
  }
  public int get_length()
  {
    return length;
  }
  public boolean get_resident()
  {
    return resident;
  }
  public int get_nameLength()
  {
    return nameLength;
  }
  public int get_offsetToName()
  {
    return offsetToName;
  }
  public int get_flags()
  {
    return flags;
  }
  public int get_attributeID()
  {
    return attributeID;
  }
  public int get_lengthOfAttribute()
  {
    return lengthOfAttribute;
  }
  public int get_offsetToAttribute()
  {
    return offsetToAttribute;
  }
  public int get_indexedFlag()
  {
    return indexedFlag;
  }
  public long get_startingVCN()
  {
    return startingVCN;
  }
  public long get_lastVCN()
  {
    return lastVCN;
  }
  public int get_offsetToDataRuns()
  {
    return offsetToDataRuns;
  }
  public int get_compressionUnitSize()
  {
    return compressionUnitSize;
  }
  public long get_allocatedSizeOfAttribute()
  {
    return allocatedSizeOfAttribute;
  }
  public long get_realSizeOfAttribute()
  {
    return realSizeOfAttribute;
  }
  public long get_initializedDataSizeOfStream()
  {
    return initializedDataSizeOfStream;
  }
  public String toString()
  {
    StringBuilder toReturn = new StringBuilder();
    toReturn.append(this.getClass().toString());
    toReturn.append("\n");
    toReturn.append("get_attributeType(): "+this.get_attributeType());
    toReturn.append("\n");
    toReturn.append("get_length(): "+this.get_length());
    toReturn.append("\n");
    toReturn.append("get_resident(): "+this.get_resident());
    toReturn.append("\n");
    toReturn.append("get_nameLength(): "+this.get_nameLength());
    toReturn.append("\n");
    toReturn.append("get_offsetToName(): "+this.get_offsetToName());
    toReturn.append("\n");
    toReturn.append("get_flags(): "+this.get_flags());
    toReturn.append("\n");
    toReturn.append("get_attributeID(): "+this.get_attributeID());
    toReturn.append("\n");
    if(this.get_resident()){
      toReturn.append("get_lengthOfAttribute(): "+this.get_lengthOfAttribute());
      toReturn.append("\n");
      toReturn.append("get_offsetToAttribute(): "+this.get_offsetToAttribute());
      toReturn.append("\n");
      toReturn.append("get_indexedFlag(): "+this.get_indexedFlag());
    }
    else{
      toReturn.append("get_startingVCN(): "+this.get_startingVCN());
      toReturn.append("\n");
      toReturn.append("get_lastVCN(): "+this.get_lastVCN());
      toReturn.append("\n");
      toReturn.append("get_offsetToDataRuns(): "+this.get_offsetToDataRuns());
      toReturn.append("\n");
      toReturn.append("get_compressionUnitSize(): "+this.get_compressionUnitSize());
      toReturn.append("\n");
      toReturn.append("get_allocatedSizeOfAttribute(): "+this.get_allocatedSizeOfAttribute());
      toReturn.append("\n");
      toReturn.append("get_realSizeOfAttribute(): "+this.get_realSizeOfAttribute());
      toReturn.append("\n");
      toReturn.append("get_initializedDataSizeOfStream(): "+this.get_initializedDataSizeOfStream());

    }
    return toReturn.toString();
  }
  //should be byte[] of length 64
  protected Attribute(byte[] header)
  {
    attributeType = Helper.bytesToInt(header,0,4);
    length = Helper.bytesToInt(header,4,4);
    resident = (header[8] == 0);
    nameLength = Helper.bytesToInt(header,9,1);
    offsetToName = Helper.bytesToInt(header,10,2);
    flags = Helper.bytesToInt(header,12,2);
    attributeID = Helper.bytesToInt(header,14,2);

    if(resident){
      lengthOfAttribute = Helper.bytesToInt(header,16,4);
      offsetToAttribute = Helper.bytesToInt(header,20,2);
      indexedFlag = Helper.bytesToInt(header,22,1);
    }else{
      startingVCN = Helper.bytesToLong(header,16,8);
      lastVCN = Helper.bytesToLong(header,24,8);
      offsetToDataRuns = Helper.bytesToInt(header,32,2);
      compressionUnitSize = Helper.bytesToInt(header,34,2);
      allocatedSizeOfAttribute = Helper.bytesToLong(header,40,8);
      realSizeOfAttribute = Helper.bytesToLong(header,48,8);
      initializedDataSizeOfStream = Helper.bytesToLong(header,56,8);
    }
  }

  public static Attribute getAttribute(byte[] header)
  {
    int type = Helper.bytesToInt(header,0,4);
    switch(type){
      case 16:
      return new StandardInformation_Attribute(header);
      case 32:
      return new AttributeList_Attribute(header);
      case 48:
      return new FileName_Attribute(header);
      case 64:
      return new ObjectID_Attribute(header);
      case 80:
      return new SecurityDescriptor_Attribute(header);
      case 96:
      return new VolumeName_Attribute(header);
      case 112:
      return new VolumeInformation_Attribute(header);
      case 128:
      return new Data_Attribute(header);
      case 144:
      return new IndexRoot_Attribute(header);
      case 160:
      return new IndexAllocation_Attribute(header);
      case 176:
      return new BitMap_Attribute(header);
      case 192:
      return new ReparsePoint_Attribute(header);
      case 208:
      return new EAInformation_Attribute(header);
      case 224:
      return new EA_Attribute(header);
      case 240:
      return new PropertySet_Attribute(header);
      case 256:
      return new LoggedUtilityStream_Attribute(header);
      default:
      throw new RuntimeException("Unrecognized Attribute type " + type);
      //return new Attribute(header);
    }
  }
}
