package westland.ntfs.uploader;

import java.util.*;

public class SecurityDescriptor
{
  public class SID
  {
    int revisionLevel;
    int iDontKnow;
    long identifierAuthority;
    List<Long> subAthorities;
    public SID(IndexConverter ic, long offset, int size)
    {
      if(size<12){
        //System.out.println("--------------------------------------------size is "+size);
      }
      revisionLevel = Helper.bytesToInt(ic,offset+0,1);
      iDontKnow = Helper.bytesToInt(ic,offset+1,1);
      byte[] littleEndian = new byte[6];
      for(int i = 0; i < littleEndian.length; i++){
        littleEndian[i] = ic.get(offset+2+(littleEndian.length-1-i));
      }
      identifierAuthority = Helper.bytesToLong(littleEndian,0,littleEndian.length);
      subAthorities = new ArrayList<>();
      for(int i = 0; i < (size-8)/4; i++){
        subAthorities.add(Helper.bytesToLong(ic,offset+8+(4*i),4));
      }
    }
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      sb.append('S');
      sb.append('-');
      sb.append(revisionLevel);
      sb.append('-');
      sb.append(identifierAuthority);
      for(long sub : subAthorities){
        sb.append('-');
        sb.append(sub);
      }
      return sb.toString();
    }
  }
  int revision;
  int controlFlags;
  int offsetToUserSID;
  int offsetToGroupSID;
  int offsetToSACL;
  int offsetToDACL;
  ACL sacl;
  ACL dacl;
  SID userSID;
  SID groupSID;

  public SecurityDescriptor(IndexConverter ic, long offset, int size)
  {
    revision = Helper.bytesToInt(ic,offset+0,1);
    controlFlags = Helper.bytesToInt(ic,offset+2,2);
    offsetToUserSID = Helper.bytesToInt(ic,offset+4,4);
    offsetToGroupSID = Helper.bytesToInt(ic,offset+8,4);
    offsetToSACL = Helper.bytesToInt(ic,offset+12,4);
    offsetToDACL = Helper.bytesToInt(ic,offset+16,4);

    if(offsetToSACL != 0){
      sacl = new ACL(ic,offset+offsetToSACL);
    }
    if(offsetToDACL != 0){
      dacl = new ACL(ic,offset+offsetToDACL);
    }
    userSID = new SID(ic,offset+offsetToUserSID,offsetToGroupSID-offsetToUserSID);
    groupSID = new SID(ic,offset+offsetToGroupSID,size-offsetToGroupSID);
    //System.out.println("usersid = " + userSID.toString());
    //System.out.println("groupid = " + groupSID.toString());

  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("-------SecurityDescriptor-------\n");
    sb.append("User SID: " + userSID.toString());
    sb.append("\nGroup SID: " + groupSID.toString());
    if(sacl!=null){
      sb.append("\n-------SACL-------");
      sb.append(sacl.toString());
    }
    sb.append("\n-------DACL-------");
    sb.append(dacl.toString());

    return sb.toString();
  }

  public class ACL
  {
    int aclRevision;
    int aclSize;
    int aceCount;
    List<ACE> aces;

    public ACL(IndexConverter ic, long offset)
    {
      aclRevision = Helper.bytesToInt(ic,offset+0,1);
      aclSize = Helper.bytesToInt(ic,offset+2,2);
      aceCount = Helper.bytesToInt(ic,offset+4,2);
      aces = new ArrayList<>(aceCount);

      long indexOfACE = offset + 8;
      for(int i = 0; i < aceCount; i++){
        ACE toAdd = new ACE(ic, indexOfACE);
        indexOfACE += toAdd.size;
        aces.add(toAdd);
      }
    }
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      for(ACE ace : aces){
        sb.append(ace.toString());
        sb.append('\n');
      }
      return sb.toString();
    }

    public class ACE
    {
      int type;
      int flags;
      int size;
      int accessMask;
      SID sid;

      public ACE(IndexConverter ic, long offset)
      {
        type = Helper.bytesToInt(ic,offset+0,1);
        flags = Helper.bytesToInt(ic,offset+1,1);
        size = Helper.bytesToInt(ic,offset+2,2);
        accessMask = Helper.bytesToInt(ic,offset+4,4);
        sid = new SID(ic,offset+8,size-8);
      }
      public String toString()
      {
        StringBuilder sb = new StringBuilder();
        if(type == 0){
          sb.append("Allow Access: ");
        }else if(type == 1){
          sb.append("Deny Access: ");
        }else if(type == 2){
          sb.append("System Audit: ");
        }else{
          throw new RuntimeException("shouldnt be a type of " + type);
        }
        sb.append(sid.toString());
        return sb.toString();
      }
    }

  }
}
