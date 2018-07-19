package westland.signature.automator;

import com.google.api.services.admin.directory.model.User;
import com.google.api.client.util.ArrayMap;
import java.util.*;

public class UserFunctions
{
  public static String getTitle(User user)throws NullPointerException, IllegalArgumentException
  {
    Object orginizations = user.getOrganizations();
    List<ArrayMap<String,Object>> orgList = null;
    orgList = SignatureBuilder.objectToArrayMapList(orginizations);
    return orgList.get(0).get("title").toString();
  }
  public static String getExt(User user)
  {
      Map<String,Map<String,Object>> cs = user.getCustomSchemas();
      if(cs!=null){
        Map<String,Object> addInfo = cs.get("Additional_Info");
        if(addInfo!=null){
          return addInfo.get("Extension").toString();

        }else{
          return "";
        }
      }else{
        return "";

      }


  }
}
