package westland.ntfs.uploader;

import java.util.*;

public class NTFS
{
  protected final int INDEX_OF_BYTES_PER_SECTOR = 11;
  protected int BYTES_PER_SECTOR;
  protected final int INDEX_OF_SECTORS_PER_CLUSTER = 13;
  protected int SECTORS_PER_CLUSTER;
  protected final int INDEX_OF_RESERVED_SECTORS = 14;
  protected int RESERVED_SECTORS;
  protected final int INDEX_OF_ALWAYS_ZERO = 16;
  protected int ALWAYS_ZERO;
  protected final int INDEX_OF_NOT_USED = 19;
  protected final int INDEX_OF_MEDIA_DESCRIPTOR = 21;
  protected int MEDIA_DESCRIPTOR;
  protected final int INDEX_OF_SECOND_ALWAYS_ZERO = 22;
  protected int SECOND_ALWAYS_ZERO;
  protected final int INDEX_OF_SECTORS_PER_TRACK = 24;
  protected int SECTORS_PER_TRACK;
  protected final int INDEX_OF_NUMBER_OF_HEADS = 26;
  protected int NUMBER_OF_HEADS;
  protected final int INDEX_OF_HIDDEN_SECTORS = 28;
  protected int HIDDEN_SECTORS;
  protected final int INDEX_OF_SECOND_NOT_USED = 32;
  protected final int INDEX_OF_THIRD_NOT_USED = 36;
  protected final int INDEX_OF_TOTAL_SECTORS = 40;
  protected long TOTAL_SECTORS;
  protected final int INDEX_OF_MFT_CLUSTER = 48;
  protected long MFT_CLUSTER;
  protected final int INDEX_OF_MFT_MIRROR_CLUSTER = 56;
  protected long MFT_MIRROR_CLUSTER;
  protected final int INDEX_OF_CLUSTERS_PER_FILE_RECORD_SEGMENT = 64;
  protected int CLUSTERS_PER_FILE_RECORD_SEGMENT;
  protected final int INDEX_OF_CLUSTERS_PER_INDEX_BUFFER = 68;
  protected int CLUSTERS_PER_INDEX_BUFFER;
  protected final int INDEX_OF_FOURTH_NOT_USED = 69;
  protected final int INDEX_OF_VOLUME_SERIAL_NUMBER = 72;
  protected long VOLUME_SERIAL_NUMBER;
  protected final int INDEX_OF_CHECKSUM = 80;
  protected int CHECKSUM;

  private HashSet<MFTEntry> entries;

  public NTFS(byte[] cluster)
  {
    BYTES_PER_SECTOR = Helper.bytesToInt(cluster, INDEX_OF_BYTES_PER_SECTOR, 2);
    SECTORS_PER_CLUSTER = Helper.bytesToInt(cluster, INDEX_OF_SECTORS_PER_CLUSTER, 1);
    RESERVED_SECTORS = Helper.bytesToInt(cluster, INDEX_OF_RESERVED_SECTORS, 2);
    ALWAYS_ZERO = Helper.bytesToInt(cluster, INDEX_OF_ALWAYS_ZERO, 3);
    MEDIA_DESCRIPTOR = Helper.bytesToInt(cluster, INDEX_OF_MEDIA_DESCRIPTOR, 1);
    SECOND_ALWAYS_ZERO = Helper.bytesToInt(cluster, INDEX_OF_SECOND_ALWAYS_ZERO, 2);
    SECTORS_PER_TRACK = Helper.bytesToInt(cluster, INDEX_OF_SECTORS_PER_TRACK, 2);
    NUMBER_OF_HEADS = Helper.bytesToInt(cluster, INDEX_OF_NUMBER_OF_HEADS, 2);
    HIDDEN_SECTORS = Helper.bytesToInt(cluster, INDEX_OF_HIDDEN_SECTORS, 4);
    TOTAL_SECTORS = Helper.bytesToLong(cluster, INDEX_OF_TOTAL_SECTORS, 8);
    MFT_CLUSTER = Helper.bytesToLong(cluster, INDEX_OF_MFT_CLUSTER, 8);
    MFT_MIRROR_CLUSTER = Helper.bytesToLong(cluster, INDEX_OF_MFT_MIRROR_CLUSTER, 8);
    CLUSTERS_PER_FILE_RECORD_SEGMENT = Helper.bytesToInt(cluster, INDEX_OF_CLUSTERS_PER_FILE_RECORD_SEGMENT, 4);
    CLUSTERS_PER_INDEX_BUFFER = Helper.bytesToInt(cluster, INDEX_OF_CLUSTERS_PER_INDEX_BUFFER, 1);
    VOLUME_SERIAL_NUMBER = Helper.bytesToLong(cluster, INDEX_OF_VOLUME_SERIAL_NUMBER, 8);
    CHECKSUM = Helper.bytesToInt(cluster, INDEX_OF_CHECKSUM, 4);

    entries = new HashSet<>();
  }
  public void addEntry(MFTEntry entry)
  {
    entries.add(entry);
  }
  public Set<MFTEntry> getEntries()
  {
    return entries;
  }
  public long relativeByteOfMFT()
  {
    return MFT_CLUSTER * (long) BYTES_PER_SECTOR * (long) SECTORS_PER_CLUSTER;
  }
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("BYTES_PER_SECTOR: "+ this.BYTES_PER_SECTOR);
    sb.append("\n");
    sb.append("SECTORS_PER_CLUSTER: "+ this.SECTORS_PER_CLUSTER);
    sb.append("\n");
    sb.append("RESERVED_SECTORS: "+ this.RESERVED_SECTORS);
    sb.append("\n");
    sb.append("ALWAYS_ZERO: "+ this.ALWAYS_ZERO);
    sb.append("\n");
    sb.append("MEDIA_DESCRIPTOR: "+ this.MEDIA_DESCRIPTOR);
    sb.append("\n");
    sb.append("SECOND_ALWAYS_ZERO: "+ this.SECOND_ALWAYS_ZERO);
    sb.append("\n");
    sb.append("SECTORS_PER_TRACK: "+ this.SECTORS_PER_TRACK);
    sb.append("\n");
    sb.append("NUMBER_OF_HEADS: "+ this.NUMBER_OF_HEADS);
    sb.append("\n");
    sb.append("HIDDEN_SECTORS: "+ this.HIDDEN_SECTORS);
    sb.append("\n");
    sb.append("TOTAL_SECTORS: "+ this.TOTAL_SECTORS);
    sb.append("\n");
    sb.append("MFT_CLUSTER: "+ this.MFT_CLUSTER);
    sb.append("\n");
    sb.append("MFT_MIRROR_CLUSTER: "+ this.MFT_MIRROR_CLUSTER);
    sb.append("\n");
    sb.append("CLUSTERS_PER_FILE_RECORD_SEGMENT: "+ this.CLUSTERS_PER_FILE_RECORD_SEGMENT);
    sb.append("\n");
    sb.append("CLUSTERS_PER_INDEX_BUFFER: "+ this.CLUSTERS_PER_INDEX_BUFFER);
    sb.append("\n");
    sb.append("VOLUME_SERIAL_NUMBER: "+ this.VOLUME_SERIAL_NUMBER);
    sb.append("\n");
    sb.append("CHECKSUM: "+ this.CHECKSUM);
    return sb.toString();
  }


}
