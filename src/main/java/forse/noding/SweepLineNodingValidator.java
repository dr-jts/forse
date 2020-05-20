package forse.noding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;

import forse.InvalidNodingException;
import forse.lineseg.LineSeg;
import forse.lineseg.NullSegmentSink;
import forse.lineseg.SegmentProcess;
import forse.lineseg.SegmentSink;

public class SweepLineNodingValidator 
implements SegmentProcess 
{
  private SegmentSink segSink = new NullSegmentSink();
  private boolean isFailFast;
  
  private LineIntersector intersector = new RobustLineIntersector();
  
  private SimpleEventQueue eventQueue = new SimpleEventQueue();
  //private SimpleSweepLineStatus sweepStatus = new SimpleSweepLineStatus();
  private IndexedSweepLineStatus sweepStatus = new IndexedSweepLineStatus();
  
  private List intersections = new ArrayList();
  
  public SweepLineNodingValidator() {
    isFailFast = false;
  }
  
  public SweepLineNodingValidator(boolean isFailFast) {
    setFailFast(isFailFast);
  }
  
  public void setSink(SegmentSink segSink)
  {
  	this.segSink = segSink;
  }
  
  /**
   * Sets whether the validator should fail as soon as a incorrect noding is found
   * @param isFailFast
   */
  public void setFailFast(boolean isFailFast)
  {
    this.isFailFast = isFailFast;
  }
  
  /**
   * 
   * @throws InvalidNodingException if invalid noding is found
   */
  public void process(LineSeg seg)
  {
  	// process events which occur before the start of this segment
  	processEventsBefore(seg.getCoordinate(0));
  	
  	checkIntersections(seg);
    // add segment end event
    addEnd(seg);

    // add the segment to the status so it can be intersected with subsequent segs
  	sweepStatus.insert(seg);
  	// pass it along
    segSink.process(seg);
  }
  
  private void addEnd(LineSeg nodedSeg)
  {
    eventQueue.add(new SegEndEvent(nodedSeg.getCoordinate(1), nodedSeg));
  }
  
  public void close()
  {
    // process any remaining events
    processEventsBefore(null);
    segSink.close();
  }
  
  private void processEventsBefore(Coordinate beforeLocation)
  {
    while (hasEventsBefore(beforeLocation, 0)) {
      Event ev = eventQueue.pop();
      LineSeg seg = ev.getSegment();
      sweepStatus.remove(seg);
    }
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
  
  private void checkIntersections(LineSeg querySeg)
  {
  	// compute intersections with all candidate segments in sweep line status
  	List candidates = sweepStatus.query(querySeg);
  	for (Iterator i = candidates.iterator(); i.hasNext(); ) {
  		LineSeg testSeg = (LineSeg) i.next();
      // don't test against itself
  		if (querySeg == testSeg) 
        continue;
  		checkIntersection(querySeg, testSeg);
  	}
  }
  
  private void checkIntersection(LineSeg seg1, LineSeg seg2)
  {
    // printIntersection(seg1. seg2);
  	intersector.computeIntersection(
  			seg1.getCoordinate(0), 
  			seg1.getCoordinate(1),
  			seg2.getCoordinate(0), 
  			seg2.getCoordinate(1)
  			);
  	if (intersector.isInteriorIntersection()) {
  	  intersections.add(intersectionGeom(seg1, seg2));
  	  if (isFailFast) {
  	    throw new InvalidNodingException(seg1, seg2);
  	  }
  	}
  }
  
  GeometryFactory geomFact = new GeometryFactory();
  
  private MultiLineString intersectionGeom(LineSeg seg1, LineSeg seg2)
  {
    return geomFact.createMultiLineString(new LineString[] {
        seg1.toGeometry(geomFact), seg2.toGeometry(geomFact)
    });
  }
  
  /**
   * Gets a Geometry indicating the intersections found.
   * 
   * @return
   */
  public GeometryCollection getGeometry()
  {
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(intersections));
  }
}
