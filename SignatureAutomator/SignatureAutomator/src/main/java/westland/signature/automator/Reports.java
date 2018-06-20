package westland.signature.automator;

import java.text.*;
import java.util.*;
public class Reports
{
  private Set<String> errors;
  private Set<String> logs;
  //fatal or just error

  public Reports()
  {
    errors=new HashSet<>();
    logs=new HashSet<>();
  }
  public void err(String toSendAsError)
  {
    errors.add(formatAsErrorMessage(toSendAsError));
  }
  private String formatAsErrorMessage(String toFormat)
  {
    StringBuilder log = new StringBuilder();

    DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    Date dateobj = new Date();
    log.append(df.format(dateobj));
    log.append(":\n\t");
    log.append(toFormat);
    log.append("\n");
    return log.toString();
  }

  public void log(String toLog)
  {


    logs.add(formatAsErrorMessage(toLog));


  }

//sendErrorReport + what to do with error
  public String getReport()
  {
    StringBuilder toSend = new StringBuilder();
    for(String s : logs){
      toSend.append("LOG: "+s);
    }
    for(String s : errors){
      toSend.append("ERROR: "+s);
    }
    if(errors.isEmpty()){
      toSend.append("\nNo errors today!\n");
    }
    return toSend.toString();

  }

}
