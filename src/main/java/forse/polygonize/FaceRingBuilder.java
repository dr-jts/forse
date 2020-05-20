package forse.polygonize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

class FaceRingBuilder
{
  public static List<LinearRing> buildHoles(Face face, GeometryFactory geomFact)
  {
    FaceRingBuilder builder = new FaceRingBuilder(face, true, geomFact);
    return builder.getHoles();
  }
  
  private GeometryFactory geomFact;
  private Face face;
  private LinearRing shell;
  private List<LinearRing> holes = new ArrayList<LinearRing>();
  private Map<Coordinate, HalfEdge> nodeMap = new TreeMap<Coordinate, HalfEdge>();
  private boolean isHole;
  
  public FaceRingBuilder(Face face, boolean isHole, GeometryFactory geomFact)
  {
    this.face = face;
    this.isHole = isHole;
    this.geomFact = geomFact;
    build();
  }
  
  public LinearRing getShell()
  {
    return shell;
  }

  public List<LinearRing> getHoles()
  {
    return holes;
  }

  private void build()
  {
    HalfEdge startEdge = face.findOuterEdge();
    HalfEdge e = startEdge;
    do {
      HalfEdge eNext = e.ringNext();
      if (eNext == startEdge)
        break;
      eNext.setRingPrev(e);
      // TESTING - skip face if final ring is a gore
      if (eNext.equalsReverse(startEdge)) {
        return;
      }
      boolean doAgain = processEdge(eNext);   
      if (! doAgain) {
        e = eNext;        
      }
    } while (e != startEdge);
    
    buildFinalRing(startEdge);
  }

  /**
   * 
   * @param e
   * @param ePrev
   * @return true if edge should be processed again (eg for multi-gores)
   */
  private boolean processEdge(HalfEdge e)
  {
    HalfEdge eNext = e.ringNext();
    
    // check for a leaf gore
    if (e.equalsReverse(eNext)) {
      // set nextRingEdge ref of previous edge, to allow skipping this completed gore subring
      e.ringPrev().setRingNext(eNext.next());
      nodeMap.remove(e.orig());
      //System.out.println("Removed gore: " + e);
      return true;
    }
    
    if (! nodeMap.containsKey(e.dest())) {
      // not a closing edge, so just add it as a candidate node and keep going
      nodeMap.put(e.orig(), e);
      return false;
    }
    
    //----- edge ends at a node, so create the subring containing it
    HalfEdge eRingStart = nodeMap.get(e.dest());
    
    // set nextRingEdge ref of previous edge, to allow skipping this completed subring
    eRingStart.ringPrev().setRingNext(eNext);
    
    // check for a non-leaf gore
    if (eRingStart.equalsReverse(e)) {
      nodeMap.remove(eRingStart.orig());
      //System.out.println("Removed gore: " + e);
      return false;
    }
    
    // scan ring to extract pts, and remove them from nodeMap
    buildHole(eRingStart);
    return true;
  }

  private void buildHole(HalfEdge eRingStart)
  {
    CoordinateList pts = new CoordinateList();
    HalfEdge e = eRingStart;
    Coordinate startNode = e.orig();
    do {
      pts.add(e.orig(), false);
      nodeMap.remove(e.orig());
      
      e = e.ringNext();
    } while (! e.orig().equals2D(startNode));
    pts.closeRing();
    
    // don't save gores
    if (pts.size() == 3)
      return;
    
    LinearRing hole = geomFact.createLinearRing(pts.toCoordinateArray());
    if ( holes.size() > 0 && (holes.get(holes.size()-1).getEnvelopeInternal())
      .equals(hole.getEnvelopeInternal())) {
      System.out.println(this + " - found bad hole");
    }
    holes.add(hole);
  }
  private void buildFinalRing(HalfEdge eRingStart)
  {
    CoordinateList pts = new CoordinateList();
    HalfEdge e = eRingStart;
    do {
      pts.add(e.orig(), false);
      e = e.ringNext();
    } while (e != eRingStart);
    pts.closeRing();
    // check for a collapsed shell
    if (pts.size() < 4) return;
    
    LinearRing ring = geomFact.createLinearRing(pts.toCoordinateArray());
    if (isHole) {
      holes.add(ring);
    }
    else shell = ring;
  }
}
