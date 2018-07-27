package westland.signature.automator;

import java.util.List;

public class OfficeSpaceEmployee
{
  private long id;
  private String email;
  private String seated;
  private List<Long> seatIDs;

  public OfficeSpaceEmployee(long id, String email, String seated, List<Long> seatIDs)
  {
    this.id = id;
    this.email = email;
    this.seated = seated;
    this.seatIDs = seatIDs;
  }
  public List<Long> getSeats()
  {
    return seatIDs;
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
    for(Long i : seatIDs){
      sb.append('\n');
      sb.append("seat id: "+i);
    }
    return sb.toString();
  }
}
