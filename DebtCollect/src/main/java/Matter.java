public class Matter
{
  private String id;
  private String shortDesc;
  private String areaOfLaw;
  private String desc;
  private String status;


  public String toXML(int amountOfTabs)
  {
    StringBuilder b = new StringBuilder();
    tabAppender(b,amountOfTabs);
    b.append("<matter>");
    tabAppender(b,amountOfTabs);
    b.append("\t<id>");
    b.append(getID());
    b.append("</id>");
    tabAppender(b,amountOfTabs);
    b.append("\t<desc>");
    b.append(getDesc());
    b.append("</desc>");
    tabAppender(b,amountOfTabs);
    b.append("\t<area of law>");
    b.append(getAreaOfLaw());
    b.append("</area of law>");
    tabAppender(b,amountOfTabs);
    b.append("</matter>\r\n");
    return b.toString();
  }
  private void tabAppender(StringBuilder b, int amountOfTabs)
  {
    b.append("\r\n");
    for(int i = 0; i < amountOfTabs; i++){
      b.append('\t');
    }
  }
  public static Builder getBuilder()
  {
    return new Builder();
  }
  public String getID()
  {
    return id;
  }
  public String getShortDesc()
  {
    return shortDesc;
  }
  public String getAreaOfLaw()
  {
    return areaOfLaw;
  }
  public String getDesc()
  {
    return desc;
  }
  public String getStatus()
  {
    return status;
  }

  public static class Builder
  {
    private String id;
    private String shortDesc;
    private String areaOfLaw;
    private String desc;
    private String status;
    private int completeCount = 5;
    private Builder()
    {

    }
    public Matter build()
    {
      if(completeCount == 0){
        Matter c = new Matter();
        c.id = this.id;
        c.shortDesc = this.shortDesc;
        c.areaOfLaw = this.areaOfLaw;
        c.desc = this.desc;
        c.status = this.status;
        completeCount--;
        return c;
      }else{
        return null;
      }
    }
    public Builder setID(String s)
    {
      if(id == null){
        id = s;
        completeCount--;
      }
      return this;
    }
    public Builder setShortDesc(String s)
    {
      if(shortDesc == null){
        shortDesc = s;
        completeCount--;
      }
      return this;
    }
    public Builder setAreaOfLaw(String s)
    {
      if(areaOfLaw == null){
        areaOfLaw = s;
        completeCount--;
      }
      return this;
    }
    public Builder setDesc(String s)
    {
      if(desc == null){
        desc = s;
        completeCount--;
      }
      return this;
    }
    public Builder setStatus(String s)
    {
      if(status == null){
        status = s;
        completeCount--;
      }
      return this;
    }

  }

}
