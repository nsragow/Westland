package westland.signature.automator;

import java.util.List;

public class OfficeSpaceDirectory
{
  private long id;
  private String name;
  private Map<Long,OfficeSpaceFloor> floors;

  public OfficeSpaceDirectory(long id, String name)
  {
    this.id = id;
    floors = new HashMap<>();
    this.name = name;
  }
}
