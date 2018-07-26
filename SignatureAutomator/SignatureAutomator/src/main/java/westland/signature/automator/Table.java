package westland.signature.automator;

import java.util.*;
import java.io.*;

public class Table
{
  private String[] header;
  private String keyType;
  private Map<String,String[]> rows;
  private String[] originalHeader;

  public static void writeTableToCSV(Table t, String fileName) throws Exception
  {
    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
    writer.write("\""+t.keyType+"\",");
    for(String s : t.header){
      writer.write("\""+s+"\",");
    }
    for(String s : t.rows.keySet()){
      writer.write("\n\""+s+"\"");
      for(String st : t.rows.get(s)){
        writer.write(",\""+st+"\"");
      }
    }
    writer.close();

  }
  public void remove(String userName)
  {
    rows.remove(userName);
  }
  //makes assumption that data is not null or empty
  public Table(String[] originalHeader)
  {
    this.originalHeader = Arrays.copyOf(originalHeader,originalHeader.length);
    keyType = originalHeader[0];
    //also assumes that there is more than one column
    header = new String[originalHeader.length-1];
    for(int i = 1; i < originalHeader.length; i++){
      header[i-1] = originalHeader[i];
    }
    rows = new HashMap<String, String[]>();
  }

  protected void addRow(String[] row)
  {
    String[] newRow = new String[row.length-1];
    for(int i = 1; i < row.length; i++){
      newRow[i-1] = row[i];
    }
    rows.put(row[0],newRow);
  }

  public String get(String key, String header)
  {
    if(!rows.containsKey(key)){

      throw new IllegalArgumentException("does not contain this key "+ key);
    }
    int index = -1;
    for(int i = 0; i < this.header.length; i++)
    {
      if(header.equals(this.header[i])){
        index = i;
      }
    }
    if(index == -1 || rows.get(key).length<=index){
      return null;
    }else{
      return rows.get(key)[index];
    }
  }
  public String[] getRow(String key)
  {
    if(!rows.containsKey(key)){

      throw new IllegalArgumentException("does not contain this key "+ key);
    }
    String[] toReturn = new String[this.header.length + 1];

    for(int i = 0; i < this.header.length; i++)
    {
      toReturn[i+1] = this.get(key,this.header[i]);
    }
    toReturn[0] = key;

    return toReturn;

  }
  public Set<String> keySet()
  {
    return rows.keySet();
  }
  public boolean containsKey(String key)
  {
    return rows.containsKey(key);
  }
  public Table leftJoin(Table toJoin)
  {
    String[] originalOne = this.getOriginalHeader();
    String[] originalTwo = toJoin.getOriginalHeader();
    String[] newHeader = new String[originalOne.length + originalTwo.length];
    for(int i = 0; i < originalOne.length; i++){
      newHeader[i] = new String(originalOne[i]);
    }
    for(int i = 0; i < originalTwo.length; i++){
      newHeader[i+originalOne.length-1] = originalTwo[i]+"_*";
    }

    Table toReturn = new Table(newHeader);
    //now I need to add the rows
    for(String key : this.keySet()){
      if(toJoin.containsKey(key)){
        String[] rowOne = this.getRow(key);
        String[] rowTwo = toJoin.getRow(key);
        String[] newRow = new String[rowOne.length + rowTwo.length];
        for(int i = 0; i < rowOne.length; i++){
          newRow[i] = new String(rowOne[i]);
        }
        for(int i = 0; i < rowTwo.length; i++){
          newRow[i+rowOne.length-1] = new String(rowTwo[i]);
        }
        toReturn.addRow(newRow);
      }
    }
    return toReturn;
  }
  public String[] getOriginalHeader()
  {
    return Arrays.copyOf(originalHeader,originalHeader.length);
  }
  public Table swapKeyTo(String newKey)
  {
    if(!containsHeader(newKey)){
      throw new IllegalArgumentException("does not contain header "+newKey);
    }
    String[] newHeader = Arrays.copyOf(this.getOriginalHeader(),this.getOriginalHeader().length);
    int toSwap;
    for(toSwap = 1; toSwap < newHeader.length; toSwap++){
      if(newHeader[toSwap].equals(newKey)){
        break;
      }
    }
    String temp = newHeader[0];
    newHeader[0] = newHeader[toSwap];
    newHeader[toSwap] = temp;

    Table newTable = new Table (newHeader);
    for(String key : this.keySet()){
      String[] newRow = Arrays.copyOf(this.getRow(key),this.getRow(key).length);
      String tempz = newRow[0];
      newRow[0] = newRow[toSwap];
      newRow[toSwap] = tempz;
      newTable.addRow(newRow);
    }
    return newTable;
  }
  public boolean containsHeader(String key)
  {
    for(String s : this.header){
      if(key.equals(key)){
        return true;
      }
    }
    return false;
  }

}
//Todo, make this whole proccess more effecient (which it can be made more effecient)
