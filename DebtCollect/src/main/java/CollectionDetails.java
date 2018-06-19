public class CollectionDetails
{
  private String id;
  private String amount;
  private String collID;
  private String desc;
  private String type;


  public String toXML(int amountOfTabs)
  {
    StringBuilder b = new StringBuilder();
    tabAppender(b,amountOfTabs);
    b.append("<detail>");
    tabAppender(b,amountOfTabs);
    b.append("\t<id>");
    b.append(getID());
    b.append("</id>");
    tabAppender(b,amountOfTabs);
    b.append("\t<amount>");
    b.append(getAmount());
    b.append("</amount>");
    tabAppender(b,amountOfTabs);
    b.append("\t<collID>");
    b.append(getCollectionID());
    b.append("</collID>");
    tabAppender(b,amountOfTabs);
    b.append("\t<desc>");
    b.append(getDescription());
    b.append("</desc>");
    tabAppender(b,amountOfTabs);
    b.append("\t<type>");
    b.append(getType());
    b.append("</type>");
    tabAppender(b,amountOfTabs);
    b.append("</detail>\r\n");
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
  public String getAmount()
  {
    return amount;
  }
  public String getCollectionID()
  {
    return collID;
  }
  public String getDescription()
  {
    return desc;
  }
  public String getType()
  {
    return type;
  }

  public static class Builder
  {
    private String id;
    private String amount;
    private String collID;
    private String desc;
    private String type;
    private int completeCount = 5;
    private Builder()
    {

    }
    public CollectionDetails build()
    {
      if(completeCount == 0){
        CollectionDetails c = new CollectionDetails();
        c.id = this.id;
        c.amount = this.amount;
        c.collID = this.collID;
        c.desc = this.desc;
        c.type = this.type;
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
    public Builder setAmount(String s)
    {
      if(amount == null){
        amount = s;
        completeCount--;
      }
      return this;
    }
    public Builder setCollectionID(String s)
    {
      if(collID == null){
        collID = s;
        completeCount--;
      }
      return this;
    }
    public Builder setDescription(String s)
    {
      if(desc == null){
        desc = s;
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

  }

}
