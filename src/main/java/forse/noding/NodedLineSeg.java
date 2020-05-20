package forse.noding;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.util.Assert;

import forse.lineseg.LineSeg;

public class NodedLineSeg 
extends LineSeg
{
  private Coordinate lastIntPt;
  
  public NodedLineSeg(LineSeg seg) {
    super(seg);
    // starts as start point of segment
    lastIntPt = getCoordinate(0);
  }

  public Coordinate getLastIntersectionPoint()
  {
    return lastIntPt;
  }
  
  public boolean isZeroLength(Coordinate intPt)
  {
    // check if entire segment has already been returned as subsegments 
   // if (lastIntPt == null) return true;
    
    // a null pointer exception here probably indicates a noding failure.
    // Above code is a hack to allow bypassing the problem (although overlay result will be incorrect)
    /**
     * An observed situation where this happens is:
     * seg: LINESTRING ( 1018774.3125019757 955288.749740758, 1018774.3125019773 955263.6247407598 )
     * 
     * intPt: (1018774.3125019773, 955263.6249969759, NaN)
     * 
     * The cause of the problem is that the intersection point in the segment is computed
     * slightly incorrectly, so that its x value is the same as the x of the vertically lower segment point.
     * This means that the intersection point is actually greater than the endpoint of the segment.
     * This causes the IntersectionEvent to occur after the SegEndEvent - which is obviously bogus.
     * 
     * Possible cure: check for intersection point being greater than segment endpoint, and 
     * perturb it so that it is less.
     */
    if (lastIntPt == null) 
      throw new IllegalStateException("Invalid segment intersection point computed");
    if (lastIntPt.equals2D(intPt))
      return true;
    return false;
  }
  
  public double getNodeMinX()
  {
    return lastIntPt.x;
  }
  
  /**
   * Chops this segment in two at an intersection point.
   * The segment is updated to record
   * that it now starts at the intersection point,
   * and the leading subsegment is returned,
   * 
   * @param intersectionPt the point of intersection
   * @return a segment which is the leading half of the chopped segment
   */
  public LineSeg chop(Coordinate intersectionPt)
  {
    checkIntPointOrder(intersectionPt);
    
    // TODO: what if lastPt = intPt?  return null?
    LineSeg seg = new LineSeg(lastIntPt, intersectionPt, topoLabel, topoDepth);
    //checkSegOrder(seg, new LineSeg(intersectionPt, getCoordinate(1)));
    lastIntPt = intersectionPt;
    return seg;
  }
  
  private void checkSegOrder(LineSeg seg1, LineSeg seg2)
  {
    Assert.isTrue(seg1.compareTo(seg2) < 0);
  }
  
  private void checkIntPointOrder(Coordinate intPt)
  {
    LineSegment seg = new LineSegment(getCoordinate(0), getCoordinate(1));
    double intFrac = seg.segmentFraction(intPt);
    double lastIntFrac = seg.segmentFraction(lastIntPt);
    if (false && intFrac < lastIntFrac) {
      System.out.println("checkIntPointOrder: int point " 
          + intPt + " is before lastIntPt " + lastIntPt
          + " in seg " + this);
    }
  }
  
  /**
   * Creates a new segment for the last
   * subsegment (the remainder) of this segment.
   * 
   * @return the last subsegment of this segment
   */
  public LineSeg last()
  {
    // TODO: what if lastPt = intPt?  return null?
    LineSeg seg = new LineSeg(lastIntPt, getCoordinate(1), topoLabel, topoDepth);
    lastIntPt = null;  // null this to cause failure if this seg is incorrectly used again
    return seg;
  }
  
}
