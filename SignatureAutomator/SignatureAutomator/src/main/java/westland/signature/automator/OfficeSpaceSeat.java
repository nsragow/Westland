package westland.signature.automator;

public class OfficeSpaceSeat
{
  private long id;
  private String label;

  public OfficeSpaceSeat(long id, String label)
  {
    this.id = id;
    this.label = label;
  }
  public long getId()
  {
    return id;
  }
  public String getLabel()
  {
    return label;
  }
}
