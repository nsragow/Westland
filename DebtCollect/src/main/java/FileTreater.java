import java.util.*;
import java.io.*;

public class FileTreater
{
  public static void main(String[] args) throws Exception
  {
    Scanner sc = new Scanner(new File("./src/main/resources/contacts.csv"));
    FileWriter fw = new FileWriter(new File("./src/main/resources/contactsTreated.csv"));
    int count = 0;
    int deletes = 0;
    int notDeletes = 0;
    boolean delete = false;
    while(sc.hasNext()){
      String string = sc.nextLine();
      for(int i = 0; i < string.length(); i++){
        char c = string.charAt(i);
        if(c == '\"'){
          count++;
          delete = !delete;
        }
        fw.write(c);
      }

      if(delete){

        fw.write(' ');
        deletes++;
      }else{
        fw.write('\n');
        notDeletes++;
      }


    }
    fw.close();
    System.out.println(count);
    System.out.println(deletes);
    System.out.println(notDeletes);
  }
}
