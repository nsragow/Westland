package westland.signature.automator;

import java.util.*;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Group;
import java.io.*;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

public class GroupWrapper
{
  Set<String> existingGroups;
  ServiceManager serviceManager;
  public GroupWrapper(ServiceManager serviceManager)
  {
    existingGroups = new HashSet<>();
    this.serviceManager = serviceManager;
  }
  public boolean hasGroup(String group)throws Exception
  {
    if(existingGroups.contains(group)){
      return true;
    }else{
      try{
        serviceManager.getGroup(group);
        existingGroups.add(group);
        return true;
      }catch(GoogleJsonResponseException e){
        if(404==e.getStatusCode()){
          return false;
        }
      }

    }
    return false;
  }
  public void makeNewStaffGroup(String orgName)) throws IOException
  {
    Group g = new Group();
    g.setEmail(Helper.orgUnitToStaffGroupEmail(orgName));
    g.setDescription("All staff at " + orgName);
    g.setName(orgName+" Staff");
    serviceManager.makeNewGroup(g);
  }
  public void addEmailToGroup(String email,String groupKey) throws IOException
  {
    if(!serviceManager.hasMemberInGroup(email,groupKey)){

      serviceManager.addMemberToGroup(new Member().setEmail(email),groupKey);
    }
  }
  public void removeEmailFromGroup(String email,String groupKey) throws IOException
  {
    if(serviceManager.hasMemberInGroup(email,groupKey)){

      serviceManager.removeMemberFromGroup(email,groupKey);
    }
  }
}
