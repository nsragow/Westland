package westland.signature.automator;
import java.util.Collection;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.admin.directory.model.Channel;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.admin.directory.Directory.Users.Watch;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.admin.directory.model.OrgUnit;
import com.google.api.services.admin.directory.model.OrgUnits;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.client.http.FileContent;

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.client.http.HttpTransport;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.SendAs;
import com.google.api.services.gmail.model.ListSendAsResponse;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.concurrent.TimeUnit;
import java.io.ByteArrayOutputStream;
import java.text.*;
import java.util.Date;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.io.FileOutputStream;


import javax.mail.*;
import javax.mail.internet.*;
import org.apache.commons.codec.binary.Base64;

public class OrgMovementDetector
{
  private static final int DO_NOTHING = 0;
  private static final int TITLE_CHANGE = 1;
  private static final int FULL_CHANGE = 2;
  private static final int OFFBOARD = 3;
  private static final int ONBOARD = 4;
  private static final int STAFF = 2;
  private static final int MANAGER = 1;
  private static final int AREA_MANAGER = 0;

  private static final String HR_EMAIL = "noah.s@westlandreg.com";
  private static final String HR_EMAIL_SUBJECT = "[Automated] Office Space Change Log";



  private Collection<User> users;
  private Map<String,SignatureBuilder> dataMap;
  private Map<String,OrgUnitDescription> orgMap;
  private StringBuilder logs;
  private ServiceManager serviceManager;
  private Directory service;
  private Table orgToGroup;
  private OfficeSpaceConnection officeSpace;
  private GroupWrapper gW;
  private List<String> updateThisSignature;
  public OrgMovementDetector(Set<User> users, Map<String,SignatureBuilder> dataMap, Map<String,OrgUnitDescription> orgMap, ServiceManager serviceManager) throws IOException
  {
    this.users = serviceManager.getUserSetBlackRemoved();
    this.dataMap = dataMap;
    this.orgMap = orgMap;
    logs = new StringBuilder();
    this.serviceManager = serviceManager;
    this.service = serviceManager.getDirectory();
    CSVReader csvread = new CSVReader(Strings.workingDirectory+"/src/main/resources/companynames.csv");
    orgToGroup = csvread.getTable();
    gW = new GroupWrapper(serviceManager);
    officeSpace = new OfficeSpaceConnection(serviceManager.getUserSetBlackRemoved());
    updateThisSignature = new ArrayList<>();
  }
  public void checkForChangeInOrg()
  {//todo!, now we must make sure that things are called in order, not sure but threads may affect this

    //first check office space and update accordingly
    Map<String,OfficeSpaceConnection.ChangeDetail> orgChanges = officeSpace.getDifference(users);
    boolean refreshNeeded = false;
    try{
      if(!officeSpace.getChangeLog().isEmpty()){
        serviceManager.sendEmail(HR_EMAIL,HR_EMAIL,HR_EMAIL_SUBJECT,officeSpace.getChangeLog());
      }
    }catch(IOException e){
      logs.append("Tried to send email to HR but failed\n");
    }

    for(String uString : orgChanges.keySet()){
      User u;
      try{
        u = serviceManager.getUser(uString);
        if(orgChanges.containsKey(u.getPrimaryEmail().toLowerCase())){
          u.setOrgUnitPath("/"+orgChanges.get(u.getPrimaryEmail().toLowerCase()).getOrg());
          UserFunctions.setExt(u,Integer.parseInt(orgChanges.get(u.getPrimaryEmail().toLowerCase()).getExt()));
          UserFunctions.setName(u,orgChanges.get(u.getPrimaryEmail().toLowerCase()).getFirstName(),orgChanges.get(u.getPrimaryEmail().toLowerCase()).getLastName());
          UserFunctions.setTitle(u,orgChanges.get(u.getPrimaryEmail().toLowerCase()).getTitle());
          try{
            //todo activate this
            service.users().update(u.getPrimaryEmail(),u).execute();
          }catch(Exception e){
            logs.append(Helper.exceptionToString(e)+"With the following user and org unit: "+u.getPrimaryEmail()+" || "+orgChanges.get(u.getPrimaryEmail().toLowerCase()).getOrg()+"\n");
            refreshNeeded = true;
          }
        }
      }catch(Exception e){
        logs.append(Helper.exceptionToString(e));
        continue;
      }
    }
    if(refreshNeeded)serviceManager.refreshUsers();


    Table oldOrgTable = Initializer.getTable(Strings.current_user_orgs);

    for(User u : users){
      try{
        checkForChangeInOrg(u,oldOrgTable);
      }catch(Exception e){
        logs.append(Helper.exceptionToString(e));
      }
    }

    try{
      Table.writeTableToCSV(oldOrgTable,Strings.current_user_orgs);

    }catch(Exception e){
      logs.append("could not update org to user matching\r\n"+Helper.exceptionToString(e));
    }
    updateSignatures();

    if(logs.length() != 0){
      throw new LogException(logs.toString());
    }
  }
  private int titleToID(String title)
  {
    title = title.toLowerCase();
    if(title.contains("manager")){
      if(title.contains("area")){
        return AREA_MANAGER;
      }else{
        return MANAGER;
      }
    }else{
      return STAFF;
    }
  }
  private int movementStatus(User u, Table oldInfo)
  {
    //do we need to offboard?
    if(u.getSuspended()){
      if(!Helper.orgPathToName(u.getOrgUnitPath()).toLowerCase().trim().equals("offboard")){
        return OFFBOARD;
      }else{
        return DO_NOTHING;
      }
    }
//todo need to deal with users being moved to unusual org units
    if(!oldInfo.containsKey(u.getPrimaryEmail())){
      return ONBOARD;
    }

    if(!oldInfo.get(u.getPrimaryEmail(),"org").trim().toLowerCase().equals(Helper.orgPathToName(u.getOrgUnitPath()).toLowerCase().trim())){
      return FULL_CHANGE;
    }
    String oldExt = oldInfo.get(u.getPrimaryEmail(),"ext");
    String newExt;
    try{
      newExt = UserFunctions.getExt(u);
    }catch(Exception e){
      newExt = "-1";
    }
    //initialize null titles
    if(newExt == null){
      newExt = "-1";
    }
    if(oldExt == null){
      oldExt = "-1";
    }
    long oldExtVal;
    long newExtVal;
    try{
      oldExtVal = Long.parseLong(oldExt);
    }catch(NumberFormatException e){
      oldExtVal = -1;
    }
    try{
      newExtVal = Long.parseLong(newExt);
    }catch(NumberFormatException e){
      newExtVal = -1;
    }
    if(oldExtVal != newExtVal){
      return FULL_CHANGE;
    }
    //at this point we know the orgs are the same
    String oldTitle = oldInfo.get(u.getPrimaryEmail(),"title");
    String newTitle;
    try{
      newTitle = UserFunctions.getTitle(u);
    }catch(Exception e){
      newTitle = "";
    }
    //initialize null titles
    if(newTitle == null){
      newTitle = "";
    }
    if(oldTitle == null){
      oldTitle = "";
    }
    if(oldTitle.toLowerCase().trim().equals(newTitle.toLowerCase().trim())){
      return DO_NOTHING;
    }else{
      return TITLE_CHANGE;
    }

  }
  private void removeFromAllGroups(String userName, String title, String org, String area, String region) throws IOException
  {
    int titleID = titleToID(title);
    switch(titleID){
      case AREA_MANAGER:
        if(area!=null){
          gW.removeEmailFromGroup(userName, Helper.areaToGroupEmail(area));
        }
        if(region!=null){
          gW.removeEmailFromGroup(userName, Helper.regionToGroupEmail(region));
        }
      case MANAGER:
        gW.removeEmailFromGroup(userName, Helper.orgUnitToManagerGroupEmail(org));
      case STAFF:
        gW.removeEmailFromGroup(userName, Helper.orgUnitToStaffGroupEmail(org));
        break;
      default:
        throw new RuntimeException("Unexpected Error");
    }
  }
  private void addToAllGroups(String userName, String title, String org, String area, String region) throws IOException
  {
    int titleID = titleToID(title);
    switch(titleID){
      case AREA_MANAGER:
        if(area!=null){
          if(!gW.hasGroup(Helper.areaToGroupEmail(area))){
            gW.createAreaGroup(area);
          }
          gW.addEmailToGroup(userName, Helper.areaToGroupEmail(area));
        }
        if(region!=null){
          if(!gW.hasGroup(Helper.regionToGroupEmail(region))){
            gW.createRegionGroup(region);
          }
          gW.addEmailToGroup(userName, Helper.regionToGroupEmail(region));
        }
      case MANAGER:
        if(!gW.hasGroup(Helper.orgUnitToManagerGroupEmail(org))){
          gW.createManagementGroup(org);
        }
        gW.addEmailToGroup(userName, Helper.orgUnitToManagerGroupEmail(org));
      case STAFF:
        if(!gW.hasGroup(Helper.orgUnitToStaffGroupEmail(org))){
          gW.createStaffGroup(org);
        }
        gW.addEmailToGroup(userName, Helper.orgUnitToStaffGroupEmail(org));
        break;
      default:
        throw new RuntimeException("Unexpected Error");
    }
  }
  private void removeFromTitleGroups(String userName, String title, String org, String area, String region) throws IOException
  {
    int titleID = titleToID(title);
    switch(titleID){
      case AREA_MANAGER:
        if(area!=null){
          gW.removeEmailFromGroup(userName, Helper.areaToGroupEmail(area));
        }
        if(region!=null){
          gW.removeEmailFromGroup(userName, Helper.regionToGroupEmail(region));
        }
      case MANAGER:
        gW.removeEmailFromGroup(userName, Helper.orgUnitToManagerGroupEmail(org));
      case STAFF:
        break;
      default:
        throw new RuntimeException("Unexpected Error");
    }
  }
  private void addToTitleGroups(String userName, String title, String org, String area, String region) throws IOException
  {
    int titleID = titleToID(title);
    switch(titleID){
      case AREA_MANAGER:
        if(area!=null){
          if(!gW.hasGroup(Helper.areaToGroupEmail(area))){
            gW.createAreaGroup(area);
          }
          gW.addEmailToGroup(userName, Helper.areaToGroupEmail(area));
        }
        if(region!=null){
          if(!gW.hasGroup(Helper.regionToGroupEmail(region))){
            gW.createRegionGroup(region);
          }
          gW.addEmailToGroup(userName, Helper.regionToGroupEmail(region));
        }
      case MANAGER:
        if(!gW.hasGroup(Helper.orgUnitToManagerGroupEmail(org))){
          gW.createManagementGroup(org);
        }
        gW.addEmailToGroup(userName, Helper.orgUnitToManagerGroupEmail(org));
      case STAFF:
        break;
      default:
        throw new RuntimeException("Unexpected Error");
    }
  }

  public void checkForChangeInOrg(User u, Table oldOrgTable)
  {
    boolean oldValid = false;
    boolean newValid = false;
    int status = movementStatus(u,oldOrgTable);
    String org = Helper.orgPathToName(u.getOrgUnitPath());
    String area;
    String region;
    String title;
    int ext;
    int oldExt;
    if(orgMap.containsKey(org)){
      OrgUnitDescription desc = orgMap.get(org);
      area = desc.get("area");
      region = desc.get("region");
    }else{
      throw new LogException("org map did not have proper set up for new info "+org+" "+u.getPrimaryEmail());
    }
    try{
      title = UserFunctions.getTitle(u);
    }catch(Exception e){
      title = "";
    }
    try{
      ext = Integer.parseInt(UserFunctions.getExt(u));
    }catch(Exception e){
      ext = -1;
    }
    String oldOrg;
    if(oldOrgTable.containsKey(u.getPrimaryEmail())){
      oldOrg = oldOrgTable.get(u.getPrimaryEmail(),"org");
      try{
        oldExt = Integer.parseInt(oldOrgTable.get(u.getPrimaryEmail(),"ext"));
      }catch(NumberFormatException e){
        oldExt = -1;
      }
    }else{
      oldOrg = null;
      oldValid = false;
      oldExt = -1;
    }
    String oldArea;
    String oldRegion;
    String oldTitle;
    if(orgMap.containsKey(oldOrg)){
      OrgUnitDescription desc = orgMap.get(oldOrg);
      oldArea = desc.get("area");
      oldRegion = desc.get("region");
      oldValid = true;
    }else{
      oldArea = null;
      oldRegion = null;
      oldValid = false;
    }
    try{
      oldTitle = oldOrgTable.get(u.getPrimaryEmail(),"title");
    }catch(IllegalArgumentException e){
      oldTitle = "";
      status = ONBOARD;
    }
    if(oldTitle == null){
      oldTitle="";
    }

    switch(status){
      case DO_NOTHING:
      break;

      case TITLE_CHANGE:
        if(!oldValid){
          throw new LogException("trying to mess with old info that is not valid on user "+u.getPrimaryEmail());
        }
        try{
          removeFromTitleGroups(u.getPrimaryEmail(),oldTitle,oldOrg,oldArea,oldRegion);
          addToTitleGroups(u.getPrimaryEmail(),title,org,area,region);
          oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),org,title,ext+""});
          updateThisSignature.add(u.getPrimaryEmail());
        }catch(Exception e){
          logs.append(Helper.exceptionToString(e));
          logs.append("\r\n");
        }
      break;

      case FULL_CHANGE:
        if(!oldValid){
          throw new LogException("trying to mess with old info that is not valid on user "+u.getPrimaryEmail());
        }
        try{
          removeFromAllGroups(u.getPrimaryEmail(),oldTitle,oldOrg,oldArea,oldRegion);
          addToAllGroups(u.getPrimaryEmail(),title,org,area,region);
          sendEmailOnOrgChange(u,oldOrg,oldExt);
          oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),org,title,ext+""});
          updateThisSignature.add(u.getPrimaryEmail());
        }catch(Exception e){
          logs.append(Helper.exceptionToString(e));
          logs.append("\r\n");
        }
      break;

      case OFFBOARD:
        u.setOrgUnitPath("/Offboard");
        try{
          removeFromAllGroups(u.getPrimaryEmail(),title,org,area,region);
          service.users().update(u.getPrimaryEmail(),u).execute();
          officeSpace.deleteUser(u.getPrimaryEmail().toLowerCase());
          oldOrgTable.remove(u.getPrimaryEmail());
        }catch(Exception e){
          logs.append(Helper.exceptionToString(e));
          logs.append("\r\n");
        }
      break;

      case ONBOARD:
        try{
          officeSpace.createUser(UserFunctions.getFirstName(u),UserFunctions.getLastName(u),title,u.getPrimaryEmail().toLowerCase());

          addToAllGroups(u.getPrimaryEmail(),title,org,area,region);
          sendEmailOnNewUser(u);
          oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),org,title,ext+""});
        }catch(Exception e){
          logs.append(Helper.exceptionToString(e));
          logs.append("\r\n");
        }
      break;

      default:
      throw new RuntimeException("Unexpected Error");
    }
  }
  private void titleAndOrgToGroup(User u, String newOrgName)
  {
    String title = null;
    try{
      title = UserFunctions.getTitle(u);
    }catch(Exception e){
      logs.append("could not get title off user: "+u.getPrimaryEmail()+"\r\n"+Helper.exceptionToString(e));
    }
    if(title == null || title.isEmpty()){

    }

  }
  private void titleToGroup(User u, String newOrgName)
  {
    String title = null;
    try{
      title = UserFunctions.getTitle(u);
    }catch(Exception e){
      logs.append("could not get title off user: "+u.getPrimaryEmail()+"\r\n"+Helper.exceptionToString(e));
    }
    if(title == null || title.isEmpty()){

    }

  }
  private void sendEmailOnOrgChange(User u, String oldOrgName, Integer oldExt) throws IOException
  {
    StringBuilder toSend = new StringBuilder();
    SignatureBuilder sb = dataMap.get(u.getPrimaryEmail());
    OrgUnitDescription oldOrg = orgMap.get(oldOrgName);
    String newOrgName = Helper.orgPathToName(u.getOrgUnitPath());
    OrgUnitDescription newOrg = orgMap.get(newOrgName);
    if(sb == null){
      logs.append( "could not find data on user "+u.getPrimaryEmail()+" when checking for orgunit change, will not update local info");
      logs.append("\r\n");


    }else if(oldOrg == null){

      if(Strings.BlackList.contains(oldOrgName)){
        if(newOrg == null){
          logs.append( "could not find data on from G-Suite new orgunit "+newOrgName+" when checking for orgunit change, will not update local info");
          logs.append("\r\n");
        }
        else{
          toSend.append(sb.get("name")+ " "+u.getPrimaryEmail()+"\n");
          toSend.append("Came from blacklisted orgUnit "+oldOrgName+"\n");
          toSend.append("Fax on new org: "+newOrg.get("fax"));
        }
      }
      else{
        logs.append( "could not find data from G-Suite on old orgunit "+oldOrgName+" when checking for orgunit change, will not update local info");
        logs.append("\r\n");
      }

    }else if(newOrg == null){
      logs.append( "could not find data on from G-Suite new orgunit "+newOrgName+" when checking for orgunit change, will not update local info");
      logs.append("\r\n");

    }else{
      toSend.append(sb.get("name")+ " "+u.getPrimaryEmail()+"\n");
      toSend.append("Fax on account: "+sb.get("work_fax")+"\n");
      toSend.append("Fax on old org: "+oldOrg.get("fax")+"\n");
      toSend.append("Fax on new org: "+newOrg.get("fax")+"\n");
      toSend.append("Old ext: "+oldExt+"\n");
      toSend.append("New ext: "+UserFunctions.getExt(u)+"\n");
      //todo make sure after testing that it actually does send to the correct ppl
      try{
        serviceManager.sendEmail(Strings.main_account_it,Strings.main_account_it,"CHANGE_DETECTED for "+ u.getPrimaryEmail()+" from "+ oldOrgName + " to "+Helper.orgPathToName(u.getOrgUnitPath()),toSend.toString(),Strings.itCC);
      }catch(Exception e){
        logs.append(Helper.exceptionToString(e));
        logs.append("\r\n");
      }
    }
  }


  private void sendEmailOnNewUser(User u) throws IOException
  {
    StringBuilder toSend = new StringBuilder();
    SignatureBuilder sb = dataMap.get(u.getPrimaryEmail());
    String newOrgName = Helper.orgPathToName(u.getOrgUnitPath());
    OrgUnitDescription newOrg = orgMap.get(newOrgName);
    if(sb == null){
      logs.append( "could not find data on user "+u.getPrimaryEmail()+" when checking for orgunit change, will not update local info");
      logs.append("\r\n");


    }else if(newOrg == null){
      logs.append( "could not find data on from G-Suite new orgunit "+newOrgName+" when checking for orgunit change, will not update local info");
      logs.append("\r\n");

    }else{
      toSend.append(sb.get("name")+ " "+u.getPrimaryEmail()+"\n");
      toSend.append("Fax on account: "+sb.get("work_fax")+"\n");

      toSend.append("Fax on org: "+newOrg.get("fax")+"\n");
      toSend.append("Ext on account: "+UserFunctions.getExt(u)+"\n");

      //todo make sure after testing that it actually does send to the correct ppl


      try{
        serviceManager.sendEmail(Strings.jesse_email,Strings.jesse_email,"ONBOARD for user: "+ u.getPrimaryEmail() + " to "+Helper.orgPathToName(u.getOrgUnitPath()),toSend.toString(),Strings.itCC);
      }catch(Exception e){
        logs.append(Helper.exceptionToString(e));
        logs.append("\r\n");
      }
    }
  }
  private void addTitleGroup(User u, String title)
  {
    try{
      if(title.toLowerCase().contains("manager")){
        gW.addEmailToGroup(u.getPrimaryEmail(),Helper.orgUnitToManagerGroupEmail(Helper.orgPathToName(u.getOrgUnitPath())));
      }
      gW.addEmailToGroup(u.getPrimaryEmail(),Helper.orgUnitToStaffGroupEmail(Helper.orgPathToName(u.getOrgUnitPath())));
    }catch(IOException e){
      logs.append(Helper.exceptionToString(e));
    }
  }
  private void removeTitleGroup(User u, String title, String oldOrg)
  {
    try{
      if(title.toLowerCase().contains("manager")){
        gW.removeEmailFromGroup(u.getPrimaryEmail(),Helper.orgUnitToManagerGroupEmail(oldOrg));
      }
      gW.removeEmailFromGroup(u.getPrimaryEmail(),Helper.orgUnitToStaffGroupEmail(oldOrg));
    }catch(IOException e){
      logs.append(Helper.exceptionToString(e));
    }
  }
  private void titleGroupChanger(User u, Table oldOrgName)
  { //assumes no org change, but maybe we should have it not assume that! have a boolean that declares if a change in org has occured also
    String newTitle = UserFunctions.getTitle(u);
    String oldTitle = oldOrgName.get(u.getPrimaryEmail(),"title");
    boolean oldInvalid = oldTitle==null || oldTitle.trim().isEmpty();
    boolean newInvalid = newTitle==null || newTitle.trim().isEmpty();
    //should check if they are the same
    if(!(oldInvalid || newInvalid)){ //only pass if both valid
      if(!oldTitle.equals(newTitle)){//not efficient because there is no need to remove from staff, only to check if need to add or remove from managers
        removeTitleGroup(u,oldTitle,oldOrgName.get(u.getPrimaryEmail(),"org"));
        addTitleGroup(u, newTitle);
      }
      //else they are both valid and the same
    }
    else if(!(oldInvalid && newInvalid)){  //only be false when both are invalid, true if either are vaild
      if(oldInvalid){
        addTitleGroup(u, newTitle);
      }else{
        removeTitleGroup(u,oldTitle,oldOrgName.get(u.getPrimaryEmail(),"org"));
      }
    }//else both are invalid
  }
  private void updateSignatures()
  {
    SignatureUpdater su = new SignatureUpdater(new DataCollector(serviceManager, new StringBuilder()).getDataMap(),serviceManager);
    for(String email : updateThisSignature){
      try{
        su.updateSignature(email);
      }catch(IOException e){
        logs.append(Helper.exceptionToString(e));
      }
    }
  }
}

//todo, am i accounting for when the org unit changes but not the title or visa versa?
