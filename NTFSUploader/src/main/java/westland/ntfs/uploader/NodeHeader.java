package westland.ntfs.uploader;


public class NodeHeader
{
  int offsetToFirstIndex;
  int offsetToEndOfUsedSpace;
  int offsetToEndOfAllocatedSpace;
  boolean indexAllocationNeeded;


  public NodeHeader(byte[] header)
  {
    offsetToFirstIndex = Helper.bytesToInt(header,0,4);
    offsetToEndOfUsedSpace = Helper.bytesToInt(header,4,4);
    offsetToEndOfAllocatedSpace = Helper.bytesToInt(header,8,4);
    indexAllocationNeeded = header[12] == 1;
  }
}
