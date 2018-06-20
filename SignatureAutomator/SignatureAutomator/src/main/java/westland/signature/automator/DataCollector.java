public class DataCollector
{
  private Directory dir;
  private Map<String,SignatureBuilder> dataMap;
  private Set<String> blackList;
  public DataCollector(Directory dir, Set<String> blackList)
  {
    this.dir = dir;
  }
  private Map<String,SignatureBuilder> getDataMap()
  {
    if(dataMap == null){
      dataMap = new HashMap<>();
      dataMap = getDataMap(null);
    }
    return dataMap;
  }
  private Map<String,SignatureBuilder> getDataMap(String token)
  {

    try{
      do{
        Directory.Users.List list = dir.users().list().setCustomer("my_customer").setMaxResults(5).setOrderBy("email").setProjection("full");
        if(token!=null)list.setPageToken(token);
        Users users = list.execute();
        token = users.getNextPageToken();
        List<User> usersList = users.getUsers();
        for(User u : usersList){
          if(!blackList.contains(u.getPrimaryEmail()) && !blackList.contains(Helper.orgPathToName(u.getOrgUnitPath()))){

            dataMap.put(u.getPrimaryEmail(),new SignatureBuilder(u));
          }else{
            //System.out.println("skipped "+u.getPrimaryEmail().toString());
          }
        }
      }while(token!=null);
    }catch(SocketTimeoutException e){
      getDataMap(token);
    }

  }
}

if(step < 1){
  step = 1;
}
//now apply orginization info to orgMap
if(step < 2){
  OrgUnits orgunits = dir.orgunits().list("my_customer").execute();
  List<OrgUnit> list = orgunits.getOrganizationUnits();

  for(OrgUnit o : list){
    if(!o.getDescription().equals("#ignore")){
      OrgUnitDescription toAdd = new OrgUnitDescription(o.getDescription());
      orgMap.put(o.getName(), toAdd);
    }
  }
  //add the standard westland org unit

  OrgUnitDescription westland = new OrgUnitDescription();
  westland.put("zip",root_org_zip).put("city",root_org_city).put("address",root_org_address).put("state",root_org_state).put("phone",root_org_phone).put("fax","");
  orgMap.put("Westland",westland);


  step = 2;
}
