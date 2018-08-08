package westland.signature.automator;

import java.util.*;

public class Report
{
  private String user;
  private Map<String,Change> changes;

  public Report(String user)
  {
    this.user = user;
    changes = new TreeMap<>();
  }

  public void addChange(String type, String oldData, String newData)
  {
    if(oldData == null){
      oldData = "";
    }
    if(newData == null){
      newData = "";
    }
    if(!oldData.equals(newData)){
      changes.put(type, new Change(type, oldData, newData));
    }
  }
  public void addChange(String type, String oldData, String newData, String message)
  {
    if(oldData == null){
      oldData = "";
    }
    if(newData == null){
      newData = "";
    }
    if(!newData.equals(oldData)){
      changes.put(type, new Change(type, oldData, newData, message));
    }
  }

  public String getName()
  {
    return user;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("email: ");
    sb.append(getName());
    for(Change c : changes.values()){
      sb.append("\n");
      sb.append(c.toString());
    }
    return sb.toString();
  }
  private class Change
  {
    private String type;
    private String oldData;
    private String newData;
    private String message;
    public Change(String type, String oldData, String newData){
      this.type = type;
      this.oldData = oldData;
      this.newData = newData;
    }
    public Change(String type, String oldData, String newData, String message){
      this.type = type;
      this.oldData = oldData;
      this.newData = newData;
      this.message = message;
    }
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      sb.append(type);
      sb.append(": ");
      sb.append(oldData);
      sb.append(" -> ");
      sb.append(newData);
      if(message!=null){
        sb.append('(');
        sb.append(message);
        sb.append(')');
      }
      return sb.toString();
    }
  }
}
