package westland.signature.automator;

public class OfficeSpaceSeat
{
  private long id;
  private String label;
  private boolean occupied;
  private long employeeID;
  private long floorId;

  public OfficeSpaceSeat(long id, String label, boolean occupied, long employeeURL, long floorId)
  {
    this.id = id;
    this.label = label;
    this.occupied = occupied;
    this.employeeID = employeeURL;
    this.floorId = floorId;
  }
}
