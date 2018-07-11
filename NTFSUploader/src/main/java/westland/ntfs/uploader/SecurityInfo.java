package westland.ntfs.uploader;

import java.util.*;

public class SecurityInfo
{
  static Map<Long,SecurityDescriptor> offsetToEntry = null;
  static Map<Integer,Long> identifierToOffset = null;
  public static SecurityDescriptor getSecurityDescriptorforSIdentifier(int sIdentifier)
  {
    Long offset = identifierToOffset.get(sIdentifier);
    if(offset == null){
      throw new RuntimeException("did not have the identifierToOffset");
    }
    if(offsetToEntry.containsKey(offset)){
      return offsetToEntry.get(offset);
    }

    else throw new RuntimeException("dfsadfasdfasd;fjas;ldfka");
  }
}
