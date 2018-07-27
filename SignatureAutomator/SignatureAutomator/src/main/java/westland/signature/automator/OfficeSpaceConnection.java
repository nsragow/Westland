package westland.signature.automator;

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
  private Map<Long, String> seatLabels;
  private List<OfficeSpaceEmployee> employeeList;
  public OfficeSpaceConnection()
  {
    System.setProperty(ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY, "org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder");
    seatLabels = new HashMap<>();
    employeeList = new LinkedList<>();
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
  private void parseEmployee(JSONObject employee)
  {
    JSONObject seated = (JSONObject)employee.get("seating");
    List<Long> ids = new LinkedList<>();
    for(Object o : (JSONArray)seated.get("seat_urls")){
      ids.add(urlToId((String)o));
    }
    employeeList.add(new OfficeSpaceEmployee((Long)employee.get("id"), (String)employee.get("email"), (String)seated.get("seated"), ids));
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
    val = seatLabels.put((Long)seat.get("id"),(String)seat.get("label"));
    System.out.println("added "+ (String)seat.get("label") + " to " + seat.get("id"));
    System.out.println("removed "+val);
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

    parseEmployees(employeeResponse.readEntity(String.class));
    parseSeats(seatResponse.readEntity(String.class));
    for(OfficeSpaceEmployee e : employeeList){
      System.out.println(e);
      for(long i : e.getSeats()){
        System.out.println("has seat: " + seatLabels.containsKey(i));
        System.out.println(seatLabels.get(i));
      }
    }
  }
  private long urlToId(String url)
  {
    String id = url.substring(1+url.lastIndexOf("/"));
    return Long.parseLong(id);
  }
}
