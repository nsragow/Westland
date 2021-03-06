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
import java.net.SocketTimeoutException;
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

public class DataCollector
{
  private ServiceManager serviceManager;
  private Map<String,SignatureBuilder> dataMap;
  private Map<String,OrgUnitDescription> orgMap;
  private boolean organized;
  private StringBuilder logs;
  private HashSet<User> users;
  /**
  * make sure the ordering is correct in the defaultAddress
  */
  public DataCollector(ServiceManager serviceManager,StringBuilder logs)
  {
    this.serviceManager = serviceManager;
    organized = false;
    users = new HashSet<>();
    this.logs = logs;


    setUpDataMap(null);
    setUpOrgMap();
    specialRules();
  }
  public Map<String,SignatureBuilder> getDataMap()
  {
    return dataMap;
  }
  public Map<String,OrgUnitDescription> getOrgMap()
  {
    return orgMap;
  }
  public Set<User> getUsers()
  {
    return users;
  }
  private void setUpDataMap(String token)
  {
    dataMap = new HashMap<>();
    Collection<User> userSet = serviceManager.getUserSetBlackRemoved();
    for(User u : userSet){
      this.users.add(u);
      dataMap.put(u.getPrimaryEmail(),new SignatureBuilder(u));
    }
  }

  private void setUpOrgMap()
  {
    orgMap = new HashMap<>();
    try{

      List<OrgUnit> list = serviceManager.getOrgList();

      for(OrgUnit o : list){
        if(!o.getDescription().equals("#ignore")){
          OrgUnitDescription toAdd = new OrgUnitDescription(o.getName(),o.getDescription());
          orgMap.put(o.getName(), toAdd);
        }
      }
      //add the standard westland org unit

      OrgUnitDescription westland = new OrgUnitDescription("Westland");
      westland.put("zip",Strings.root_org_zip).put("city",Strings.root_org_city).put("address",Strings.root_org_address).put("state",Strings.root_org_state).put("phone",Strings.root_org_phone).put("fax","");
      orgMap.put("Westland",westland);


    }catch(IOException e){
      throw new FatalException("FATAL: Could not connect to Google for OrgMap initialization: "+e.toString());
    }


  }
  private void specialRules()
  {
    for(SignatureBuilder sb : dataMap.values()){
      String org = sb.get("org");
      if(org == null)throw new FatalException("ran into unusual problem "+sb.get("email"));
      OrgUnitDescription oud = orgMap.get(org);
      if(oud != null){
        sb.applyOUD(orgMap.get(org));
      }else{
        String email = sb.get("email");

        logs.append(email);
        logs.append(" has orgunit ");
        logs.append(org);
        logs.append(", but that orgunit does not seem to have any info on gsuite\r\n");
      }

    }
    //rule: boomy should have mobile in signature
    if(dataMap.containsKey(Strings.avi_email)){
      dataMap.get(Strings.avi_email).setDisplayMobile(true);
    }
    //rule: abe dont display fax and dont use main number
    if(dataMap.containsKey(Strings.abe_email)){
      dataMap.get(Strings.abe_email).remove("work_fax");
      //dataMap.get(Strings.abe_email).remove("work");
    }
  }
}
