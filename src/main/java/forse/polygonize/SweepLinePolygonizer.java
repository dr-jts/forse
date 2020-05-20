package forse.polygonize;

import java.util.*;
import org.locationtech.jts.algorithm.*;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.util.*;
import forse.geomsink.GeometrySink;
import forse.lineseg.LineSeg;
import forse.lineseg.SegmentSink;

public class SweepLinePolygonizer 
implements SegmentSink
{
	private GeometryFactory geomFact;
	
  private Coordinate currStartPoint = null;
  private List currSegs = new ArrayList();
  private EdgeStatus edgeStatus = new EdgeStatus();
  private GeometrySink geomSink;
  private boolean isUnion = false;
  private boolean unionCreateHolesAsPolys = false;
  
  public SweepLinePolygonizer(GeometryFactory geomFact)
  {
  	this.geomFact = geomFact;
  }
  
  public void setUnion(boolean isUnion)
  {
    this.isUnion = isUnion;
  }
  
  public void setUnionCreateHolesAsPolys(boolean unionCreateHolesAsPolys)
  {
    this.unionCreateHolesAsPolys = unionCreateHolesAsPolys;
  }
  
  public void setSink(GeometrySink geomSink)
  {
  	this.geomSink = geomSink;
  }
  
  private boolean hasSameStartPointAsCurrent(LineSeg seg)
  {
  	if (currStartPoint == null) return true;
  	return seg.getX0() == currStartPoint.x
  			&& seg.getY0() == currStartPoint.y;
  }
  
  // debugging only
  private Set segTracker = new HashSet();
  
  public void process(LineSeg seg)
  {
    //DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
  	if (Debug.isDebugging()) {
  	  Debug.println("Adding: " + seg);
  		if (segTracker.contains(seg))
  			throw new IllegalStateException("Duplicate seg: " + seg);
  		segTracker.add(seg);
  	}
  	//DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
    
  	// accumulate all segs which originate at the current point and process them as a batch
  	if (hasSameStartPointAsCurrent(seg)) {
  		currSegs.add(seg);
  		if (currStartPoint == null)
  			currStartPoint = seg.getCoordinate(0);
  	}
  	else {
      /**
       * Segment has new start point
       * Process any previous unprocessed nodes.
       * Then process the segments around this node.
       */
  		processNodesBefore(currStartPoint);
    	processNode(currStartPoint, currSegs);
  		
      //currSegs = new ArrayList();
      // is this faster?
      currSegs.clear();
  		currSegs.add(seg);
  		currStartPoint = seg.getCoordinate(0);
  	}
  }
  
  public void close()
  {
		// process any previous unprocessed nodes
		processNodesBefore(currStartPoint);
		// flush segment buffer by processing final current node
  	processNode(currStartPoint, currSegs);
  	// process any remaining nodes
		processNodesBefore(null);
		
		geomSink.close();
  }

  /**
   * Processes nodes in the edgeStatus prior to the given point.
   * If argument is null, process all remaining nodes in edgeStatus.
   * Nodes are processed in sweepline order.
   * 
   * @param nodePt the point to process up to, or null
   */
  private void processNodesBefore(Coordinate nodePt)
  {
  	while (true) {
  		// check if done
  		if (edgeStatus.isEmpty()) 
  			return;
  		
  		Coordinate pt = edgeStatus.firstKey();
  		// check if all prior nodes have been processed
  		if (nodePt != null && pt.compareTo(nodePt) >= 0) 
  			return;
  		
    	processNode(pt, null);
    	//Assert.isTrue(! edgeStatus.containsKey(pt), "previous node point was not removed from status");
  	}
  }
  
  /**
   * Process a node which the sweepline is passing
   * to form the local topology around the node.
   * At this point all segments which terminate
   * at this node are present.
   * The segments are converted to HalfEdges,
   * and the edges are linked to form the
   * topology around the node.
   * Finally the implied faces are assigned and/or updated.
   * 
   * @param nodePt the node point
   * @param originatingSegs new segments to add to the node
   */
  private void processNode(Coordinate nodePt, List originatingSegs)
  {
    if (Debug.isDebugging()) {
      Debug.println("Processing Node: " + nodePt);
      //Debug.breakIfEqual(nodePt, new Coordinate(420, 190), .01);
    }
    
  	List prevNodeEdges = edgeStatus.getEdges(nodePt);
  	edgeStatus.remove(nodePt);
		//Assert.isTrue(! edgeStatus.containsKey(nodePt));
  	
  	List originatingEdges = toEdges(originatingSegs);
  	// add the syms of the new edges to ensure the other end nodes are present
  	edgeStatus.addSym(originatingEdges);
  	
    // combine all edges at this node for linking
    List nodeEdges = originatingEdges;
  	if (prevNodeEdges != null) {
  		nodeEdges.addAll(prevNodeEdges);
  	}
  	
  	linkEdgesAroundNode(nodeEdges);
  	updateFaces(nodeEdges);
  }

  private static List toEdges(List segs)
  {
  	List edges = new ArrayList();
  	if (segs == null) return edges;
  	for (Iterator i = segs.iterator(); i.hasNext(); ) {
  		LineSeg seg = (LineSeg) i.next();
  		HalfEdge edge = HalfEdge.makeEdge(
          seg.getCoordinate(0), seg.getCoordinate(1), 
          seg.getTopoLabel(), seg.getDepth());
  		edges.add(edge);
  	}
  	return edges;
  }
  
  /**
   * Links the halfedges around a node through their next/prev pointers.
   * 
   * @param nodeEdges a list of all halfedges originating at a node
   */
  private void linkEdgesAroundNode(List nodeEdges)
  {
  	// Assert: all edges have the same origin
  	
  	// sort edges in CCW order around origin
    // this works because edges compare according to the their CCW order, starting at the positive X axis
  	Collections.sort(nodeEdges);
  	
    //System.out.println("Linking edges around " + nodeEdges.get(0));
  	// link next pointers of sym edges
  	int n = nodeEdges.size();
  	for (int i = 0; i < n; i++) {
  		int nexti = (i + 1) < n ? i + 1 : 0;
  		HalfEdge ei = (HalfEdge) nodeEdges.get(i);
      //System.out.println(ei);
      
      // DEBUGGING
      // if (ei.face() != null) ei.face().toString();
      
  		HalfEdge eNext = (HalfEdge) nodeEdges.get(nexti);
  		
  		// Assert: ei.sym().next() == null
  		// Assert: eNext.prev() == null
  		// Assert: ei.sym().dest() = eNext.orig()
  		Assert.equals(ei.sym().dest(), eNext.orig(), "linked face edges have inconsistent vertices");
  		
  		// link next pointer
  		ei.sym().setNext(eNext);
  	}
  }
  
  /**
   * Updates the face pointers of edges around a node.
   * After the edges around a node are linked, the Face pointers of
   * the edges around the node may be different 
   * (ie. inconsistent across a single face)
   * or null (in the case of new edges).
   * Updates are:
   * <ul>
   * <li>Adjacent edges with different Faces: 
   * faces are merged to make the topological structure consistent
   * (one Face is kept, one is discarded)
   * <li>Adjacent edges with the same Face: indicates that the Face ring is complete.
   * <li>Adjacent edges with only one edge having a Face: set face for other edge
   * <li>Adjacent edges with neither edge having a Face: create a new Face and assign it to both edges
   * </ul>
   * 
   * @param nodeEdges the edges around the node, in CCW order
   */
  private void updateFaces(List nodeEdges)
  {
  	//Debug.breakIfEqual( ((HalfEdge)nodeEdges.get(0)).orig(), new Coordinate(264.20260782347043, 160.29087261785355), .01);

  	// Assert: nodeEdges are sorted in CCW order
  	int n = nodeEdges.size();
  	for (int i = 0; i < n; i++) {

  		HalfEdge edge = (HalfEdge) nodeEdges.get(i);
  		// Get edge of face to L of edge
  		HalfEdge eSym = edge.sym();
  		// the next edge CW around the face (also the next edge CCW around node)
  		HalfEdge eNext = eSym.next();
  		
      /**
       * These are the two faces 
       * to the L (CCW) of the current edge 
       * around the node
       */
  		Face face = eSym.face();
  		Face faceNext = eNext.face();
  		
  	   // Adjacent edges with neither edge having a Face: create a new Face and assign it to both edges
  		if (face == null && faceNext == null) {
  			Face newFace = new Face(eSym);
  			eSym.setFace(newFace);
  			eNext.setFace(newFace);
  		}
  	  // Adjacent edges with only one edge having a Face: set face for other edge
  		else if (face == null) {
  			eSym.setFace(faceNext);
  		}
  		else if (faceNext == null) {
  			eNext.setFace(face);
  		}
  		else if (face == faceNext) {
        /**
         * If the face is closed, process it.
         * When the two face pointers are the same the face is usually closed,
         * but there are some situations where this is not the case
         * (e.g. the triangle-kite case where the apexes meet at a single node)        
         */
        if (face.isClosed()) {
          boolean removeFace = false;
          if (isUnion) {
            removeFace = processClosedUnionFace(eSym, face);
          }
          else {
            removeFace = processClosedFace(eSym, face);
          }
          /**
           * This call to remove the face can be deleted without affecting correctness
           * of the result.
           * However, it hugely reduces memory footprint when present.
           * 
           * It is not necessary to explicitly remove hole faces, 
           * since they are not linked to any other object in the face/edge graph.
           */
          if (removeFace) face.remove();
        }
  		}
  		else {
  			/**
         * If the faces are different, merge them.
         * One of the faces is discarded.
         * 
  			 * Since the halfedge linking favours
  			 * traversing the face in the forward direction,
  			 * the prev face is propagated forward,
  			 * with the information from the next face
  			 * merged into it.
  			 */
  			face.merge(faceNext);
  			eNext.setFaceForward(face);
  		}
  	}
  }
  
  /**
   * 
   * @param closingEdge
   * @param face
   * @return true if face can be removed
   */
  private boolean processClosedFace(HalfEdge closingEdge, Face face)
  {
    face.build();
    
    // DEBUGGING
    if (face.size() == 32) {
      if (CGAlgorithms.isPointInRing(new Coordinate(148.36,-37.65), face.getPartialGeometry().getCoordinates())) {
        Debug.println(face.size());
        Debug.println(face);
      }
    }
    
    boolean isWorldface = false;
  	if (face.isHole()) {
      isWorldface = ! processHole(closingEdge, face);
  		return false;
  	}
    
    /**
     * At this point the face is known to be 
     * a polygon shell face.  
     * If it is interior to some input polygon
     * emit it.
     * The valid ring check is required for robustness reasons
     * (in a few pathological cases,
     * to remove 3-pt rings)
     */
    boolean isInteriorFace = isInterior(closingEdge, face);

    if (isInteriorFace && face.isValidRing()) {
      Geometry poly = face.getGeometry(geomFact);
      processPolygon(poly);   
    }
    
    /**
     * This return request to remove the face 
     * can be deleted without affecting correctness
     * of the result.
     *   
     * However, it hugely reduces memory footprint when present.
     * 
     * It is not necessary to explicitly remove hole faces, 
     * since they are not linked to any other part of the structure.
     */
    return true;
  }

  private void processPolygon(Geometry poly)
  {
    if (poly == null || poly.isEmpty()) return;
    geomSink.process(poly);
  }

  /**
   * Cleans a raw polygon outline to remove gores and create holes
   * for inverted areas.
   * 
   * Seems like this should be slow, but it actually doesn't seem to have much perf impact.
   * Should probably still be replaced with something more purpose-built, though.
   * 
   * WARNIG: this may result in invalid noding, since the buffer may round the polygon slightly
   * in complex cases.
   * 
   * @param poly
   * @return
   */
  private Geometry cleanInvertedPolygon(Geometry poly)
  {
    //int nptsIn = poly.getNumPoints();
    /**
     * It can happen that "flat" (collapsed) polygons are created.
     * In this case the result of the buffer is an empty polygon. 
     * These are filtered out later.
     */
    Geometry polyClean = poly.buffer(0);
    //int npts = polyClean.getNumPoints();
    return polyClean;
  }
    
  private boolean processClosedUnionFace(HalfEdge closingEdge, Face face)
  {
    face.build();
    boolean isHole = face.isHole();
    boolean isInterior = face.isInterior();
    
    // interior faces are never output
    if (isInterior) {
      return true;
    }
    
    //debugPrint(face);

    /**
     * If face is exterior, then it may be either a hole in the result,
     * or a pseudo-hole which is covered by another input face.
     * If the latter, it is not output.
     * 
     * Test if the face is interior to some other face.
     * This can happen even if the face is labelled as exterior, 
     * since it might be nested inside some other face.
     * (This only happens in overlapping datasets)
     * If so, don't include face in the result.
     * 
     * Only CCW (hole) faces can be world faces.
     * Result holes may be either CW or CCW
     * (e.g. 
     * - an input hole will create a CW hole face;
     * - a hole created by an area isolated by a ring of adjacent polygons will be CCW;
     * ) 
     */
    Face enclosingWorldFace = findEnclosingWorldFace(closingEdge);
    int depth = edgeStatus.getDepth(closingEdge);
    
//    boolean isWorldFace = enclosingWorldFace == null;
    boolean isWorldFace = depth < 0 && isHole;

    boolean isHoleFace = depth == 0 && ! isHole; 
    //if (isWorldFaceHoleFace) { 
      // do not output interior face
    //  return true;
    //}
     
    // Only world faces are output for union
    // (along with any attached exterior hole faces)
    if (isWorldFace 
        || unionCreateHolesAsPolys 
        ) { 
      Geometry poly = face.getGeometry(geomFact, false);
      processPolygon(poly);   
    }
    else if (isHoleFace) {
      enclosingWorldFace.addHole(face);      
    }
  
    return true;
  }
  
  private static void debugPrint(Face face)
  {
    Geometry geom = face.getPartialGeometry();
    if (geom.getNumPoints() < 100)
      System.out.println(geom);
  }
  
  private boolean isInterior(HalfEdge closingEdge, Face face)
  {
    if (closingEdge.topoDepth() > 0) return true;
    // test if face is directly labelled as interior 
    if (face.isInterior()) return true;
    
    // otherwise, still need to check containment in some other enclosing face
    
    /*
    // MD - this approach is almost certainly wrong, so should be deleted.
    //TODO: fix this!  It is not correctly determining enclosing polys
    HalfEdge enclosingFaceEdge = edgeStatus.getHighestOddFaceEdgeBelow(closingEdge);
    boolean isInterior = enclosingFaceEdge != null;
    return isInterior;
    */
    int depth = edgeStatus.getDepth(closingEdge);
    if (depth > 0) return true;
    
    return false;
  }
  
  /**
   * Assigns a hole face to an enclosing polygon.
   * 
   * If the hole can not be assigned to an enclosing polygon,
   * it is assumed to be a world face. 
   * 
   * @param closingEdge the edge which closed the face
   * @param hole the hole face
   * @return true if the hole was assigned to an enclosing polygon
   * @return false if the hole could not be assigned to an enclosing polygon,
   *   and thus is presumably a world face
   */
  private boolean processHole(HalfEdge closingEdge, Face hole)
  {
    /*
    if (Debug.hasSegment(hole.getGeometry(geomFact), 
        new Coordinate(997033.625, 1038805.9375), 
        new Coordinate(997052.6875, 1038833.6875))) {
      System.out.println(closingEdge);
    }
    //*/
    
    // determine face containing hole (if any)
    HalfEdge enclosingFaceEdge = edgeStatus.getHighestOddFaceEdgeBelow(closingEdge);
    // if enclosingFaceEdge is null, hole must be a world hole - do nothing
    if (enclosingFaceEdge == null)
      return false;
    
    // Assert: enclosingFaceEdge is not vertical
    
    // add hole to enclosing face
    HalfEdge upper = enclosingFaceEdge.upper();
//      System.out.println("Adding hole to face with edge " + upper);
//      System.out.println("Shell face is " + face.)
    upper.face().addHole(hole);
    return true;
  }
  
  private Face findEnclosingFace(HalfEdge closingEdge)
  {
    return edgeStatus.getHighestOddFaceBelow(closingEdge);
  }
  
  private Face findEnclosingWorldFace(HalfEdge closingEdge)
  {
    // determine face containing edge (if any)
    Face enclosingFace = edgeStatus.getLowestOddFaceBelow(closingEdge);
    return enclosingFace;
  }
  
}
