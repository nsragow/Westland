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
  private Map<String,String> emailToOrgMap;
  private StringBuilder log;

  //private static final String MAIN_DIRECTORY = "SYS: Make Public";
  private static final String MAIN_DIRECTORY = "SYS: Make Private";
  private OfficeSpaceDirectory dir;
  public OfficeSpaceConnection()
  {
    System.setProperty(ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY, "org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder");
    emailToOrgMap = new HashMap<>();
    seatMap = new HashMap<>();
    employeeList = new LinkedList<>();
    log = new StringBuilder();
    download();
  }
  public Map<String,String> getDifference(Collection<User> users)
  {
    HashMap<String,String> emailToNewOrg = new HashMap<String,String>();
    for(User u : users){
      if(emailToOrgMap.containsKey(u.getPrimaryEmail().toLowerCase())){
        if(Helper.orgPathToName(u.getOrgUnitPath()).toLowerCase().equals(emailToOrgMap.get(u.getPrimaryEmail().toLowerCase()).toLowerCase())){
          System.out.println("they are the same");
        }else{
          System.out.println("different for "+ u.getPrimaryEmail());
          emailToNewOrg.put(u.getPrimaryEmail(),emailToOrgMap.get(u.getPrimaryEmail().toLowerCase()));
        }
      }
    }
    return emailToNewOrg;
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
        emailToOrgMap.put(e.getEmail(),extMapper.getOrg(Integer.parseInt(e.getSeat().getLabel())));
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
