package forse.polygonize;

import java.util.*;

import forse.lineseg.TopologyDepth;
import forse.lineseg.TopologyLabel;

import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.*;

public class Face 
{
  /**
   * The face is on the R of this edge
   */
	private HalfEdge startEdge;
	private boolean isHole = false;
	private Face shell = null;  // not actually needed
  private List<Face> holeFaces;  // List<Face>
  private boolean isInterior = false;
  // the coordinates for the face ring
  private Coordinate[] pts;
  
  /**
   * Creates a new face lying to the R of the given edge
   */
	public Face(HalfEdge edge)
	{
	  startEdge = edge;
	}
	
  /**
   * @return the startEdge
   */
  public HalfEdge getStartEdge()
  {
    return startEdge;
  }

  /**
   * Reports whether this face is interior to 
   * some input polygon. 
   * 
   * @return true if the face is an interior one
   */
  public boolean isInterior()
  {
    return isInterior;
  }
  
  public int size()
  {
    return pts.length;
  }
  /**
   * Builds the face ring, and computes information about it.
   * Requires the face to be complete (ie. a closed ring).
   */
  public void build()
  {
    // also sets interior status
    pts = getCoordinates(true);
    
    // set hole flag by testing ring orientation
    // assert: pts array is a closed ring
    if (pts.length > 3)
      isHole = CGAlgorithms.isCCW(pts);
  }
  
  public boolean isValidRing()
  {
    return pts.length > 3;
  }
  
  /**
   * Returns an edge which is guaranteed to be on the face shell
   * 
   * @return
   */
  public HalfEdge findOuterEdge()
  {
    HalfEdge eShell = getStartEdge();
    Coordinate maxCoord = eShell.getHighestCoord();
    HalfEdge e = eShell;
    while (true) {
      e = e.next();
      if (e == getStartEdge())
        break;
      
      Coordinate eMaxCoord = e.getHighestCoord();
      int comp = eMaxCoord.compareTo(maxCoord);
      boolean isHigher = false;
      if (comp > 0) {
        isHigher = true;
      }
      else if (comp == 0) {
        Coordinate otherShell = eShell.other(maxCoord);
        Coordinate other = e.other(eMaxCoord);
        if (other.compareTo(otherShell) > 0) {
          isHigher = true;
        }
      }
      if (isHigher) {
        eShell = e;
        maxCoord = eMaxCoord;
      }
    }
    return eShell;
  }
  
  public void addHole(Face hole)
  {
    // TODO: just add holes as geometry, rather than face?
    if (holeFaces == null) 
      holeFaces = new ArrayList();
    holeFaces.add(hole);
    hole.setShell(this);
    
    // Shells can't be holes, and holes can't be shells
    // Assert: hole.holes = null
    // Assert: this.shell = null;
  }
  
  private void addHoles(Collection newHoles)
  {
    if (newHoles == null) return;
    for (Iterator i = newHoles.iterator(); i.hasNext(); ) {
      Face hole = (Face) i.next();
      addHole(hole);
    }
  }
  
  private void setShell(Face face)
  {
    shell = face;
  }
  
	public boolean isHole() { return isHole; }
	
  /**
   * Merges a source face into this one,
   * updating topological information as required.
   * The merged face is assumed to be discarded.
   * 
   * @param face the face to merge
   */
	public void merge(Face srcFace)
	{
    /*
    if (Debug.hasSegment(getPartialGeometry(), 
        new Coordinate(186.8421052631579, 263.1578947368421),
        new Coordinate(80, 230)))
    {
      System.out.println(this);
    }
    */
    
    /**
     * When merging, holes can turn into shells, 
     * but not vice-versa.
     * Equivalently, if the target is a hole then
     * it becomes whatever the src face is.
     */
		if (isHole) {
//      if (srcFace.holes!= null)
//        System.out.println("Found face with holes");
      
			isHole = srcFace.isHole;
    }
    /**
     * If the src face is not a hole, then the target will not
     * be either, due to the shell-override rule.
     * Transfer any holes from the src to the target.
     */
    if (! srcFace.isHole) {
//      if (holes != null)
//        System.out.println("Adding to non-null holes");
      
      // this is a shell, so merge any holes currently attached to src face
      addHoles(srcFace.holeFaces);
    }
    
    // shell should not be set if still merging
    // (This means that it does not have to be updated)
    //Assert.isTrue(shell == null);
    //Assert.isTrue(srcFace.shell == null);

	}
	
	/*
  public boolean isExteriorHole()
  {
    return isHole && TopologyLabel.isLeftExterior(getStartEdge().topoLabel);
  }
  */
	
  /**
   * Tests if this face is closed.
   * This is the case if the chain of edges
   * from the start edge determined by next pointers
   * is a ring
   * 
   * @return true if the face is closed
   */
  public boolean isClosed()
  {
    HalfEdge e = getStartEdge();
    do {
      HalfEdge eNext = e.next();
      if (eNext == null) {
        return false;
      }
      e = eNext;
    } while (e != getStartEdge());
    return true;
  }
  
  // TODO: build coordinates only once, along with topological information
  private Coordinate[] getCoordinates(boolean requireRing)
  {
    CoordinateList pts = new CoordinateList();
    HalfEdge e = getStartEdge();
    do {
      // compute interior status
      // test if face is interior by virtue of being directly interior to an input polygon
      //if (TopologyLabel.isRightInterior(e.topoLabel))
      if (TopologyDepth.isInteriorRight(e.topoDepth))
        isInterior = true;
      
      pts.add(e.orig(), false);
      HalfEdge eNext = e.next();
      
      if (eNext == null) {
        // add last point of edge just added
        pts.add(e.dest(), false);
        if (requireRing) {
          Debug.println("Current face pts:");
          Debug.println(getGeometryDebug(pts.toCoordinateArray()));
        
          throw new TopologyException("Found null next ref while tracing face.  Last point:", e.dest());
        }
        else {
          // just return the unclosed current list of points
          return pts.toCoordinateArray();
        }
      }
      e = eNext;
    } while (e != getStartEdge());
    
    // TODO: test whether face is interior to some world face as well

    pts.closeRing();
    return pts.toCoordinateArray();
  }
  
  public LinearRing getRing(GeometryFactory geomFact)
  {
    return geomFact.createLinearRing(getCoordinates(true));
  }
  
  public Geometry getRawGeometry(GeometryFactory geomFact)
  {
    return buildRawPolygon(geomFact);
  }

  private Polygon buildRawPolygon(GeometryFactory geomFact)
  {
    int nHoles = 0;
    if (holeFaces != null) 
      nHoles = holeFaces.size();
    LinearRing[] holeRings = new LinearRing[nHoles];
    for (int i = 0; i < nHoles; i++) {
      holeRings[i] = (LinearRing) ((Face) holeFaces.get(i)).getRing(geomFact);
    }
    LinearRing shellRing = getRing(geomFact);
    return geomFact.createPolygon(shellRing, holeRings);
  }
  

  /**
   * Gets a geometry representing the face.
   * The Geometry is created as a:
   * <ul>
   * <li>Polygon (possibly with holes), if the face is a polygon
   * <li>LinearRing, if the face is a hole
   * </ul>
   * @param geomFact
   * @return the geometry for the face
   */
  public Geometry getGeometry(GeometryFactory geomFact)
  {
    return getGeometry(geomFact, true);
  }
  
  /**
   * Gets a geometry representing the face.
   * The Geometry is created as a:
   * <ul>
   * <li>Polygon (possibly with holes), if the face is a polygon
   * <li>LinearRing, if the face is a hole and holeAsRing is <code>true</code>
   * </ul>
   * 
   * @param geomFact
   * @param holeAsRing indicates if a face hole should be returned as a ring
   * @return the geometry for the face
   */
  public Geometry getGeometry(GeometryFactory geomFact, boolean holeAsRing)
  {
    if (isHole && holeAsRing) {
      return getRing(geomFact);
    }
    
    return buildPolygon(geomFact);
  }
  
  
  private Polygon buildPolygon(GeometryFactory geomFact)
  {
    FaceRingBuilder polyBuilder = new FaceRingBuilder(this, false, geomFact);
    LinearRing shellRing = polyBuilder.getShell();
    // check for collapsed polygon
    if (shellRing == null) return null;
    
    List<LinearRing> polyHoleRings = polyBuilder.getHoles();
    // Add all holes into final polygon
    //LinearRing[] holeRings = collectHoles(polyHoleRings, geomFact);
    addHoles(polyHoleRings, geomFact);
    LinearRing[] holes = GeometryFactory.toLinearRingArray(polyHoleRings);
    
    return geomFact.createPolygon(shellRing, holes);
  }

  private void addHoles(List<LinearRing> holes, GeometryFactory geomFact)
  {
    int nHoleFaces = 0;
    if (holeFaces != null)
      nHoleFaces = holeFaces.size();
    for (int i = 0; i < nHoleFaces; i++) {
      holes.addAll(FaceRingBuilder.buildHoles(holeFaces.get(i), geomFact));
    }
  }
  
  private boolean isRing()
  {
    Coordinate[] pts = getCoordinates(false);
    return CoordinateArrays.isRing(pts);
  }
  
  private static Geometry getGeometryDebug(Coordinate[] pts)
  {
    return (new GeometryFactory()).createLineString(pts);
  }
  
  public Geometry getPartialGeometry()
  {
    if (! isRing())
      return getGeometryDebug(getCoordinates(false));

    return getRawGeometry(new GeometryFactory());
  }
  
  /**
   * Removes this face and its {@link HalfEdge}s.
   * This frees the memory consumed by the face and edges.
   * HalfEdges are detached from their syms.
   * Since this face and its edges are never revisited,
   * the unlinking does not cause problems.
   *
   */
  public void remove()
  {
    holeFaces = null;
    shell = null;
    
    HalfEdge e = getStartEdge();
    while (true) {
      HalfEdge nextEdge = e.next();
      // detach e from its sym
      HalfEdge sym = e.sym();
      if (sym != null) sym.removeSym();
      
      e = nextEdge;
      
      if (e == getStartEdge())
        break;
    }
  }
  
  public String toString()
  {
    return getPartialGeometry().toString();
  }
}
