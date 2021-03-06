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
  public void createAreaGroup(String name) throws IOException
  {
    createGroup(Helper.areaToGroupEmail(name),name + " Area","Area managers of "+name,MANAGEMENT);
  }
  public void createManagementGroup(OrgUnitDescription name) throws IOException
  {
    createGroup(Helper.orgUnitToManagerGroupEmail(name),name.getName() + " Management","Managers of "+name.getName(),MANAGEMENT);
  }
  public void createStaffGroup(OrgUnitDescription name) throws IOException
  {
    createGroup(Helper.orgUnitToStaffGroupEmail(name),name.getName() + " Staff","Staff of "+name.getName(),STAFF);
  }
  public void createRegionGroup(String name) throws IOException
  {
    createGroup(Helper.regionToGroupEmail(name),name + " Region","Area managers of "+name,MANAGEMENT);
  }
  private void makeNewStaffGroup(String groupKey, String name, String description) throws IOException
  {
    Group g = new Group();
    g.setEmail(groupKey);
    g.setDescription(description);
    g.setName(name);
    serviceManager.makeNewGroup(g);

    serviceManager.updateGroupSettings(groupKey, GroupSettingUpdater.setSettingsToStaff(serviceManager.getSettingsOfGroup(groupKey)));
  }
  private void makeNewManagementGroup(String groupKey, String name, String description) throws IOException
  {
    Group g = new Group();
    g.setEmail(groupKey);
    g.setDescription(description);
    g.setName(name);
    serviceManager.makeNewGroup(g);
    serviceManager.updateGroupSettings(groupKey, GroupSettingUpdater.setSettingsToManagement(serviceManager.getSettingsOfGroup(groupKey)));
  }
  public boolean createGroup(String groupKey, String name, String description, int groupType) throws IOException
  {
    if(!hasGroup(groupKey)){
      try{
        switch(groupType){
          case STAFF:
          makeNewStaffGroup(groupKey, name, description);
          break;
          case MANAGEMENT:
          makeNewManagementGroup(groupKey, name, description);
          break;
          default:
          throw new RuntimeException("Unknown grouptype "+ groupType);
        }
      }catch(IOException e){
        throw new IOException(Helper.exceptionToString(e)+"GroupKey: "+groupKey);
      }
      return true;
    }
    return false;
  }
  public boolean addEmailToGroup(String email,String groupKey) throws IOException
  { //maybe instead of polling first just request the add and then catch an exception
    if(!hasGroup(groupKey)){
      throw new RuntimeException("could not find group "+ groupKey);
    }
    if(!serviceManager.hasMemberInGroup(email,groupKey)){

      serviceManager.addMemberToGroup(new Member().setEmail(email),groupKey);
      return true;
    }
    return false;
  }
  public boolean removeEmailFromGroup(String email,String groupKey) throws IOException
  {
    if(hasGroup(groupKey)){
      if(serviceManager.hasMemberInGroup(email,groupKey)){

        serviceManager.removeMemberFromGroup(email,groupKey);
        return true;
      }
    }
    return false;
  }
  public boolean deleteGroup(String groupKey) throws IOException
  {
    if(hasGroup(groupKey)){
      serviceManager.deleteGroup(groupKey);
      return true;
    }
    return false;
  }
}
