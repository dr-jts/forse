package forse.lineseg;

import java.util.*;

public class OrderValidatingSegmentSink
implements SegmentSink
{
  private boolean isOutputEnabled = false;
  private LineSeg lastSeg = null;
  
  public OrderValidatingSegmentSink()
  {
  }
  
  public OrderValidatingSegmentSink(boolean isOutputEnabled)
  {
    this.isOutputEnabled = isOutputEnabled;
  }
  
  private static Comparator orientComp = new LineSeg.OrientationComparator();
  
  public void process(LineSeg seg)
  {
  	if (isOutputEnabled)
      System.out.println(seg);
    
    if (lastSeg == null) {
      lastSeg = seg;
      return;
    }
    
    if (seg.compareTo(lastSeg) < 0) {
      throw new IllegalStateException("Segments are out of canonical order"
          + " - (last seg = " + lastSeg
          + ", current seg = "+ seg);
    }
    
    if (orientComp.compare(seg, lastSeg) < 0) {
      throw new IllegalStateException("Segments are out of orientation order"
          + " - (last seg = " + lastSeg
          + ", current seg = "+ seg);
    }
    
    lastSeg = seg;
  }
  
  public void close()
  {
  	// do nothing!
  }
}
