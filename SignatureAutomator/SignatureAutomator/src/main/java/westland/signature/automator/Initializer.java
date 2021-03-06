package westland.signature.automator;

import java.util.*;
import java.io.*;
import com.google.api.services.drive.Drive;

public class Initializer
{

  public static Table getTable(String path)
  {
    CSVReader csvread = new CSVReader(path);
    return csvread.getTable();
  }
  public static Set<String> fileToSet(String filePath) throws FileNotFoundException
  {
    HashSet<String> toReturn = new HashSet<>();
    Scanner sc = new Scanner(new File(filePath));
    while(sc.hasNextLine()){
      toReturn.add(sc.nextLine());
    }
    sc.close();
    return toReturn;
  }
  public static void overwriteLocalFilesWithDrive(Drive drive,Table fileInfo,Table paths,String workingDirectory) throws Exception
  {
    //Drive drive = getDrive("jesse.n@westlandreg.com");


    for(String key : fileInfo.keySet()){
      String fileid = fileInfo.get(key,"id");
      String type = fileInfo.get(key,"export_type");
      String fileToOverwrite = paths.get(key,"val");
      boolean tryAgain = true;
      while(tryAgain){
        try{
          drive.files().export(fileid,type).executeMediaAndDownloadTo(new FileOutputStream(workingDirectory+fileToOverwrite));
          tryAgain = false;
        }catch(java.net.SocketTimeoutException e){
          tryAgain = true;
        }

      }
    }
  }
}
