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


public class SignatureUpdater
{
  private Directory dir;
  private Map<String,SignatureBuilder> dataMap;
  private ServiceManager serviceManager;
  private StringBuilder logs;

  //private static Map<String,OrgUnitDescription> orgMap = null;

  public SignatureUpdater(Map<String,SignatureBuilder> dataMap, ServiceManager serviceManager)
  {
    //this.orgMap = orgMap;
    this.dataMap = dataMap;
    this.serviceManager = serviceManager;
    logs = new StringBuilder();
  }

  private void updateSignatures() throws IOException
  {



    //at this point we only need to apply the dataMap to the signatures
    for(String email : dataMap.keySet()){
      //updateUserSignature(email,dataMap.get(email).toString());
      if(!dataMap.get(email).isComplete()){
        logs.append(email + " does not have sufficient information on their user+orgunit to create proper signature - signature not created\n");
      }else{
        //System.out.println("updated sig for "+email);
        serviceManager.changeUserSignature(email, SignatureGenerator.makeSignature(dataMap.get(email)));
      }

    }
    System.out.println("\n\n\n\n");
    Table table = new Table(new String[]{"Email","Name","Org","Website","Address","Phones","Title"});
    for(SignatureBuilder sigB : dataMap.values()){

      if(sigB.isComplete()){
        table.addRow(new String[]{sigB.get("email"),sigB.get("name"),sigB.get("org"),sigB.get("website"),sigB.get("addressPartOne")+sigB.get("addressPartTwo"),sigB.getPhoneHTML(),sigB.get("title")});
      }
    }
    try{
      Table.writeTableToCSV(table,"complete_data.csv");//todo what about updating drive?
    }catch(Exception e){
      e.printStackTrace();
    }
    System.out.println("\n\n\n\n");
    //System.out.println(orgMap);



    if(logs.length() == 0){
      throw new LogException(logs.toString());
    }
  }


}
