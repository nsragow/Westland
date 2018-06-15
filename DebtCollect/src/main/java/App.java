
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import westland.signature.automator.*;
import java.io.*;
import java.util.*;

public class App {

  public static void main(String[] args) {
run();
    AppRun.run();
  }


  private static void run()
  {
    Table fullResults = Initializer.getTable("./src/main/resources/fullResults.csv");
    int negitive = 0;
    int positive = 0;
    int zero = 0;
    for(String s: fullResults.keySet()){
      double d = Double.parseDouble(fullResults.get(s,"amount"));
      if(d>0){
        positive++;
      }else if(d<0){
        negitive++;
      }else{
        zero++;
      }
    }
    System.out.println(negitive+" p:"+positive+" Z:"+zero);
  }
}
