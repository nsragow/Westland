package westland.signature.automator;

import java.util.*;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.admin.directory.model.User;

public class SignatureBuilder
{
  HashMap<String,String> pairs;
  static HashSet<String> allowedKeys = null;
  static HashSet<String> allowedPhoneKeys = null;
  static HashSet<String> neededForCompletion = null;
  protected HashMap<String,String> phones;
  private boolean displayMobile;



  public void setDisplayMobile(boolean val)
  {
    displayMobile = val;
  }

  public boolean isComplete()
  {
    for(String s : neededForCompletion){
      if(!pairs.containsKey(s)){
        return false;
      }
    }
    if(phones.size()<1){
      return false;
    }
    return true;
  }
  public String get(String key)
  {
    if(allowedKeys.contains(key)){
      return pairs.get(key);
    }else if(allowedPhoneKeys.contains(key)){
      return phones.get(key);
    }else{
      throw new IllegalArgumentException("key not recognized: "+ key);
    }
  }
  public void put(String key, String val)
  {
    if(allowedKeys.contains(key)){
      pairs.put(key,val);
    }else if(allowedPhoneKeys.contains(key)){
      //we do not want duplicate values in different fields
      if(!phones.values().contains(val)){
        phones.put(key,val);
      }
    }else{
      throw new IllegalArgumentException("key not recognized: "+ key);
    }
  }
  public SignatureBuilder(){
    this.phones = new HashMap<>();
    this.pairs = new HashMap<>();

    if(allowedKeys == null){
      allowedKeys = new HashSet<>();
      allowedKeys.add("name");
      allowedKeys.add("ext");
      allowedKeys.add("email");
      allowedKeys.add("org");
      allowedKeys.add("addressPartOne");
      allowedKeys.add("addressPartTwo");
      allowedKeys.add("website");
      allowedKeys.add("title");
      allowedKeys.add("company");

    }
    if(neededForCompletion == null){
      neededForCompletion = new HashSet<>();
      neededForCompletion.add("name");
      neededForCompletion.add("email");
      neededForCompletion.add("org");
      neededForCompletion.add("addressPartOne");
      neededForCompletion.add("addressPartTwo");
      neededForCompletion.add("website");
    }
    if(allowedPhoneKeys == null){
      allowedPhoneKeys = new HashSet<>();
      allowedPhoneKeys.add("mobile");
      allowedPhoneKeys.add("work");
      allowedPhoneKeys.add("work_fax");
      allowedPhoneKeys.add("home");
      allowedPhoneKeys.add("main");
    }
  }
  public String toString()
  {

    return SignatureGenerator.makeSignature(this);
  }
  public void remove(String key)
  {
    pairs.remove(key);phones.remove(key);
  }
  public void removeNulls()
  {
    for(String k : allowedKeys){
      if(pairs.get(k) == null){
        pairs.put(k,"");
      }
    }
  }
  public String getPhoneHTML()
  {
    boolean needBRbeforeAddingAnotherLine = false;
    StringBuilder toReturn = new StringBuilder();
    if(phones.isEmpty()){
      throw new IllegalStateException("there are no phones on "+ pairs.get("name"));
    }
    if(phones.containsKey("work")){
      toReturn.append("O: "+phones.get("work")+" ");
      if(pairs.containsKey("ext")&&!pairs.get("ext").equals("")){

        //System.out.println(this.get("email")+" : " + pairs.get("ext"));

        toReturn.append("X: "+pairs.get("ext"));
      }
      needBRbeforeAddingAnotherLine = true;
    }else if(phones.containsKey("main")){
      toReturn.append("P: "+phones.get("main"));
      needBRbeforeAddingAnotherLine = true;
    }

    if(displayMobile){
      if(phones.containsKey("mobile")){
        if(needBRbeforeAddingAnotherLine)toReturn.append("<br>");
        toReturn.append("C: "+phones.get("mobile"));
        needBRbeforeAddingAnotherLine = true;
      }
    }
    if(phones.containsKey("work_fax")){
      if(needBRbeforeAddingAnotherLine)toReturn.append("<br>");
      toReturn.append("F: "+phones.get("work_fax"));
      needBRbeforeAddingAnotherLine = true;
    }
    return toReturn.toString();
  }




  //adding a lot not yet put together
  //customAttributesToSB
  public void applyCustomAttributes(Map<String,Map<String,Object>> schemaMap)
  {
    Map<String,Object> addInfo = schemaMap.get("Additional_Info");
    if(addInfo!=null){
      Object ext = addInfo.get("Extension");
      if(ext != null){
        this.put("ext",ext.toString());
      }else{
        //todo
      }
    }else{
      //todo
    }
    Map<String,Object> web = schemaMap.get("Website");
    if(web!=null){
      Object webString = web.get("Website");
      if(webString != null){
        this.put("website",webString.toString());
      }else{
        this.put("website","westlandreg.com");
      }
    }else{
      this.put("website","westlandreg.com");
    }
  }
  protected static List<ArrayMap<String,Object>> objectToArrayMapList(Object toCast) throws IllegalArgumentException
  {
    if(toCast == null){
      throw new NullPointerException("trying to cast null object");
    }
    if(toCast instanceof List){
      List<Object> stepOne = (List<Object>)toCast;
      if(!stepOne.isEmpty()){
        Object stepOneSub = stepOne.get(0);
        if(stepOneSub instanceof ArrayMap){
          ArrayMap<Object,Object> map = (ArrayMap<Object,Object>)stepOneSub;
          Set<Object> keys = map.keySet();
          if(keys.isEmpty()) throw new IllegalArgumentException("could not determine if object qualified for cast due to empty keyset");

          for(Object o : keys){
            if(!(o instanceof String)){

              throw new IllegalArgumentException("not of type String String but rather "+ o.getClass().toString()+", {Object}");
            }
            break;
          }

          return (List<ArrayMap<String,Object>>)toCast;
        }
        throw new IllegalArgumentException("not of type List<ArrayMap> but rather List<"+ stepOneSub.getClass().toString()+">");
      }
    }
    throw new IllegalArgumentException("not of type List but rather "+ toCast.getClass().toString());

  }
  public SignatureBuilder(User u)
  {
    this();
    this.put("name",u.getName().getFullName());


    Map<String,Map<String,Object>> schemaMap = u.getCustomSchemas();
    if(schemaMap != null){
      this.applyCustomAttributes(schemaMap);
    }else{
      //todo what is the policy
    this.put("website","westlandreg.com");
    }



    //check to make sure that orginizations is a list of strings
    Object orginizations = u.getOrganizations();
    if(orginizations == null){
      //todo
    }else{

      List<ArrayMap<String,Object>> orgList = null;
      try{
        orgList = objectToArrayMapList(orginizations);
      }catch(IllegalArgumentException i){
        i.printStackTrace();
        throw i;
      }
      Object title = orgList.get(0).get("title");
      Object company = orgList.get(0).get("name");


      if(title == null){
        //todo problem

      }else{
        this.put("title",title.toString());
      }if(company!=null){
        this.put("company",company.toString());
      }
    }




    this.put("email",u.getPrimaryEmail());
    //now set the org unit from the path
    String orgUnitPath = u.getOrgUnitPath();
    this.put("org", Helper.orgPathToName(orgUnitPath));




    //now to get phone numbers
    Object phoneObj = u.getPhones();
    if(phoneObj != null){
      this.setPhone(phoneObj);
      //todo perhaps some checking is in order
    }else{
      //todo
    }


  }


  public void applyOUD(OrgUnitDescription oud)
  {
    String p1 = oud.getPartOne();
    String p2 = oud.getPartTwo();

    this.put("addressPartOne",p1);
    this.put("addressPartTwo",p2);

    if(oud.contains("phone")){
      this.put("work",oud.get("phone"));
    }
    if(oud.contains("website")){
      this.put("website",oud.get("website"));
    }
  }
  public void setPhone(Object phoneObj) throws IllegalArgumentException
  {
    List<ArrayMap<String,Object>> phoneList = null;
    try{
      phoneList = objectToArrayMapList(phoneObj);
    }catch(IllegalArgumentException i){
      throw i;
    }

    for(ArrayMap<String,Object> aM : phoneList){
      String result = aM.get("type").toString();
      String value = aM.get("value").toString();

      this.put(result,value);



    }
  }

}
