package westland.signature.automator;

import java.util.*;

public class ExtensionMapper
{
  private Table extTable;
  private HashMap<String,Collection<String>> labelsOfOrg;
  public ExtensionMapper()
  {
    extTable = Initializer.getTable(Strings.ext_to_orgunit);
    labelsOfOrg = new HashMap<>();
    for(String key : extTable.keySet()){
      if(!labelsOfOrg.containsKey(extTable.get(key,"org"))){
        labelsOfOrg.put(extTable.get(key,"org").toLowerCase(),new ArrayList<String>());
      }
      labelsOfOrg.get(extTable.get(key,"org").toLowerCase()).add(key);
    }
  }

  public String getOrg(String extension)
  {
    if(!extTable.containsKey(extension)){
      return "Lost in Space";
    }
    return extTable.get(extension,"org");
  }
  public Collection<String> getLabels(String org)
  {
    return labelsOfOrg.get(org.toLowerCase());
  }
}
