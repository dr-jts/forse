package forse.polygonize;

import java.util.*;

import forse.lineseg.TopologyLabel;

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geomgraph.Quadrant;
import org.locationtech.jts.util.*;

/**
 * Half-edges are a data structure which 
 * support representing a polygonal coverage (mesh). 
 * A half-edge represents an directed edge which
 * links an origin node with a destination node.
 * Half-edges always occur in pairs.
 * 
 * @author Martin Davis
 *
 */
public class HalfEdge 
implements Comparable
{
	/**
	 * Creates a new edge pair between two coordinates.
	 * Two edges are created which are each other's <tt>sym</tt>.
	 * The <tt>next</tt> and <tt>prev</tt> pointers are <tt>null</tt>.
	 * 
	 * @param p0 a coordinate
	 * @param p1 a coordinate
	 * @return the edge which originates at p0
	 */
  public static HalfEdge makeEdge(Coordinate p0, Coordinate p1)
  {
    return makeEdge(p0, p1, (byte) 0, 0);
  }
  
  public static HalfEdge makeEdge(Coordinate p0, Coordinate p1, byte topoLabel, int topoDepth)
  {
    HalfEdge e0 = new HalfEdge(p0, p1, topoLabel, topoDepth);
    HalfEdge e1 = new HalfEdge(p1, p0, TopologyLabel.flip(topoLabel), -topoDepth);
    e0.setSym(e1);
    e1.setSym(e0);
    return e0;
  }
	
  /**
   * The edge equal to this one but oriented in the opposite direction
   */
	private HalfEdge sym = null;
  /**
   * The edge CW around the face to the R of this edge
   */
	private HalfEdge next = null;
	/**
	 * The edge CCW around the face to the R of this edge.
	 * Not populated until polyon building
	 */
  private HalfEdge ringPrev;
  private HalfEdge ringNext;
	
	private Coordinate orig = null;
  private Coordinate dest = null;
  
  protected byte topoLabel = 0; 
  protected int topoDepth = 0; 
  
	private Face face;  // the face on the right of this edge
	
  private HalfEdge(Coordinate orig, Coordinate dest)
  {
    this.orig = orig;
    this.dest = dest;
  }
  
  private HalfEdge(Coordinate orig, Coordinate dest, byte topoLabel, int topoDepth)
  {
    this.orig = orig;
    this.dest = dest;
    this.topoLabel = topoLabel;
    this.topoDepth = topoDepth;
  }
  
  public byte topoLabel()
  {
    return topoLabel;
  }
  
  public int topoDepth()
  {
    return topoDepth;
  }
  
	public HalfEdge sym() { return sym; }
	public Coordinate orig() { return orig; }
  public Coordinate dest() { return dest; }
	
  /**
   * Gets the edge CW around the face to the right of this edge.
   * (This is the edge which is CCW around the dest of this edge.)
   * 
   * @return the next linked edge.
   */
	public HalfEdge next() { return next; }
	
  /**
   * Gets the edge CCW around the face to the right of this edge.
   * 
   * @return the previous linked edge.
   */	
	public HalfEdge prev() { return sym.dPrev(); }
	
  public void setRingPrev(HalfEdge ePrev)
  {
    ringPrev = ePrev;
  }

  public HalfEdge ringPrev()
  {
    return ringPrev;
  }

  public void setRingNext(HalfEdge e)
  {
    ringNext = e;
  }

  public HalfEdge ringNext()
  {
    if (ringNext != null)
      return ringNext;
    return next;
  }

  /**
   * Gets the previous edge CW around the dest of this edge.
   */
  public HalfEdge dPrev()
  {
    HalfEdge curr = next;
    HalfEdge prev = this;
    while (true) {
      if (curr.sym == this) return prev;
      prev = curr.sym;
      curr = curr.sym.next;
    }
  }
  
	/**
	 * Gets the next edge CCW around the origin of this edge
	 */
	public HalfEdge oNext() {
		return sym.next;
	}
		
	/**
	 * Tests whether this edge is in canonical (lexicographic) order. 
	 * 
	 * @return true if the edge is in canonical order
	 */
	public boolean isOrdered()
	{
		return orig.compareTo(sym.orig) <= 0;
	}
	
	/**
	 * Tests whether this edge is vertical
	 * 
	 * @return true if the edge is vertical
	 */
	public boolean isVertical() {
		return orig.x == dest.x;
	}

	/**
	 * Tests whether this edge is horizontal
	 * 
	 * @return true if the edge is vertical
	 */
	public boolean isHorizontal() {
		return orig.y == dest.y;
	}

	public boolean isRightward()
	{
	  return dest.x > orig.x;
	}
	
  /*
	public void addPoints(CoordinateList dest)
	{
		dest.add(orig, false);
		dest.add(sym.orig, false);
	}
	*/
  
	public Face face() { return face; }
	public void setFace(Face face) { this.face = face; }
	
  /*
  public boolean isComplete()
  {
    return face.isComplete() && sym.face.isComplete();
  }
  */
  
	public double deltaX() { return dest.x - orig.x; }
	public double deltaY() { return dest.y - orig.y; }
	
	private void setSym(HalfEdge sym)
	{
		this.sym = sym;
	}
	public void setNext(HalfEdge next)
	{
		this.next = next;
	}
  /*
	public void setOrig(Coordinate orig)
	{
		this.orig = orig;
	}
	*/
  
	public double angle()
	{
		return Angle.normalizePositive(Angle.angle(orig, sym.orig));
	}
	
	public double maxY()
	{
		if (orig.y > sym.orig.y)
			return orig.y;
		return sym.orig.y;
	}
	
	public double minY()
	{
		if (orig.y < sym.orig.y)
			return orig.y;
		return sym.orig.y;
	}
	
  /**
   * Gets the edge of the pair which is upppermost.
   * Assumes that the edge is not vertical.
   * 
   * @return
   * @throws IllegalStateException if the edge is vertical
   */
  public HalfEdge upper()
  {
    if (orig.x == sym.orig.x)
      throw new IllegalStateException("upper() is undefined for vertical edges");
    if (orig.x < sym.orig.x) {
      // edge points to right - upper face is on opposite side
      return sym;
    }
    return this;
  }
  
  public HalfEdge nextNonVertical()
  {
    HalfEdge start = this;
    HalfEdge e = this.next();
    while (e.isVertical() && e != start) {
      e = e.next();
    }
    return e;
  }
  /**
   * Gets the edge of the pair which is lowest.
   * Assumes that the edge is not vertical.
   * 
   * @return
   * @throws IllegalStateException if the edge is vertical
   */
  public HalfEdge lower()
  {
    if (orig.x == sym.orig.x)
      throw new IllegalStateException("lower() is undefined for vertical edges");
    return upper().sym;
  }
  
  public boolean isBelow(Coordinate p)
  {
    // if edge is completely below point => true
    if (orig.y < p.y && sym.orig.y < p.y)
      return true;
    // if edge is completely at or above point => false
    if (orig.y >= p.y && sym.orig.y >= p.y)
      return false;
    
    // Otherwise check orientation of point relative to edge
    // (with edge in canonical order)
    // If point is CCW to edge then edge is below point
    
    Coordinate p0 = orig;
    Coordinate p1 = sym.orig;
    if (! isOrdered()) {
      p0 = sym.orig;
      p1 = orig;
    }
    boolean isBelow = CGAlgorithms.orientationIndex(p0, p1, p) == CGAlgorithms.COUNTERCLOCKWISE;
    return isBelow;
  }
  
  public boolean extendsLeftOf(Coordinate queryPt)
  {
    if (orig.x < queryPt.x || dest.x < queryPt.x) return true;
    return false;
  }

  public void removeSym()
  {
    sym = null;
  }
  
  /**
   * Removes this edge and its sym, relinking
   * the edges on either side of it to preserve topology.
   */
  public void remove()
  {
    HalfEdge dPrev = dPrev();
    HalfEdge oNext = oNext();
    HalfEdge prev = prev();
    
    // unlink at dest end
    dPrev.next = next;
    
    // unlink at orig end
    prev.next = oNext;
  }
  
	/**
	 * Compares edges which originate at the same vertex
	 * based on the angle they make at their origin vertex with the positive X-axis.
	 * This allows sorting edges around their origin vertex in CCW order.
	 */
  public int compareTo(Object obj)
  {
      HalfEdge e = (HalfEdge) obj;
      int comp = compareAngularDirection(e);
      
      if (Debug.isDebugging()) {
      	double ang = angle();
      	double ang2 = e.angle();
      	int compAngle = 0;
      	if (ang < ang2)
      		compAngle = -1;
      	else if (ang > ang2)
      		compAngle = 1;

      	Debug.breakIf(comp != compAngle);

      	compareAngularDirection(e);
      }
      
      return comp;
  }

  /**
   * Implements the total order relation:
   * <p>
   *    The angle of edge a is greater than the angle of edge b,
   *    where the angle of an edge is the angle made by 
   *    the first segment of the edge with the positive x-axis
   * <p>
   * When applied to a list of edges originating at the same point,
   * this produces a CCW ordering of the edges around the point.
   * <p>
   * Using the obvious algorithm of computing the angle is not robust,
   * since the angle calculation is susceptible to roundoff error.
   * A robust algorithm is:
   * <ul>
   * <li>First, compare the quadrants the edge vectors lie in.  
   * If the quadrants are different, 
   * it is trivial to determine which edge has a greater angle.
   * 
   * <li>if the vectors lie in the same quadrant, the 
   * <tt>computeOrientation</tt> function
   * can be used to determine the relative orientation of the vectors.
   * </ul>
   */
  public int compareAngularDirection(HalfEdge e)
  {
  	double dx = deltaX();
  	double dy = deltaY();
  	double dx2 = e.deltaX();
  	double dy2 = e.deltaY();
  	
  	// same vector
    if (dx == dx2 && dy == dy2)
      return 0;
    
    double quadrant = Quadrant.quadrant(dx, dy);
    double quadrant2 = Quadrant.quadrant(dx2, dy2);
    
    // if the rays are in different quadrants, determining the ordering is trivial
    if (quadrant > quadrant2) return 1;
    if (quadrant < quadrant2) return -1;
    // vectors are in the same quadrant - check relative orientation of direction vectors
    // this is > e if it is CCW of e
    return CGAlgorithms.computeOrientation(e.orig, e.dest, dest);
  }

  private static final GeometryFactory geomFact = new GeometryFactory();
  
  public String toString()
  {
  	Geometry g = geomFact.createLineString(new Coordinate[] { orig, dest });
  	return g.toString();
  }
  
  /**
   * Sets the face for all edges found by
   * following the <tt>next</tt> pointers.
   * (e.g. all those around the face to the R of the given edge)
   * @param e
   * @param face
   */
  void setFaceForward(Face face)
  {
  	HalfEdge edgeToSet = this;
  	do {
  		edgeToSet.setFace(face);
  		edgeToSet = edgeToSet.next();
  	}
  	while (edgeToSet != null && edgeToSet != this);
  }

  public static final DisjointAboveComparator ABOVE_COMP = new DisjointAboveComparator();
  
  /**
   * Computes the <tt>above</tt> relation for edges
   * which are:
   * <ul>
   * <li>Correctly noded (and hence interior-disjoint)
   * <li>disjoint
   * <li>Comparable, in the sense that their X-ranges intersect
   * </ul>
   * Canonical orientation is not required - edges will be flipped
   * if necessary.
   * <p>
   * If the edges are not interior-disjoint (e.g. they cross), 
   * aboveness is indeterminate.
   * This still provides an effective ordering when
   * used for containment computation via vertical stabbing line,
   * since containment only occurs between disjoint edges.
   * 
   * @author mbdavis
   *
   */
  public static class DisjointAboveComparator
  implements Comparator
  {
  	public int compare(Object o1, Object o2)
  	{
  		HalfEdge P = (HalfEdge) o1;
  		HalfEdge Q = (HalfEdge) o2;
  		
  		// put edges in canonical order
  		// this does not change their vertical order
  		P = P.isOrdered() ? P : P.sym();
  		Q = Q.isOrdered() ? Q : Q.sym();
  		
  		int yComp = comparePartialYExtent(P, Q);
  		if (yComp != 0) return yComp;

  		// check relative segment orientation
  		// the following logic relies on each input segment being canonically ordered
  		int orientPQ = compareByOrientation(P, Q);
  		if (orientPQ != 0) return orientPQ;
  		
  		// flip the segments and test.  If the test is determinative, the result must be flipped too
  		int orientQP = compareByOrientation(Q, P);
  		if (orientQP != 0) return -orientQP;
  		
  		/**
  		 * At this point the segments are known to be collinear
  		 * (but possibly not horizontal).
  		 * Due to the noding, they can be assumed to be interior-disjoint.
  		 * So aboveness can be tested by checking ordinates
  		 */
  		if (P.orig.x == P.sym.orig.x) {
  			if (P.orig.y > Q.orig.y) {
  				return 1;
  			}
  			return -1;
  		}
 
		if (P.orig.x > Q.orig.x) {
			return 1;
		}
		else {
			if (P.orig.x < Q.orig.x)
				return -1;
		}
		// at this point both edges should be horizontal and have equal Y
		// testing seems to verify this, so this check is disable for prod use
		/*
		//DDDDDDDDDDD
		boolean isBothHoriz = P.isHorizontal() && Q.isHorizontal();
		boolean hasSameY = P.orig.y == Q.orig.y;
		if (! isBothHoriz || ! hasSameY) {
			Assert.shouldNeverReachHere("Expected both HalfEdges horizontal with same Y: " + P + " and " + Q);
		}
		//DDDDDDDDDDD
		 */
  		return 0;
  	}

	private int comparePartialYExtent(HalfEdge P, HalfEdge Q) {
		double P_maxy = P.maxY();
  		double Q_maxy = Q.maxY();
  		double P_miny = P.minY();
  		double Q_miny = Q.minY();
  		
  		// if the segs are disjoint in Y, aboveness can be computed directly
  		if (P_miny > Q_maxy)	return 1;
  		if (P_miny == Q_maxy && Q_maxy < P_maxy)	return 1;
  		if (Q_miny > P_maxy)	return -1;
  		if (Q_miny == P_maxy && P_maxy < Q_maxy)	return -1;
  		// this means the segments overlap in Y, and vertical order is not known
  		return 0;
	}
  	
  	/**
  	 * Computes aboveness for segments which interact in Y.
  	 * This requires checking relative orientation.
  	 * If the segments are collinear, 
  	 * the results of this test are inconclusive
  	 * and further logic must be applied.
  	 * 
  	 * @param P
  	 * @param Q
  	 * @return the comparison value, if determined
  	 * @return 0 if the aboveness cannot be determined
  	 */
  	private int compareByOrientation(HalfEdge P, HalfEdge Q)
  	{
  		// check relative segment orientation
  		// the following logic relies on each input segment being canonically ordered
      int Pq1 = CGAlgorithms.orientationIndex(P.orig, P.dest, Q.orig);
      int Pq2 = CGAlgorithms.orientationIndex(P.orig, P.dest, Q.sym.orig);

      int PQorient = 0;
      // only deterministic if both orientations are the same
      if (Pq1 == Pq2) PQorient = Pq1;
      
      // TODO: verify this is correct!!!
      // also deterministic if segments touch at one end
      if (Pq1 == 0) {
    	  PQorient = Pq2;
      }
      else if (Pq2 == 0) {
    	  PQorient = Pq1;
      }
      
      // MD - following checks are erroneous
//      else if (Pq1 == 0) PQorient = Pq2;
//      else if (Pq2 == 0) PQorient = Pq1;
      
      if (PQorient == CGAlgorithms.CLOCKWISE) return 1;
      if (PQorient == CGAlgorithms.COUNTERCLOCKWISE) return -1;
      
      return 0;
  	}
  }

  public static final VertexComparator VERTEX_COMP = new VertexComparator();

  public static class VertexComparator
  implements Comparator
  {
    public int compare(Object o1, Object o2)
    {
      HalfEdge P = (HalfEdge) o1;
      HalfEdge Q = (HalfEdge) o2;
      
      return P.orig().compareTo(Q.orig());
    }
  }

  public Coordinate getHighestCoord()
  {
    if (orig.compareTo(dest) >= 1) return orig;
    return dest;
  }

  public Coordinate other(Coordinate p)
  {
    if (orig.equals2D(p)) return dest;
    return orig;
  }

  public boolean equalsAnyOrient(HalfEdge e)
  {
    if (! (orig.equals2D(e.orig) || orig.equals2D(e.dest))) return false;
    if (! (dest.equals2D(e.orig) || dest.equals2D(e.dest))) return false;
    return true;
  }
  public boolean equalsReverse(HalfEdge e)
  {
    return orig.equals2D(e.dest) && dest.equals2D(e.orig);
  }

}
