package forse.noding;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import forse.lineseg.LineSeg;
import forse.lineseg.SegmentProcess;
import forse.lineseg.SegmentSink;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Handles both standard noding and snap-rounding noding.
 * 
 * @author Martin Davis
 *
 */
public class SweepLineNoder 
implements SegmentProcess 
{
  private SimpleEventQueue eventQueue = new SimpleEventQueue();
  private SimpleEventQueue intEventQueue = new SimpleEventQueue(new Event.LocSegComparator());
  //private SimpleSweepLineStatus sweepStatus = new SimpleSweepLineStatus();
  private IndexedSweepLineStatus sweepStatus = new IndexedSweepLineStatus();
  private SegmentProcess segSorter;
  private PrecisionModel precisionModel = null;
  private boolean isSnapRounding = false;
  private LineIntersector intersector = new RobustLineIntersector();
  private double pixelWidth = 0.0;
  
  public SweepLineNoder() {
    segSorter = new SegmentStreamSorter(sweepStatus);
  }

  public SweepLineNoder(boolean isBulkSort) {
    if (isBulkSort) {
      segSorter = new SegmentBulkSorter();
    }
    else {
      segSorter = new SegmentStreamSorter(sweepStatus);
    }
  }

  public SweepLineNoder(PrecisionModel precisionModel) {
    this();
    if (precisionModel != null) setPrecisionModel(precisionModel);
  }

  public SweepLineNoder(PrecisionModel precisionModel, boolean isBulkSort) {
    this(isBulkSort);
    if (precisionModel != null) setPrecisionModel(precisionModel);
  }

  /**
   * Sets the {@link PrecisionModel} to use for noding.
   * <p>
   * NOTE: the same precision model should be used for the input coordinates
   * <p>
   * The default is to use FLOATING precision.
   * 
   * @param precModel the precision model to use
   */
  public void setPrecisionModel(PrecisionModel precisionModel)
  {
    this.precisionModel = precisionModel;
    intersector.setPrecisionModel(precisionModel);
    pixelWidth = 1.0/precisionModel.getScale();
    isSnapRounding = true;
  }
  
  public void setSink(SegmentSink segSink)
  {
    segSorter.setSink(segSink);
  }
  
  public void process(LineSeg seg)
  {
  	// process events which occur before the start of this segment
  	processEventsBefore(seg.getCoordinate(0));
  	
    /**
     * Process the segment by creating 
     * Intersection events (if any)
     * and an End event for it.
     */ 
  	NodedLineSeg nodedSeg = new NodedLineSeg(seg);
    // compute and add any intersection events found
  	if (isSnapRounding)
      addSnapRoundIntersections(nodedSeg);
  	else
  	  addIntersections(nodedSeg);
    // add segment end event
    addEnd(nodedSeg);

    // add the segment to the status so it can be intersected with subsequent segs
  	sweepStatus.insert(nodedSeg);
  }
  
  private void addEnd(NodedLineSeg nodedSeg)
  {
    eventQueue.add(new SegEndEvent(nodedSeg.getCoordinate(1), nodedSeg));
  }
  
  public void close()
  {
    // process any remaining events
    processEventsBefore(null);
    segSorter.close();
//  	segSink.close();
//  	System.out.println("Max entries in sweep status: " + sweepStatus.getMaximumSize());
  }
  
  private void processEventsBefore(Coordinate beforeLocation)
  {
    if (isSnapRounding) {
      processSnapRoundEventsBefore(beforeLocation);      
    }
    else {
      processFullPrecisionEventsBefore(beforeLocation);
    }
  }
   
  private void processFullPrecisionEventsBefore(Coordinate beforeLocation)
  {
    while (hasEventsBefore(beforeLocation, 0)) {
      Event ev = eventQueue.pop();
      processEvent(ev);
    }
  }

  /**
   * Processes events under snap-rounding.
   * Ordinary approach 
   * causes incorrect chopping of downward pointing segments into "backwards" subsegs.
   * 
   * E.g.  This occurs when using PM = 1 with
   * LINESTRING (13 -58, 15 -76)  and   LINESTRING (16 -76, 13 -59) 
   * 
   * Fix is to order intersection pts with same X ordinate along segment,
   * and process in a direction determined by the segment orientation.
   * (in order for upward segs, in reverse order for downward segs)
   * 
   * @param beforeLocation
   */
  private void processSnapRoundEventsBefore(Coordinate beforeLocation)
  {
    while (hasEventsBefore(beforeLocation, pixelWidth)) {
      int colSize = eventQueue.columnSize();
      //System.out.println(colCount);
//      System.out.println(ev);
      
      //---- for upward segs process intersection events in order
      for (int i = 0; i < colSize; i++) {
        Event evUp = eventQueue.peek(i);
        if (! (evUp instanceof IntersectionEvent))
          continue;
        NodedLineSeg seg = (NodedLineSeg) evUp.getSegment();
        if (! seg.isDown())
          processEvent(evUp);
      }
      //---- for downward segs process intersection events in reverse order
      for (int i = colSize - 1; i >= 0; i--) {
        Event evDown = eventQueue.peek(i);
        if (! (evDown instanceof IntersectionEvent))
          continue;
        NodedLineSeg seg = (NodedLineSeg) evDown.getSegment();
        if (seg.isDown())
          processEvent(evDown);
      }
      //---- process segend events
      for (int i = 0; i < colSize; i++) {
        Event evEnd = eventQueue.peek(i);
        if (! (evEnd instanceof SegEndEvent))
          continue;
        processEvent(evEnd);
      }
      // pop all segments in column
      eventQueue.pop(colSize);
    }
    // intersection hot pixels before curr column are no longer relevant
    if (beforeLocation != null)
      intEventQueue.popBefore(beforeLocation.x - pixelWidth);
  }
  
  private void processEvent(Event ev)
  {
    if (ev instanceof IntersectionEvent) {
      NodedLineSeg seg = (NodedLineSeg) ev.getSegment();
      /**
       * If subseg would be zero-length skip it.
       * This takes care of multiple intersections at the same point
       * on a segment
       */
      if (seg.isZeroLength(ev.getLocation()))
        return;
      
      /**
       * Chop the segment at the intersection point.
       * The leading subsegment can now be processed further.
       */
      LineSeg split = seg.chop(ev.getLocation());
      segSorter.process(split);
    }
    else if (ev instanceof SegEndEvent) {
      processSegEnd((SegEndEvent) ev);
    } 
  }
  

  private void processSegEnd(SegEndEvent ev)
  {
    NodedLineSeg seg = (NodedLineSeg) ev.getSegment();
    /**
     * The segment is done, and is not needed further
     */
    sweepStatus.remove(seg);
    
    /**
     * If subseg would be zero-length skip it.
     * This takes care of multiple intersections at the same point
     * on a segment
     */
    if (seg.isZeroLength(ev.getLocation()))
      return;
    
    /**
     * Process the last sub-segment
     */
    LineSeg split = seg.last();
    segSorter.process(split);    
  }
  
  private boolean hasEventsBefore(Coordinate queryLocation, double lagWidth)
  {
    /**
     * Order is important here - event queue contents
     * must be checked before the location value
     */
  	if (eventQueue.isEmpty()) return false;
    if (queryLocation == null) return true;
  	
  	Event ev = eventQueue.peek();
    double queryX = queryLocation.x;
    double evX = ev.getLocation().x;
    if (evX < queryX - lagWidth) 
      return true;
    return false;
  }
  
  private void addIntersections(NodedLineSeg querySeg)
  {
    // compute intersections with all candidate segments in sweep line status
    List candidates = sweepStatus.query(querySeg, pixelWidth);
    for (Iterator it = candidates.iterator(); it.hasNext(); ) {
      NodedLineSeg testSeg = (NodedLineSeg) it.next();
      // don't test against itself
      if (querySeg == testSeg) 
        continue;
      addIntersections(querySeg, testSeg);
    }
  }
  
  private void addIntersections(NodedLineSeg seg1, NodedLineSeg seg2)
  {
    // printIntersection(seg1. seg2);
    intersector.computeIntersection(
        seg1.getCoordinate(0), 
        seg1.getCoordinate(1),
        seg2.getCoordinate(0), 
        seg2.getCoordinate(1)
        );

    for (int i = 0; i < intersector.getIntersectionNum(); i++) {
      // MD - it would be nice if the LineIntersector could provide the endpoint information directly
      Coordinate intPt = intersector.getIntersection(i);
      
      /**
       * IMPORTANT: Assert - intPt is less than segment endpoints.
       * If not, perturb it so that it is?
       */
      // testing only
      /*
      checkIntersectionLessThanEnd(intPt, nodedSeg);
      checkIntersectionLessThanEnd(intPt, testSeg);
      intersector.computeIntersection(
          nodedSeg.getCoordinate(0), 
          nodedSeg.getCoordinate(1),
          testSeg.getCoordinate(0), 
          testSeg.getCoordinate(1)
          );
      */
      
      if (! seg1.isEndpoint(intPt)) {
        eventQueue.add(new IntersectionEvent(intPt, seg1));
      }
      if (! seg2.isEndpoint(intPt)) {
        eventQueue.add(new IntersectionEvent(intPt, seg2));
      }
    }
  }
  
  /**
   * Add Snap-Rounded points for any intersections of the query seg
   * 
   * @param querySeg
   */
  private void addSnapRoundIntersections(NodedLineSeg querySeg)
  {
    // compute intersections with all candidate segments in sweep line status
    List candidates = sweepStatus.query(querySeg, pixelWidth);
    HotPixelSet hotPts = new HotPixelSet(precisionModel.getScale());
    //List newHotPts = new ArrayList();
    for (Iterator it = candidates.iterator(); it.hasNext(); ) {
      NodedLineSeg candSeg = (NodedLineSeg) it.next();
      // don't test against itself
      if (querySeg == candSeg) 
        continue;
      
      addSRIntersections(querySeg, candSeg, hotPts);
      
      // snap query seg to endpoints of candidate segs
      snap(candSeg.getCoordinate(0), querySeg);
      snap(candSeg.getCoordinate(1), querySeg);
    }
    hotPts.add(querySeg.getCoordinate(0));
    hotPts.add(querySeg.getCoordinate(1));
    
    /**
     * Snap new hot pixels to existing segs
     */
      // for each hot vertex, snap candidates to it if needed
    for (Iterator<HotPixel> ihp = hotPts.iterator(); ihp.hasNext(); ) {
      HotPixel hotPixel = ihp.next();
      //Coordinate hotPt = (Coordinate) newHotPts.get(i);
      //HotPixel hotPixel = new HotPixel(hotPt, precisionModel.getScale());
      for (Iterator it = candidates.iterator(); it.hasNext(); ) {
        NodedLineSeg candSeg = (NodedLineSeg) it.next();
        snap(hotPixel.getCoordinate(), hotPixel, candSeg);
      }
    }

    /**
     * Snap query segment to
     * previous intersection points as well
     */
    snapToIntersectionEvents(querySeg);
  }
  
  private void OLDaddSRIntersections(NodedLineSeg querySeg)
  {
    // compute intersections with all candidate segments in sweep line status
    List candidates = sweepStatus.query(querySeg, pixelWidth);
    Set<Coordinate> hotPts = new HashSet<Coordinate>();
    //List newHotPts = new ArrayList();
    for (Iterator it = candidates.iterator(); it.hasNext(); ) {
      NodedLineSeg candSeg = (NodedLineSeg) it.next();
      // don't test against itself
      if (querySeg == candSeg) 
        continue;
      
      // FIX if used again!!!!!
      //addSRIntersections(querySeg, candSeg, hotPts);
      
      // snap query seg to endpoints of candidate segs
      snap(candSeg.getCoordinate(0), querySeg);
      snap(candSeg.getCoordinate(1), querySeg);
    }
    hotPts.add(querySeg.getCoordinate(0));
    hotPts.add(querySeg.getCoordinate(1));
    
    /**
     * Snap new hot pixels to existing segs
     */
      // for each hot vertex, snap candidates to it if needed
    for (Coordinate hotPt : hotPts) {
      //Coordinate hotPt = (Coordinate) newHotPts.get(i);
      HotPixel hotPixel = new HotPixel(hotPt, precisionModel.getScale());
      for (Iterator it = candidates.iterator(); it.hasNext(); ) {
        NodedLineSeg candSeg = (NodedLineSeg) it.next();
        snap(hotPt, hotPixel, candSeg);
      }
    }

    /**
     * Snap query segment to
     * previous intersection points as well
     */
    snapToIntersectionEvents(querySeg);
  }
  
  private void snapToIntersectionEvents(NodedLineSeg querySeg)
  {
    Event prevEv = null;
    for (int i = 0; i < intEventQueue.size(); i++) {
      Event ev = intEventQueue.peek(i);
      // don't reprocess same ev (leads to an infinite loop)
      if (ev == prevEv)
        continue;
      if (ev instanceof IntersectionEvent) {
        // don't compute against self
        if (ev.getSegment() == querySeg)
          continue;
        snap(ev.getLocation(), querySeg);
        prevEv = ev;
      }
    }
  }
  
  /**
   * Add SR points for any intersections between query seg 
   * and another seg
   * 
   * @param querySeg
   * @param candSeg
   * @param hotPts
   */
  private void addSRIntersections(NodedLineSeg querySeg, 
      NodedLineSeg candSeg, 
      HotPixelSet hotPts)
  {
    // printIntersection(seg1. seg2);
    intersector.computeIntersection(
        querySeg.getCoordinate(0), 
        querySeg.getCoordinate(1),
        candSeg.getCoordinate(0), 
        candSeg.getCoordinate(1)
        );

    // For proper intersections need to add a new Hot point
    if (hotPts != null 
        && intersector.getIntersectionNum() == 1) {
      hotPts.add(intersector.getIntersection(0));
    }
    
    for (int i = 0; i < intersector.getIntersectionNum(); i++) {
      // MD - it would be nice if the LineIntersector could provide the endpoint information directly
      Coordinate intPt = intersector.getIntersection(i);
      
      /**
       * IMPORTANT: Assert - intPt is less than segment endpoints.
       * If not, perturb it so that it is?
       */
      // testing only
      /*
      checkIntersectionLessThanEnd(intPt, nodedSeg);
      checkIntersectionLessThanEnd(intPt, testSeg);
      intersector.computeIntersection(
          nodedSeg.getCoordinate(0), 
          nodedSeg.getCoordinate(1),
          testSeg.getCoordinate(0), 
          testSeg.getCoordinate(1)
          );
      */
      
      if (! querySeg.isEndpoint(intPt)) {
        addIntEvent(intPt, querySeg);
      }
      if (! candSeg.isEndpoint(intPt)) {
        addIntEvent(intPt, candSeg);
      }
    }
  }
  
  /*
  private void addSRNodes(NodedLineSeg seg1, NodedLineSeg seg2)
  {
    snap(seg1.getCoordinate(0), seg2);
    snap(seg1.getCoordinate(1), seg2);
    snap(seg2.getCoordinate(0), seg1);
    snap(seg2.getCoordinate(0), seg1);
  }
  
  private void addSRNode(NodedLineSeg seg1,
    NodedLineSeg seg2, Coordinate properInt) {
    snap(properInt, seg1);
    snap(properInt, seg2);
  }
*/
  
  private void snap(Coordinate hotPt, NodedLineSeg seg)
  {
    // don't bother testing if hot pt is outside range of segment
    if (hotPt.y < seg.getMinY() - pixelWidth) return;
    if (hotPt.y > seg.getMaxY() + pixelWidth) return;
    
    if (hotPt.equals2D(seg.getCoordinate(0))) return;
    if (hotPt.equals2D(seg.getCoordinate(1))) return;
    
    HotPixel hotPixel = new HotPixel(hotPt, precisionModel.getScale());
    if (hotPixel.intersects(seg)) {
      addIntEvent(hotPt, seg);      
    }
  }
  
  private void snap(Coordinate hotPt, HotPixel hotPixel, NodedLineSeg seg)
  {
    // don't bother testing if hot pt is outside range of segment
    if (hotPt.y < seg.getMinY() - pixelWidth) return;
    if (hotPt.y > seg.getMaxY() + pixelWidth) return;
    
    if (hotPt.equals2D(seg.getCoordinate(0))) return;
    if (hotPt.equals2D(seg.getCoordinate(1))) return;
    
    if (hotPixel.intersects(seg)) {
      addIntEvent(hotPt, seg);      
    }
  }
  
  private void addIntEvent(Coordinate p, LineSeg seg)
  {
    Event ev = new IntersectionEvent(p, seg);
    eventQueue.add(ev);
    intEventQueue.add(ev);
  }
  
  private void checkIntersectionLessThanEnd(Coordinate pt, LineSeg seg)
  {
    Coordinate endPt = seg.getCoordinate(1);
    if (pt.compareTo(endPt) > 0) {
//      Assert.shouldNeverReachHere("Intersection pt is greater than segment endpoint");
      System.out.println("Intersection pt is greater than segment endpoint");
    }
      
  }
}
