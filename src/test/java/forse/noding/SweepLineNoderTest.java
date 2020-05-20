package forse.noding;


import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import forse.LineNoder;
import junit.framework.TestCase;
import junit.textui.TestRunner;

public class SweepLineNoderTest 
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(SweepLineNoderTest.class);
  }
  
  static final double COMPARISON_TOLERANCE = 1.0e-7;

  private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader();

  public SweepLineNoderTest(String name) { super(name); }

  /**
   * Case motivated by Tomologic
   * @throws Exception
   */
  public void testTomoBadNoding1()
  throws Exception
  {
    String[] wkt = {
      "LINESTRING (2.8413960098292605 -57.95412725709538, 469.5999060061197 -502.638517323291)",
      "LINESTRING (165.5952348963687 -213.43901077703873, 166.500934440699 -216.47660123364355)",
      "LINESTRING (163.8186706655689 -211.31840377639213, 165.91742520164917 -214.16650750245725)"    
      };
    checkNodingValid(wkt, new PrecisionModel(1e8));
  }

  public void testTomoBadNoding2()
  throws Exception
  {
    String[] wkt = {
        "LINESTRING (36.6251751934 33.7626083503, 27.7609564372 42.6268271065)",
        "LINESTRING (27.243318347 42.1091890162, 32.3825345063 47.2484051755)",
        "LINESTRING (33.4495452632 36.9382382804, 27.243318347 43.1444651967)",
        "LINESTRING (36.6251751934 33.7626083503, 27.243318347 43.1444651967)"
    };
    checkNodingValid(wkt, new PrecisionModel(1e10));
  }

  public void testTomoBadNoding3()
  throws Exception
  {
    String[] wkt = {
      "LINESTRING (-58.00593335955 -1.43739086465, -513.86101637525 -457.29247388035)",
      "LINESTRING (-217.38579586835 -160.3789540324, -220.44442445355 -161.2108426211)",
      "LINESTRING (-215.22279674875 -158.65425425385, -218.1208801283 -160.68343590235)"
    };
    checkNodingValid(wkt, new PrecisionModel(1e10));
  }

  /**
   * Case motivated by Tomologic
   * @throws Exception
   */
  public void testSegmentsNotNodedDueToBadHotPixelIntersectionTest()
  throws Exception
  {
    String[] wkt = {
        "MULTILINESTRING ((1 1, 0 0), (0 1, 1 0))",
    };
    String expected = "MULTILINESTRING ((0 0, 1 1), (0 1, 1 1), (1 0, 1 1))";
    checkNoding(wkt, new PrecisionModel(1), expected);
  }

  public void testSegmentNotNoded()
  throws Exception
  {
    String[] wkt = {
        "MULTILINESTRING ((0 0, 3 17),   (2 4, 3 17),   (2 4, 3 22))",
    };
    String expected = "MULTILINESTRING ((0 0, 3 15), (2 4, 3 15), (3 15, 3 17), (3 17, 3 22))";
    checkNoding(wkt, new PrecisionModel(1), expected);
  }

  public void testDownwardSegment()
  throws Exception
  {
    String[] wkt = {
        "LINESTRING (13 -58, 15 -76)",
        "LINESTRING (16 -76, 13 -59)"
    };
    String expected = "MULTILINESTRING ((13 -61, 15 -76), (13 -61, 16 -76), (13 -61, 13 -59), (13 -59, 13 -58))";
    checkNoding(wkt, new PrecisionModel(1), expected);
  }

  public void testSegmentAheadOfHotPixel()
  throws Exception
  {
    String[] wkt = {
        "MULTILINESTRING ((82 -99, 83 -106), (83 -106, 79 -94), (83 -108, 82 -99))",
    };
    String expected = "MULTILINESTRING ((79 -94, 83 -105), (82 -99, 83 -105), (83 -108, 83 -106), (83 -106, 83 -105))";
    checkNoding(wkt, new PrecisionModel(1), expected);
  }

  private void checkNodingValid(String[] wkt, PrecisionModel precisionModel) throws Exception
  {
    checkNoding(wkt, precisionModel, null);
  }

  void checkNoding(String[] wkt, 
      PrecisionModel pm,
      String expectedWKT) 
  throws Exception 
  {
    MultiLineString result = LineNoder.node(read(wkt), pm);
    
    // just check noding is correct
    if (expectedWKT == null) return;
    
    Geometry expected = reader.read(expectedWKT);

    boolean isExpectedResult = expected.equalsNorm(result);
    if (! isExpectedResult) {
      System.out.println("Expected: ");
      System.out.println(expected);
      System.out.println("Actual: ");
      System.out.println(result);
    }
    assertTrue(isExpectedResult);
  }
  
  Geometry read(String[] wkt) throws ParseException
  {
    GeometryFactory geomFact = null;
    List geoms = new ArrayList();
    for (int i = 0; i < wkt.length; i++) {
      Geometry g = reader.read(wkt[i]);
      geomFact = g.getFactory();
      geoms.add(g);
    }
    return geomFact.buildGeometry(geoms);
  }
}
