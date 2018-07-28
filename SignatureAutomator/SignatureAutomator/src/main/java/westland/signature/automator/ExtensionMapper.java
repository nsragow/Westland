package westland.signature.automator;


public class ExtensionMapper
{
  private Table extTable;

  public ExtensionMapper()
  {
    extTable = Initializer.getTable(Strings.ext_to_orgunit);

  }

  public String getOrg(int extension)
  {
    return extTable.get(extension+"","org");
  }
}
