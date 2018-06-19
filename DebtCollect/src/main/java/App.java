
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import westland.signature.automator.*;
import java.io.*;
import java.util.*;

public class App {

  public static void main(String[] args)throws Exception {
    dataPrep();

  }


  private static void dataPrep() throws Exception
  {
    List<Contact> contacts = Generator.getContacts("./src/main/resources/contacts.csv");
    List<Matter> matters = Generator.getMatters("./src/main/resources/matters.csv");
    List<CollectionDetails> details = Generator.getDetails("./src/main/resources/collectiondetails.csv");
    Map<String,Collection> collMapping = Generator.hashMapCollections("./src/main/resources/collections.csv");
    Map<String, List<Collection>> contMapping = new HashMap<>();
    Map<String, List<Collection>> matterMapping = new HashMap<>();
    for(String colID : collMapping.keySet()){
      Collection c = collMapping.get(colID);
      if(contMapping.containsKey(c.getContactID())){
        List<Collection> cols = contMapping.get(c.getContactID());
        cols.add(c);
      }else{
        ArrayList<Collection> colList = new ArrayList<>();
        colList.add(c);
        contMapping.put(c.getContactID(),colList);
      }
    }
    for(String colID : collMapping.keySet()){
      Collection c = collMapping.get(colID);

      if(matterMapping.containsKey(c.getMatterID())){

        List<Collection> cols = matterMapping.get(c.getMatterID());
        cols.add(c);
      }else{
        ArrayList<Collection> colList = new ArrayList<>();
        colList.add(c);
        matterMapping.put(c.getMatterID(),colList);
      }
    }
    for(Matter m : matters){


      if(matterMapping.containsKey(m.getID())){
        for(Collection collection : matterMapping.get(m.getID())){
          collection.setMatter(m);

        }
      }
      else{
        System.out.println(m.getID().toString() + " was not found in matterMapping");
      }
    }
    int count = 0,count2 = 0;
    for(Contact c : contacts){
      count2++;

      if(contMapping.containsKey(c.getID())){
        for(Collection collection : contMapping.get(c.getID())){
          collection.addContact(c);

        }
        count++;
      }
      else{
        System.out.println(c.getID().toString() + " was not found in contactMapping");
      }
    }

    System.out.println(count+"/"+count2);
    count = 0;
    count2 = 0;
    for(CollectionDetails cd : details){
      count2++;
      if(collMapping.containsKey(cd.getCollectionID())){
        count++;
        collMapping.get(cd.getCollectionID()).addDetails(cd);


      }
      else{
        System.out.println(cd.getCollectionID().toString() + " was not found in collectionMapping");
      }
    }
    System.out.println(count+"/"+count2);
    FileWriter f = new FileWriter("./src/main/resources/DebtList.csv");
    int positive = 0, zero = 0, negitive = 0;
    long totalL = 0l;
    double totalD = 0d;
    long cutL = 0l;
    double cutD = 0d;


    int dollar = 0, ten = 0, fifty = 0, hundred = 0, fiveHundred = 0, thousand = 0, more = 0;
    int negDollar = 0, negTen = 0, negFifty = 0, negHundred = 0, negFiveHundred = 0, negThousand = 0, negMore = 0;

    double dollarT = 0, tenT = 0, fiftyT = 0, hundredT = 0, fiveHundredT = 0, thousandT = 0, moreT = 0;
    double negDollarT = 0, negTenT = 0, negFiftyT = 0, negHundredT = 0, negFiveHundredT = 0, negThousandT = 0, negMoreT = 0;

    ArrayList<Collection> debts = new ArrayList<>();
    ArrayList<Collection> openCollections = new ArrayList<>();
    for(Collection c : collMapping.values()){
      if(c.getMatter() != null && c.getMatter().getStatus().trim().toLowerCase().equals("open")){
        openCollections.add(c);
      }
    }
    for(Collection c : openCollections){
      //f.write(c.toXML());


      double test = c.totalCollectionValue();

      if (test > 1000d){
        more++;
        moreT+=test;
      }else if (test > 500d){
        thousand++;
        thousandT+=test;
      }else if (test > 100d){
        fiveHundred++;
        fiveHundredT+=test;
      }else if (test > 50d){
        hundred++;
        hundredT+=test;
      }else if (test > 10d){
        fifty++;
        fiftyT+=test;
      }else if (test > 1d){
        ten++;
        tenT+=test;
      }else if (test > 0d){
        dollar++;
        dollarT+=test;
      }else if (test >= -10d){
        negTen++;
        negTenT+=test;
      }else if (test >= -50d){
        negFifty++;
        negFiftyT+=test;
      }else if (test >= -100d){
        negHundred++;
        negHundredT+=test;
        debts.add(c);
      }else if (test >= -500d){
        negFiveHundred++;
        negFiveHundredT+=test;
        debts.add(c);
      }else if (test >= -1000d){
        negThousand++;
        negThousandT+=test;
        debts.add(c);
      }else{
        negMore++;
        negMoreT+=test;
        debts.add(c);
      }




    }
    Collections.sort(debts);

    System.out.println("p: " +positive+" n: " +negitive+" z: "+zero );
    System.out.println(totalD+" :total debt: "+totalL);
    System.out.println(cutD+ " total cut "+ cutL);
    System.out.println("final: " + totalD+cutD);
    System.out.println("\r\n\r\n-----------------------------\r\n");
    f.write("firstName,middleName,lastName,suffix,contactType,value,Desc,Social\r\n");
    for(Collection val : debts){
      StringBuilder firstName = new StringBuilder();
      StringBuilder middleName = new StringBuilder();
      StringBuilder lastName = new StringBuilder();
      StringBuilder suffix = new StringBuilder();
      StringBuilder type = new StringBuilder();
      StringBuilder ssn = new StringBuilder();
      boolean timeTwo = false;
      for(Contact contact : val.getContacts()){

        firstName.append(contact.getFirstName());
        middleName.append(contact.getMiddleName());
        lastName.append(contact.getLastName());
        suffix.append(contact.getSuffix());
        type.append(contact.getType());
        ssn.append(contact.getSSN());

        timeTwo = true;
      }
      f.write("\""+firstName.toString() +"\""+ ","+"\""+ middleName.toString() +"\""+ "," +"\""+lastName.toString()+"\",\""+suffix.toString() +"\""+ ","+"\""+ type.toString()+"\""+","+"\"");
      f.write(val.totalCollectionValue()+"\""+","+"\""+val.getMatter().getShortDesc()+" : "+val.getMatter().getDesc()+"\",\""+ssn.toString()+"\"\r\n");
    }

    System.out.println("\r\n\r\n-----------------------------\r\n");
    System.out.println("more count: " + more + " total: " + moreT);
    System.out.println("thousand count: " + thousand + " total: " + thousandT);
    System.out.println("fiveHundred count: " + fiveHundred + " total: " + fiveHundredT);
    System.out.println("hundred count: " + hundred + " total: " + hundredT);
    System.out.println("fifty count: " + fifty + " total: " + fiftyT);
    System.out.println("ten count: " + ten + " total: " + tenT);
    System.out.println("dollar count: " + dollar + " total: " + dollarT);
    System.out.println("negTen count: " + negTen + " total: " + negTenT);
    System.out.println("negFifty count: " + negFifty + " total: " + negFiftyT);
    System.out.println("negHundred count: " + negHundred + " total: " + negHundredT);
    System.out.println("negFiveHundred count: " + negFiveHundred + " total: " + negFiveHundredT);
    System.out.println("negThousand count: " + negThousand + " total: " + negThousandT);
    System.out.println("negMore count: " + negMore + " total: " + negMoreT);
    f.close();


  }

  private static void counter()
  {
    /*int negitive = 0;
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
    System.out.println(negitive+" p:"+positive+" Z:"+zero);*/
  }
}
