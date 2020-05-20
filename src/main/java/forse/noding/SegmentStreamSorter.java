package forse.noding;

import java.util.*;
import forse.*;
import forse.lineseg.LineSeg;
import forse.lineseg.NullSegmentSink;
import forse.lineseg.SegmentProcess;
import forse.lineseg.SegmentSink;
import forse.lineseg.SegmentSource;
import forse.lineseg.TopologyLabel;

/**
 * Sorts a stream of segments according to their orientation 
 * around their origin node.
 * {@link LineSeg.OrientationComparator} is used 
 * as the order function.
 * Segments are accumulated from the input stream. 
 * To limit the memory footprint, 
 * the sorter emits segments as soon
 * as they can be guaranteed to be ordered over
 * the total stream. 
 * Duplicate segments are optionally merged into a single output segment.
 * 
 * @author Martin Davis
 *
 */
public class SegmentStreamSorter 
implements SegmentProcess
{
  public static final int sortFrequency = 10000;
  
  private SweepLineFront sweepFront;
  
//  private SweepLineStatus sweepStatus;
  private SegmentSink segSink = new NullSegmentSink();
  private ExtendedArrayList segmentBuffer = new ExtendedArrayList();
  private int inputCount = 0;
  private static Comparator ORIENT_COMPARATOR = new LineSeg.OrientationComparator();
  private static boolean removeDuplicates = true;
  private boolean removeInteriorSegs = false;
    
  public SegmentStreamSorter(SweepLineFront sweepFront) 
  {
    this.sweepFront = sweepFront;
  }

  public void setSink(SegmentSink segSink)
  {
    this.segSink = segSink;
  }
  
  public void process(LineSeg seg)
  {
    segmentBuffer.add(seg);
    inputCount++;
    if (inputCount >= sortFrequency) {
      double maxX = sweepFront.minimumActiveX();
      sortAndEmitUpTo(maxX);
      inputCount = 0;
    }
  }
  
  /**
   * Gets the max x value of the first segment in the buffer, if any
   * 
   * @return the max x value of the first segment in the buffer
   */
  private double bufferHeadMaxX()
  {
    if (segmentBuffer.size() <= 0)
      return Double.NaN;
    LineSeg seg = (LineSeg) segmentBuffer.get(0);
    return seg.getMaxX();
  }
  
  /**
   * Sorts and emits all segments with x-ordinates
   * strictly less than a given value.
   * 
   * @param maxX
   */
  private void sortAndEmitUpTo(double maxX)
  {
    /**
     * Heuristic: Check if there are no obvious segments to process
     * and skip sorting & processing if so
     * 
     * This is a heuristic only.
     * There may be new segments at the end of the buffer
     * which are less than maxx.  But this should be rare,
     * and they will all get processed eventually.
     */
    if (bufferHeadMaxX() >= maxX)
      return;
    
    // this often sorts more than are going to be processed.
    //TODO: Can this be done faster?  eg priority queue?
    
    // sort segs in buffer, since they arrived in potentially unsorted order
    Collections.sort(segmentBuffer, ORIENT_COMPARATOR);
    int i = 0;
    int n = segmentBuffer.size();
    LineSeg prevSeg = null;
    
    //reportProcessed(maxX);
    
    while (i < n) {
      LineSeg seg = (LineSeg) segmentBuffer.get(i);
      if (seg.getMaxX() >= maxX)
      	break;
      
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
    removeSegsFromBuffer(i);
  }
  
  private void reportProcessed(double maxX)
  {
    int n = segmentBuffer.size();
    // compute n processed
    int nProcessed = n;
    for (int i = 0; i < n; i ++) {
      LineSeg seg = (LineSeg) segmentBuffer.get(i);
      if (seg.getMaxX() >= maxX) {
        nProcessed = i;
        break;
      }
    }
    System.out.println("Total: " + n + ", n to process: " + nProcessed);
  }
  
  private void removeSegsFromBuffer(int upToIndex)
  {
    int n = segmentBuffer.size();
    // remove emitted segs from buffer
    if (upToIndex < n) {
      if (upToIndex > 0)
        segmentBuffer.removeRange(0, upToIndex);
    }
    else {
      // all segs in array have been processed
      segmentBuffer.clear();
    }

  }
  
  public void close()
  {
    sortAndEmitUpTo(Double.MAX_VALUE);
    segSink.close();
  }

}
