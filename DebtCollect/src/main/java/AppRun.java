
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import westland.signature.automator.*;
import java.io.*;
import java.util.*;

public class AppRun
{
  public static void run()
  {
    // Connect to database
    String hostName;
    String dbName;
    String user;
    String password;
    Connection connection = null;

    try {
      Scanner sc = new Scanner(new File("./src/main/resources/credentials.txt"));
      hostName = sc.nextLine();
      dbName = sc.nextLine();
      user = sc.nextLine();
      password = sc.nextLine();

      String url = String.format("jdbc:sqlserver://%s:1433;user=%s;password=%s;", hostName, user, password);
      connection = DriverManager.getConnection(url);
      String schema = connection.getSchema();
      System.out.println("Successful connection - Schema: " + schema);

      System.out.println("Query data example:");
      System.out.println("=========================================");

      // Create and execute a SELECT SQL statement.
      String selectSql = "Select c.collections,c.amount,c.collecttype,c.collectdesc,c.collectionsDetail from Prolaw2008.dbo.collectionsDetail as c";

      try (Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery(selectSql)) {
        System.out.println("got Past");
        StringBuilder b;
        String comma = ", ";
        String end = "\n";
        FileWriter fw = new FileWriter("./src/main/resources/collectionDetails.csv");
        System.out.println("goan");
        while(resultSet.next()){
          System.out.println("dfsda;df");
          b = new StringBuilder();
          System.out.println("1");
          b.append(resultSet.getString(1));
          b.append(comma);
          b.append(resultSet.getString(2));
          b.append(comma);
          b.append(resultSet.getString(3));
          b.append(comma);
          b.append(resultSet.getString(4));
          b.append(comma);
          b.append(resultSet.getString(5));
          b.append(end);
          System.out.println("2");
          System.out.println("about");
          fw.write(b.toString());
        }
        System.out.println("done");
        connection.close();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
