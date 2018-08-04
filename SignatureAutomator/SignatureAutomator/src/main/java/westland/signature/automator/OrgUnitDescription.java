package westland.signature.automator;

import java.util.*;

public class OrgUnitDescription
{
  HashMap<String,String> pairs;
  HashSet<String> tags;
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
      allowedKeys.add("area");
      allowedKeys.add("region");
      allowedKeys.add("pretty_string");

    }
  }
  public void addTag(String tag)
  {
    tags.add(tag);
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
    tags = new HashSet<>();
    List<String> list = new LinkedList<>();
    char[] chars = description.toCharArray();
    StringBuilder builder = new StringBuilder();

    for(int i = 0; i < chars.length; i++){
      switch(chars[i]){
        case '<':
          //builder = new StringBuilder(); there is no need to do this because it would already be reset
          break;
        case '>':
          list.add(builder.toString());
          builder = new StringBuilder();
          break;
        default:
          builder.append(chars[i]);
      }
    }//todo make enums or something
    for(String s : list){
      if(s.contains("=")){
        String[] split = s.split("=");
        put(split[0],split[1]);

      }else{
        addTag(s);
      }
    }
  }
  public OrgUnitDescription put(String key,String value)
  {

    pairs.put(key,value);
    
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
      for(String s : tags){
        sb.append("<"+s+">");
      }
      return sb.toString();
    }
  }
}
