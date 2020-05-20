package forse;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import forse.geomsink.GeometryCreatorSink;
import forse.lineseg.GeometrySegmentExtracterSource;
import forse.noding.SweepLineNoder;
import forse.polygonize.SweepLinePolygonizer;
import junit.framework.TestCase;
import junit.textui.TestRunner;



/**
 * Simple test cases to validate polygon overlay.
 * 
 * @author mbdavis
 */
public class PolygonUnionTest 
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(PolygonUnionTest.class);
  }
  
	static final double COMPARISON_TOLERANCE = 1.0e-7;

	private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader();

  public PolygonUnionTest(String name) { super(name); }

  public void testDebug()
  throws ParseException
  {
    //testCovering();
    testHoleNestedInsideHole();
  }

  public void testEdgeTouchPolyInsideHole()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((10 10, 10 90, 90 90, 90 10, 10 10), (20 20, 20 80, 80 80, 80 20, 20 20)), ((20 70, 50 70, 50 30, 20 30, 20 70)))";
    String expected = "POLYGON ((10 90, 10 10, 90 10, 90 90, 10 90), (80 20, 20 20, 20 30, 50 30, 50 70, 20 70, 20 80, 80 80, 80 20))";
    checkUnion(wkt0, expected);
  }

  public void testCovering()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((20 30, 30 30, 30 20, 20 20, 20 30)), ((0 0, 0 50, 50 50, 50 0, 0 0)))";
    String expected = "POLYGON ((0 0, 0 50, 50 50, 50 0, 0 0))";
    checkUnion(wkt0, expected);
  }
  public void testMultipleCovering()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((30 40, 40 40, 40 30, 30 30, 30 40)), ((20 50, 50 50, 50 20, 20 20, 20 50)), ((10 60, 60 60, 60 10, 10 10, 10 60)), ((0 70, 70 70, 70 0, 0 0, 0 70)))";
    String expected = "POLYGON ((0 70, 70 70, 70 0, 0 0, 0 70))";
    checkUnion(wkt0, expected);
  }
  public void testNestedInsideHole()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((20 30, 30 30, 30 20, 20 20, 20 30)), ((0 50, 50 50, 50 0, 0 0, 0 50), (10 40, 40 40, 40 10, 10 10, 10 40)))";
    String expected = wkt0;
    checkUnion(wkt0, expected);
  }
  public void testHoleNestedInsideHole()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((20 50, 50 50, 50 20, 20 20, 20 50), (30 40, 40 40, 40 30, 30 30, 30 40)), ((0 70, 70 70, 70 0, 0 0, 0 70), (10 60, 60 60, 60 10, 10 10, 10 60)))";
    String expected = wkt0;
    checkUnion(wkt0, expected);
  }
  public void testPartiallyOverlapping()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((10 10, 10 40, 40 40, 40 10, 10 10)), ((50 50, 50 20, 20 20, 20 50, 50 50)))";
    String expected = "POLYGON ((10 40, 10 10, 40 10, 40 20, 50 20, 50 50, 20 50, 20 40, 10 40))";
    checkUnion(wkt0, expected);
  }
  public void testSinglePolygonWithHole()
  throws ParseException
  {
    String wkt0 = "POLYGON ((10 10, 10 70, 70 70, 70 10, 10 10), (20 20, 20 60, 60 60, 60 20, 20 20))";
    String expected = wkt0;
    checkUnion(wkt0, expected);
  }
  public void testAdjacentHorizontal()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((0 10, 10 10, 10 0, 0 0, 0 10)), ((20 10, 20 0, 10 0, 10 10, 20 10)))";
    String expected = "POLYGON ((0 10, 0 0, 10 0, 20 0, 20 10, 10 10, 0 10))";
    checkUnion(wkt0, expected);
  }
  public void testAdjacentVertical()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((0 10, 10 10, 10 0, 0 0, 0 10)), ((10 20, 10 10, 0 10, 0 20, 10 20)))";
    String expected = "POLYGON ((0 10, 0 0, 10 0, 10 10, 10 20, 0 20, 0 10))";
    checkUnion(wkt0, expected);
  }
  public void testAbove()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((10 0, 0 0, 0 50, 10 50, 10 0)), ((50 0, 10 0, 10 10, 50 10, 50 0)), ((30 30, 20 30, 20 40, 30 40, 30 30)))";
    String expected = "MULTIPOLYGON (((50 10, 10 10, 10 50, 0 50, 0 0, 10 0, 50 0, 50 10)), ((30 30, 20 30, 20 40, 30 40, 30 30)))";
    checkUnion(wkt0, expected);
  }
  public void testOverlappingFormingHole()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((10 0, 0 0, 0 30, 10 30, 10 0)), ((30 0, 0 0, 0 10, 30 10, 30 0)), ((30 0, 20 0, 20 30, 30 30, 30 0)), ((30 20, 0 20, 0 30, 30 30, 30 20)))";
    String expected = "POLYGON ((0 10, 0 0, 10 0, 20 0, 30 0, 30 10, 30 20, 30 30, 20 30, 10 30, 0 30, 0 20, 0 10),(10 20, 20 20, 20 10, 10 10, 10 20))";
    checkUnion(wkt0, expected);
  }

  void checkUnion(String wkt0, String expectedWKT)
  throws ParseException
  {
    checkUnion(wkt0, null, expectedWKT);
  }

  void checkUnion(String wkt0, String wkt1, String expectedWKT)
  throws ParseException
  {
    Geometry g1 = reader.read(wkt0);
    Geometry g2 = null;
    if (wkt1 != null) g2 = reader.read(wkt1);
    
    // set up pipeline
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
    SweepLineNoder noder = new SweepLineNoder();
    SweepLinePolygonizer polygonizer = new SweepLinePolygonizer(geomFact);
    polygonizer.setUnion(true);
    GeometryCreatorSink polySink = new GeometryCreatorSink(geomFact);
    
    extracter.setSink(noder);
    noder.setSink(polygonizer);
    polygonizer.setSink(polySink);
    
    extracter.add(g1);
    if (g2 != null) extracter.add(g2);
    extracter.extract();
    
    Geometry result = polySink.getGeometry();
    
    //System.out.println(result);
    
    //TODO: extract polygons from both result and expected and compare them (to eliminate GC/MultiPoly clashes)
    
    Geometry expected = reader.read(expectedWKT);
    result.normalize();
    expected.normalize();
    boolean isExpectedResult = PolygonOverlayTest.equalsExactComponents(result, expected, COMPARISON_TOLERANCE);
    if (! isExpectedResult) {
      System.out.println("Expected: ");
      System.out.println(expected);
      System.out.println("Actual: ");
      System.out.println(result);
    }
    assertTrue(isExpectedResult);
  }
  
  void OLDcheckUnion(String wkt0, String wkt1, String expectedWKT)
  throws ParseException
  {
    Geometry g1 = reader.read(wkt0);
    Geometry g2 = null;
    if (wkt1 != null) g2 = reader.read(wkt1);
    
    // set up pipeline
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
    SweepLineNoder noder = new SweepLineNoder();
    SweepLinePolygonizer polygonizer = new SweepLinePolygonizer(geomFact);
    polygonizer.setUnion(true);
    GeometryCreatorSink polySink = new GeometryCreatorSink(geomFact);
    
    extracter.setSink(noder);
    noder.setSink(polygonizer);
    polygonizer.setSink(polySink);
    
    extracter.add(g1);
    if (g2 != null) extracter.add(g2);
    extracter.extract();
    
    Geometry result = polySink.getGeometry();
    
    //System.out.println(result);
    
    //TODO: extract polygons from both result and expected and compare them (to eliminate GC/MultiPoly clashes)
    
    Geometry expected = reader.read(expectedWKT);
    result.normalize();
    expected.normalize();
    boolean isExpectedResult = PolygonOverlayTest.equalsExactComponents(result, expected, COMPARISON_TOLERANCE);
    if (! isExpectedResult) {
      System.out.println("Expected: ");
      System.out.println(expected);
      System.out.println("Actual: ");
      System.out.println(result);
    }
    assertTrue(isExpectedResult);
  }

}
