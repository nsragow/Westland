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
  public OrgMovementDetector(Set<User> users, Map<String,SignatureBuilder> dataMap, Map<String,OrgUnitDescription> orgMap, ServiceManager serviceManager)
  {
    this.users = users;
    this.dataMap = dataMap;
    this.orgMap = orgMap;
    logs = new StringBuilder();
    this.serviceManager = serviceManager;
    this.service = serviceManager.getDirectory();
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
          if(oldOrgTable.containsKey(u.getPrimaryEmail())){
            try{
              oldOrgName =Helper.orgPathToName(oldOrgTable.get(u.getPrimaryEmail(),"org"));
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
                logs.append( "could not find data from G-Suite on old orgunit "+oldOrgName+" when checking for orgunit change, will not update local info");
                logs.append("\r\n");

              }else if(newOrg == null){
                logs.append( "could not find data on from G-Suite new orgunit "+newOrgName+" when checking for orgunit change, will not update local info");
                logs.append("\r\n");

              }else{
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

                oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),Helper.orgPathToName(u.getOrgUnitPath())});
              }
            }

          }else{// we havent seen this user before
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
              toSend.append("Fax on account: "+sb.get("work_fax")+"\n");

              toSend.append("Fax on new org: "+newOrg.get("fax"));
              //todo make sure after testing that it actually does send to the correct ppl

              oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),Helper.orgPathToName(u.getOrgUnitPath())});
              try{
                serviceManager.sendEmail(Strings.jesse_email,Strings.jesse_email,"ORGCHANGE for new user: "+ u.getPrimaryEmail() + " to "+Helper.orgPathToName(u.getOrgUnitPath()),toSend.toString(),Strings.itCC);
              }catch(Exception e){
                logs.append(Helper.exceptionToString(e));
                logs.append("\r\n");
              }
            }
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
}
