package westland.signature.automator;
public class Helper
{
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
}
