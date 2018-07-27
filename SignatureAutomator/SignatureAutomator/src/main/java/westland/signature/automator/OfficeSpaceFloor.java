package westland.signature.automator;

import java.util.List;

public class OfficeSpaceFloor
{
  private long id;
  private String label;
  private List<Long> directories;

  public OfficeSpaceFloor(long id, String label, List<Long> directories)
  {
    this.id = id;
    this.label = label;
    this.directories = directories;
  }
}
