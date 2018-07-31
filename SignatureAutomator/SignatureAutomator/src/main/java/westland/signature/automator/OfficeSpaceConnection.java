package westland.signature.automator;

import com.google.api.services.admin.directory.model.User;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.client.jaxrs.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.util.*;

public class OfficeSpaceConnection
{
  private Map<Long, OfficeSpaceSeat> seatMap;
  private List<OfficeSpaceEmployee> employeeList;
  private Map<String,ChangeDetail> emailToDetail;
  private StringBuilder log;

  //private static final String MAIN_DIRECTORY = "SYS: Make Public";
  private static final String MAIN_DIRECTORY = "SYS: Make Private"; //todo
  private OfficeSpaceDirectory dir;
  public OfficeSpaceConnection()
  {
    System.setProperty(ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY, "org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder");
    emailToDetail = new HashMap<>();
    seatMap = new HashMap<>();
    employeeList = new LinkedList<>();
    log = new StringBuilder();
    download();
  }
  public void changeUserTitle(String id, String title)
  {
    Client client = ClientBuilder.newClient();
    Entity<String> payload = Entity.text("{\"response\": {\"title\": "+title+"}}");


    Response response = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/employees/"+id)
    .request(MediaType.TEXT_PLAIN_TYPE)
    .header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"")
    .put(payload);

    System.out.println("status: " + response.getStatus());
    System.out.println("headers: " + response.getHeaders());
    System.out.println("body:" + response.readEntity(String.class));
  }
  public Map<String,ChangeDetail> getDifference(Collection<User> users)
  {
    HashMap<String,ChangeDetail> emailToNewOrg = new HashMap<>();
    for(User u : users){
      if(emailToDetail.containsKey(u.getPrimaryEmail().toLowerCase())){
        //see if both org and ext are the same
        if(Helper.orgPathToName(u.getOrgUnitPath()).toLowerCase().equals(emailToDetail.get(u.getPrimaryEmail().toLowerCase()).org.toLowerCase()) && Long.parseLong(UserFunctions.getExt(u)) == emailToDetail.get(u.getPrimaryEmail().toLowerCase()).ext){
          System.out.println("they are the same");
        }else{
          System.out.println("different for "+ u.getPrimaryEmail());
          emailToNewOrg.put(u.getPrimaryEmail(),emailToDetail.get(u.getPrimaryEmail().toLowerCase()));
        }
      }
    }
    return emailToNewOrg;
  }
  public class ChangeDetail
  {
    String org;
    long ext;
    public ChangeDetail(String org, long ext)
    {
      this.org = org;
      this.ext = ext;
    }
    public String getOrg()
    {
      return org;
    }
    public long getExt()
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
    if(((String)seated.get("seated")).equals("seated")){
      for(Object o : (JSONArray)seated.get("seat_urls")){
        if(seatMap.containsKey(urlToId((String)o))){
          ids.add(urlToId((String)o));
          employeeList.add(new OfficeSpaceEmployee((Long)employee.get("id"), (String)employee.get("email"), (String)seated.get("seated"), seatMap.get(urlToId((String)o))));
        }
      }
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
      OfficeSpaceSeat newSeat = new OfficeSpaceSeat((Long)seat.get("id"),(String)seat.get("label"));
      floor.addSeat(newSeat);
      seatMap.put((Long)seat.get("id"),newSeat);
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
    ExtensionMapper extMapper = new ExtensionMapper();
    for(OfficeSpaceEmployee e : employeeList){
      try{
        emailToDetail.put(e.getEmail(),new ChangeDetail(extMapper.getOrg(Integer.parseInt(e.getSeat().getLabel())), Long.parseLong(e.getSeat().getLabel())));
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
