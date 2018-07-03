package westland.ntfs.uploader;


public class MFTEntry
{
  final int indexOfmagicNumber = 0;
  final int lengthOfmagicNumber = 4;
  final int indexOfoffsetToUpdateSequence = 4;
  final int lengthOfoffsetToUpdateSequence = 2;
  final int indexOfnumberOfEntriesInFixupArray = 6;
  final int lengthOfnumberOfEntriesInFixupArray = 2;
  final int indexOfLSN = 8;
  final int lengthOfLSN = 8;
  final int indexOfsequenceNumber = 16;
  final int lengthOfsequenceNumber = 2;
  final int indexOfhardLinkCount = 18;
  final int lengthOfhardLinkCount = 2;
  final int indexOfoffsetToFirstAttribute = 20;
  final int lengthOfoffsetToFirstAttribute = 2;
  final int indexOfflags = 22;
  final int lengthOfflags = 2;
  final int indexOfusedSize = 24;
  final int lengthOfusedSize = 4;
  final int indexOfallocatedSizeOfEntry = 28;
  final int lengthOfallocatedSizeOfEntry = 4;
  final int indexOffileReferenceToTheBaseOfFileRecord = 32;
  final int lengthOffileReferenceToTheBaseOfFileRecord = 8;
  final int indexOfnextAttributeID = 40;
  final int lengthOfnextAttributeID = 2;
  final int indexOfnumberOfRecord = 44;
  final int lengthOfnumberOfRecord = 4;

  String magicNumber;//deal with baad instead of file
  int offsetToUpdateSequence;
  int numberOfEntriesInFixupArray;
  long LSN;
  int sequenceNumber;
  int hardLinkCount;
  int offsetToFirstAttribute;
  int flags;
  int usedSize;
  int allocatedSizeOfEntry;
  long fileReferenceToTheBaseOfFileRecord;
  int nextAttributeID;
  int numberOfRecord;

  public MFTEntry(byte[] data)
  {
    magicNumber = Helper.bytesToString(data,indexOfmagicNumber,lengthOfmagicNumber);
    offsetToUpdateSequence = Helper.bytesToInt(data,indexOfoffsetToUpdateSequence,lengthOfoffsetToUpdateSequence);
    numberOfEntriesInFixupArray = Helper.bytesToInt(data,indexOfnumberOfEntriesInFixupArray,lengthOfnumberOfEntriesInFixupArray);
    LSN = Helper.bytesToLong(data,indexOfLSN,lengthOfLSN);
    sequenceNumber = Helper.bytesToInt(data,indexOfsequenceNumber,lengthOfsequenceNumber);
    hardLinkCount = Helper.bytesToInt(data,indexOfhardLinkCount,lengthOfhardLinkCount);
    offsetToFirstAttribute = Helper.bytesToInt(data,indexOfoffsetToFirstAttribute,lengthOfoffsetToFirstAttribute);
    flags = Helper.bytesToInt(data,indexOfflags,lengthOfflags);
    usedSize = Helper.bytesToInt(data,indexOfusedSize,lengthOfusedSize);
    allocatedSizeOfEntry = Helper.bytesToInt(data,indexOfallocatedSizeOfEntry,lengthOfallocatedSizeOfEntry);
    fileReferenceToTheBaseOfFileRecord = Helper.bytesToLong(data,indexOffileReferenceToTheBaseOfFileRecord,lengthOffileReferenceToTheBaseOfFileRecord);
    nextAttributeID = Helper.bytesToInt(data,indexOfnextAttributeID,lengthOfnextAttributeID);
    numberOfRecord = Helper.bytesToInt(data,indexOfnumberOfRecord,lengthOfnumberOfRecord);
  }


}
