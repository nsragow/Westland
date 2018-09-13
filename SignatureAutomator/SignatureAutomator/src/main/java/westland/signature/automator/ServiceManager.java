package westland.signature.automator;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import java.net.SocketTimeoutException;
import com.google.api.services.admin.directory.model.Users;
import com.google.api.services.admin.directory.model.User;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.client.http.FileContent;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.SendAs;
import com.google.api.services.gmail.model.ListSendAsResponse;
import java.util.Properties;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import com.google.api.services.admin.directory.model.OrgUnit;
import com.google.api.services.drive.model.File;
import com.google.api.services.groupssettings.Groupssettings;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;


public class ServiceManager
{
  private String main_account_it;
  private String pathToP12;
  private String serviceAccount;
  private List<String> scopes;
  private Directory directory;
  private Groupssettings groupssettings;
  private Map<String,User> userList;
  private List<OrgUnit> orgList;
  protected ServiceManager(String main_email, String pathToP12, String serviceAccount)
  {
    main_account_it = main_email;
    this.pathToP12 = pathToP12;
    this.serviceAccount = serviceAccount;

    scopes= new ArrayList<>();
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER);
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP);
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER);
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_ORGUNIT);
    scopes.add(DriveScopes.DRIVE);
    scopes.add(com.google.api.services.groupssettings.GroupssettingsScopes.APPS_GROUPS_SETTINGS);



    scopes.add(GmailScopes.GMAIL_SETTINGS_BASIC);
    scopes.add(GmailScopes.GMAIL_COMPOSE);
    scopes.add(GmailScopes.GMAIL_SEND);

    GoogleCredential credential = getImpersonatedCredential(main_account_it);
    HttpTransport httpTransport = getHttpTransport();
    JacksonFactory jsonFactory = getJacksonFactory();
    directory = new Directory.Builder(httpTransport, jsonFactory, null)
    .setHttpRequestInitializer(credential).setApplicationName("Noah'sAppWestlandMiddleWare").build();
  }
  private HttpTransport getHttpTransport()
  {
    return new NetHttpTransport();
  }
  private JacksonFactory getJacksonFactory()
  {
    return new JacksonFactory();
  }
  public List<OrgUnit> getOrgList() throws IOException //todo, what if there are more orgunits then one call can handle
  {
    if(orgList == null){
      orgList = getDirectory().orgunits().list("my_customer").execute().getOrganizationUnits();
    }
    return orgList;
  }
  public void updateOrg(OrgUnit o) throws IOException //todo, what if there are more orgunits then one call can handle
  {
    //try{
      getDirectory().orgunits().update("my_customer",Collections.singletonList(o.getOrgUnitId()),o).execute();
    //}catch(GoogleJsonResponseException e){
      //throw e; //todo
    //}
  }
  public OrgUnit getOrgUnit(String orgUnitName) throws IOException //todo, what if there are more orgunits then one call can handle
  {
    orgUnitName = orgUnitName.toLowerCase();
    for(OrgUnit o : getOrgList()){
      if(o.getName().toLowerCase().equals(orgUnitName)){
        return o;
      }
    }
    throw new RuntimeException("Could not find orgunit "+ orgUnitName);
  }
  public Group getGroup(String key) throws IOException
  {
    return getDirectory().groups().get(key).execute();
  }
  public com.google.api.services.groupssettings.model.Groups getSettingsOfGroup(String key) throws IOException
  {
    return getGroupsettings().groups().get(key).execute();
  }
  public void updateGroupSettings(String key, com.google.api.services.groupssettings.model.Groups content) throws IOException
  {
    getGroupsettings().groups().update(key,content).execute();
  }
  public void makeNewGroup(Group group) throws IOException
  {
    getDirectory().groups().insert(group).execute();
  }
  public void deleteGroup(String group) throws IOException
  {
    getDirectory().groups().delete(group).execute();
  }
  public void addMemberToGroup(Member member, String group) throws IOException
  {
    getDirectory().members().insert(group,member).execute();
  }
  public void removeMemberFromGroup(String member, String group) throws IOException
  {
    getDirectory().members().delete(group,member).execute();
  }
  public boolean hasMemberInGroup(String member, String group) throws IOException
  {
    return getDirectory().members().hasMember(group,member).execute().getIsMember();
  }

  public void updateSheet(String fileID, String name, java.io.File file, String accountName) throws IOException
  {
    File fileMetadata = new File();
    fileMetadata.setName(name);
    fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");


    FileContent mediaContent = new FileContent("text/csv", file);
    getDrive(accountName).files().update(fileID, fileMetadata, mediaContent).execute();

  }
  protected Gmail getGmail(String userEmail)
  {
    HttpTransport httpTransport = getHttpTransport();
    JacksonFactory jsonFactory = getJacksonFactory();
    return new Gmail.Builder(httpTransport, jsonFactory, getImpersonatedCredential(userEmail)).setApplicationName("Noah'sAppWestlandMiddleWare").build();
  }
  private GoogleCredential getImpersonatedCredential(String userEmail)
  {
    HttpTransport httpTransport = getHttpTransport();
    JacksonFactory jsonFactory = getJacksonFactory();
    try{
      return new GoogleCredential.Builder()
      .setTransport(httpTransport)
      .setJsonFactory(jsonFactory)
      .setServiceAccountId(serviceAccount)
      .setServiceAccountScopes(scopes)
      .setServiceAccountUser(userEmail)
      .setServiceAccountPrivateKeyFromP12File(
      new java.io.File(pathToP12))
      .build();
    }
    catch(Exception e){
      e.printStackTrace();
      throw new RuntimeException("oops");
    }
  }
  protected Directory getDirectory()
  {
    return directory;
  }
  public User getUser(String email) throws IOException
  {
    getUserList(); //makes sure userList is not null
    User toReturn =userList.get(email.toLowerCase());
    if(toReturn == null) throw new IOException("The user "+email+" does not exist");
    return toReturn;
  }
  protected Groupssettings getGroupsettings()
  {
    if(groupssettings != null){
      return groupssettings;
    }
    GoogleCredential credential = getImpersonatedCredential(main_account_it);
    HttpTransport httpTransport = getHttpTransport();
    JacksonFactory jsonFactory = getJacksonFactory();

    return (groupssettings = new Groupssettings.Builder(httpTransport, jsonFactory, null)
    .setHttpRequestInitializer(credential).setApplicationName("Noah'sAppWestlandMiddleWare").build());
  }
  protected Drive getDrive(String email)
  {
    GoogleCredential credential = getImpersonatedCredential(email);
    HttpTransport httpTransport = getHttpTransport();
    JacksonFactory jsonFactory = getJacksonFactory();

    return new Drive.Builder(httpTransport, jsonFactory, null)
    .setHttpRequestInitializer(credential).setApplicationName("Noah'sAppWestlandMiddleWare").build();
  }
  public void sendEmail(String to, String from, String subject, String bodyText) throws IOException
  {
    sendEmail(to,from,subject,bodyText,null);
  }
  public void sendErrorReport(String bodyText) throws IOException
  {
    sendEmail(Strings.exception_reporting_account,Strings.exception_reporting_account,Strings.exception_email_subject,bodyText,null);
  }
  public void sendEmail(String to, String from, String subject, String bodyText, String[] ccs) throws IOException
  {
    //if(true){
      //System.out.println("Email to: "+to+" Subject: "+subject+" Body: \n"+bodyText);
      //return;
    //}
    //todo removeEmailFromGroup
    try{
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);

      MimeMessage email = new MimeMessage(session);

      email.setFrom(new InternetAddress(from));
      if(ccs != null){
        for(String s : ccs){
          //email.addRecipient(javax.mail.Message.RecipientType.CC,new InternetAddress(s));
//todo
        }

      }
      email.addRecipient(javax.mail.Message.RecipientType.TO,new InternetAddress(to));
      email.setSubject(subject);
      email.setText(bodyText);


      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      email.writeTo(buffer);
      byte[] bytes = buffer.toByteArray();
      String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
      Message message = new Message();
      message.setRaw(encodedEmail);



      this.getGmail(from).users().messages().send(from, message).execute();
    }catch(javax.mail.MessagingException e){
      throw new LogException(Helper.exceptionToString(e));
    }

  }
  public void changeUserSignature(String user, String toChangeTo) throws IOException
  {
    Gmail gmail = getGmail(user);
    SendAs primaryAlias = null;
    ListSendAsResponse aliases = gmail.users().settings().sendAs().list(user).execute();
    for (SendAs alias: aliases.getSendAs()) {
      if (alias.getIsPrimary()) {

        primaryAlias = alias;
        break;
      }
    }
    SendAs aliasSettings = new SendAs().setSignature(toChangeTo);
    SendAs sendAsResult = gmail.users().settings().sendAs().patch(
    "me",
    primaryAlias.getSendAsEmail(),
    aliasSettings)
    .execute();

  }
  public Collection<User> getUserList()
  {
    if(userList == null){
      userList = getUserList(null,null);
    }

    return userList.values();
  }
  public void refreshUsers()
  {
    userList = null;
  }
  public Collection<User> getUserSetBlackRemoved()
  {

    return getUserList();
  }
  private Map<String,User> getUserList(String token, Set<User> results)
  {
    Set<User> users;
    if(results != null){
      users = results;
    }else{
      users = new HashSet<User>();
    }
    try{
      do{
        Directory.Users.List list = getDirectory().users().list().setCustomer("my_customer").setMaxResults(5).setOrderBy("email").setProjection("full");
        if(token!=null)list.setPageToken(token);
        Users userobj = list.execute();
        token = userobj.getNextPageToken();
        users.addAll(userobj.getUsers());
      }while(token!=null);
      Map<String,User> userMap = new HashMap<>();

      for(User u : users){
        if(!Strings.BlackList.contains(u.getPrimaryEmail())&&!Strings.BlackList.contains(Helper.orgPathToName(u.getOrgUnitPath()))){
          userMap.put(u.getPrimaryEmail().toLowerCase(),u);
        }
      }
      return userMap;
    }catch(SocketTimeoutException e){
      return getUserList(token, users);
    }catch(IOException e){
      throw new FatalException("FATAL: Could not connect to Google for DataMap initialization: "+e.toString());
    }
  }

}
