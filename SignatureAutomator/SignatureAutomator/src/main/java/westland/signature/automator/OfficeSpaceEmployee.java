package westland.signature.automator;

import java.util.List;

public class OfficeSpaceEmployee
{
  private long id;
  private String email;
  private String seated;
  private OfficeSpaceSeat seat;

  public OfficeSpaceEmployee(long id, String email, String seated, OfficeSpaceSeat seat)
  {
    this.id = id;
    this.email = email;
    this.seated = seated;
    this.seat = seat;
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
