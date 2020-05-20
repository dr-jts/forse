package forse.polygonize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.util.Assert;
import org.locationtech.jts.util.ObjectCounter;

import forse.util.Comparators;

/**
 * Records all current "loose" edges (edges which 
 * are not linked at one end).
 * The status is updated at every node entry and exit event.
 * Edges are indexed by their origin coordinate, which is the loose node.
 * 
 * HalfEdges in this structure always have <b>reverse</b> orientation
 * relative to the underlying LineSeg,
 * since the loose end must occur ahead of the sweeepline.
 * 
 * @author Martin Davis
 */
public class EdgeStatus 
{
  // need to use a TreeMap, since an ordered structure 
  // is required to be able to access lowest key
	private TreeMap nodeMap = new TreeMap();
	
	public EdgeStatus()
	{
		
	}
	
	public void add(HalfEdge edge)
	{
		Assert.isTrue(! edge.isOrdered());
		
		Coordinate nodePt = edge.orig();
		List edgeList = (List) nodeMap.get(nodePt);
		if (edgeList == null) {
			edgeList = new ArrayList();
			nodeMap.put(nodePt, edgeList);
		}
		edgeList.add(edge);
	}
		
	public void addSym(Collection edges)
	{
		for (Iterator i = edges.iterator(); i.hasNext(); ) {
			HalfEdge e = (HalfEdge) i.next();
//			System.out.println("Adding sym edge " + e.sym());
			add(e.sym());
		}
	}
	
	public boolean isEmpty() { return nodeMap.isEmpty(); }
	
	public Coordinate firstKey()
	{
		return (Coordinate) nodeMap.firstKey();
	}
	/**
	 * 
	 * @param p
	 * @return
	 * @return null if there are no edges ending at the point
	 */
	public List getEdges(Coordinate p)
	{
		return (List) nodeMap.get(p);
	}
	
	/** 
	 * Removes the edges incident on a given point from the status.
	 * 
	 * @param p the point to remove
	 */
	public void remove(Coordinate p)
	{
		nodeMap.remove(p);
	}
	
	public boolean containsKey(Coordinate p)
	{
		return nodeMap.containsKey(p);
	}
	
	/*
	public void remove(Edge edge)
	{
		Coordinate nodePt = edge.orig();
		List edgeList = (List) nodeMap.get(nodePt);
		if (edgeList != null) {
			edgeList.remove(edge);
			// remove the edgelist as well if empty
			if (edgeList.isEmpty())
				nodeMap.remove(nodePt);
		}
	}
	*/
	
	public List getEdges()
	{
		List edges = new ArrayList();
		Collection edgeLists = nodeMap.values();
		for (Iterator i = edgeLists.iterator(); i.hasNext(); ) {
			List nodeEdges = (List) i.next();
			edges.addAll(nodeEdges);
		}
		return edges;
	}
	
  /*
  public List OLDgetEdgesBelow(HalfEdge queryEdge)
  {
    List edges = new ArrayList();
    Collection edgeLists = nodeMap.values();
    for (Iterator i = edgeLists.iterator(); i.hasNext(); ) {
      List nodeEdges = (List) i.next();
      for (Iterator j = nodeEdges.iterator(); j.hasNext(); ) {
        HalfEdge e = (HalfEdge) j.next();
        if (HalfEdge.ABOVE_COMP.compare(queryEdge, e) > 0)
          edges.add(e);
      }
    }
    return edges;
  }
  */
  
  public List getEdgesBelowAndLeft(HalfEdge queryEdge)
  {
    Coordinate queryPt = queryEdge.dest();
    List edges = new ArrayList();
    Collection edgeLists = nodeMap.values();
    for (Iterator i = edgeLists.iterator(); i.hasNext(); ) {
      List nodeEdges = (List) i.next();
      for (Iterator j = nodeEdges.iterator(); j.hasNext(); ) {
        HalfEdge e = (HalfEdge) j.next();
        // assert: dest() is greater than orig()
        if (e.isBelow(queryPt) && e.extendsLeftOf(queryPt))
          edges.add(e);
      }
    }
    return edges;
  }
  public List getEdgesBelow(HalfEdge queryEdge)
  {
    Coordinate queryPt = queryEdge.dest();
    List edges = new ArrayList();
    Collection edgeLists = nodeMap.values();
    for (Iterator i = edgeLists.iterator(); i.hasNext(); ) {
      List nodeEdges = (List) i.next();
      for (Iterator j = nodeEdges.iterator(); j.hasNext(); ) {
        HalfEdge e = (HalfEdge) j.next();
        // assert: dest() is greater than orig()
        if (e.isBelow(queryPt))
          edges.add(e);
      }
    }
    return edges;
  }
	
	/**
	 * Gets the highest edge below a query edge which
	 * has a face with an odd edge occurrence count.
	 * This is used to find the face which contains a hole face. 
   * There may be no such face.  In this case, the hole
   * is part of the world face.
	 * 
	 * @param queryEdge
	 * @return
	 */
  public HalfEdge getHighestOddFaceEdgeBelow(HalfEdge queryEdge)
  {
//    System.out.println("...Finding highest odd face below " + queryEdge);
    
    List edges = getEdgesBelow(queryEdge);
    
    // TODO: replace this with OddParityHashSet
    // count faces
    ObjectCounter counter = new ObjectCounter();
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      HalfEdge e = (HalfEdge) i.next();
      counter.add(e.face());
      counter.add(e.sym().face());
    }
    
    // TODO: keep edges in sort order in edge status to avoid this step?
    Collections.sort(edges, HalfEdge.ABOVE_COMP);
    Collections.reverse(edges);
    
    // find highest odd face
    HalfEdge highestOddEdge = null;
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      HalfEdge e = (HalfEdge) i.next();
      
//      System.out.println(e);
      
      /**
       * As in the standard stabbing-line algorithm,
       * vertical edges are ignored.
       * The face parity is still determined correctly
       * by the non-vertical edge(s) incident on it.
       */
      if (e.isVertical())
        continue;
      
      if (counter.count(e.face()) % 2 == 1) {
        highestOddEdge = e;
        break;
      }
      if (counter.count(e.sym().face()) % 2 == 1) {
        highestOddEdge = e;
        break;
      }
    }
  
//    System.out.println("Highest odd edge = " + highestOddEdge);

    return highestOddEdge;
  }
  
  public Face getLowestOddFaceBelow(HalfEdge queryEdge)
  {
    return getOddFaceBelow(queryEdge, false);
  }
  
  public Face getHighestOddFaceBelow(HalfEdge queryEdge)
  {
    return getOddFaceBelow(queryEdge, true);
  }
  
  private Face getOddFaceBelow(HalfEdge queryEdge, boolean findHighest)
  {
//    System.out.println("...Finding highest odd face below " + queryEdge);
    
    List edgesBelow = getEdgesBelow(queryEdge);
    
    // TODO: replace this with OddParityHashSet
    // count faces
    ObjectCounter counter = new ObjectCounter();
    for (Iterator i = edgesBelow.iterator(); i.hasNext(); ) {
      HalfEdge e = (HalfEdge) i.next();
      // don't count vertical edges
      if (e.isVertical()) continue;
      counter.add(e.face());
      counter.add(e.sym().face());
    }
    //Comparators.verifyTransitivity(HalfEdge.ABOVE_COMP, edgesBelow);
    // TODO: keep edges in sort order in edge status to avoid this step?
    // sort edges from lowest to highest
    Collections.sort(edgesBelow, HalfEdge.ABOVE_COMP);
    if (findHighest) 
      Collections.reverse(edgesBelow);
    
    // find highest odd face
    HalfEdge foundOddFaceEdge = null;
    for (Iterator i = edgesBelow.iterator(); i.hasNext(); ) {
      HalfEdge e = (HalfEdge) i.next();
      
//      System.out.println(e);
      
      /**
       * As in the standard stabbing-line algorithm,
       * vertical edges are ignored.
       * The face parity is still determined correctly
       * by the non-vertical edge(s) incident on it.
       */
      if (e.isVertical())
        continue;
      
      boolean edgeFaceOdd = counter.count(e.face()) % 2 == 1;
      boolean symFaceOdd = counter.count(e.sym().face()) % 2 == 1;
      
      if (edgeFaceOdd && symFaceOdd) {
        if (findHighest) 
          return e.upper().face();
        else 
          return e.lower().face();
      }
      else if (edgeFaceOdd) {
        return e.face();
      }
      else if (symFaceOdd) {
        return e.sym().face();
      }
    }
    return null;
  }
  
  public int getDepth(HalfEdge queryEdge)
  {
    //System.out.println("D...Finding depth of " + queryEdge);
    // assert: queryEdge points rightward
    /*
    if (! queryEdge.isRightward()) {
      System.out.println("NOT rightward! " + queryEdge);
    }
    */
    
    int depth = 0;
    List edges = getEdgesBelowAndLeft(queryEdge);
    for (Iterator i = edges.iterator(); i.hasNext(); ) {
      HalfEdge e = (HalfEdge) i.next();
//      System.out.println(e);
      
      /**
       * As in the standard stabbing-line algorithm, vertical edges are ignored.
       * The face parity is still determined correctly
       * by the non-vertical edge(s) incident on the stabbing line.
       */
      if (e.isVertical())
        continue;
      depth += e.topoDepth();
    }
    /**
     * Add in edge of face after current one, to represent starting from a point inside the face
     */
    HalfEdge eNextNonVert = queryEdge.nextNonVertical();
    depth += eNextNonVert.topoDepth();
    
    return depth;
  }
}
