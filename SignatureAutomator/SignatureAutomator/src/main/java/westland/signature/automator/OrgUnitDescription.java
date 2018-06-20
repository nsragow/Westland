package westland.signature.automator;

import java.util.*;

public class OrgUnitDescription
{
  HashMap<String,String> pairs;
  static HashSet<String> allowedKeys = null;
  private void setUp()
  {
    if(allowedKeys == null){
      allowedKeys = new HashSet<>();
      allowedKeys.add("address");
      allowedKeys.add("zip");
      allowedKeys.add("city");
      allowedKeys.add("state");
      allowedKeys.add("website");
      allowedKeys.add("details");
      allowedKeys.add("fax");
      allowedKeys.add("phone");

    }
  }
  public boolean contains(String key)
  {
    if(pairs.get(key) != null) return true;
    return false;
  }
  public String getPartOne()
  {
    return pairs.get("address")+",";
  }
  public String getPartTwo()
  {
    return pairs.get("city")+", "+pairs.get("state")+" "+pairs.get("zip");
  }
  protected OrgUnitDescription()
  {
    pairs = new HashMap<>();
    setUp();
  }
  protected OrgUnitDescription(String description)
  {
    setUp();
    pairs = new HashMap<>();
    HashMap<String,String> map = new HashMap<>();
    char[] chars = description.toCharArray();
    StringBuilder builder = new StringBuilder();

    String key = null;
    for(int i = 0; i < chars.length; i++){
      switch(chars[i]){
        case '<':
          //builder = new StringBuilder(); there is no need to do this because it would already be reset
          break;
        case '>':
          map.put(key,builder.toString());
          builder = new StringBuilder();
          break;
        case '=':
          key = builder.toString();
          builder = new StringBuilder();
          break;
        default:
          builder.append(chars[i]);
      }
    }//todo make enums or something
    for(String s : map.keySet()){
      put(s,map.get(s));
    }
  }
  public OrgUnitDescription put(String key,String value)
  {
    if(allowedKeys.contains(key)){
      pairs.put(key,value);
    }else{
      throw new IllegalStateException("has unrecognized key "+ key);
    }
    return this;
  }
  public String get(String key)
  {
    if(allowedKeys.contains(key)){
      return pairs.get(key);
    }else{
      throw new IllegalStateException("has unrecognized key "+ key);
    }
  }

  public String toString()
  {
    if(pairs.get("address") == null || pairs.get("zip") == null || pairs.get("city") == null || pairs.get("state") == null || pairs.get("website") == null){
      throw new IllegalStateException("you must have address,zip,website,city and state filled before requesting the string");
    }else{
      StringBuilder sb = new StringBuilder();
      for(String s : pairs.keySet()){
        sb.append("<"+s+"="+pairs.get(s)+">");
      }
      return sb.toString();
    }
  }
}
