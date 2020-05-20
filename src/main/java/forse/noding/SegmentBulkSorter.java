package forse.noding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import forse.lineseg.LineSeg;
import forse.lineseg.NullSegmentSink;
import forse.lineseg.SegmentProcess;
import forse.lineseg.SegmentSink;
import forse.lineseg.TopologyLabel;

/**
 * Sorts an in-memory stream of segments according to their orientation 
 * around their origin node.
 * {@link LineSeg.OrientationComparator} is used 
 * as the order function.
 * Segments are accumulated
 * and then processed in a single operation, 
 * then passed out to the sink. 
 * Duplicate segments are optionally merged into a single output segment.
 * <p>
 * MD - 3/2013 - Testing seems to indicate that this is no faster than stream sorting!
 * Perhaps because sorting a large array is slower than sorting 
 * several smaller almost-sorted ones?
 * So not used after all.
 * 
 * @author Martin Davis
 *
 */
public class SegmentBulkSorter 
implements SegmentProcess
{
  
  private SegmentSink segSink = new NullSegmentSink();
  private ArrayList segmentBuffer = new ArrayList();
  private int inputCount = 0;
  private static Comparator ORIENT_COMPARATOR = new LineSeg.OrientationComparator();
  private static boolean removeDuplicates = true;
  private boolean removeInteriorSegs = false;
    
  public SegmentBulkSorter() 
  {
  }

  public void setSink(SegmentSink segSink)
  {
    this.segSink = segSink;
  }
  
  public void process(LineSeg seg)
  {
    segmentBuffer.add(seg);
  }
  
  
  /**
   * Sorts and emits all segments with x-ordinates
   * strictly less than a given value.
   * 
   * @param maxX
   */
  private void sortAndEmit()
  {
    
    // sort segs in buffer, since they arrived in potentially unsorted order
    Collections.sort(segmentBuffer, ORIENT_COMPARATOR);
    
    int i = 0;
    int n = segmentBuffer.size();
    LineSeg prevSeg = null;

    //reportProcessed(maxX);
    
    while (i < n) {
      LineSeg seg = (LineSeg) segmentBuffer.get(i);      
      i++;
      if (removeDuplicates) {
        if (prevSeg != null) {
          // skip this segment if it is a duplicate
          if (prevSeg.equals(seg)) {
            // merge in side label of dup seg
            prevSeg.merge(seg);
            continue;
          }
        }
        prevSeg = seg;
      }
      // remove interior segments (for dissolving)
      if (removeInteriorSegs
          && seg.getTopoLabel() == TopologyLabel.BOTH_INTERIOR)
        continue;
      
      segSink.process(seg);
    }
    //System.out.println("SegmentStreamSorter: total segs = " + n + ", processed = " + i);
  }
    
  public void close()
  {
    sortAndEmit();
    segSink.close();
  }

}
