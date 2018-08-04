package westland.signature.automator;

import com.google.api.services.drive.Drive;
import com.google.api.services.admin.directory.model.User;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.admin.directory.model.UserName;
import java.util.*;
import java.io.*;

public class LiveSheet
{
  private static final int COLUMN_COUNT = 14;
  private static final int UPDATE = 0;
  private static final int ORG = 1;
  private static final int EXT = 2;
  private static final int FIRST_NAME = 3;
  private static final int LAST_NAME = 4;
  private static final int EMAIL = 5;
  private static final int TITLE = 6;
  private static final int FAX = 7;
  private static final int CELL = 8;
  private static final String CLEAR_COMMAND = "#clear";

  private static final String FILE_ID = "10JPgtTbfkwfpq-80I0piIU8Lcj_QgAuQBCuTYyCBf9g";
  private static final String ACCOUNT = "noah.s@westlandreg.com";

  private ServiceManager serviceManager;
  private Set<String> signaturesToUpdate;

  public LiveSheet(ServiceManager sM)
  {
    serviceManager = sM;
    signaturesToUpdate = new HashSet<>();
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
          updateUser(table[i]);
          table[i][UPDATE] = "no";
          signaturesToUpdate.add(table[i][EMAIL]);
        }catch(Exception e){

          stringBuilder.append(Helper.exceptionToString(e));
          stringBuilder.append("\n");
          table[i][UPDATE] = "ERROR";
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
    serviceManager.updateSheet(FILE_ID, "User_Live_Sheet", file, ACCOUNT);
    if(!signaturesToUpdate.isEmpty()){
      SignatureUpdater su = new SignatureUpdater(new DataCollector(serviceManager, new StringBuilder()).getDataMap(),serviceManager);
      for(String email : signaturesToUpdate){
        try{
          //su.updateSignature(email);
          //todo now orgcheck takes care of thsi?
        }catch(Exception e){
          stringBuilder.append(Helper.exceptionToString(e));
        }
      }
    }
    if(stringBuilder.length() != 0){
      throw new LogException("There were error: " + stringBuilder.toString());
    }

  }

  private void updateUser(String[] line) throws IOException
  {
    String org = line[ORG];
    String ext = line[EXT];
    String firstName = line[FIRST_NAME];
    String lastName = line[LAST_NAME];
    String email = line[EMAIL];
    String title = line[TITLE];
    String fax = line[FAX];
    String cell = line[CELL];

    User user;
    try{
      user = serviceManager.getUser(email);

    }catch(Exception e){
      throw new LogException(Helper.exceptionToString(e));
    }

    if(!lastName.isEmpty()){
      if(lastName.toLowerCase().equals(CLEAR_COMMAND)){
        lastName = "";
      }
    }
    //set ext
    if(!ext.isEmpty()){
      if(ext.toLowerCase().equals(CLEAR_COMMAND)){
        ext = "";
      }
      Map<String,Map<String,Object>> cs = user.getCustomSchemas();
      if(cs!=null){
        Map<String,Object> addInfo = cs.get("Additional_Info");
        if(addInfo==null){
          cs.put("Additional_Info",new HashMap<String,Object>());
          addInfo = cs.get("Additional_Info");
        }
        addInfo.put("Extension",ext);
      }else{

        cs = new HashMap<String,Map<String,Object>>();
        Map<String,Object> toAdd = new HashMap<String,Object>();
        toAdd.put("Extension",ext);
        cs.put("Additional_Info",toAdd);

      }
      user.setCustomSchemas(cs);
    }
    //set org
    if(!org.isEmpty()){
      if(org.toLowerCase().equals(CLEAR_COMMAND)){
        org = "";
      }
      user.setOrgUnitPath("/"+org);
    }
    //set firstName/lastname
    UserName userName = user.getName();
    if(!firstName.isEmpty()){
      if(firstName.toLowerCase().equals(CLEAR_COMMAND)){
        firstName = "";
      }
      userName.setGivenName(firstName);
    }
    if(!lastName.isEmpty()){
      if(lastName.toLowerCase().equals(CLEAR_COMMAND)){
        lastName = "";
      }
      userName.setFamilyName(lastName);
    }
    user.setName(userName);
    //set title
    if(!title.isEmpty()){
      if(title.toLowerCase().equals(CLEAR_COMMAND)){
        title = "";
      }
      Object orginizations = user.getOrganizations();
      List<ArrayMap<String,Object>> orgList = null;
      try{
        orgList = SignatureBuilder.objectToArrayMapList(orginizations);
      }catch(IllegalArgumentException i){
        throw new LogException(i.toString()+"\n");
      }catch(NullPointerException e){
        orgList = new ArrayList<ArrayMap<String,Object>>();
        orgList.add(new ArrayMap<String,Object>());
      }
      orgList.get(0).put("title",title);
      user.setOrganizations(orgList);

    }
//    //set fax and cell
    Object phoneObj = user.getPhones();
    List<ArrayMap<String,Object>> phoneList = null;
    try{
      phoneList = SignatureBuilder.objectToArrayMapList(phoneObj);
    }catch(IllegalArgumentException i){
      throw new LogException(i.toString()+"\n");

    }catch(NullPointerException e){
      phoneList = new ArrayList<ArrayMap<String,Object>>();
    }
    int indexOfMobile = -1;
    int indexOfFax = -1;
    for(int i = 0; i < phoneList.size(); i++){
      if(phoneList.get(i).get("type").toString().toLowerCase().equals("mobile")){
        indexOfMobile = i;
      }else if(phoneList.get(i).get("type").toString().toLowerCase().equals("work_fax")){
        indexOfFax = i;
      }
    }
    if(!fax.isEmpty()){
      if(fax.toLowerCase().equals(CLEAR_COMMAND)){
        fax = "";
      }
      if(indexOfFax == -1){
        ArrayMap<String,Object> toAdd = new ArrayMap<>();
        toAdd.put("type","work_fax");
        toAdd.put("value",fax);
        phoneList.add(toAdd);
      }else{
        ArrayMap<String,Object> toAdd = phoneList.get(indexOfFax);
        toAdd.put("value",fax);
        phoneList.add(toAdd);
      }
    }
    if(!cell.isEmpty()){
      if(cell.toLowerCase().equals(CLEAR_COMMAND)){
        cell = "";
      }
      if(indexOfMobile == -1){
        ArrayMap<String,Object> toAdd = new ArrayMap<>();
        toAdd.put("type","mobile");
        toAdd.put("value",cell);
        phoneList.add(toAdd);
      }else{
        ArrayMap<String,Object> toAdd = phoneList.get(indexOfMobile);
        toAdd.put("value",cell);
        phoneList.add(toAdd);
      }
    }
    user.setPhones(phoneList);

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

        String putIntoTable;
        if(toAdd.length>j){
          putIntoTable = toAdd[j].trim();
        }else{
          putIntoTable = "";
        }
        table[i][j] = putIntoTable;
      }
    }
    return table;
  }



}
