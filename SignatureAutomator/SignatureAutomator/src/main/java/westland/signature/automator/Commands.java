package westland.signature.automator;

import com.google.api.services.admin.directory.model.OrgUnit;
import java.util.*;
import java.io.IOException;
import com.google.api.services.admin.directory.model.User;


//this class is meant to be used from the command line
public class Commands
{
  public static final boolean APPLY_BLACKLIST = false;
  public static final boolean IGNORE_BLACKLIST = true;
  public static final String FIELD_NOT_FOUND = "field not found";
  public static final String SEPERATOR = "____________________________________________________";

  private ServiceManager serviceManager;
  private GroupWrapper gW;
  private OfficeSpaceConnection officeSpace;
  private Scanner sc;

  public Commands(ServiceManager serviceManager, Scanner sc)
  {
    this.officeSpace = officeSpace;
    this.serviceManager = serviceManager;
    gW = new GroupWrapper(serviceManager);
    this.sc = sc;
  }

  public void addToOfficeSpace(String userString)
  {
    if(officeSpace == null){
      try{
        System.out.println("initializing OfficeSpace...");
        officeSpace = new OfficeSpaceConnection(serviceManager.getUserSetBlackRemoved());

      }catch(Exception e){
        e.printStackTrace();
        System.out.println("Could not establish connection to OfficeSpace. Command aborted");
        return;
      }
    }
    try{
      Collection<User> users = formattedUserStringToSet(userString);


      for(User u : users){
        System.out.println(u.getPrimaryEmail());
        try{
          officeSpace.createUser(UserFunctions.getFirstName(u), UserFunctions.getLastName(u), UserFunctions.getTitle(u), u.getPrimaryEmail());
        }catch(Exception e){
          e.printStackTrace();
          System.out.println("Hit above exception when trying to add " + u.getPrimaryEmail() + " to OfficeSpace");
        }
      }
    }catch(IOException e){
      System.out.println("error while collecting users");
      e.printStackTrace();
    }
  }
  public void printUserData(String pathToPrint)
  {
    Table table = new Table(new String[]{"First","Last","Email","Org","Title","Ext"});
    for(User u : serviceManager.getUserSetBlackRemoved()){
      if(!u.getSuspended()){
        String first = u.getName().getGivenName();
        String last = u.getName().getFamilyName();
        String email = u.getPrimaryEmail();
        String org = Helper.orgPathToName(u.getOrgUnitPath());
        String title;
        try{
          title = UserFunctions.getTitle(u);
        }catch(Exception e){
          System.out.println(email);
          title = "";
        }
        String ext = UserFunctions.getExt(u);



        table.addRow(new String[]{email,first,last,org,title,ext});
      }

    }
    boolean again;
    do{
      again = false;
      try{
        Table.writeTableToCSV(table,pathToPrint);
      }catch(Exception e){
        System.out.println(e);
        System.out.println("Hit the above exception when trying to write to CSV. Change path? Otherwise the operation will be aborted. (Y/N)");
        if(0==Helper.waitOnOption(new String[]{"Y","N"},sc)){
          again = true;
          System.out.println("Enter the path to csv. ex/ /home/joe/documents/userdata.csv");
          pathToPrint = sc.nextLine();
        }
      }
    }while(again);
  }
  public void uns()
  {
    System.out.println("hello");
    for(User u : serviceManager.getUserList()){
      try{
        System.out.println(u.getPrimaryEmail());
        u.setSuspended(false);
        serviceManager.getDirectory().users().update(u.getPrimaryEmail(),u).execute();


      }catch(Exception e){
      e.printStackTrace();
      }

    }

  }
  public Collection<User> formattedUserStringToSet(String userString) throws IOException
  {
    Collection<User> users;
    userString = userString.toLowerCase();
    users = serviceManager.getUserSetBlackRemoved();
    if(userString.contains("*")){
      if(userString.contains(" where ")){
        String[] afterWhere = userString.split(" where ");
        if(afterWhere.length != 2){
          throw new RuntimeException("formatted user string has unfinished or multiple where statements");
        }
        String[] qualifiers = afterWhere[1].split(",");

        for(String q : qualifiers){//only does contains and title now todo
          String[] qualifier = q.split(" ");
          if(qualifier.length != 3){
            throw new RuntimeException("formatted user string has improper qualifier: "+q);
          }
          Iterator<User> i = users.iterator();
          while(i.hasNext()){
            User next = i.next();
            try{
              if(!UserFunctions.getTitle(next).toLowerCase().contains(qualifier[2])){
                i.remove();
              }
            }catch(Exception e){
              i.remove();
            }
          }

        }
        return users;
      }else{
        return users;
      }
    }else{//todo assumes only one name
      String[] usersArray = userString.split(",");
      HashSet<String> userSet = new HashSet<String>();
      for(String s : usersArray){
        userSet.add(s.toLowerCase().trim());
        System.out.println(" kee p "+ s);
      }

      Iterator<User> i = users.iterator();
      while(i.hasNext()){
        User next = i.next();
        if(!userSet.contains(next.getPrimaryEmail().toLowerCase().trim())){
          System.out.println(next.getPrimaryEmail() + " removed");
          i.remove();
        }
      }
      return users;

    }
  }
  public void printUserInfo(String formattedString, String[] fields)
  {

    Collection<User> users;
    try{
      users = formattedUserStringToSet(formattedString);
    }catch(IOException e){
      e.printStackTrace();
      System.out.println("The above exception was caught when trying to collect users from Google. Try again? (Y/N)");
      if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
        printUserInfo(formattedString,fields);
      }
      return;
    }
    for(String field : fields){
      field = field.toLowerCase().trim();
    }
    for(User u : users){
      System.out.println(SEPERATOR);
      for(String field : fields){
        switch(field){
          case "title": try{System.out.println("title: "+UserFunctions.getTitle(u));}catch(Exception e){System.out.println("title: not found");}
          break;
          default:System.out.println(field+": field not supported");
        }
      }
    }
  }
  public boolean editOrgTags(String orgUnit, String[] tags)
  {
    if(orgUnit.contains("*")){
      List<OrgUnit> orgs;
      try{
        orgs = serviceManager.getOrgList();
      }catch(IOException e){
        System.out.println("Hit the following exception when collecting orgunits. Do you want to try again? (Y/N)");
        e.printStackTrace();
        if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
          return editOrgTags(orgUnit,tags);
        }else{
          return false;
        }
      }
      for(OrgUnit o : orgs){
        printOrgInfo(o.getName(),tags);//inefficient as of now
      }
      return true;
    }else{
      OrgUnit o;
      try{
        o = serviceManager.getOrgUnit(orgUnit);
      }catch(Exception e){
        System.out.println("Hit the following exception when finding orgunit "+ orgUnit+". Do you want to try again? (Y/N)");
        e.printStackTrace();
        if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
          return editOrgTags(orgUnit,tags);
        }else{
          return false;
        }
      }

      OrgUnitDescription oD = new OrgUnitDescription(o.getName(),o.getDescription());
      for(String tag : tags){
        if(tag.contains("=")){
          String[] split = tag.split("=");
          if(split.length<2){
            System.out.println("Tag format " + tag + " is not accepted, skipping...");
          }else{
            oD.put(split[0],split[1]);
          }
        }else{
          oD.addTag(tag);
        }
      }
      o.setDescription(oD.toString());
      try{
        serviceManager.updateOrg(o);
        return true;
      }catch(Exception e){
        System.out.println("Hit the following exception when editing orgunit "+ o.getName()+". Do you want to try again? (Y/N)");
        e.printStackTrace();
        if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
          return editOrgTags(orgUnit,tags);
        }else{
          return false;
        }
      }
    }
  }
  public boolean printOrgInfo(String orgUnit, String[] tags)
  {
    if(orgUnit.contains("*")){
      List<OrgUnit> orgs;
      try{
        orgs = serviceManager.getOrgList();
      }catch(IOException e){
        System.out.println("Hit the following exception when collecting orgunits. Do you want to try again? (Y/N)");
        e.printStackTrace();
        if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
          return printOrgInfo(orgUnit,tags);
        }else{
          return false;
        }
      }
      for(OrgUnit o : orgs){
        printOrgInfo(o.getName(),tags);//inefficient as of now
      }
      return true;
    }else{
      OrgUnit o;
      try{
        o = serviceManager.getOrgUnit(orgUnit);
      }catch(Exception e){
        System.out.println("Hit the following exception when finding orgunit "+ orgUnit+". Do you want to try again? (Y/N)");
        e.printStackTrace();

        if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
          return printOrgInfo(orgUnit,tags);
        }else{
          return false;
        }
      }

      OrgUnitDescription oD = new OrgUnitDescription(o.getName(),o.getDescription());
      System.out.println("--------------------------------------");
      for(String tag : tags){
        String field = tag.toLowerCase().trim();
        System.out.print(field+": ");
        switch(field){
          case "name": System.out.println(o.getName()); break;
          default:
          if(oD.contains(field)){
            System.out.println(oD.get(field));
          }else{
            System.out.println(FIELD_NOT_FOUND);
          }
        }

      }
      return true;
    }
  }
  public boolean makeGroupMembers(String userDef, String orgDef, String groupDef)
  {

    if(userDef.trim().contains("*")){
      Collection<User> users;
      try{
        users = formattedUserStringToSet(userDef);
      }catch(IOException e){
        System.out.println("Hit the following exception when collecting users. Do you want to try again? (Y/N)");
        e.printStackTrace();
        if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
          return makeGroupMembers(userDef,orgDef,groupDef);
        }else{
          return false;
        }
      }
      if(orgDef.trim().equals("*")){
        if(groupDef.contains("#")){//this is not necessarily making the right assumptions
          String editedGroupKey;
          for(User u : users){
            if(!u.getOrgUnitPath().equals("/")){
              editedGroupKey = groupDef.replace("#",(Helper.orgPathToName(u.getOrgUnitPath()).replace(" ","")));
              makeUserMember(u,editedGroupKey);
            }

          }
        }else{
          makeUsersMembers(users, groupDef);

        }
      }
    }else{
      //work on this later, also maybe more stuff above todo
      throw new RuntimeException("not currently supported");
    }
    return true; //todo
  }
  private boolean makeUserMember(User u, String groupKey)
  {
    try{
      return gW.addEmailToGroup(u.getPrimaryEmail(),groupKey);
    }catch(IOException e){
      System.out.println("Hit the following exception when adding "+u.getPrimaryEmail()+ " to " + groupKey + ". Do you want to try again? (Y/N)");
      if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
        return makeUserMember(u,groupKey);
      }else{
        return false;
      }
    }
  }
  private void makeUsersMembers(Collection<User> users, String groupKey)
  {
    for(User u : users){
      try{
        gW.addEmailToGroup(u.getPrimaryEmail(),groupKey);
      }catch(IOException e){
        System.out.println("Hit the following exception when adding "+u.getPrimaryEmail()+ " to " + groupKey + ". Do you want to try again? (Y/N)");
        if(Helper.waitOnOption(new String[]{"Y","N"},sc)==0){
          makeUserMember(u,groupKey);
        }
      }
    }
  }
  public void massGroupCreator(String formattedEmail, String formattedName, String formattedDesc, int groupType ,boolean allowBlackListed)
  {
    // the # charater is used to indicate where the org name should be placed in
    GroupWrapper gW = new GroupWrapper(serviceManager);
    List<OrgUnit> orgs;
    try{
      orgs = serviceManager.getOrgList();
    }catch(IOException e){
      System.out.println("failed to retrieve org units: ");
      e.printStackTrace();
      return;
    }
    String groupEmail;
    String groupName;
    String groupDesc;
    String orgUnitName;
    int createCount = 0;
    boolean attemptAgain = false;
    for(OrgUnit o : orgs){
      attemptAgain = true;
      orgUnitName = Helper.orgPathToName(o.getOrgUnitPath());
      if(!Strings.BlackList.contains(orgUnitName) || allowBlackListed == IGNORE_BLACKLIST){
        groupEmail = formattedEmail.replace("#",(orgUnitName.replace(" ","")));
        groupName = formattedName.replace("#",(orgUnitName));
        groupDesc = formattedDesc.replace("#",(orgUnitName));
        while(attemptAgain){
          attemptAgain = false;
          try{
            if(gW.createGroup(groupEmail,groupName,groupDesc,groupType)){
              createCount++;
            }
          }catch(IOException e){
            System.out.println("Hit the following error when trying to create group "+groupEmail);
            e.printStackTrace();
            System.out.println("\nAttempt again or skip? (Y/N)");
            attemptAgain = waitOnYesOrNo();
          }
        }
      }
    }
    System.out.println("created " +createCount+ " groups");
  }
  public void createGroup(String email, String name, String desc, int type ,boolean allowBlackListed)
  {
    // the # charater is used to indicate where the org name should be placed in
    GroupWrapper gW = new GroupWrapper(serviceManager);


    boolean attemptAgain = false;

    attemptAgain = true;
    while(attemptAgain){
      attemptAgain = false;
      try{
        gW.createGroup(email,name,desc,type);
      }catch(IOException e){
        System.out.println("Hit the following error when trying to create group "+email);
        e.printStackTrace();
        System.out.println("\nAttempt again or skip? (Y/N)");
        attemptAgain = waitOnYesOrNo();
      }
    }
  }
  public void massGroupDeleter(String formattedString, boolean allowBlackListed)
  {
    // the # charater is used to indicate where the org name should be placed in
    GroupWrapper gW = new GroupWrapper(serviceManager);
    List<OrgUnit> orgs;
    try{
      orgs = serviceManager.getOrgList();
    }catch(IOException e){
      System.out.println("failed to retrieve org units: ");
      e.printStackTrace();
      return;
    }
    String groupEmail;
    String orgUnitName;
    int createCount = 0;
    boolean attemptAgain = false;
    for(OrgUnit o : orgs){
      attemptAgain = true;
      orgUnitName = Helper.orgPathToName(o.getOrgUnitPath());
      if(!Strings.BlackList.contains(orgUnitName) || allowBlackListed == IGNORE_BLACKLIST){
        groupEmail = formattedString.replace("#",(orgUnitName.replace(" ","")));

        while(attemptAgain){
          attemptAgain = false;
          try{
            if(gW.deleteGroup(groupEmail)){
              createCount++;
            }
          }catch(IOException e){
            System.out.println("Hit the following error when trying to delete group "+groupEmail);
            e.printStackTrace();
            System.out.println("\nAttempt again or skip? (Y/N)");
            attemptAgain = waitOnYesOrNo();
          }
        }
      }
    }
    System.out.println("deleted " +createCount+ " groups");
  }

  private boolean waitOnYesOrNo()
  {

    while(true){
      String line = sc.nextLine();
      line = line.toLowerCase().trim();
      if(line.equals("y")){
        return true;
      }else if(line.equals("n")){
        return false;
      }else{
        System.out.println("Please answer 'Y' or 'N'");
      }
    }
  }



}
