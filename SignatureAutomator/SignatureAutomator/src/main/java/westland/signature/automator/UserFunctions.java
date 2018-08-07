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
      return orgList.get(0).get("title").toString().trim();
    }catch(NullPointerException e){
      return "";
    }
  }
  public static void setTitle(User user, String title)throws NullPointerException, IllegalArgumentException
  {
    Object orginizations = user.getOrganizations();
    if(orginizations==null){
      ArrayList<ArrayMap<String,Object>> mapiList = new ArrayList<ArrayMap<String,Object>>();
      ArrayMap<String,Object> mapizap = new ArrayMap<>();
      mapizap.put("title",title);
      mapiList.add(mapizap);

      orginizations = mapiList;
    }
    List<ArrayMap<String,Object>> orgList = null;
    try{
      orgList = SignatureBuilder.objectToArrayMapList(orginizations);
      orgList.get(0).put("title",title);
      user.setOrganizations(orgList);
    }catch(NullPointerException e){
      throw new NullPointerException("Could not set title on user "+user.getPrimaryEmail()+" "+Helper.exceptionToString(e));
    }
  }
  public static String getExt(User user)
  {
      Map<String,Map<String,Object>> cs = user.getCustomSchemas();
      if(cs!=null){
        Map<String,Object> addInfo = cs.get("Additional_Info");
        if(addInfo!=null){
          return addInfo.get("Extension").toString().trim();

        }else{
          return "";
        }
      }else{
        return "";

      }


  }
  public static String getFirstName(User u)
  {
    return u.getName().getGivenName().trim();
  }
  public static void setName(User u, String firstName, String lastName)
  {
    u.getName().setGivenName(firstName);
    u.getName().setFamilyName(lastName);
  }
  public static String getLastName(User u)
  {
    return u.getName().getFamilyName().trim();
  }
  public static void setExt(User user, String ext)
  {
    Map<String,Map<String,Object>> cs = user.getCustomSchemas();
    if(cs!=null){
      Map<String,Object> addInfo = cs.get("Additional_Info");
      if(addInfo!=null){
        addInfo.put("Extension",ext);

      }else{
        cs.put("Additional_Info",new HashMap<String,Object>());
        addInfo = cs.get("Additional_Info");
        addInfo.put("Extension",ext);
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
