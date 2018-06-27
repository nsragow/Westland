package westland.signature.automator;

import com.google.api.services.drive.Drive;
import com.google.api.services.admin.directory.model.User;
import java.util.*;
import java.io.*;

public class LiveSheet
{
  private static final int COLUMN_COUNT = 14;
  private static final int UPDATE = 7;
  private static final int EMAIL = 8;
  private static final int EXT = 1;
  private static final int FIRST_NAME = 2;
  private static final int ORG = 0;
  private static final String FILE_ID = "17Ppz1cTflURCq9XSxF00-cR3bO_OXPCxIXoqw3hYDYA";
  private static final String ACCOUNT = "noah.s@westlandreg.com";

  private ServiceManager serviceManager;

  public LiveSheet(ServiceManager sM)
  {
    serviceManager = sM;
  }

  //as of now this only works with the one live sheet
  public void runLiveSheet() throws IOException
  {
    String[][] table = downloadLiveSheet(FILE_ID);
    StringBuilder stringBuilder = new StringBuilder();
    for(int i = 0; i < table.length; i++){
      String update = table[i][UPDATE].toLowerCase();
      if(update.equals("yes")){
        try{
          updateUser(table[i][EMAIL],table[i][EXT],table[i][ORG]);
          table[i][UPDATE] = "no";
        }catch(LogException e){
          System.out.println(e);
          stringBuilder.append(Helper.exceptionToString(e));
          stringBuilder.append("\n");
          table[i][UPDATE] = "error";
        }
      }
    }
    File file = new File(Strings.workingDirectory+"/src/main/resources/faxChanges.csv");
    FileWriter fw = new FileWriter(file);
    for(String[] line : table){
      for(int i = 0; i < line.length; i++){
        if(i!=0)fw.write(",");
        fw.write(line[i]);
      }
      fw.write("\n");
    }
    fw.close();
    serviceManager.updateSheet(FILE_ID, "extChanges", file, ACCOUNT);
    if(stringBuilder.length() != 0){
      throw new LogException("There were error: " + stringBuilder.toString());
    }

  }

  private void updateUser(String email, String newExt, String newOrg) throws IOException
  {
    User user = serviceManager.getUser(email);
    Map<String,Map<String,Object>> cs = user.getCustomSchemas();
    if(cs!=null){
      Map<String,Object> addInfo = cs.get("Additional_Info");
      if(addInfo!=null){
        Object ext = addInfo.put("Extension",newExt);

      }else{
        throw new LogException("could not update " + email + " because Additional Info was not found");
      }
    }else{
      System.out.println("not found");
      cs = new HashMap<String,Map<String,Object>>();
      Map<String,Object> toAdd = new HashMap<String,Object>();
      toAdd.put("Extension",newExt);
      cs.put("Additional_Info",toAdd);

    }
    user.setCustomSchemas(cs);

    user.setOrgUnitPath("/"+newOrg);
    
    serviceManager.getDirectory().users().update(email,user).execute();
  }

  private String[][] downloadLiveSheet(String id) throws IOException
  {
    ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
    serviceManager.getDrive(ACCOUNT).files().export(id,"text/csv").executeMediaAndDownloadTo(bAOS);
    String[] lines = bAOS.toString().split("\n");


    String[][] table = new String[lines.length][COLUMN_COUNT];

    String [] toAdd;
    for(int i = 0; i < lines.length; i++){
      toAdd = lines[i].split(",");
      for(int j = 0; j < COLUMN_COUNT; j++){
        String putIntoTable = toAdd[j].trim();
        table[i][j] = putIntoTable;
      }
    }
    return table;
  }



}
