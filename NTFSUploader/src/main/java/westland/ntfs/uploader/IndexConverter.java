package westland.ntfs.uploader;

import java.nio.*;
public class IndexConverter
{
  long[] addThisMuch;
  long[] ifLessThanThis;
  long[] lengths;
  long[] subtractThisMuch;
  MappedByteBuffer[] buffers;
  long length;
  long next;
  public IndexConverter(long[] locAndLen)
  {
    addThisMuch = new long[locAndLen.length/2];
    ifLessThanThis = new long[locAndLen.length/2];
    subtractThisMuch = new long[locAndLen.length/2];
    lengths = new long[locAndLen.length/2];
    long currentIndex = 0;
    for(int i = 0; i < addThisMuch.length; i++){
      addThisMuch[i] = locAndLen[i*2];
      subtractThisMuch[i] = currentIndex;
      currentIndex += locAndLen[i*2+1];
      lengths[i] = locAndLen[i*2+1];
      ifLessThanThis[i] = currentIndex;
    }
    length = ifLessThanThis[ifLessThanThis.length-1];
    next = 0;
  }
  public long sizeInLong()
  {
    return length;
  }
  public long convert(long index)
  {
    for(int i = 0; i < addThisMuch.length; i++){
      if(index < ifLessThanThis[i]){
        return index + addThisMuch[i];
      }
    }
    throw new IndexOutOfBoundsException("tried to read past end " + index);
  }
  public void map()
  {
    if(buffers != null) return;
    buffers = new MappedByteBuffer[addThisMuch.length];
    for(int i = 0; i < addThisMuch.length; i++){
      buffers[i] = VHDReader.map(addThisMuch[i],lengths[i]);
    }
  }
  public byte get(long index)
  {
    int i = 0;
    for(; true; i++){
      if(index < ifLessThanThis[i]){
        return buffers[i].get((int) (index-subtractThisMuch[i]));

      }
    }

  }
  public boolean has(long index)
  {
    return index<sizeInLong();
  }
  public boolean hasNext()
  {
    return next<sizeInLong();
  }
  public byte next()
  {
    return get(next++);
  }
}
