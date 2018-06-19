public class Contact
{
  private String firstName;
  private String lastName;
  private String middleName;
  private String ssn;
  private String id;
  private String type;
  private String suffix;

  private Contact()
  {

  }
  public String toXML(int amountOfTabs)
  {
    StringBuilder b = new StringBuilder();
    tabAppender(b,amountOfTabs);
    b.append("<contact>");
    tabAppender(b,amountOfTabs);
    b.append("\t<firstName>");
    b.append(getFirstName());
    b.append("</firstName>");
    tabAppender(b,amountOfTabs);
    b.append("\t<lastName>");
    b.append(getLastName());
    b.append("</lastName>");
    tabAppender(b,amountOfTabs);
    b.append("\t<middleName>");
    b.append(getMiddleName());
    b.append("</middleName>");
    tabAppender(b,amountOfTabs);
    b.append("\t<ssn>");
    b.append(getSSN());
    b.append("</ssn>");
    tabAppender(b,amountOfTabs);
    b.append("\t<id>");
    b.append(getID());
    b.append("</id>");
    tabAppender(b,amountOfTabs);
    b.append("\t<type>");
    b.append(getType());
    b.append("</type>");
    tabAppender(b,amountOfTabs);
    b.append("\t<suffix>");
    b.append(getSuffix());
    b.append("</suffix>");
    tabAppender(b,amountOfTabs);
    b.append("</contact>\r\n");
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
  public String getFirstName()
  {
    return firstName;
  }
  public String getLastName()
  {
    return lastName;
  }
  public String getMiddleName()
  {
    return middleName;
  }
  public String getSSN()
  {
    return ssn;
  }
  public String getID()
  {
    return id;
  }
  public String getType()
  {
    return type;
  }
  public String getSuffix()
  {
    return suffix;
  }
  public static class Builder
  {
    private String firstName;
    private String lastName;
    private String middleName;
    private String ssn;
    private String id;
    private String type;
    private String suffix;
    private int completeCount = 7;
    private Builder()
    {

    }
    public Contact build()
    {
      if(completeCount == 0){
        Contact c = new Contact();
        c.firstName = this.firstName;
        c.lastName = this.lastName;
        c.middleName = this.middleName;
        c.ssn = this.ssn;
        c.id = this.id;
        c.type = this.type;
        c.suffix = this.suffix;
        completeCount--;
        return c;
      }else{
        return null;
      }
    }
    public Builder setFirstName(String s)
    {
      if(firstName == null){
        firstName = s;
        completeCount--;
      }
      return this;
    }
    public Builder setLastName(String s)
    {
      if(lastName == null){
        lastName = s;
        completeCount--;
      }
      return this;
    }
    public Builder setMiddleName(String s)
    {
      if(middleName == null){
        middleName = s;
        completeCount--;
      }
      return this;
    }
    public Builder setSSN(String s)
    {
      if(ssn == null){
        ssn = s;
        completeCount--;
      }
      return this;
    }
    public Builder setID(String s)
    {
      if(id == null){
        id = s;
        completeCount--;
      }
      return this;
    }
    public Builder setType(String s)
    {
      if(type == null){
        type = s;
        completeCount--;
      }
      return this;
    }
    public Builder setSuffix(String s)
    {
      if(suffix == null){
        suffix = s;
        completeCount--;
      }
      return this;
    }
  }

}
