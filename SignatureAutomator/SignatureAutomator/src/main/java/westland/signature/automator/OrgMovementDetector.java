package westland.signature.automator;

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
  private Set<User> users;
  private Map<String,SignatureBuilder> dataMap;
  private Map<String,OrgUnitDescription> orgMap;
  private StringBuilder logs;
  private ServiceManager serviceManager;
  private Directory service;
  private Table orgToGroup;
  private GroupWrapper gW;
  public OrgMovementDetector(Set<User> users, Map<String,SignatureBuilder> dataMap, Map<String,OrgUnitDescription> orgMap, ServiceManager serviceManager)
  {
    this.users = users;
    this.dataMap = dataMap;
    this.orgMap = orgMap;
    logs = new StringBuilder();
    this.serviceManager = serviceManager;
    this.service = serviceManager.getDirectory();
    CSVReader csvread = new CSVReader(Strings.workingDirectory+"/src/main/resources/companynames.csv");
    orgToGroup = csvread.getTable();
    gW = new GroupWrapper(serviceManager);
  }

  public void checkForChangeInOrg()
  {//todo!, now we must make sure that things are called in order, not sure but threads may affect this





    Table oldOrgTable = Initializer.getTable(Strings.current_user_orgs);

    for(User u : users){

      if(!u.getSuspended()){
        if(!Strings.BlackList.contains(u.getPrimaryEmail())&&!Strings.BlackList.contains(Helper.orgPathToName(u.getOrgUnitPath()))){
          String oldOrgName = null;
          String newOrgName = null;
          newOrgName = Helper.orgPathToName(u.getOrgUnitPath());
          //titleAndOrgToGroup(u,newOrgName);
          if(oldOrgTable.containsKey(u.getPrimaryEmail())){

            userFoundWorkflow(newOrgName,u,oldOrgTable);

          }else{// we havent seen this user before

            newUserWorkflow(u,newOrgName,oldOrgTable);
          }


        }
      }else{//need to move this user to offload because he is suspended
        u.setOrgUnitPath("/Offboard");
        try{
          service.users().update(u.getPrimaryEmail(),u).execute();
        }catch(Exception e){
          logs.append(e);
          logs.append("\r\n");
        }
      }
    }
    try{
      Table.writeTableToCSV(oldOrgTable,Strings.current_user_orgs);

    }catch(Exception e){
      logs.append("could not update org to user matching\r\n"+Helper.exceptionToString(e));
    }
    if(logs.length() != 0){
      throw new LogException(logs.toString());
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
  private void userFoundWorkflow(String newOrgName, User u, Table oldOrgTable)
  {
    String oldOrgName = null;
    try{
      oldOrgName = Helper.orgPathToName(oldOrgTable.get(u.getPrimaryEmail(),"org"));
    }catch(IllegalArgumentException e){
      oldOrgName = "{could not find in old org table: "+ u.getPrimaryEmail() + "}";
    }

    if(!newOrgName.equals(oldOrgName)){
      StringBuilder toSend = new StringBuilder();
      SignatureBuilder sb = dataMap.get(u.getPrimaryEmail());
      OrgUnitDescription oldOrg = orgMap.get(oldOrgName);
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
        toSend.append("Fax on new org: "+newOrg.get("fax"));
        //todo make sure after testing that it actually does send to the correct ppl
        try{
          serviceManager.sendEmail(Strings.main_account_it,Strings.main_account_it,"ORGCHANGE for "+ u.getPrimaryEmail()+" from "+ oldOrgName + " to "+Helper.orgPathToName(u.getOrgUnitPath()),toSend.toString(),Strings.itCC);
        }catch(Exception e){
          logs.append(Helper.exceptionToString(e));
          logs.append("\r\n");
        }
        String title = oldOrgTable.get(u.getPrimaryEmail(),"title");
        removeTitleGroup(u,title,oldOrgName);
        try{
          title = UserFunctions.getTitle(u);
          addTitleGroup(u,title);
        }catch(NullPointerException e){
          addTitleGroup(u,"");
        }catch(IllegalArgumentException e){
          logs.append(Helper.exceptionToString(e));
        }catch(Exception e){
          logs.append(Helper.exceptionToString(e));
        }

        //add to title and org groups

        //group management
        oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),Helper.orgPathToName(u.getOrgUnitPath()),oldOrgTable.get(u.getPrimaryEmail(),"title")});
      }
    }else{
      //only need to check changes to title
      titleGroupChanger(u, oldOrgTable);
    }

  }

  private void newUserWorkflow(User u,String newOrgName,Table oldOrgTable)
  {
    StringBuilder toSend = new StringBuilder();
    SignatureBuilder sb = dataMap.get(u.getPrimaryEmail());
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

      toSend.append("Fax on new org: "+newOrg.get("fax"));
      //todo make sure after testing that it actually does send to the correct ppl

      String title = "";
      try{
        title = UserFunctions.getTitle(u);
      }catch(Exception e){
        logs.append(Helper.exceptionToString(e));
      }
      String newGroup = Helper.orgUnitToStaffGroupEmail(newOrgName);

      //add to title and org groups
      if(title == null) title = "";
      oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),Helper.orgPathToName(u.getOrgUnitPath()),title});
      addTitleGroup(u,title);
      try{
        serviceManager.sendEmail(Strings.jesse_email,Strings.jesse_email,"ORGCHANGE for new user: "+ u.getPrimaryEmail() + " to "+Helper.orgPathToName(u.getOrgUnitPath()),toSend.toString(),Strings.itCC);
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
        gW.addEmailToGroup(u.getPrimaryEmail(),Helper.orgUnitToManagerGroupEmail(Helper.orgPathToName(u.getOrgUnitPath())),GroupWrapper.MANAGEMENT);
      }
      gW.addEmailToGroup(u.getPrimaryEmail(),Helper.orgUnitToStaffGroupEmail(Helper.orgPathToName(u.getOrgUnitPath())),GroupWrapper.STAFF);
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
}

//todo, am i accounting for when the org unit changes but not the title or visa versa?
