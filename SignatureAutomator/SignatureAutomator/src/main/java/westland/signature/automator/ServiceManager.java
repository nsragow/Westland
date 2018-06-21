package westland.signature.automator;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
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

public class ServiceManager
{
  private String main_account_it;
  private String pathToP12;
  private String serviceAccount;
  private List<String> scopes;
  private Directory directory;
  protected ServiceManager(String main_email, String pathToP12, String serviceAccount)
  {
    main_account_it = main_email;
    this.pathToP12 = pathToP12;
    this.serviceAccount = serviceAccount;

    scopes= new ArrayList<>();
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_USER);
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY);
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER_READONLY);
    scopes.add(DirectoryScopes.ADMIN_DIRECTORY_ORGUNIT);
    scopes.add(DriveScopes.DRIVE);



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
  protected Drive getDrive(String email)
  {
    GoogleCredential credential = getImpersonatedCredential(email);
    HttpTransport httpTransport = getHttpTransport();
    JacksonFactory jsonFactory = getJacksonFactory();

    return new Drive.Builder(httpTransport, jsonFactory, null)
    .setHttpRequestInitializer(credential).setApplicationName("Noah'sAppWestlandMiddleWare").build();
  }
  public void sendEmail(String to, String from, String subject, String bodyText) throws Exception
  {
    sendEmail(to,from,subject,bodyText,null);
  }
  public void sendEmail(String to, String from, String subject, String bodyText, String[] ccs) throws Exception
  {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    MimeMessage email = new MimeMessage(session);

    email.setFrom(new InternetAddress(from));
    if(ccs != null){
      for(String s : ccs){
        email.addRecipient(javax.mail.Message.RecipientType.CC,new InternetAddress(s));

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
}
