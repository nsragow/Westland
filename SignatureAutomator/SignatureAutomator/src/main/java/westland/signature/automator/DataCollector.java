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
  private Directory dir;
  private Map<String,SignatureBuilder> dataMap;
  private Map<String,OrgUnitDescription> orgMap;
  private Set<String> blackList;

  /**
  * make sure the ordering is correct in the defaultAddress
  */
  public DataCollector(Directory dir, Set<String> blackList)
  {
    this.dir = dir;
  }
  public Map<String,SignatureBuilder> getDataMap()
  {
    if(dataMap == null){
      dataMap = new HashMap<>();
      getDataMap(null);
    }
    return dataMap;
  }
  private void getDataMap(String token)
  {

    try{
      do{
        Directory.Users.List list = dir.users().list().setCustomer("my_customer").setMaxResults(5).setOrderBy("email").setProjection("full");
        if(token!=null)list.setPageToken(token);
        Users users = list.execute();
        token = users.getNextPageToken();
        List<User> usersList = users.getUsers();
        for(User u : usersList){
          if(!blackList.contains(u.getPrimaryEmail()) && !blackList.contains(Helper.orgPathToName(u.getOrgUnitPath()))){

            dataMap.put(u.getPrimaryEmail(),new SignatureBuilder(u));
          }else{
            //System.out.println("skipped "+u.getPrimaryEmail().toString());
          }
        }
      }while(token!=null);
    }catch(SocketTimeoutException e){
      getDataMap(token);
    }catch(IOException e){
      throw new RuntimeException("Could not connect to Google for DataMap initialization: "+e.toString());
    }

  }

  public Map<String,OrgUnitDescription> getOrgMap()
  {
    if(orgMap == null){
      try{

        OrgUnits orgunits = dir.orgunits().list("my_customer").execute();
        List<OrgUnit> list = orgunits.getOrganizationUnits();

        for(OrgUnit o : list){
          if(!o.getDescription().equals("#ignore")){
            OrgUnitDescription toAdd = new OrgUnitDescription(o.getDescription());
            orgMap.put(o.getName(), toAdd);
          }
        }
        //add the standard westland org unit

        OrgUnitDescription westland = new OrgUnitDescription();
        westland.put("zip",Strings.root_org_zip).put("city",Strings.root_org_city).put("address",Strings.root_org_address).put("state",Strings.root_org_state).put("phone",Strings.root_org_phone).put("fax","");
        orgMap.put("Westland",westland);


      }catch(IOException e){
        throw new RuntimeException("Could not connect to Google for OrgMap initialization: "+e.toString());
      }
    }
    return orgMap;
  }
}