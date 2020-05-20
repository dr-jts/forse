package forse.noding;

import org.locationtech.jts.algorithm.LineIntersector;
import org.locationtech.jts.algorithm.RobustLineIntersector;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;

import forse.lineseg.LineSeg;

/**
 * Implements a "hot pixel" as used in the Snap Rounding algorithm.
 * A hot pixel contains the interior of the tolerance square and
 * the boundary <b>minus</b> the top and right segments.
 * In other words, the hot pixel is open along the top and right edges.
 * 
 * @author Martin Davis
 *
 */
public class HotPixel
{
  private Coordinate hotPt;
  private double minX;
  private double maxX;
  private double minY;
  private double maxY;
  private Coordinate diagUp0;
  private Coordinate diagUp1;
  private Coordinate diagDown0;
  private Coordinate diagDown1;

  private RectangleSegmentIntersector rectInt;
  
  public HotPixel(Coordinate hotPt, double pixelSize) 
  {
    this.hotPt = hotPt;
    //this.scaleFactor = scaleFactor;
    
    double pixelRadius = 0.5 / pixelSize;
    Envelope hotPixelEnv = new Envelope(
        hotPt.x - pixelRadius, hotPt.x + pixelRadius,
        hotPt.y - pixelRadius, hotPt.y + pixelRadius
        );
    minX = hotPt.x - pixelRadius;
    minY = hotPt.y - pixelRadius;
    maxX = hotPt.x + pixelRadius;
    maxY = hotPt.y + pixelRadius;
    
    /**
     * Compute segments forming diagonals of rectangle.
     * Up and Down are relative to the Left side of the rectangle.
     * These will be tested for intersection with query segment if needed.
     */
    diagUp0 = new Coordinate(minX, minY);
    diagUp1 = new Coordinate(maxX, maxY);
    diagDown0 = new Coordinate(minX, maxY);
    diagDown1 = new Coordinate(maxX, minY);

    rectInt = new RectangleSegmentIntersector(hotPixelEnv);
    
    corner[0] = new Coordinate(maxX, maxY);
    corner[1] = new Coordinate(minX, maxY);
    corner[2] = new Coordinate(minX, minY);
    corner[3] = new Coordinate(maxX, minY);
  }
  
  public Coordinate getCoordinate()
  {
    return hotPt;
  }
  
  /**
   * Tests for intersection with a LineSeg.
   * Does NOT check whether an endpoint of the segment 
   * is equal to the hot pixel centre point. 
   * Tests against the hot pixel tolerance square, which is open along the top and right.
   * 
   * @param seg
   * @return true if the segment interescts the hot pixel
   */
  public boolean intersects(LineSeg seg)
  {
    // testing only
    /*
    if (true) {
      return intersectsToleranceSquare(seg.getCoordinate(0), seg.getCoordinate(1));
        //return intersects(seg.getCoordinate(0), seg.getCoordinate(1));
    }
    */
    
    /**
     * Uses an optimized method of testing which requires only one segment/segment test, 
     * rather than 4.
     */
    
    // check if envelopes are disjoint
    if (seg.getMinX() > maxX) return false;
    if (seg.getMaxX() < minX) return false;
    if (seg.getMinY() > maxY) return false;
    if (seg.getMaxY() < minY) return false;
    
    // don't need to check for seg endpoints same as hot pt, since this has been done already
    
    // check intersection with appropriate diagonal
    boolean isSegUpwards = ! seg.isDown();
    /**
     * Since we now know that neither segment endpoint
     * lies in the rectangle, there are two possible 
     * situations:
     * 1) the segment is disjoint to the rectangle
     * 2) the segment crosses the rectangle completely.
     * 
     * In the case of a crossing, the segment must intersect 
     * a diagonal of the rectangle.
     * 
     * To distinguish these two cases, it is sufficient 
     * to test intersection with 
     * a single diagonal of the rectangle,
     * namely the one with slope "opposite" to the slope
     * of the segment.
     * (Note that if the segment is axis-parallel,
     * it must intersect both diagonals, so this is
     * still sufficient.)  
     */
    LineIntersector li = new RobustLineIntersector();
    if (isSegUpwards) {
      li.computeIntersection(seg.getCoordinate(0), seg.getCoordinate(1), diagDown0, diagDown1);
    }
    else {
      li.computeIntersection(seg.getCoordinate(0), seg.getCoordinate(1), diagUp0, diagUp1);
    }
    if (li.isProper())
      return true;
    /**
     * if seg is downwards then need to check if it intersects LL corner of pixel
     * (the only one in the tolerance square)
     */ 
    if (! isSegUpwards && li.hasIntersection()) {
      if (li.getIntersection(0).equals2D(diagUp0))
        return true;
    }
    return false;

    //return intersects(seg.getCoordinate(0), seg.getCoordinate(1));
  }
  
  private boolean intersects(Coordinate p0, Coordinate p1)
  {
    return rectInt.intersects(p0, p1);
  }
  
  private LineIntersector li = new RobustLineIntersector();
  /**
   * The corners of the hot pixel, in the order:
   *  10
   *  23
   */
  private Coordinate[] corner = new Coordinate[4];


  /**
   * Tests whether the segment p0-p1 intersects the hot pixel tolerance square.
   * Because the tolerance square point set is partially open (along the
   * top and right) the test needs to be more sophisticated than
   * simply checking for any intersection.  
   * However, it can take advantage of the fact that the hot pixel edges
   * do not lie on the coordinate grid.  
   * It is sufficient to check if any of the following occur:
   * <ul>
   * <li>a proper intersection between the segment and any hot pixel edge
   * <li>an intersection between the segment and <b>both</b> the left and bottom hot pixel edges
   * (which detects the case where the segment intersects the bottom left hot pixel corner)
   * <li>an intersection between a segment endpoint and the hot pixel coordinate
   * </ul>
   *
   * @param p0
   * @param p1
   * @return
   */
  private boolean intersectsToleranceSquare(Coordinate p0, Coordinate p1)
  {
    boolean intersectsLeft = false;
    boolean intersectsBottom = false;

    li.computeIntersection(p0, p1, corner[0], corner[1]);
    if (li.isProper()) return true;

    li.computeIntersection(p0, p1, corner[1], corner[2]);
    if (li.isProper()) return true;
    if (li.hasIntersection()) intersectsLeft = true;

    li.computeIntersection(p0, p1, corner[2], corner[3]);
    if (li.isProper()) return true;
    if (li.hasIntersection()) intersectsBottom = true;

    li.computeIntersection(p0, p1, corner[3], corner[0]);
    if (li.isProper()) return true;

    if (intersectsLeft && intersectsBottom) return true;

    if (p0.equals(hotPt)) return true;
    if (p1.equals(hotPt)) return true;

    return false;
  }

}
