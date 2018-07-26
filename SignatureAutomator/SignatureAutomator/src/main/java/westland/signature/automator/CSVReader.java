package westland.signature.automator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class CSVReader
{//todo, the errors on this are not well handled
  private CSVParser parser;
  private Table table;
  protected CSVReader(String fileName)
  {
    try{
      parser = CSVFormat.EXCEL.parse(new BufferedReader(new FileReader(fileName)));

      List<CSVRecord> records = parser.getRecords();
      table = new Table(recordToArray(records.get(0)));
      for(int i = 1; i < records.size(); i++){
        table.addRow(recordToArray(records.get(i)));
      }
      this.table = table;

    }catch(Exception e){
      //do something
      System.out.println("hit exception");
      e.printStackTrace();
    }
  }
  public Table getTable()
  {
    return table;
  }

  protected static String[] recordToArray(CSVRecord rec)
  {
    String[] toReturn = new String[rec.size()];
    for(int i = 0; i < rec.size(); i++){
      toReturn[i] = rec.get(i);
    }
    return toReturn;
  }
}
