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

public class Main
{//clean this up todo make non static

  static String[] itCC = null;
  static Table STRINGS=null;
  static Table PATHS=null;
  static Table FILE_IDS=null;
  static String workingDirectory = null;

  //error values

  static Reports reports = null;


  public static Table oldOrgTable = null;
  public static Map<String,OrgUnitDescription> orgMap = null;
  public static Map<String,SignatureBuilder> dataMap = null;
  public static boolean orgCheckFailed = false;

  //strings

  //paths




  //BlackList


  static ServiceManager serviceManager = null;




  private static void stringInit()
  {//todo get rid of this
    Strings.service_account = STRINGS.get("service_account","val");
    Strings.root_org_address = STRINGS.get("root_org_address","val");
    Strings.root_org_zip = STRINGS.get("root_org_zip","val");
    Strings.root_org_city = STRINGS.get("root_org_city","val");
    Strings.root_org_state = STRINGS.get("root_org_state","val");
    Strings.root_org_phone = STRINGS.get("root_org_phone","val");
    Strings.main_account_it = STRINGS.get("main_account_it","val");
    Strings.exception_reporting_account = STRINGS.get("exception_reporting_account","val");
    Strings.exception_email_subject = STRINGS.get("exception_email_subject","val");
    Strings.local_user_orgs_not_updated_message = STRINGS.get("local_user_orgs_not_updated_message","val");
    Strings.suspended_org_path = STRINGS.get("suspended_org_path","val");
    Strings.SERVICE_ACCOUNT_PKCS12_FILE_PATH = workingDirectory+PATHS.get("p12","val");
    Strings.black_list = workingDirectory+PATHS.get("black_list","val");
    Strings.roll_out = workingDirectory+PATHS.get("roll_out","val");
    Strings.current_user_orgs = workingDirectory+PATHS.get("current_user_orgs","val");
    Strings.log_file = workingDirectory+PATHS.get("log_file","val");
    Strings.SERVICE_ACCOUNT_EMAIL = STRINGS.get("service_account","val");
    Strings.abe_email = STRINGS.get("abe_email","val");
    Strings.avi_email = STRINGS.get("avi_email","val");
    Strings.noah_email = STRINGS.get("noah_email","val");
    Strings.jesse_email = STRINGS.get("jesse_email","val");
    Strings.tomer_email = STRINGS.get("tomer_email","val");
    Strings.itCC = new String[]{Strings.tomer_email,Strings.abe_email,Strings.noah_email};
    Strings.workingDirectory = Main.workingDirectory;

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

    PATHS = Initializer.getTable(workingDirectory+"/src/main/resources/Paths.csv");
    FILE_IDS = Initializer.getTable(workingDirectory+"/src/main/resources/drive_file_id.csv");
    STRINGS = Initializer.getTable(workingDirectory+"/src/main/resources/Strings.csv");
    stringInit();
    serviceManager = new ServiceManager(Strings.main_account_it,Strings.SERVICE_ACCOUNT_PKCS12_FILE_PATH,Strings.SERVICE_ACCOUNT_EMAIL);
    try{
      Initializer.overwriteLocalFilesWithDrive(serviceManager.getDrive(Strings.main_account_it),FILE_IDS,PATHS,workingDirectory);
    }catch(Exception e){
      //todo deal with this Exception
      e.printStackTrace();
      System.exit(1);
    }

    STRINGS = Initializer.getTable(workingDirectory+"/src/main/resources/Strings.csv");
    stringInit();

    oldOrgTable = Initializer.getTable(Strings.current_user_orgs);
    orgMap = new HashMap<>();
    dataMap = new HashMap<>();
    try{
      Strings.BlackList = Initializer.fileToSet(Strings.black_list);
      Strings.Rollout = Initializer.fileToSet(Strings.roll_out);
    }catch(Exception e){
      e.printStackTrace();
      throw new RuntimeException();
    }
  }
  private static void fullRun()
  {



    Directory service = null;
    Gmail gmail = null;
    try{
      service = serviceManager.getDirectory();



    }
    catch(Exception e){
      emailOrErr(e);
      exit(1);
    }
    StringBuilder logs = new StringBuilder();
    DataCollector dataCollector = null;
    try{

      dataCollector = new DataCollector(service,logs);
    }catch(FatalException e){
      emailOrErr(e);
      exit(1);
    }
    if(logs.length() != 0){
      emailOrLog(new RuntimeException(logs.toString()));
    }

    Map<String,SignatureBuilder> dataMap = dataCollector.getDataMap();
    SignatureUpdater sU = new SignatureUpdater(dataMap,serviceManager);
    try{
      //todo make sure signatures are updated
      sU.updateSignatures();
    }
    catch(FatalException e){//todo redo the response
      emailOrErr(e);
      exit(1);
    }catch(LogException e){//todo redo the response
      emailOrLog(e);
    }catch(IOException e){
      emailOrErr(e);
      exit(1);
    }
    OrgMovementDetector orgDetector = new OrgMovementDetector(dataCollector.getUsers(),dataCollector.getDataMap(),dataCollector.getOrgMap(),serviceManager);
    try{
      orgDetector.checkForChangeInOrg();




    }catch(LogException e){
      emailOrLog(e);
    }
  }

  private static void liveSheetRun()
  {
    LiveSheet ls = new LiveSheet(serviceManager);
    try{
      ls.runLiveSheet();
    }catch(IOException e){

      emailOrErr(e);
      exit(1);
    }catch(LogException e){
      emailOrLog(e);
      exit(1);
    }
  }

  private static void orgCheck()
  {



    Directory service = null;
    Gmail gmail = null;
    try{
      service = serviceManager.getDirectory();



    }
    catch(Exception e){

      emailOrErr(e);
      exit(1);
    }
    StringBuilder logs = new StringBuilder();
    DataCollector dataCollector = null;
    try{

      dataCollector = new DataCollector(service,logs);
    }catch(FatalException e){
      emailOrErr(e);
      exit(1);
    }
    if(logs.length() != 0){
      emailOrLog(new RuntimeException(logs.toString()));
    }

    OrgMovementDetector orgDetector = new OrgMovementDetector(dataCollector.getUsers(),dataCollector.getDataMap(),dataCollector.getOrgMap(),serviceManager);
    try{
      orgDetector.checkForChangeInOrg();
    }catch(LogException e){
      emailOrLog(e);
    }

  }
  //args must have workingdirectory as first arg and it must end without a /
  public static void main(String[] args)
  {
    String option = null;
    workingDirectory = args[0];
    if(args.length > 1){
      option = args[1];
    }else{
      option = "-all";
    }

    reports = new Reports();
    try{

      initilize();
    }catch(Exception e){
      reports.err(Helper.exceptionToString(e));
      exit(1);
    }

    switch(option){
      case "-all":
        try{
          fullRun();
        }catch(Exception e){

          reports.err(Helper.exceptionToString(e));
        }
        break;
      case "-orgcheck":
        orgCheck();
        break;
      case "-livesheet":
        liveSheetRun();
        break;
      case "-shortCheck":
        liveSheetRun();
        orgCheck();
        break;
      case "-server":
        runServer();
        break;
      default:
        reports.err("Improper commandline argument: " + option+ "\n");
    }
    exit(0);
  }
  private static void runServer()
  {
    new WatchServer().run();
  }
  private static void emailOrErr(Exception e)
  {
    System.out.println(Helper.exceptionToString(e));
    try{
      serviceManager.sendEmail(Strings.exception_reporting_account,Strings.exception_reporting_account,Strings.exception_email_subject,Helper.exceptionToString(e));
    }catch(Exception b)
    {
      reports.err(Helper.exceptionToString(b));
    }
  }
  private static void emailOrLog(Exception e)
  {
    System.out.println(Helper.exceptionToString(e));
    try{
      serviceManager.sendEmail(Strings.exception_reporting_account,Strings.exception_reporting_account,Strings.exception_email_subject,Helper.exceptionToString(e));
    }catch(Exception b)
    {
      reports.log(Helper.exceptionToString(b));
    }
  }
  private static void exit(int i)
  {
    try{
      if(reports.hasReport()){
        serviceManager.sendEmail(Strings.main_account_it,Strings.main_account_it,"[Automated] G-Suite Manager Error Reports",reports.getReport(),Strings.itCC);
      }
    }catch(Exception e){
      e.printStackTrace();
      reports.err(Helper.exceptionToString(e));
    }
    try {
      Files.write(Paths.get(Strings.log_file), reports.getReport().getBytes(), StandardOpenOption.APPEND);
    }catch (IOException e) {
      e.printStackTrace();
    }

    System.err.println(reports.getReport());
    System.exit(i);
  }



  //calling this function should run the signature update







}
//todo! make sure that every Exception is accounted for
