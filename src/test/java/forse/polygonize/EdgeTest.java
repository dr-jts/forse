package forse.polygonize;

import org.locationtech.jts.geom.Coordinate;

import junit.framework.TestCase;
import junit.textui.TestRunner;



public class EdgeTest 
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(EdgeTest.class);
  }

  public EdgeTest(String name) { super(name); }

  public void testAbove_debug()
  {
  	runAbove(new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(10,10), 1);
  }
  
  public void testAbove_disjointInY()
  {
  	runAbove(new Coordinate(0,0), new Coordinate(10,10), new Coordinate(0,20), new Coordinate(10,30), -1);
  }
  
  public void testAbove_overlapInY()
  {
  	runAbove(new Coordinate(0,0), new Coordinate(10,10), new Coordinate(0,2), new Coordinate(10,12), -1);
  	runAbove(new Coordinate(0,0), new Coordinate(10,10), new Coordinate(10,10), new Coordinate(10,12), -1);
  }
  
  public void testAbove_overlapInX()
  {
  	runAbove(new Coordinate(0,0), new Coordinate(10,10), new Coordinate(-5,2), new Coordinate(5,6), -1);
  	runAbove(new Coordinate(0,0), new Coordinate(10,10), new Coordinate(5,3), new Coordinate(15,4), 1);
  }
  
  public void testAbove_containedInBothAxes()
  {
  	runAbove(new Coordinate(0,0), new Coordinate(10,10), new Coordinate(3,5), new Coordinate(4,8), -1);
  	runAbove(new Coordinate(0,0), new Coordinate(10,10), new Coordinate(3,8), new Coordinate(4,5), -1);
  }
  
  public void testAbove_verticalAndAngled()
  {
  	runAbove(new Coordinate(0,0), new Coordinate(0,10), new Coordinate(-5,5), new Coordinate(0, 11), -1);
  }
  
  public void testAbove_vertical()
  {
  	// touching
  	runAbove(new Coordinate(0,0), new Coordinate(0,10), new Coordinate(0,10), new Coordinate(0,20), -1);
  	// disjoint
  	runAbove(new Coordinate(0,0), new Coordinate(0,10), new Coordinate(0,20), new Coordinate(0,25), -1);
  }
  
  public void testAbove_horizontal()
  {
  	// touching
  	runAbove(new Coordinate(0,0), new Coordinate(10,0), new Coordinate(10,0), new Coordinate(20,0), -1);
  	// disjoint
  	runAbove(new Coordinate(0,0), new Coordinate(10,0), new Coordinate(20,0), new Coordinate(25,0), -1);
  }
  
  public void testAbove_rightAngle()
  {
  	runAbove(new Coordinate(0,10), new Coordinate(10,10), new Coordinate(10,0), new Coordinate(10,10), 1);
  }
  
	static HalfEdge.DisjointAboveComparator aboveComp = new HalfEdge.DisjointAboveComparator();

  void runAbove(Coordinate p00, Coordinate p01, Coordinate p10, Coordinate p11, int expectedCompareVal)
  {
  	HalfEdge e0 = HalfEdge.makeEdge(p00, p01);
  	HalfEdge e1 = HalfEdge.makeEdge(p10, p11);
  	
  	runAbove(e0, e1, expectedCompareVal);
  	runAbove(e1, e0, -expectedCompareVal);
  }
  
  void runAbove(HalfEdge e0, HalfEdge e1, int expectedCompareVal)
  {
  	int compareVal = aboveComp.compare(e0, e1);
  	if (compareVal != expectedCompareVal) {
  		System.out.println("Comparing " + e0 + " with " + e1 
  				+ " - expected value of " + expectedCompareVal + " not found");
  		
    	int debug = aboveComp.compare(e0, e1);
  	}
   	assertTrue(compareVal == expectedCompareVal);

  }
}
