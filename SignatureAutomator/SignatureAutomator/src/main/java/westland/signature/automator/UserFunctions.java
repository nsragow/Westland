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
    try{
      orgList = SignatureBuilder.objectToArrayMapList(orginizations);
      return orgList.get(0).get("title").toString();
    }catch(NullPointerException e){
      throw new NullPointerException("Could not get title on user "+user.getPrimaryEmail()+" "+Helper.exceptionToString(e));
    }
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
  public static String getFirstName(User u)
  {
    return u.getName().getGivenName();
  }
  public static void setName(User u, String firstName, String lastName)
  {
    u.getName().setGivenName(firstName);
    u.getName().setFamilyName(lastName);
  }
  public static String getLastName(User u)
  {
    return u.getName().getFamilyName();
  }
  public static void setExt(User user, int ext)
  {
    Map<String,Map<String,Object>> cs = user.getCustomSchemas();
    if(cs!=null){
      Map<String,Object> addInfo = cs.get("Additional_Info");
      if(addInfo!=null){
        addInfo.put("Extension",ext);

      }else{
        throw new LogException("could not set ext for " + user.getPrimaryEmail() + " because Additional Info was not found");
      }
    }else{
      cs = new HashMap<String,Map<String,Object>>();
      Map<String,Object> toAdd = new HashMap<String,Object>();
      toAdd.put("Extension",ext);
      cs.put("Additional_Info",toAdd);
    }
    user.setCustomSchemas(cs);
  }
}
