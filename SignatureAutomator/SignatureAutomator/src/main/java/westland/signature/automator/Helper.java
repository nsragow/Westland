package westland.signature.automator;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;

public class Helper
{
  //will not work for nested orgs
  protected static String orgPathToName(String path)
  {
    String orgUnitPath = path;
    int lastIndexOf = orgUnitPath.lastIndexOf("/");
    if(orgUnitPath.length()>lastIndexOf+1){
      String toSet = orgUnitPath.substring(lastIndexOf+1);
      if(toSet.isEmpty()){
        return "Westland";
      }
      return toSet;
    }else{
      //todo must have been '/'
      return "Westland";

    }
  }
  protected static String exceptionToString(Exception e)
  {
    StringWriter errors = new StringWriter();
    e.printStackTrace(new PrintWriter(errors));
    return errors.toString();
  }
  public static String orgUnitToStaffGroupEmail(OrgUnitDescription oud)
  {
    if(oud.containsKey("domain") && null != oud.get("domain") && !oud.get("domain").trim().isEmpty()){
      return "staff"+oud.get("domain");
    }
    return oud.getName().replace(" ","") + "staff@westlandreg.com";
  }
  public static String orgUnitToManagerGroupEmail(OrgUnitDescription oud)
  {
    if(oud.containsKey("domain") && null != oud.get("domain") && !oud.get("domain").trim().isEmpty()){
      return "staff"+oud.get("domain");
    }
    return oud.getName().replace(" ","") + "management@westlandreg.com";
  }
  public static String areaToGroupEmail(String areaName)
  {
    return areaName.replace(" ","") + "area@westlandreg.com";
  }
  public static String regionToGroupEmail(String regionName)
  {
    return regionName.replace(" ","") + "region@westlandreg.com";
  }
  public static int waitOnOption(String[] options, Scanner sc)
  {

    while(true){
      String line = sc.nextLine();
      line = line.toLowerCase().trim();
      for(int i = 0; i < options.length; i++){
        if(line.equals(options[i].toLowerCase())){
          return i;
        }
      }
      System.out.println("Option not recognized");
      System.out.print("Please choose (");
      for(int i = 0; i < options.length; i++){
        System.out.print(options[i]);
        if(i != options.length-1){
          System.out.print("/");
        }
      }
      System.out.println(")");
    }
  }
}
