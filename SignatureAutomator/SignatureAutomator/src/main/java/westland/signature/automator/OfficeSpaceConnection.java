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

public class OfficeSpaceConnection
{
  public OfficeSpaceConnection()
  {
    System.setProperty(ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY, "org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder");
  }
  public void download()
  {
    System.out.println("almost");
    Client client = ClientBuilder.newClient();
    System.out.println("got here");
    Response response = client.target("https://westland.officespacesoftware.com")
    .path("/api/1/seats")
    .request(MediaType.TEXT_PLAIN_TYPE)
    .header("Content-Type", "application/json; charset=utf-8")
    .header("AUTHORIZATION", "Token token=\""+Strings.officeSpaceAPIkey+"\"")
    .get();


    String json = response.readEntity(String.class);
    try{
      JSONObject obj = (JSONObject)new JSONParser().parse(json);
      System.out.println(obj instanceof java.util.Map);
      for(Object key : obj.keySet()){

        System.out.println("key "+key.getClass());
        System.out.println(key);
        System.out.println("val "+obj.get(key).getClass());
        if(obj.get(key) instanceof JSONArray){
          JSONArray array = (JSONArray) obj.get(key);
          for(Object p : array){
            System.out.println(p.getClass());
            System.out.println(p);
          }
        }else{
          System.out.println(obj.get(key));
        }
      }
    }catch(ParseException e){
      e.printStackTrace();
    }

  }
}
