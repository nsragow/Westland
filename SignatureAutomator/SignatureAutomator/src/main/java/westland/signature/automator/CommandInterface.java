package westland.signature.automator;

import java.util.*;


public class CommandInterface
{
  private Commands com;
  private Scanner sc;
  private static final String welcomeMessage = "Welcome to Bandle's commandline interface\ntry \"help\" or \"quit\"";
  private static final String notSupported = "currently not supported";
  private static final String[] commands = new String[]{
    "exit - leave program",
    "help - display command list",
    "create - make user, group, or orgunit",
    "delete - delete user, group, or orgunit",
    "massCreate - make many users, groups, or orgunits based on some list of values",
    "massDelete - delete many users, groups, or orgunits based on some list of values",
    "add - add user(s) to group or orgunit",
    "edit - change settings and values of orgunits, groups and users",
    "print - get data on orgunits, groups, and users"
  };
  public CommandInterface(ServiceManager serviceManager)
  {
    com = new Commands(serviceManager);
    sc = new Scanner(System.in);

    run();
  }
  private void run()
  {
    System.out.println(welcomeMessage);
    while(true){
      switch(sc.nextLine().toLowerCase().trim()){
        case "quit":
        case "q":
        case "exit":
          System.out.println("exiting...");
          System.exit(0);
        case "help":
          System.out.println("Commands:");
          for(String s : commands){
            System.out.println(s);
          }
          break;
        case "create":
          createCommand();
          break;
        case "masscreate":
          massCreateCommand();
          break;
        case "massdelete":
          massDeleteCommand();
          break;
        case "delete":
          //deleteCommand();
          System.out.println(notSupported);
          break;
        case "add":
          addCommand();
          break;
        case "edit":
          editCommand();
          break;
        case "print":
          printCommand();
          break;
        default:
          System.out.println("Command not recongnized, try \"help\"");

      }
    }
  }

  private void addCommand()
  {
    System.out.println("Adding to Group or OrgUnit?");
    switch(Helper.waitOnOption(new String[]{"Group","OrgUnit","Quit"})){
      case 0:
        addToGroupCommand();
        break;
      case 1:
        System.out.println(notSupported);
        //createGroupCommand();
        break;
      case 2:
        break;
      default:
        throw new RuntimeException("Unexpected Error");
    }

  }
  private void editCommand()
  {
    System.out.println("Edit to OrgUnit, Group, or User? (OrgUnit/Group/User)");
    switch(Helper.waitOnOption(new String[]{"OrgUnit","Group","User","Quit"})){
      case 0:
        orgUnitEditCommand();
        break;
      case 1:
        System.out.println(notSupported);
        //createGroupCommand();
        break;
      case 2:
      System.out.println(notSupported);

        break;
      case 3:
        break;
      default:
        throw new RuntimeException("Unexpected Error");
    }

  }
  private void printCommand()
  {
    System.out.println("Print info on OrgUnits, Groups, or Users? (OrgUnit/Group/User)");
    switch(Helper.waitOnOption(new String[]{"OrgUnit","Group","User","Quit"})){
      case 0:
        orgUnitPrintCommand();
        break;
      case 1:
        System.out.println(notSupported);
        //createGroupCommand();
        break;
      case 2:
      System.out.println(notSupported);

        break;
      case 3:
        break;
      default:
        throw new RuntimeException("Unexpected Error");
    }

  }
  private void orgUnitEditCommand()
  {
    System.out.println("Enter formatted org unit");
    String orgunit = sc.nextLine();
    System.out.println("Enter formatted tag");
    String tag = sc.nextLine();
    System.out.println("Org Unit: " + orgunit);
    System.out.println("Tag: " + tag);
    System.out.println("(Y/N)");
    if(Helper.waitOnOption(new String[]{"Y","N"})==0){
      System.out.println("executing...");
      String[] splitOrgs = orgunit.split(",");
      String[] splitTags = tag.split(",");
      if(orgunit.contains("*")){
        com.editOrgTags("*",splitTags);
      }else{
        for(String s : splitOrgs){
          com.editOrgTags(s.trim(),splitTags);
        }
      }
      System.out.println("done");
    }else{
      System.out.println("Do you want to start over? (Y/N)");
      if(Helper.waitOnOption(new String[]{"Y","N"})==0){
        orgUnitEditCommand();
      }
    }

  }
  private void orgUnitPrintCommand()
  {
    System.out.println("Enter formatted org unit");
    String orgunit = sc.nextLine();
    System.out.println("Enter formatted field");
    String info = sc.nextLine();
    System.out.println("Org Unit: " + orgunit);
    System.out.println("Field: " + info);
    System.out.println("(Y/N)");
    if(Helper.waitOnOption(new String[]{"Y","N"})==0){
      System.out.println("executing...");
      String[] splitOrgs = orgunit.split(",");
      String[] splitTags = info.split(",");
      if(orgunit.contains("*")){
        com.printOrgInfo("*",splitTags);
      }else{
        for(String s : splitOrgs){
          com.printOrgInfo(s.trim(),splitTags);
        }
      }
      System.out.println("done");
    }else{
      System.out.println("Do you want to start over? (Y/N)");
      if(Helper.waitOnOption(new String[]{"Y","N"})==0){
        orgUnitPrintCommand();
      }
    }

  }
  private void addToGroupCommand()
  {
    System.out.println("Enter formatted user email");
    String email = sc.nextLine();
    System.out.println("Enter formatted org unit");
    String orgunit = sc.nextLine();
    System.out.println("Enter formatted group");
    String group = sc.nextLine();
    System.out.println("Are you satisfied with this setup?");
    System.out.println("Email: " + email);
    System.out.println("Org Unit: " + orgunit);
    System.out.println("Group: " + group);
    System.out.println("(Y/N)");
    if(Helper.waitOnOption(new String[]{"Y","N"})==0){
      System.out.println("executing...");

      com.makeGroupMembers(email,orgunit,group);
      System.out.println("done");
    }else{
      System.out.println("Do you want to start over? (Y/N)");
      if(Helper.waitOnOption(new String[]{"Y","N"})==0){
        addToGroupCommand();
      }
    }

  }
  private void createCommand()
  {
    System.out.println("Create what? (User/Group/OrgUnit)");
    switch(Helper.waitOnOption(new String[]{"User","Group","OrgUnit","Quit"})){
      case 0:
        System.out.println(notSupported);
        break;
      case 1:
        System.out.println(notSupported);
        //createGroupCommand();
        break;
      case 2:
        System.out.println(notSupported);
        break;
      case 3:
        return;
      default:
        throw new RuntimeException("Unexpected Error");
    }

  }
  private void massCreateCommand()
  {
    System.out.println("Create what? (User/Group/OrgUnit)");
    switch(Helper.waitOnOption(new String[]{"User","Group","OrgUnit","Quit"})){
      case 0:
        System.out.println(notSupported);
        break;
      case 1:
        massCreateGroupCommand();
        break;
      case 2:
        System.out.println(notSupported);
        break;
      case 3:
        return;
      default:
        throw new RuntimeException("Unexpected Error");
    }

  }
  private void massDeleteCommand()
  {
    System.out.println("Delete what? (User/Group/OrgUnit)");
    switch(Helper.waitOnOption(new String[]{"User","Group","OrgUnit","Quit"})){
      case 0:
        System.out.println(notSupported);
        break;
      case 1:
        massDeleteGroupCommand();
        break;
      case 2:
        System.out.println(notSupported);
        break;
      case 3:
        return;
      default:
        throw new RuntimeException("Unexpected Error");
    }

  }
  private void massCreateGroupCommand()
  {
    System.out.println("Enter formatted email");
    String email = sc.nextLine();
    System.out.println("Enter formatted name");
    String name = sc.nextLine();
    System.out.println("Enter formatted description");
    String desc = sc.nextLine();
    System.out.println("Enter schema (Staff/Management)");
    int type = Helper.waitOnOption(new String[]{"Staff","Management"});
    String schema;
    switch(type){
      case 0: schema = "Staff"; break;
      case 1: schema = "Management"; break;
      default: throw new RuntimeException("Unexpected Error");
    }
    System.out.println("Are you satisfied with this setup?");
    System.out.println("Email: " + email);
    System.out.println("Name: " + name);
    System.out.println("Description: " + desc);
    System.out.println("Schema: " + schema);
    System.out.println("(Y/N)");
    if(Helper.waitOnOption(new String[]{"Y","N"})==0){
      System.out.println("executing...");

      com.massGroupCreator(email,name,desc,type,Commands.APPLY_BLACKLIST);
      System.out.println("done");
    }else{
      System.out.println("Do you want to start over? (Y/N)");
      if(Helper.waitOnOption(new String[]{"Y","N"})==0){
        massCreateGroupCommand();
      }
    }

  }
  private void massDeleteGroupCommand()
  {
    System.out.println("Enter formatted email");
    String email = sc.nextLine();

    System.out.println("Are you satisfied with this setup?");
    System.out.println("Email: " + email);
    System.out.println("(Y/N)");
    if(Helper.waitOnOption(new String[]{"Y","N"})==0){
      System.out.println("executing...");
      com.massGroupDeleter(email,Commands.APPLY_BLACKLIST);
      System.out.println("done");

    }else{
      System.out.println("Do you want to start over? (Y/N)");
      if(Helper.waitOnOption(new String[]{"Y","N"})==0){
        massDeleteGroupCommand();
      }
    }

  }
}