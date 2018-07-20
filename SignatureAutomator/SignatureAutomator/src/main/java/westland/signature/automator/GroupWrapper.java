package westland.signature.automator;

import java.util.*;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Group;
import java.io.*;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;

public class GroupWrapper
{
  public static final int STAFF = 0;
  public static final int MANAGEMENT = 1;

  Set<String> existingGroups;
  ServiceManager serviceManager;
  public GroupWrapper(ServiceManager serviceManager)
  {
    existingGroups = new HashSet<>();
    this.serviceManager = serviceManager;
  }
  public boolean hasGroup(String group) throws IOException
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
  public void makeNewStaffGroup(String groupKey) throws IOException
  {
    Group g = new Group();
    g.setEmail(groupKey);
    g.setDescription("All staff at " + groupKey);
    g.setName(groupKey+" Staff");
    serviceManager.makeNewGroup(g);

    serviceManager.updateGroupSettings(groupKey, GroupSettingUpdater.setSettingsToStaff(serviceManager.getSettingsOfGroup(groupKey)));
  }
  public void makeNewManagementGroup(String groupKey) throws IOException
  {
    Group g = new Group();
    g.setEmail(groupKey);
    g.setDescription("Management of " + groupKey);
    g.setName(groupKey+" Management");
    serviceManager.makeNewGroup(g);

    serviceManager.updateGroupSettings(groupKey, GroupSettingUpdater.setSettingsToManagement(serviceManager.getSettingsOfGroup(groupKey)));
  }
  public void addEmailToGroup(String email,String groupKey, int groupType) throws IOException
  { //maybe instead of polling first just request the add and then catch an exception
    if(!hasGroup(groupKey)){
      switch(groupType){
        case STAFF:
        makeNewStaffGroup(groupKey);
        break;
        case MANAGEMENT:
        makeNewManagementGroup(groupKey);
        break;
        default:
        throw new RuntimeException("Unknown grouptype "+ groupType);
      }
    }
    if(!serviceManager.hasMemberInGroup(email,groupKey)){

      serviceManager.addMemberToGroup(new Member().setEmail(email),groupKey);
    }
  }
  public void removeEmailFromGroup(String email,String groupKey) throws IOException
  {
    if(hasGroup(groupKey)){
      if(serviceManager.hasMemberInGroup(email,groupKey)){

        serviceManager.removeMemberFromGroup(email,groupKey);
      }
    }
  }
}
