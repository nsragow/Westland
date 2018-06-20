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

public class ServiceConnection
{//clean this up todo make non static
  
  static final String[] itCC = null;
  static Table STRINGS=null;
  static Table PATHS=null;
  static Table FILE_IDS=null;

  //error values

  static Reports reports = null;


  public static Table oldOrgTable = null;
  public static Map<String,OrgUnitDescription> orgMap = null;
  public static Map<String,SignatureBuilder> dataMap = null;
  public static boolean orgCheckFailed = false;

  //strings
  static String service_account = null;
  static String root_org_address = null;
  static String root_org_zip = null;
  static String root_org_city = null;
  static String root_org_state = null;
  static String root_org_phone = null;
  static String main_account_it = null;
  static String exception_reporting_account = null;
  static String exception_email_subject = null;
  static String local_user_orgs_not_updated_message = null;
  static String suspended_org_path = null;
  //paths

  static String black_list = null;
  static String roll_out = null;
  static String current_user_orgs = null;
  static String log_file = null;



  //BlackList
  static Set<String> BlackList = null;
  static Set<String> Rollout = null;

  static ServiceManager serviceManager = null;


  /** Email of the Service Account */
  private static final String SERVICE_ACCOUNT_EMAIL = null;

  /** Path to the Service Account's Private Key file */
  private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = null;

  private static void stringInit()
  {//todo get rid of this
    service_account = STRINGS.get("service_account","val");
    root_org_address = STRINGS.get("root_org_address","val");
    root_org_zip = STRINGS.get("root_org_zip","val");
    root_org_city = STRINGS.get("root_org_city","val");
    root_org_state = STRINGS.get("root_org_state","val");
    root_org_phone = STRINGS.get("root_org_phone","val");
    main_account_it = STRINGS.get("main_account_it","val");
    exception_reporting_account = STRINGS.get("exception_reporting_account","val");
    exception_email_subject = STRINGS.get("exception_email_subject","val");
    local_user_orgs_not_updated_message = STRINGS.get("local_user_orgs_not_updated_message","val");
    suspended_org_path = STRINGS.get("suspended_org_path","val");
    SERVICE_ACCOUNT_PKCS12_FILE_PATH = PATHS.get("p12","val");
    black_list = PATHS.get("black_list","val");
    roll_out = PATHS.get("roll_out","val");
    current_user_orgs = PATHS.get("current_user_orgs","val");
    log_file = PATHS.get("log_file","val");
    SERVICE_ACCOUNT_EMAIL = STRINGS.get("service_account","val");
    abe_email = STRINGS.get("abe_email","val");
    avi_email = STRINGS.get("avi_email","val");
    noah_email = STRINGS.get("noah_email","val");
    jesse_email = STRINGS.get("jesse_email","val");
    tomer_email = STRINGS.get("tomer_email","val");
    itCC = new String[]{tomer_email,abe_email,noah_email};
  }
  /**
  * Build and returns a Directory service object authorized with the service accounts
  * that act on behalf of the given user.
  *
  * @param userEmail The email of the user. Needs permissions to access the Admin APIs.
  * @return Directory service object that is ready to make requests.
  */
  private static void initilize()
  {

    PATHS = Initializer.getTable("./src/main/resources/Paths.csv");
    FILE_IDS = Initializer.getTable("./src/main/resources/drive_file_id.csv");
    serviceManager = new ServiceManager(main_account_it,SERVICE_ACCOUNT_PKCS12_FILE_PATH,SERVICE_ACCOUNT_EMAIL);
    try{
      Initializer.overwriteLocalFilesWithDrive(serviceManager.getDrive(main_account_it),FILE_IDS,PATHS);
    }catch(Exception e){
      //todo deal with this Exception
      e.printStackTrace();
      System.exit(1);
    }
    STRINGS = Initializer.getTable("./src/main/resources/Strings.csv");


    stringInit();

    oldOrgTable = Initializer.getTable(current_user_orgs);
    orgMap = new HashMap<>();
    dataMap = new HashMap<>();
    try{
      BlackList = Initializer.fileToSet(black_list);
      Rollout = Initializer.fileToSet(roll_out);
    }catch(Exception e){
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
  private static void run()
  {
    try{

      initilize();
    }catch(Exception e){
      reports.log(e.toString());
      return;
    }

    Directory service = null;
    Gmail gmail = null;
    try{
      service = serviceManager.getDirectory();



    }
    catch(Exception e){
      e.printStackTrace();
      try{
        serviceManager.sendEmail(exception_reporting_account,exception_reporting_account,exception_email_subject,e.toString());
      }catch(Exception b){
        reports.err(b.toString());
        exit(1);
      }
    }

    try{
      //updateSignatures(service);

    }
    catch(Exception e){
      e.printStackTrace();
      try{
        serviceManager.sendEmail(exception_reporting_account,exception_reporting_account,exception_email_subject,e.toString());
      }catch(Exception b){
        reports.err(b.toString());
      }
      exit(1);
    }
    //networkLooper("checkForChangeInOrg");
    //networkLooper("mainfunction");
    System.exit(1);

    if(!orgCheckFailed){
      try{
        Table.writeTableToCSV(oldOrgTable,current_user_orgs);

      }catch(Exception e){
        e.printStackTrace();//todo handle this better
        try{
          serviceManager.sendEmail(exception_reporting_account,exception_reporting_account,exception_email_subject,e.toString());
        }catch(Exception b)
        {
          reports.err(b.toString());
        }
      }

    }else{
      reports.err(local_user_orgs_not_updated_message);
    }
    exit(0);
  }

  public static void main(String[] args)
  {
    reports = new Reports();
    try{
      run();
    }catch(Exception e){
      reports.err(e.toString());
    }
  }

  private static void exit(int i)
  {
    try{
      serviceManager.sendEmail(main_account_it,main_account_it,"[Automated] G-Suite Manager Error Reports",reports.getReport(),itCC);
    }catch(Exception e){
      e.printStackTrace();
      reports.err(e.toString());
    }
    try {
      Files.write(Paths.get(log_file), reports.getReport().getBytes(), StandardOpenOption.APPEND);
    }catch (IOException e) {
      e.printStackTrace();
    }
    System.exit(i);
  }
  private static void updateSignatures(Directory dir, String token, int step) throws IOException
  {
    try{
      if(step < 1){
        do{
          Directory.Users.List list = dir.users().list()
          .setCustomer("my_customer")
          .setMaxResults(5)//todo maybe optimize based on how often exception happens
          .setOrderBy("email")
          .setProjection("full");

          //System.out.println("before listing");

          //first collect whatever you can from Users, (name,email,title,fax,website,ext,company)

          if(token!=null)list.setPageToken(token);
          Users users = list.execute();
          token = users.getNextPageToken();
          List<User> usersList = users.getUsers();
          for(User u : usersList){
            if(!BlackList.contains(u.getPrimaryEmail()) && !BlackList.contains(Helper.orgPathToName(u.getOrgUnitPath()))){

              dataMap.put(u.getPrimaryEmail(),new SignatureBuilder(u));
            }else{
              //System.out.println("skipped "+u.getPrimaryEmail().toString());
            }
          }
        }while(token!=null);
        step = 1;
      }
      //now apply orginization info to orgMap
      if(step < 2){
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
        westland.put("zip",root_org_zip).put("city",root_org_city).put("address",root_org_address).put("state",root_org_state).put("phone",root_org_phone).put("fax","");
        orgMap.put("Westland",westland);


        step = 2;
      }

      //now time to apply orgMap to dataMap
      if(step<3){
        for(SignatureBuilder sb : dataMap.values()){
          String org = sb.get("org");
          if(org == null)throw new RuntimeException("ran into unusual problem "+sb.get("email"));
          OrgUnitDescription oud = orgMap.get(org);
          if(oud != null){
            sb.applyOUD(orgMap.get(org));
          }else{
            reports.err(sb.get("email")+ " has orgunit "  + org+", but that orgunit does not seem to have any info on gsuite");
          }

        }


        step = 3;
      }
      if(step < 4){
        //finally apply special rules
        specialRules();
      }
      //at this point we only need to apply the dataMap to the signatures
      for(String email : dataMap.keySet()){
        //updateUserSignature(email,dataMap.get(email).toString());
        if(!dataMap.get(email).isComplete()){
          reports.err(email + " does not have sufficient information on their user+orgunit to create proper signature - signature not created");
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
        Table.writeTableToCSV(table,"complete_data.csv");
      }catch(Exception e){
        e.printStackTrace();
      }
      System.out.println("\n\n\n\n");
      //System.out.println(orgMap);


    }catch(java.net.SocketTimeoutException e){
      //call again
      updateSignatures(dir,token,step);
    }
  }
  private static void updateOrg(Directory dir, Set<String> finishedOrgs, int secsToWait) throws Exception
  {//todo there must be a better way than just running threw the entired orglist everytime (it gets very slow near the end)
    //todo get rid of this and replace with more generic function
    CSVReader csvread = new CSVReader("./xcel sheets/orgtophone.csv");
    Table table = csvread.getTable();


    Set<String> alreadyDone = new HashSet<String>();
    if(finishedOrgs != null) alreadyDone = finishedOrgs;
    try{//got unknownhost and networkunreachable exception
      OrgUnits orgunits = dir.orgunits().list("my_customer").execute();
      List<OrgUnit> list = orgunits.getOrganizationUnits();

      for(OrgUnit o : list){

        String name = o.getName();
        if(!alreadyDone.contains(name)){
          if(!o.getDescription().equals("#ignore")){
            if(table.containsKey(name)){
              String fax = table.get(name,"phone");
              OrgUnitDescription orgDes = new OrgUnitDescription(o.getDescription());

              orgDes.put("phone",fax);

              o.setDescription(orgDes.toString());
              ArrayList<String> toUpdate = new ArrayList<>();
              toUpdate.add(o.getName());
              dir.orgunits().update("my_customer",toUpdate,o).execute();

            }else{
              //reports.err(name+" does not contain valid org info");




            }
          }
          alreadyDone.add(name);
          secsToWait = 1;
        }else{
          //skip this
        }

      }

    }catch(java.net.SocketTimeoutException e){

      updateOrg(dir,alreadyDone,secsToWait);
    }catch(GoogleJsonResponseException gj){
      try{

        TimeUnit.SECONDS.sleep(secsToWait);
      }catch(InterruptedException e){
        throw e;
      }

      secsToWait *= 2;
      if(secsToWait>16){
        reports.err("updateOrg function failed to connect to cloud after many attempts");
        throw gj;
      }else{
        updateOrg(dir,alreadyDone,secsToWait);
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  //calling this function should run the signature update
  private static void updateSignatures(Directory dir) throws IOException
  {
    //steps
    //1)Build user profiles locally collecting info from GSuite (use a hashmap)
    //2)apply BlackList
    //3)update signatures
    //4)apply rules
    updateSignatures(dir,null,0);

  }
  private static void networkLooper(String functionName)
  {
    networkLooperWithToken(functionName, null);
  }
  private static void networkLooperWithToken(String functionName, String token)
  {
    boolean runAgain = false;

    try{
      Directory service = serviceManager.getDirectory();
      do{
        runAgain=false;//to prevent never ending loop
        if(functionName.equals("mainfunction")){
          token = mainFunction(token,service);
          if(token!=null){
            runAgain = true;
          }
        }else if(functionName.equals("checkForChangeInOrg")){
          token = checkForChangeInOrg(token);
          if(token!=null){
            runAgain = true;
          }
        }else{
          throw new IllegalArgumentException("functionName not found: " + functionName);
        }

      }while(runAgain);
    }catch(java.net.SocketTimeoutException ste){
      networkLooperWithToken(functionName,token);
    }catch(Exception e){
      reports.err(e.toString());
    }
  }
  private static void updateOrg(Directory dir) throws Exception
  {
    updateOrg(dir,null,1);
  }
  private static void reviewOrgInfo(Directory dir)
  {

    try{


      OrgUnits orgunits = dir.orgunits().list("my_customer").execute();
      List<OrgUnit> list = orgunits.getOrganizationUnits();

      for(OrgUnit o : list){


        if(!o.getDescription().equals("#ignore"))new OrgUnitDescription(o.getDescription()).toString();


        //skip this


      }

    }catch(Exception e){
      e.printStackTrace();
    }
  }
  private static String mainFunction(String pageToken, Directory service) throws java.net.SocketTimeoutException
  {


    try{
      //System.out.println("Start");
      //changeUserSignature(gmail,"jsmith@exampleopractog.com");
      if(service == null){

        service = serviceManager.getDirectory();
      }
      Directory.Users.List list = service.users().list()
      .setCustomer("my_customer")
      .setMaxResults(10)
      .setOrderBy("email");

      //System.out.println("before listing");

      if(pageToken!=null)list.setPageToken(pageToken);
      Users users = list.execute();
      pageToken = users.getNextPageToken();
      List<User> usersList = users.getUsers();

      for(User u : usersList){
        SignatureBuilder sb = new SignatureBuilder(u);
        String company = sb.get("company");
        if(company!=null){
          if(sb.get("company").toLowerCase().equals("djt")){
            System.out.println(sb.get("email"));
          }

        }
      }

      return pageToken;


    }
    catch(java.net.SocketTimeoutException ste){
      throw ste;
    }
    catch(Exception e){
      e.printStackTrace();
      throw new RuntimeException("bad");
    }
  }
  private static void specialRules()
  {
    //rule: boomy should have mobile in signature
    if(dataMap.containsKey(avi_email)){
      dataMap.get(avi_email).setDisplayMobile(true);
    }
    //rule: abe dont display fax and dont use main number
    if(dataMap.containsKey(abe_email)){
      dataMap.get(abe_email).remove("work_fax");
      dataMap.get(abe_email).remove("work");
    }
    //rule: Change org names so that they display company name instead
    CSVReader csvread = new CSVReader("./src/main/resources/companynames.csv");
    Table table = csvread.getTable();
    if(table == null){
      reports.err("FATAL: Was unable to get company names for orgs in specialRules(), exiting to prevent poor signatures...");
      exit(1);
    }
    for(SignatureBuilder sb : dataMap.values()){
      String org = sb.get("org");
      if(org == null){
        reports.err("org company name not found for "+sb.get("email")+sb.get("org")+", removing company name from signature");
        sb.put("org","");
      }else if(!BlackList.contains(org)){

        String newOrg = table.get(org,"company");
        if(newOrg == null){
          reports.err("org company name not found for "+sb.get("email")+sb.get("org")+", removing company name from signature");
          sb.put("org","");
        }else{

          sb.put("org",newOrg);
        }
      }else{
        reports.err("org company name blacklisted for "+sb.get("email")+", "+sb.get("org")+", removing company name from signature");
        sb.put("org","");
      }

    }
  }
  private static String checkForChangeInOrg(String token) throws java.net.SocketTimeoutException
  {//todo!, now we must make sure that things are called in order, not sure but threads may affect this
    if(orgMap.isEmpty()||dataMap.isEmpty()){
      reports.err("could not check for org changes due to lack of orgMap/dataMap initialization");
      orgCheckFailed = true;
    }
    Directory service;



    try{
      //System.out.println("Start");
      //changeUserSignature(gmail,"jsmith@exampleopractog.com");

      service = serviceManager.getDirectory();
      Directory.Users.List list = service.users().list()
      .setCustomer("my_customer")
      .setMaxResults(5)
      .setOrderBy("email");

      //System.out.println("before listing");

      if(token!=null)list.setPageToken(token);
      Users users = list.execute();
      token = users.getNextPageToken();
      List<User> usersList = users.getUsers();

      for(User u : usersList){

        if(!u.getSuspended()){
          if(!BlackList.contains(u.getPrimaryEmail())&&!BlackList.contains(Helper.orgPathToName(u.getOrgUnitPath()))){
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
                  reports.err( "could not find data on user "+u.getPrimaryEmail()+" when checking for orgunit change, will not update local info");

                  orgCheckFailed = true;
                }else if(oldOrg == null){
                  reports.err( "could not find data from G-Suite on old orgunit "+oldOrgName+" when checking for orgunit change, will not update local info");
                  orgCheckFailed = true;
                }else if(newOrg == null){
                  reports.err( "could not find data on from G-Suite new orgunit "+newOrgName+" when checking for orgunit change, will not update local info");
                  orgCheckFailed = true;
                }else{
                  toSend.append("Fax on account: "+sb.get("work_fax")+"\n");
                  toSend.append("Fax on old org: "+oldOrg.get("fax")+"\n");
                  toSend.append("Fax on new org: "+newOrg.get("fax"));
                  //todo make sure after testing that it actually does send to the correct ppl
                  serviceManager.sendEmail(main_account_it,main_account_it,"ORGCHANGE for "+ u.getPrimaryEmail()+" from "+ oldOrgName + " to "+Helper.orgPathToName(u.getOrgUnitPath()),toSend.toString(),itCC);
                  oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),Helper.orgPathToName(u.getOrgUnitPath())});
                }
              }

            }else{// we havent seen this user before
              StringBuilder toSend = new StringBuilder();
              SignatureBuilder sb = dataMap.get(u.getPrimaryEmail());
              OrgUnitDescription newOrg = orgMap.get(newOrgName);
              if(sb == null){
                reports.err( "could not find data on user "+u.getPrimaryEmail()+" when checking for orgunit change, will not update local info");

                orgCheckFailed = true;
              }else if(newOrg == null){
                reports.err( "could not find data on from G-Suite new orgunit "+newOrgName+" when checking for orgunit change, will not update local info");
                orgCheckFailed = true;
              }else{
                toSend.append("Fax on account: "+sb.get("work_fax")+"\n");

                toSend.append("Fax on new org: "+newOrg.get("fax"));
                //todo make sure after testing that it actually does send to the correct ppl

                oldOrgTable.addRow(new String[]{u.getPrimaryEmail(),Helper.orgPathToName(u.getOrgUnitPath())});
                serviceManager.sendEmail(jesse_email,jesse_email,"ORGCHANGE for new user: "+ u.getPrimaryEmail() + " to "+Helper.orgPathToName(u.getOrgUnitPath()),toSend.toString(),itCC);
              }
            }


          }
        }else{//need to move this user to offload because he is suspended
          u.setOrgUnitPath("/Offboard");
          service.users().update(u.getPrimaryEmail(),u).execute();
        }
      }
      return token;


    }
    catch(java.net.SocketTimeoutException ste){
      throw ste;
    }
    catch(Exception e){
      e.printStackTrace();
      throw new RuntimeException("bad");
    }

  }
}
//todo! make sure that every Exception is accounted for
