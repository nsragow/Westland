package westland.signature.automator;

import java.util.*;

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
  public void addFloor(OfficeSpaceFloor floor)
  {
    floors.put(floor.getId(),floor);
  }
  public long getId()
  {
    return id;
  }
  public OfficeSpaceFloor get(long id)
  {
    return floors.get(id);
  }
}
