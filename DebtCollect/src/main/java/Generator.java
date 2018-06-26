import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.util.*;
import java.io.*;

public class Generator
{
  public static List<Contact> getContacts(String path)
  {
    CSVParser parser;
    ArrayList<Contact> toReturn = new ArrayList<>();
    try{
      parser = CSVFormat.EXCEL.parse(new BufferedReader(new FileReader(path)));

      List<CSVRecord> records = parser.getRecords();


      for(int i = 1; i < records.size(); i++){
        CSVRecord toConvert = records.get(i);
        Contact.Builder b = Contact.getBuilder();
        b.setID(toConvert.get(0));
        b.setFirstName(toConvert.get(1));
        b.setMiddleName(toConvert.get(2));
        b.setLastName(toConvert.get(3));
        b.setSuffix(toConvert.get(4));
        b.setType(toConvert.get(5));
        b.setSSN(toConvert.get(6));
        b.setAddr(toConvert.get(7));
        toReturn.add(b.build());
      }
      return toReturn;

    }catch(Exception e){
      //do something
      System.out.println("hit exception");
      e.printStackTrace();
    }
    return null;
  }
  public static List<CollectionDetails> getDetails(String path)
  {
    CSVParser parser;
    ArrayList<CollectionDetails> toReturn = new ArrayList<>();
    try{
      parser = CSVFormat.EXCEL.parse(new BufferedReader(new FileReader(path)));

      List<CSVRecord> records = parser.getRecords();


      for(int i = 1; i < records.size(); i++){
        CSVRecord toConvert = records.get(i);
        CollectionDetails.Builder b = CollectionDetails.getBuilder();
        b.setCollectionID(toConvert.get(0));
        b.setAmount(toConvert.get(1));
        b.setType(toConvert.get(2));
        b.setDescription(toConvert.get(3));
        b.setID(toConvert.get(4));

        toReturn.add(b.build());
      }
      return toReturn;

    }catch(Exception e){
      //do something
      System.out.println("hit exception");
      e.printStackTrace();
    }
    return null;
  }
  public static List<Matter> getMatters(String path)
  {
    CSVParser parser;
    ArrayList<Matter> toReturn = new ArrayList<>();
    try{
      parser = CSVFormat.EXCEL.parse(new BufferedReader(new FileReader(path)));

      List<CSVRecord> records = parser.getRecords();


      for(int i = 1; i < records.size(); i++){
        CSVRecord toConvert = records.get(i);
        Matter.Builder b = Matter.getBuilder();
        b.setID(toConvert.get(0));
        b.setShortDesc(toConvert.get(1));
        b.setAreaOfLaw(toConvert.get(2));
        b.setStatus(toConvert.get(3));
        b.setDesc(toConvert.get(4));

        toReturn.add(b.build());
      }
      return toReturn;

    }catch(Exception e){
      //do something
      System.out.println("hit exception");
      e.printStackTrace();
    }
    return null;
  }
  public static HashMap<String,Collection> hashMapCollections(String path)
  {
    CSVParser parser;
    HashMap<String,Collection> toReturn = new HashMap<>();
    try{
      parser = CSVFormat.EXCEL.parse(new BufferedReader(new FileReader(path)));

      List<CSVRecord> records = parser.getRecords();


      for(int i = 1; i < records.size(); i++){
        CSVRecord toConvert = records.get(i);
        String id = toConvert.get(0);
        Collection col = new Collection(id,toConvert.get(2),toConvert.get(1));



        if(toReturn.put(id, col)!=null){
          System.out.println("overwritten");
        }
      }
      return toReturn;

    }catch(Exception e){
      //do something
      System.out.println("hit exception");
      e.printStackTrace();
    }
    return null;
  }
}
