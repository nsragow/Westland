package westland.signature.automator;

import com.google.api.services.admin.directory.model.User;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.client.jaxrs.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.util.*;
import java.net.URL;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;


public class OfficeSpaceConnection
{
  private Map<Long, OfficeSpaceSeat> seatMap;
  private Set<String> availableExt;
  private List<OfficeSpaceEmployee> employeeList;
  private Map<String,OfficeSpaceEmployee> employeeMap;
  private Map<String,ChangeDetail> emailToDetail;
  private StringBuilder log;
  private Collection<LogDetail> logDetails;
  private HashMap<String,ChangeDetail> emailToNewOrg;
  private Set<String> otherUsers;
  private Set<String> inGSuiteNotOfficeSpace;
  private Set<String> notSeated;
  private List<String> extraUserList;
  private Map<String,LinkedList<IdAndStatus>> emailsToIDandStatus;

  private static final String MAIN_DIRECTORY = "SYS: Make Public";
  //private static final String MAIN_DIRECTORY = "SYS: Make Private"; //todo
  private OfficeSpaceDirectory dir;
  private ExtensionMapper extMapper;

  public Collection<String> nonSeatedUsers()
  {
    return notSeated;
  }
  public Collection<String> missingUsers()
  {
    return inGSuiteNotOfficeSpace;
  }
  private class IdAndStatus
  {
    boolean seated;
    long id;
    IdAndStatus(long id, boolean seated)
    {
      this.id = id;
      this.seated = seated;
    }
  }
  public OfficeSpaceConnection(Collection<User> users)
  {
    System.setProperty(ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY, "org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder");
    emailToDetail = new HashMap<>();
    inGSuiteNotOfficeSpace = new HashSet<>();
    notSeated = new HashSet<>();
    otherUsers = new HashSet<>();
    extraUserList = new LinkedList<>();
    emailToNewOrg = new HashMap<>();
    availableExt = new HashSet<>();
    employeeMap = new HashMap<>();
    emailsToIDandStatus = new HashMap<>();
    extMapper = new ExtensionMapper();
    seatMap = new HashMap<>();
    employeeList = new LinkedList<>();
    log = new StringBuilder();
    logDetails = new LinkedList<LogDetail>();
    download();
    for(User u : users){
      try{
        if(emailToDetail.containsKey(u.getPrimaryEmail().toLowerCase())){
          String currentExt;
          if(UserFunctions.getExt(u) == null){
            currentExt = "";
          }else{
            currentExt = UserFunctions.getExt(u);
          }

          boolean extChange = !(currentExt.toLowerCase().equals(emailToDetail.get(u.getPrimaryEmail().toLowerCase()).ext.toLowerCase()));
          boolean orgChange = !Helper.orgPathToName(u.getOrgUnitPath()).toLowerCase().equals(emailToDetail.get(u.getPrimaryEmail().toLowerCase()).org.toLowerCase());
          boolean nameChange = !(UserFunctions.getFirstName(u).toLowerCase().equals(emailToDetail.get(u.getPrimaryEmail().toLowerCase()).firstName.toLowerCase()) && UserFunctions.getLastName(u).toLowerCase().equals(emailToDetail.get(u.getPrimaryEmail().toLowerCase()).lastName.toLowerCase()));
          String title;
          try{
            title = UserFunctions.getTitle(u);
          }catch(Exception e){
            title = "";
          }
          if(title == null){
            title = "";
          }
          boolean titleChange = !title.toLowerCase().equals(emailToDetail.get(u.getPrimaryEmail().toLowerCase()).getTitle().toLowerCase());
          //see if both org and ext are the same
          if(extChange||orgChange||nameChange||titleChange){
            emailToNewOrg.put(u.getPrimaryEmail(),emailToDetail.get(u.getPrimaryEmail().toLowerCase()));
            if(extChange){
              logDetails.add(new LogDetail(u.getPrimaryEmail(), UserFunctions.getExt(u),emailToDetail.get(u.getPrimaryEmail().toLowerCase()).ext+""));
            }
            if(orgChange){
              logDetails.add(new LogDetail(u.getPrimaryEmail(), Helper.orgPathToName(u.getOrgUnitPath()),emailToDetail.get(u.getPrimaryEmail().toLowerCase()).org));
            }
            if(nameChange){
              logDetails.add(new LogDetail(u.getPrimaryEmail(), UserFunctions.getFirstName(u)+" "+UserFunctions.getLastName(u),emailToDetail.get(u.getPrimaryEmail().toLowerCase()).firstName+" "+emailToDetail.get(u.getPrimaryEmail().toLowerCase()).lastName));
            }
            if(titleChange){
              logDetails.add(new LogDetail(u.getPrimaryEmail(), title,emailToDetail.get(u.getPrimaryEmail()).getTitle()));
            }
          }
        }else{ //not seated
          if(otherUsers.contains(u.getPrimaryEmail().toLowerCase())){
            notSeated.add(u.getPrimaryEmail().toLowerCase());
          }else{
            inGSuiteNotOfficeSpace.add(u.getPrimaryEmail().toLowerCase());
          }
        }
      }catch(Exception e){
        log.append(Helper.exceptionToString(e));
      }
    }
    removeDuplicates();
    if(log.length()!=0){
      throw new LogException(log.toString());
    }
  }
  private void removeDuplicates()
  {
    for(String s : emailsToIDandStatus.keySet()){
      List<IdAndStatus> lisst = emailsToIDandStatus.get(s);
      if(lisst.size()>1){
        boolean seated = false;
        for(IdAndStatus thing : lisst){
          seated = thing.seated || seated;
        }
        if(seated){
          for(IdAndStatus thing : lisst){
            if(!thing.seated){
              deleteUser(thing.id);
            }
          }
        }else{
          for(int i = 1; i < lisst.size(); i++){
            IdAndStatus thing = lisst.get(i);

            deleteUser(thing.id);
          }
        }
      }
    }
  }
  public void changeUserField(String email, String key, String value)
  {
    OfficeSpaceEmployee empl = employeeMap.get(email.toLowerCase());
    if(empl == null){
      throw new LogException("could not find OfficeSpace employee for email "+ email);
    }
    String id = ""+empl.getId();
    if(id == null){
      throw new LogException("could not find OfficeSpaceID for email "+ email);
    }

    Entity<String> payload = Entity.json("{\"record\":{\""+key+"\":\""+value+"\"}}");
    Client client = ClientBuilder.newClient();
    Invocation.Builder builder = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/employees/"+id)
    .request(MediaType.APPLICATION_JSON).header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"");
    try{
      Response response = builder.put(payload);
      ChangeDetail cd = emailToDetail.get(email);
      String oldTitle;
      if(cd != null){
        oldTitle = cd.getTitle();
      }else{
        oldTitle = "could not find old value";
      }
      logDetails.add(new LogDetail(email,"",value));
    }catch(Exception e){
      throw new LogException(Helper.exceptionToString(e));
    }


  }
  public String getChangeLog()
  {
    StringBuilder sb = new StringBuilder();
    for(LogDetail ld : logDetails){
      sb.append(ld.toString());
      sb.append("\n");
    }
    return sb.toString();
  }
  private class LogDetail
  {
    String email;
    String sourceLabel;
    String destinationLabel;
    LogDetail(String email, String sourceLabel, String destinationLabel)
    {
      this.email = email;
      this.sourceLabel = sourceLabel;
      this.destinationLabel = destinationLabel;
    }
    LogDetail(String email, String destinationLabel)
    {
      this.email = email;
      this.sourceLabel = "";
      this.destinationLabel = destinationLabel;
    }
    public String toString()
    {
      return email+": "+sourceLabel+"->"+destinationLabel;
    }
  }
  public void createUser(String firstName, String lastName, String title, String email)
  {
    if(emailsToIDandStatus.keySet().contains(email.toLowerCase())){
      return;
    }
    Entity<String> payload = Entity.json("{\"record\": {\"client_employee_id\": \""+email+"\",\"first_name\": \""+firstName+"\",\"last_name\": \""+lastName+"\",\"source\": \"API\",\"email\": \""+email+"\"}}");
    Client client = ClientBuilder.newClient();
    Invocation.Builder builder = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/employees")
    .request(MediaType.APPLICATION_JSON).header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"");
    try{
      Response response = builder.post(payload);

      logDetails.add(new LogDetail(email,"New User"));
    }catch(Exception e){
      throw new LogException(Helper.exceptionToString(e));
    }


  }
  public void deleteUser(String email)
  {

    String id = null;
    OfficeSpaceEmployee employee = null;
    if(email != null){
      employee = employeeMap.get(email.toLowerCase());
    }
    if(employee!=null){
      id = ""+employee.getId();
    }
    employeeMap.remove(email.toLowerCase());
    if(id == null){
      throw new LogException("could not find OfficeSpaceID for email "+ email);
    }
    deleteUser(Long.parseLong(id));
    logDetails.add(new LogDetail(email,"User Deleted"));
  }

  private void deleteUser(long id)
  {
    Client client = ClientBuilder.newClient();
    Invocation.Builder builder = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/employees/"+id)
    .request(MediaType.APPLICATION_JSON).header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"");
    try{
      Response response = builder.delete();

    }catch(Exception e){
      throw new LogException(Helper.exceptionToString(e));
    }
  }

  public Collection<String> getOrgsExtensions(String org)
  {
    return extMapper.getLabels(org.toLowerCase());
  }
  public boolean isLabelAvailable(String ext)
  {
    return availableExt.contains(ext.toLowerCase());
  }
  public Map<String,ChangeDetail> getDifference(Collection<User> users)
  {

    return emailToNewOrg;
  }
  public class ChangeDetail
  {
    String org;
    String ext;
    String firstName;
    String lastName;
    String title;
    public ChangeDetail(String org, String ext, String firstName, String lastName, String title)
    {
      this.org = org;
      this.ext = ext;
      this.firstName = firstName;
      this.lastName = lastName;
      this.title = title;
      if(this.title == null){
        this.title = "";
      }
    }
    public String getOrg()
    {
      return org;
    }
    public String getFirstName()
    {
      return firstName;
    }
    public String getLastName()
    {
      return lastName;
    }
    public String getTitle()
    {

      return title;
    }
    public String getExt()
    {
      return ext;
    }
  }
  private void parseEmployees(String json)
  {
    try{
      JSONObject obj = (JSONObject)new JSONParser().parse(json);
      JSONArray array = (JSONArray) obj.get("response");
      for(Object o : array){
        parseEmployee((JSONObject) o);
      }
    }catch(ParseException e){
      e.printStackTrace();
    }
  }
  private void parseFloors(String json)
  {
    try{
      JSONObject obj = (JSONObject)new JSONParser().parse(json);
      JSONArray array = (JSONArray) obj.get("response");
      for(Object o : array){
        JSONObject floor = (JSONObject) o;
        for(Object dirURLs : (JSONArray)floor.get("directories")){
          if(urlToId((String)dirURLs) == dir.getId()){
            dir.addFloor(new OfficeSpaceFloor((Long)floor.get("id"),(String)floor.get("label")));
          }
        }
      }
    }catch(ParseException e){
      e.printStackTrace();
    }
  }
  private OfficeSpaceDirectory findDirectory(String json)
  {
    try{
      JSONObject obj = (JSONObject)new JSONParser().parse(json);
      JSONArray array = (JSONArray) obj.get("response");
      for(Object o : array){
        JSONObject directory = (JSONObject) o;
        if(directory.get("name").equals(MAIN_DIRECTORY)){
          return new OfficeSpaceDirectory((Long)directory.get("id"),(String)directory.get("name"));
        }
      }
    }catch(ParseException e){
      e.printStackTrace();
    }
    throw new RuntimeException("could not find the main directory");
  }
  private void parseEmployee(JSONObject employee)
  {
    JSONObject seated = (JSONObject)employee.get("seating");
    List<Long> ids = new LinkedList<>();
    //todo for now ignore multi seating
    String lowerCaseEmail = ((String)employee.get("email")).toLowerCase();
    boolean isContained = emailsToIDandStatus.containsKey(lowerCaseEmail);
    if(!isContained){
      emailsToIDandStatus.put(((String)employee.get("email")).toLowerCase(),new LinkedList<IdAndStatus>());
    }

    if(((String)seated.get("seated")).equals("seated")){
      emailsToIDandStatus.get(((String)employee.get("email")).toLowerCase()).add(new IdAndStatus((Long)employee.get("id"),true));
      for(Object o : (JSONArray)seated.get("seat_urls")){
        if(seatMap.containsKey(urlToId((String)o))){
          //we know your seat is good, you should not be deleted
          ids.add(urlToId((String)o));
          employeeList.add(new OfficeSpaceEmployee((Long)employee.get("id"), (String)employee.get("email"), (String)seated.get("seated"), seatMap.get(urlToId((String)o)), (String)employee.get("first_name"), (String)employee.get("last_name"), (String) employee.get("title")));
        }
      }
    }
    else{
      otherUsers.add(((String)employee.get("email")).toLowerCase());
      emailsToIDandStatus.get(((String)employee.get("email")).toLowerCase()).add(new IdAndStatus((Long)employee.get("id"),false));
    }
  }
  private void parseSeats(String json)
  {
    try{
      JSONObject obj = (JSONObject)new JSONParser().parse(json);
      JSONArray array = (JSONArray) obj.get("response");
      for(Object o : array){
        parseSeat((JSONObject) o);
      }
    }catch(ParseException e){
      e.printStackTrace();
    }
  }
  private void parseSeat(JSONObject seat)
  {
    String val;
    OfficeSpaceFloor floor = dir.get(urlToId((String)seat.get("floor_url")));
    if(floor!=null){
      JSONObject occupancy = (JSONObject)seat.get("occupancy");
      OfficeSpaceSeat newSeat = new OfficeSpaceSeat((Long)seat.get("id"),(String)seat.get("label"));
      floor.addSeat(newSeat);
      seatMap.put((Long)seat.get("id"),newSeat);
      if(((String)occupancy.get("occupied")).toLowerCase().equals("occupied")){
        availableExt.add(((String)seat.get("label")).toLowerCase());
      }
    }
  }
  public void download()
  {

    Client client = ClientBuilder.newClient();
    Response seatResponse = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/seats")
    .request(MediaType.TEXT_PLAIN_TYPE)
    .header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"")
    .get();

    Response responseDirectory = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/directories")
    .request(MediaType.TEXT_PLAIN_TYPE)
    .header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"")
    .get();

    Response employeeResponse = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/employees")
    .request(MediaType.TEXT_PLAIN_TYPE)
    .header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"")
    .get();

    Response responseFloors = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/floors")
    .request(MediaType.TEXT_PLAIN_TYPE)
    .header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"")
    .get();

    dir = findDirectory(responseDirectory.readEntity(String.class));
    parseFloors(responseFloors.readEntity(String.class));
    parseSeats(seatResponse.readEntity(String.class));
    parseEmployees(employeeResponse.readEntity(String.class));
    for(OfficeSpaceEmployee e : employeeList){
      employeeMap.put(e.getEmail().toLowerCase(),e);
      try{
        String title = e.getTitle();
        emailToDetail.put(e.getEmail().toLowerCase(),new ChangeDetail(extMapper.getOrg(e.getSeat().getLabel()), e.getSeat().getLabel(), e.getFirstName(), e.getLastName(), title));
      }catch(Exception excep){
        log.append(Helper.exceptionToString(excep));
      }

    }
  }
  private long urlToId(String url)
  {
    String id = url.substring(1+url.lastIndexOf("/"));
    return Long.parseLong(id);
  }
}
