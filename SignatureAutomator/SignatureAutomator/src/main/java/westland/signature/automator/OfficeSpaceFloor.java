package westland.signature.automator;

import java.util.*;

public class OfficeSpaceFloor
{
  private long id;
  private String label;
  private Map<Long,OfficeSpaceSeat> seatMap;

  public OfficeSpaceFloor(long id, String label)
  {
    seatMap = new HashMap<>();
    this.id = id;
    this.label = label;
  }
  public long getId()
  {
    return id;
  }
  public void addSeat(OfficeSpaceSeat seat)
  {
    seatMap.put(seat.getId(),seat);
  }
}
