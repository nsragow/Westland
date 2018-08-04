package westland.signature.automator;

import java.util.List;

public class OfficeSpaceEmployee
{
  private long id;
  private String email;
  private String seated;
  private String firstName;
  private String lastName;
  private OfficeSpaceSeat seat;
  private String title;

  public OfficeSpaceEmployee(long id, String email, String seated, OfficeSpaceSeat seat, String firstName, String lastName, String title)
  {
    this.firstName = firstName;
    this.lastName = lastName;
    this.id = id;
    this.email = email;
    this.seated = seated;
    this.seat = seat;
    this.title = title;
    if(title == null){
      title = "";
    }
  }
  public String getFirstName()
  {
    return firstName;
  }
  public String getTitle()
  {
    return title;
  }
  public long getId()
  {
    return id;
  }
  public String getLastName()
  {
    return lastName;
  }
  public OfficeSpaceSeat getSeat()
  {
    return seat;
  }
  public String getEmail()
  {
    return email;
  }
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("email: "+email);
    sb.append('\n');
    sb.append("id: "+ id);
    sb.append('\n');
    sb.append("seated "+seated);
    sb.append('\n');
    sb.append(seat.toString());
    return sb.toString();
  }
}
