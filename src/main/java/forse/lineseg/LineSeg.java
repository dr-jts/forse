package forse.lineseg;

import java.util.*;
import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.*;

/**
 * A line segment.
 * LineSegs are always normalized so that
 * seg.getCoordinate(0) <= seg.getCoordinate(1).
 * In other words,
 * either seg.x0 < seg x1,
 * or seg.x0 = seg.x1 and seg.y0 < seg.y1.
 * In other words, segments are directed to the right, or vertically upwards.
 * 
 * @author Martin Davis
 *
 */
public class LineSeg 
  implements Comparable
{
  public static String toWKT(LineSeg seg1, LineSeg seg2)
  {
    return "MULTILINESTRING((" 
        + seg1.getX0() + " " + seg1.getY0()
        + ", " 
        + seg1.getX1() + " " + seg1.getY1()
        + "),("
        + seg2.getX0() + " " + seg2.getY0()
        + ", " 
        + seg2.getX1() + " " + seg2.getY1()
        + "))";
  }
  

  protected double x0, y0, x1, y1;
  protected byte topoLabel = 0; 
  /**
   * The change in topological depth travelling from left to right.
   * 
   * A positive depth indicates that the area to the right of the edge
   * is in the interior of one or more polygons.
   * 
   * A depth of zero can be assumed to indicate that the right area
   * is interior.  A topology collapse can cause this to happen
   * when both sides are exterior - but in this can only happen
   * for all edges of an area if the area is completely collapsed,
   * in which case it will be eliminated anyway.
   * 
   * A negative depth indicates that the area MAY be an exterior area,
   * but this must be confirmed by traversing all edges between it and
   * the world face (since there may be a larger polygon that completely
   * covers the area).
   * 
   */
  protected int topoDepth = 0;
  
  public LineSeg(Coordinate p0, Coordinate p1) {
    if (p0.compareTo(p1) <= 0) {
      init(p0, p1);
    }
    else {
      init(p1, p0);     
    }
  }

  public LineSeg(Coordinate p0, Coordinate p1, byte topoLabel) {
    if (p0.compareTo(p1) <= 0) {
      init(p0, p1);
      this.topoLabel = topoLabel;
    }
    else {
      init(p1, p0);  
      this.topoLabel = TopologyLabel.flip(topoLabel);
    }
    topoDepth = TopologyLabel.depth(this.topoLabel);
  }
  
  public LineSeg(Coordinate p0, Coordinate p1, byte topoLabel, int topoDepth) {
    if (p0.compareTo(p1) <= 0) {
      init(p0, p1);
      this.topoLabel = topoLabel;
      this.topoDepth = topoDepth;
    }
    else {
      init(p1, p0);  
      this.topoLabel = TopologyLabel.flip(topoLabel);
      this.topoDepth = -topoDepth;
    }
  }

  public LineSeg(LineSeg seg) {
    x0 = seg.x0;
    y0 = seg.y0;
    x1 = seg.x1;
    y1 = seg.y1;
    topoLabel = seg.topoLabel;
    topoDepth = seg.topoDepth;
  }

  protected void init(Coordinate p0, Coordinate p1)
  {
    x0 = p0.x;
    y0 = p0.y;
    x1 = p1.x;
    y1 = p1.y;
  }
  
  public Coordinate getCoordinate(int index)
  {
  	if (index == 0) return new Coordinate(x0, y0);
  	return new Coordinate(x1, y1);
  }
  
  public double getX0() { return x0; }
  public double getY0() { return y0; }
  
  public double getX1() { return x1; }
  public double getY1() { return y1; }
  
  public double getMinX() 
  {
    return x0;
  }
  
  public double getMinY() 
  {
    if (y0 <= y1)
      return y0;
    return y1;
  }
  
  public double getMaxX() 
  {
    return x1;
  }

  public double getMaxY() 
  {
    if (y0 > y1)
      return y0;
    return y1;
  }
  
  public boolean envelopeIntersects(LineSeg seg)
  {
    if (x0 > seg.x1) return false;
    if (x1 < seg.x0) return false;
    
    if (getMinY() > seg.getMaxY()) return false;
    if (getMaxY() < seg.getMinY()) return false;
    
    return true;
  }

  public boolean envelopeIntersects(LineSeg seg, double expandBy)
  {
    if (x0 > seg.x1 + expandBy) return false;
    if (x1 < seg.x0 - expandBy) return false;
    
    if (getMinY() > seg.getMaxY() + expandBy) return false;
    if (getMaxY() < seg.getMinY() - expandBy) return false;
    
    return true;
  }

  public Envelope getEnvelope()
  {
  	return new Envelope(x0, x1, y0, y1);
  }
  
  public boolean envelopeIntersectsProperly(LineSeg seg)
  {
  	if (x0 >= seg.x1) return false;
  	if (x1 <= seg.x0) return false;
  	
  	if (getMinY() >= seg.getMaxY()) return false;
  	if (getMaxY() <= seg.getMinY()) return false;
  	
  	return true;
  }

  public boolean isEndpoint(Coordinate p)
  {
  	if (p.x == x0 && p.y == y0) return true;
  	if (p.x == x1 && p.y == y1) return true;
  	return false;
  }
  
  public boolean isDown()
  {
    return y1 < y0;
  }

  public byte getTopoLabel()
  {
    return topoLabel;
  }
  
  public int getDepth()
  {
    return topoDepth;
  }
  
  /**
   * Merges two segments.
   * Segments are assumed to be geometrically equal.
   * Topogical flags and depths are merged.
   * 
   * @param seg segment to merge
   */
  public void merge(LineSeg seg)
  {
    /*
    if (TopologyLabel.merge(topoLabel, seg.topoLabel) == TopologyLabel.BOTH_INTERIOR && topoDepth+seg.topoDepth != 0) {
      System.out.println("Bad edge merge? " + this);
    }
    */
    // Assert: equals(seg)
    topoLabel = TopologyLabel.merge(topoLabel, seg.topoLabel);
    topoDepth += seg.topoDepth;
    
  }
  
  /**
   * Implements lexicograpic order.
   */
  public int compareTo(Object o) {
    LineSeg seg = (LineSeg) o;

    if (x0 < seg.x0) return -1;
    if (x0 > seg.x0) return 1;
    
    if (y0 < seg.y0) return -1;
    if (y0 > seg.y0) return 1;
    
    // start points are equal
    
    if (x1 < seg.x1) return -1;
    if (x1 > seg.x1) return 1;
    
    if (y1 < seg.y1) return -1;
    if (y1 > seg.y1) return 1;
    
    return 0;
  }

  public boolean equals(Object obj)
  {
    LineSeg seg = (LineSeg) obj;

    return x0 == seg.x0
    && y0 == seg.y0
    && x1 == seg.x1
    && y1 == seg.y1;
  }
  
  /**
   * Gets a hashcode for this object.
   * 
   * @return a hashcode for this object
   */
  public int hashCode() {
    long bits0 = java.lang.Double.doubleToLongBits(x0);
    bits0 ^= java.lang.Double.doubleToLongBits(y0) * 31;
    int hash0 = (((int) bits0) ^ ((int) (bits0  >> 32)));
    
    long bits1 = java.lang.Double.doubleToLongBits(x1);
    bits1 ^= java.lang.Double.doubleToLongBits(y1) * 31;
    int hash1 = (((int) bits1) ^ ((int) (bits1  >> 32)));

    // this is supposed to be a good way to combine hashcodes
    return hash0 ^ hash1;
  }

  public String toString()
  {
  	return WKTWriter.toLineString(new Coordinate(x0, y0), new Coordinate(x1, y1));
  }
  
  /**
   * A Comparator for LineSegs 
   * which orders segments by the lexocographic ordering of their
   * initial point, 
   * and then by their vector orientation CCW around the initial point
   * (as in the usual angular measure).
   * Due to the ordering of the vertices,
   * the angles of LineSegs will always lie in the range
   * (-90, 90]. 
   *  
   * @author mbdavis
   *
   */
  public static class OrientationComparator
  implements Comparator
  {
    public int compare(Object o1, Object o2) {
      LineSeg seg1 = (LineSeg) o1;
      LineSeg seg2 = (LineSeg) o2;

      if (seg1.x0 < seg2.x0) return -1;
      if (seg1.x0 > seg2.x0) return 1;
      
      if (seg1.y0 < seg2.y0) return -1;
      if (seg1.y0 > seg2.y0) return 1;
      
      // start points are equal, so order by relative orientation
      int orient = CGAlgorithms.computeOrientation(seg1.getCoordinate(0), seg1.getCoordinate(1), seg2.getCoordinate(1));
      if (orient == CGAlgorithms.COUNTERCLOCKWISE) return -1;
      if (orient == CGAlgorithms.CLOCKWISE) return 1;
      
      // this should only happen if the segments are exactly equal. 
      // Should assert this!
      return 0;
    }

  }
  
  public LineString toGeometry(GeometryFactory geomFactory)
  {
    return geomFactory.createLineString(new Coordinate[] { 
        getCoordinate(0), getCoordinate(1) });
  }
}
