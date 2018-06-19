import java.util.*;
public class Collection implements Comparable<Collection>
{
    private Set<Contact> contacts;
    private Matter matter;
    private Set<CollectionDetails> details;
    private String id;
    private String contactID;
    private String matterID;


    public String toXML()
    {
      StringBuilder b = new StringBuilder();
      b.append("<Collection>\r\n");
      b.append("\t<id>");
      b.append(getID());
      b.append("</id>\r\n");
      b.append("\t<contactID>");
      b.append(getContactID());
      b.append("</contactID>\r\n");
      b.append("\t<contacts>\r\n");
      for(Contact c : getContacts()){
        b.append(c.toXML(2));
      }
      b.append("\r\n\t</contacts>\r\n");
      b.append("\t<details>\r\n");
      for(CollectionDetails d : getDetails()){
        b.append(d.toXML(2));
      }
      b.append("\r\n\t</details>");
      b.append("\r\n</Collection>\r\n");
      return b.toString();
    }

    public Collection(String id, String contactID, String matterID)
    {
      this.id = id;
      this.contactID = contactID;
      this.matterID = matterID;
      contacts = new HashSet<>();
      details = new HashSet<>();

    }
    public void addContact(Contact c)
    {
      contacts.add(c);
    }
    public void setMatter(Matter m)
    {
      if(matter == null){
        matter = m;

      }else{
        throw new RuntimeException("Matter Already Set");
      }

    }
    public double totalCollectionValue()
    {
      double toReturn = 0d;
      for(CollectionDetails cd : getDetails()){
        if(cd.getAmount()!=null && !cd.getAmount().toLowerCase().equals("null")){
          toReturn += Double.parseDouble(cd.getAmount());
        }
      }
      return toReturn;
    }
    public void addDetails(CollectionDetails c)
    {
      details.add(c);
    }
    public Set<Contact> getContacts()
    {
      return contacts;
    }
    public Set<CollectionDetails> getDetails()
    {
      return details;
    }
    public String getID()
    {
      return id;
    }
    public String getContactID()
    {
      return contactID;
    }
    public Matter getMatter()
    {
      return matter;
    }
    public String getMatterID()
    {
      return matterID;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof Collection)){
          return false;
        }
        Collection c = (Collection) o;
        return c.getID().equals(this.getID()) && c.getContactID().equals(this.getContactID()) && c.totalCollectionValue() == this.totalCollectionValue();
    }

    public int hashCode()
    {
        return 31*getID().hashCode() + getContactID().hashCode() + 31*new Double(totalCollectionValue()).hashCode();
    }



    public int compareTo(Collection c)
    {
      Double d1 = new Double(this.totalCollectionValue());
      Double d2 = new Double(c.totalCollectionValue());
        return d1.compareTo(d2);
    }

}
