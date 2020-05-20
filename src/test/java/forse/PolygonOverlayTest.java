package forse;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;



/**
 * Simple test cases to validate polygon overlay.
 * 
 * @author mbdavis
 */
public class PolygonOverlayTest 
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(PolygonOverlayTest.class);
  }
  
	static final double COMPARISON_TOLERANCE = 1.0e-7;

	private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader();

  public PolygonOverlayTest(String name) { super(name); }

  public void testDebug()
  throws ParseException
  {
    //testPolysWithSurroundedArea();
    //testPolygonsSurroundingGap();
    //testGoreBackwardEdge();
    testCollapsedHoleInfiniteLoop();
  }

  public void testBig()
  throws ParseException
  {
    /**
     * Test that overlay code handles topology collapse
     * that results in an inside dangle
     * (gore)
     * (both in input geometry and in output geometry).
     * By removing coincident edges?
     * Or by removing gores during polygonization?
     */
    String wkt0 = "GEOMETRYCOLLECTION (POLYGON ((148.96354274200002 -37.171642532999996, 148.962622308 -37.17192445, 148.962040742 -37.17041065, 148.962797967 -37.169646267, 148.961733633 -37.16797095, 148.961785733 -37.164115658, 148.962450633 -37.163129642, 148.961757433 -37.162163258, 148.961786958 -37.161704542, 148.9617768 -37.161680032999996, 148.961721867 -37.161551583, 148.961666933 -37.1614231, 148.961219808 -37.161136708, 148.960850008 -37.161206608, 148.958982208 -37.163858733, 148.957255483 -37.164727067, 148.955939217 -37.164218983, 148.9499724 -37.17053235, 148.947211167 -37.175826517, 148.94659505 -37.175931192, 148.9463205 -37.175977842, 148.946021108 -37.1760287, 148.946070867 -37.175833533, 148.946032 -37.175741208, 148.94599095 -37.1756437, 148.945949917 -37.1755462, 148.945779017 -37.175140242, 148.946003967 -37.174758092, 148.9457825 -37.17415195, 148.946244083 -37.174158033, 148.947036083 -37.17162345, 148.947918142 -37.17096565, 148.947945158 -37.169676566999996, 148.948479967 -37.16888205, 148.949745467 -37.16788495, 148.950207617 -37.166559142, 148.952946233 -37.164578217, 148.953157492 -37.163726692, 148.953635758 -37.163572258, 148.953740308 -37.163538508, 148.95384485 -37.16350475, 148.955077392 -37.1631068, 148.956770742 -37.163304342, 148.957774133 -37.162890467, 148.957834183 -37.162819058, 148.9587625 -37.161715183, 148.9611632 -37.160175608, 148.962132867 -37.160234208, 148.962803608 -37.162727883, 148.963346367 -37.16343935, 148.962600992 -37.164117617, 148.96290975 -37.164896458, 148.962938317 -37.166121508, 148.963105542 -37.167441342, 148.963440058 -37.168122333, 148.967140533 -37.168190617, 148.967076383 -37.170372883, 148.966875892 -37.170434792, 148.966468183 -37.170677217, 148.965308458 -37.170882192, 148.964622617 -37.170906467, 148.964342417 -37.171352692, 148.96404475 -37.171690417, 148.96354274200002 -37.171642532999996)), POLYGON ((148.9473488 -37.179211683, 148.947420383 -37.177979042, 148.947540492 -37.176940217, 148.947526233 -37.176447967, 148.947043508 -37.176078942, 148.94659505 -37.175931192, 148.947211167 -37.175826517, 148.952386642 -37.174947067, 148.95249745 -37.17492825, 148.952608258 -37.174909408, 148.953207467 -37.174807567, 148.962622308 -37.17192445, 148.96354274200002 -37.171642532999996, 148.96404475 -37.171690417, 148.964342417 -37.171352692, 148.964622617 -37.170906467, 148.965308458 -37.170882192, 148.966468183 -37.170677217, 148.966875892 -37.170434792, 148.967076383 -37.170372883, 148.967152308 -37.170349433, 148.967494858 -37.170079583, 148.967955717 -37.169698692, 148.969038192 -37.169787233, 148.970799958 -37.169628208, 148.9473488 -37.179211683)))";
    String wkt1 = "GEOMETRYCOLLECTION (POLYGON ((148.961219817 -37.161136708, 148.960850008 -37.161206608, 148.958982208 -37.163858733, 148.957255483 -37.164727067, 148.955939208 -37.164218983, 148.9499724 -37.17053235, 148.947211167 -37.175826517, 148.94659505 -37.175931192, 148.9463205 -37.175977842, 148.946021108 -37.1760287, 148.946070867 -37.175833533, 148.946032 -37.175741208, 148.94599095 -37.1756437, 148.945949917 -37.1755462, 148.945779025 -37.175140242, 148.946003967 -37.174758092, 148.945782508 -37.17415195, 148.946244083 -37.174158033, 148.947036092 -37.17162345, 148.947918133 -37.17096565, 148.947945158 -37.169676566999996, 148.948479975 -37.16888205, 148.949745467 -37.16788495, 148.950207625 -37.16655915, 148.952946233 -37.164578217, 148.953157492 -37.163726692, 148.953635758 -37.163572258, 148.953740308 -37.163538508, 148.95384485 -37.16350475, 148.955077392 -37.1631068, 148.956770742 -37.163304342, 148.957774142 -37.162890467, 148.957834183 -37.162819058, 148.958762492 -37.161715183, 148.9611632 -37.160175608, 148.961219817 -37.161136708)), POLYGON ((148.9611632 -37.160175608, 148.962132867 -37.160234208, 148.962803608 -37.162727883, 148.963346367 -37.16343935, 148.962601 -37.164117617, 148.96290975 -37.164896458, 148.962938317 -37.166121508, 148.963105542 -37.167441342, 148.963440067 -37.168122333, 148.967140533 -37.168190617, 148.967076392 -37.170372883, 148.966875892 -37.170434792, 148.966468183 -37.170677217, 148.965308467 -37.170882192, 148.964622617 -37.170906467, 148.964342417 -37.171352692, 148.964044742 -37.171690417, 148.96354274200002 -37.171642532999996, 148.962622308 -37.17192445, 148.962040742 -37.170410642, 148.962797967 -37.169646258, 148.961733633 -37.16797095, 148.961785733 -37.164115658, 148.962450633 -37.163129642, 148.961757433 -37.162163258, 148.96178695 -37.161704542, 148.9617768 -37.161680032999996, 148.961721867 -37.161551583, 148.961666925 -37.1614231, 148.961219817 -37.161136708, 148.9611632 -37.160175608)))";
    String expected = "MULTIPOLYGON (((148.958762 -37.161715, 148.958763 -37.161715, 148.957834 -37.162819, 148.958762 -37.161715)), ((148.961163 -37.160176, 148.96122 -37.161137, 148.96085 -37.161207, 148.958982 -37.163859, 148.957255 -37.164727, 148.955939 -37.164219, 148.949972 -37.170532, 148.947211 -37.175827, 148.946595 -37.175931, 148.946321 -37.175978, 148.946021 -37.176029, 148.946071 -37.175834, 148.946032 -37.175741, 148.945991 -37.175644, 148.94595 -37.175546, 148.945779 -37.17514, 148.946004 -37.174758, 148.945783 -37.174152, 148.946244 -37.174158, 148.947036 -37.171623, 148.947918 -37.170966, 148.947945 -37.169677, 148.94848 -37.168882, 148.949745 -37.167885, 148.950208 -37.166559, 148.952946 -37.164578, 148.953157 -37.163727, 148.953636 -37.163572, 148.95374 -37.163539, 148.953845 -37.163505, 148.955077 -37.163107, 148.956771 -37.163304, 148.957774 -37.16289, 148.957834 -37.162819, 148.958763 -37.161715, 148.961163 -37.160176)), ((148.96344 -37.168122, 148.967141 -37.168191, 148.967076 -37.170373, 148.966876 -37.170435, 148.966468 -37.170677, 148.965308 -37.170882, 148.964623 -37.170906, 148.964342 -37.171353, 148.964045 -37.17169, 148.963543 -37.171643, 148.962622 -37.171924, 148.962041 -37.170411, 148.962798 -37.169646, 148.961734 -37.167971, 148.961786 -37.164116, 148.962451 -37.16313, 148.961757 -37.162163, 148.961787 -37.161705, 148.961777 -37.16168, 148.961722 -37.161552, 148.961667 -37.161423, 148.96122 -37.161137, 148.961163 -37.160176, 148.962133 -37.160234, 148.962804 -37.162728, 148.963346 -37.163439, 148.962601 -37.164118, 148.96291 -37.164896, 148.962938 -37.166122, 148.963106 -37.167441, 148.96344 -37.168122)), ((148.969038 -37.169787, 148.9708 -37.169628, 148.947349 -37.179212, 148.94742 -37.177979, 148.94754 -37.17694, 148.947526 -37.176448, 148.947044 -37.176079, 148.946595 -37.175931, 148.947211 -37.175827, 148.952387 -37.174947, 148.952497 -37.174928, 148.952608 -37.174909, 148.953207 -37.174808, 148.962622 -37.171924, 148.963543 -37.171643, 148.964045 -37.17169, 148.964342 -37.171353, 148.964623 -37.170906, 148.965308 -37.170882, 148.966468 -37.170677, 148.966876 -37.170435, 148.967076 -37.170373, 148.967152 -37.170349, 148.967495 -37.17008, 148.967956 -37.169699, 148.969038 -37.169787)))";
    checkOverlay(wkt0, wkt1, 1000000, expected);
  }

  public void testSquare()
  throws ParseException
  {
    /**
     * Test that overlay code handles topology collapse
     * that results in an inside dangle
     * (gore)
     * (both in input geometry and in output geometry).
     * By removing coincident edges?
     * Or by removing gores during polygonization?
     */
    String wkt0 = "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))";
    String expected = "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))";
    checkOverlay(wkt0, null, expected);
  }

  public void testTwoSquares()
  throws ParseException
  {
    /**
     * Test that overlay code handles topology collapse
     * that results in an inside dangle
     * (gore)
     * (both in input geometry and in output geometry).
     * By removing coincident edges?
     * Or by removing gores during polygonization?
     */
    String wkt0 = "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))";
    String wkt1 = "POLYGON ((300 200, 300 100, 200 100, 200 200, 300 200))";
    String expected = "POLYGON ((100 200, 200 200, 200 100, 100 100, 100 200))";
    checkOverlay(wkt0, null, expected);
  }

  public void testGoreBackwardEdge()
  throws ParseException
  {
    /**
     * Test that overlay code handles topology collapse
     * that results in an inside dangle
     * (gore)
     * (both in input geometry and in output geometry).
     * By removing coincident edges?
     * Or by removing gores during polygonization?
     */
    String wkt0 = "POLYGON ((300 100, 200 200, 300 100, 100 100, 100 300, 300 300, 300 100))";
    String expected = "POLYGON ((300 100, 100 100, 100 300, 300 300, 300 100))";
    checkOverlay(wkt0, null, expected);
  }

  public void testGoreBackwardEdge2()
  throws ParseException
  {
    /**
     * Test that overlay code handles topology collapse
     * that results in an inside dangle
     * (gore)
     * (both in input geometry and in output geometry).
     * By removing coincident edges?
     * Or by removing gores during polygonization?
     */
    String wkt0 = "POLYGON ((300 100, 250 150, 200 200, 250 150, 300 100, 100 100, 100 300, 300 300, 300 100))";
    String expected = "POLYGON ((300 100, 100 100, 100 300, 300 300, 300 100))";
    checkOverlay(wkt0, null, expected);
  }

  public void testGoreForwardEdge()
  throws ParseException
  {
    /**
     * Test that overlay code handles topology collapse
     * (both in input geometry and in output geometry).
     * By removing coincident edges,
     * and by removing dangling edges during polygonization.
     */
    String wkt0 = "POLYGON ((300 100, 100 100, 200 200, 100 100, 100 300, 300 300, 300 100))";
    String expected = "POLYGON ((300 100, 100 100, 100 300, 300 300, 300 100))";
    checkOverlay(wkt0, null, expected);
  }

  public void testBranchingGore()
  throws ParseException
  {
    /**
     * A branching gore is not fixed by simple gore-removal approaches.
     * It needs the same kind of topological cleaning as a series of touching holes.
     */
    String wkt0 = "POLYGON ((100 100, 100 400, 200 300, 200 200, 200 300, 300 200, 300 150, 300 200, 350 150, 300 200, 350 200, 300 200, 100 400, 400 400, 400 100, 100 100))";
    String expected = "POLYGON ((400 100, 100 100, 100 400, 400 400, 400 100))";
    checkOverlay(wkt0, null, expected);
  }

  public void testMultipleGoreSameNode()
  throws ParseException
  {
    String wkt0 = "POLYGON ((100 100, 100 400, 200 300, 100 400, 200 350, 100 400, 400 400, 400 100, 100 100))";
    String expected = "POLYGON ((400 100, 100 100, 100 400, 400 400, 400 100))";
    checkOverlay(wkt0, null, expected);
  }

  public void testSelfTouchingHole()
  throws ParseException
  {
    /**
     * Tests case of input linework producing an inverted polygon
     * (which should be represented as a polygon with a self-touching hole).
     * Doing this correctly requires converting maximal rings into minimal rings.
     */
    String wkt0 = "POLYGON ((300 100, 200 200, 250 200, 300 100, 100 100, 100 300, 300 300, 300 100))";
    String expected = "GEOMETRYCOLLECTION (POLYGON ((300 100, 200 200, 250 200, 300 100)), POLYGON ((300 100, 100 100, 100 300, 300 300, 300 100), (250 200, 200 200, 300 100, 250 200)))";
    checkOverlay(wkt0, null, expected);
  }

  public void testHoleCollapsingToGore()
  throws ParseException
  {
    /**
     * This case revealed issues with GoreRemover.
     * 
     * Precision used = 10000000
     */
    String wkt0 = "POLYGON ((148.59199652000007 -37.40806689099992, 148.59136735000004 -37.407861849999904, 148.59150100800002 -37.40640220799992, 148.59149822231305 -37.4052, 148.59172214867263 -37.4052, 148.59171809200006 -37.40653929399991, 148.591778395 -37.40782250299992, 148.59199652000007 -37.40806689099992), (148.591778395 -37.40782250299992, 148.59177210000007 -37.407815899999946, 148.5917533820001 -37.40779468699992, 148.59177208000006 -37.40781593299994, 148.591778395 -37.40782250299992))";
    String expected = "POLYGON ((148.5919965 -37.4080669, 148.5913674 -37.4078618, 148.591501 -37.4064022, 148.5914982 -37.4052, 148.5917221 -37.4052, 148.5917181 -37.4065393, 148.5917784 -37.4078225, 148.5919965 -37.4080669))";
    checkOverlay(wkt0, null, 10000000, expected);
  }

  /**
   * Stress test for edge merging and exterior face detection.
   * 
   * @throws ParseException
   */
  public void testPolygonsWithMultipleCollapsesSurroundingEmptyArea()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((100 50, 200 50, 100 50, 200 50, 200 100, 100 100, 200 100, 100 100, 100 50)), ((150 200, 100 100, 150 200, 100 100, 100 200, 150 200)), ((150 200, 200 200, 200 100, 150 200)))";
    String wkt1 = null;
    String expected = "MULTIPOLYGON (((100 200, 150 200, 100 100, 100 200)), ((100 100, 200 100, 200 50, 100 50, 100 100)), ((150 200, 200 200, 200 100, 150 200)))";
    checkOverlay(wkt0, wkt1, expected);
  }
  
  /**
   * Same as above, but using precision model to create collapes.
   * 
   * @throws ParseException
   */
  public void testPolygonsWithPrecisionCollapsesSurroundingEmptyArea()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((100 50, 200 50, 100.2 49.9, 200 50, 200 100, 100.2 99.8, 199.8 100.2, 100 100, 100 50)), ((150 200, 100.3 100.2, 150 200, 100 100, 100 200, 150 200)), ((150 200, 200 200, 200 100, 150 200)))";
    String wkt1 = null;
    String expected = "MULTIPOLYGON (((100 200, 150 200, 100 100, 100 200)), ((100 100, 200 100, 200 50, 100 50, 100 100)), ((150 200, 200 200, 200 100, 150 200)))";
    checkOverlay(wkt0, wkt1, 1, expected);
  }
  
  public void testAdjacentSquares()
  throws ParseException
  {
    String wkt0 = "POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))";
    String wkt1 = "POLYGON ((100 100, 200 100, 200 0, 100 0, 100 100))";
    String expected = "GEOMETRYCOLLECTION (POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0)), POLYGON ((100 0, 100 100, 200 100, 200 0, 100 0)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  /**
   * Tests whether unfilled gap in middle of overlay is left empty.
   * 
   * Does not currently work, because gap removal code is not fully implemented
   * 
   * @throws ParseException
   */
  public void testPolygonsSurroundingGap()
  throws ParseException
  {
    String wkt0 = "POLYGON ((250 200, 100 200, 100 292, 169 292, 169 299, 250 299, 250 200))";
    String wkt1 = "POLYGON ((100 400, 100 292, 162 292, 162 299, 250 299, 250 400, 100 400))";
    String expected = "GEOMETRYCOLLECTION (POLYGON ((250 200, 100 200, 100 292, 162 292, 169 292, 169 299, 250 299, 250 200)), POLYGON ((162 292, 100 292, 100 400, 250 400, 250 299, 169 299, 162 299, 162 292)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  /**
   * Holes should be empty, and constructed as touching holes rather than a single inverted hole.
   * @throws ParseException
   */
  public void testPolygonWithTouchingHoles()
  throws ParseException
  {
    String wkt0 = "POLYGON ((100 100, 100 300, 300 300, 300 100, 100 100), (200 200, 200 150, 150 150, 150 200, 200 200), (250 250, 250 200, 200 200, 200 250, 250 250))";
    String expected = "POLYGON ((100 100, 100 300, 300 300, 300 100, 100 100), (200 200, 200 150, 150 150, 150 200, 200 200), (250 250, 250 200, 200 200, 200 250, 250 250))";
    checkOverlay(wkt0, null, expected);
  }

  /**
   * Holes should be constructed as touching holes rather than a single inverted hole.
   * @throws ParseException
   */
  public void testPolygonsFormingTouchingHoles()
  throws ParseException
  {
    String wkt0 = "POLYGON ((100 300, 300 300, 300 100, 100 100, 100 300))";
    String wkt1 = "MULTIPOLYGON (((150 250, 200 250, 200 200, 150 200, 150 250)), ((250 150, 200 150, 200 200, 250 200, 250 150)))";
    String expected = "MULTIPOLYGON (((200 200, 150 200, 150 250, 200 250, 200 200)), ((250 150, 200 150, 200 200, 250 200, 250 150)), ((300 100, 100 100, 100 300, 300 300, 300 100), (200 200, 200 250, 150 250, 150 200, 200 200), (200 200, 200 150, 250 150, 250 200, 200 200)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  public void testSimpleOverlap()
  throws ParseException
  {
    String wkt0 = "POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))";
    String wkt1 = "POLYGON ((20 30, 20 200, 150 200, 150 30, 20 30))";
    String expected = "MULTIPOLYGON (((100 0, 0 0, 0 100, 20 100, 20 30, 100 30, 100 0)), ((100 30, 20 30, 20 100, 100 100, 100 30)), ((150 30, 100 30, 100 100, 20 100, 20 200, 150 200, 150 30)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  public void testOverlappingTriangles()
  throws ParseException
  {
  	String wkt0 = "POLYGON ((10 10, 10 70, 40 40, 10 10))";
  	String wkt1 = "POLYGON ((20 10, 20 70, 50 40, 20 10))";
  	String expected = "MULTIPOLYGON (((20 20, 10 10, 10 70, 20 60, 20 20)), ((40 40, 20 20, 20 60, 40 40)), ((50 40, 20 10, 20 20, 40 40, 20 60, 20 70, 50 40)))";
  	checkOverlay(wkt0, wkt1, expected);
  }

  public void testPartiallyCoincidentEdges()
  throws ParseException
  {
  	String wkt0 = "POLYGON ((0 0, 0 60, 60 60, 60 0, 0 0))";
  	String wkt1 = "POLYGON ((60 20, 60 90, 130 90, 130 20, 60 20))";
  	String expected = "MULTIPOLYGON (((60 0, 0 0, 0 60, 60 60, 60 20, 60 0)), ((130 20, 60 20, 60 60, 60 90, 130 90, 130 20)))";
  	checkOverlay(wkt0, wkt1, expected);
  }

  public void testOverlappingPolys_ResultMissingLeftPoly()
  throws ParseException
  {
    String wkt0 = "POLYGON ((250 210, 280 105, 107 105, 250 210))";
    String wkt1 = "POLYGON ((190 150, 327 169, 280 60, 190 150))";
    String expected = "MULTIPOLYGON (((264.20260782347043 160.29087261785355, 190 150, 235 105, 107 105, 250 210, 264.20260782347043 160.29087261785355)), ((235 105, 190 150, 264.20260782347043 160.29087261785355, 280 105, 235 105)), ((280 60, 235 105, 280 105, 264.20260782347043 160.29087261785355, 327 169, 280 60)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  public void testOverlappingPolys_OutsideHoleReturned()
  throws ParseException
  {
    String wkt0 = "POLYGON ((120 330, 350 100, 420 190, 120 330))";
    String wkt1 = "POLYGON ((80 230, 190 90, 370 320, 80 230))";
    String expected = "MULTIPOLYGON (((186.8421052631579 263.1578947368421, 120 330, 232.72189349112426 277.396449704142, 186.8421052631579 263.1578947368421)), ((190 90, 80 230, 186.8421052631579 263.1578947368421, 264.6341463414634 185.3658536585366, 190 90)), ((264.6341463414634 185.3658536585366, 186.8421052631579 263.1578947368421, 232.72189349112426 277.396449704142, 308.85350318471336 241.8683651804671, 264.6341463414634 185.3658536585366)), ((308.85350318471336 241.8683651804671, 232.72189349112426 277.396449704142, 370 320, 308.85350318471336 241.8683651804671)), ((350 100, 264.6341463414634 185.3658536585366, 308.85350318471336 241.8683651804671, 420 190, 350 100)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  /**
   * Does not currently work, because gap handling logic is not working.
   * 
   * @throws ParseException
   */
  public void testOverlappingPolysWithHolesCreatingGaps()
  throws ParseException
  {
    String wkt0 = "POLYGON ((10 10, 10 120, 120 120, 120 10, 10 10), (30 100, 100 100, 100 30, 30 30, 30 100))";
    String wkt1 = "POLYGON ((40 150, 40 40, 150 40, 150 150, 40 150), (60 60, 130 60, 130 130, 60 130, 60 60))";
    String expected = "GEOMETRYCOLLECTION (POLYGON ((10 10, 10 120, 40 120, 40 100, 30 100, 30 30, 100 30, 100 40, 120 40, 120 10, 10 10)), POLYGON ((40 40, 40 100, 60 100, 60 60, 100 60, 100 40, 40 40)), POLYGON ((40 100, 40 120, 60 120, 60 100, 40 100)), POLYGON ((40 120, 40 150, 150 150, 150 40, 120 40, 120 60, 130 60, 130 130, 60 130, 60 120, 40 120)), POLYGON ((60 100, 60 120, 120 120, 120 60, 100 60, 100 100, 60 100)), POLYGON ((100 40, 100 60, 120 60, 120 40, 100 40)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  public void testPolysWithSurroundedArea()
  throws ParseException
  {
    /**
     * Tests that an area in A surrounded fully by polygons but covered by
     * B still computes as a polygon.
     */
    String wkt0 = "MULTIPOLYGON (((20 20, 20 60, 40 60, 40 20, 20 20)),   ((20 60, 20 80, 60 80, 60 60, 20 60)),  ((60 40, 60 80, 80 80, 80 40, 60 40)),  ((40 20, 40 40, 80 40, 80 20, 40 20)))";
    String wkt1 = "POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))";
    //String expected = "GEOMETRYCOLLECTION (POLYGON ((40 20, 20 20, 20 60, 40 60, 40 40, 40 20)), POLYGON ((40 60, 20 60, 20 80, 60 80, 60 60, 40 60)), POLYGON ((80 20, 40 20, 40 40, 60 40, 80 40, 80 20)), POLYGON ((80 40, 60 40, 60 60, 60 80, 80 80, 80 40)), POLYGON ((100 0, 0 0, 0 100, 100 100, 100 0), (20 60, 20 20, 40 20, 80 20, 80 40, 80 80, 60 80, 20 80, 20 60)), POLYGON ((40 40, 40 60, 60 60, 60 40, 40 40)))";
    String expected = "GEOMETRYCOLLECTION (POLYGON ((40 20, 20 20, 20 60, 40 60, 40 40, 40 20)), POLYGON ((60 40, 40 40, 40 60, 60 60, 60 40)), POLYGON ((40 60, 20 60, 20 80, 60 80, 60 60, 40 60)), POLYGON ((80 20, 40 20, 40 40, 60 40, 80 40, 80 20)), POLYGON ((80 40, 60 40, 60 60, 60 80, 80 80, 80 40)), POLYGON ((100 0, 0 0, 0 100, 100 100, 100 0), (20 60, 20 20, 40 20, 80 20, 80 40, 80 80, 60 80, 20 80, 20 60)))";    
    checkOverlay(wkt0, wkt1, expected);    
  }
  
  public void testTriangleKiteTouchingInPoint()
  throws ParseException
  {
    // this case revealed that adjacent edges with identical faces do not necessarily indicate face is closed 
    String wkt0 = "POLYGON ((40 180, 220 280, 120 100, 40 180))";
    String wkt1 = "POLYGON ((310 390, 207 338, 220 280, 271 258, 310 390))";
    String expected = "MULTIPOLYGON (((120 100, 40 180, 220 280, 120 100)), ((220 280, 207 338, 310 390, 271 258, 220 280)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  public void testPolygonWithSpike()
  throws ParseException
  {
    String wkt0 = "POLYGON ((100 200, 200 200, 200 150, 300 150, 200 150, 200 100, 100 100, 100 200))";
    String expected = "POLYGON ((100 200, 200 200, 200 150, 200 100, 100 100, 100 200))";
    checkOverlay(wkt0, null, expected);
  }

  /**
   * Tests that spikes are discard correctly, and that a self-intersecting outer one is not formed 
   * 
   * @throws ParseException
   */
  public void testPolygonsWithInnerSpikePiercingOuterShell()
  throws ParseException
  {
    String wkt0 = "POLYGON ((100 200, 200 200, 200 150, 300 150, 200 150, 200 100, 100 100, 100 200))";
    String wkt1 = "POLYGON ((50 300, 250 300, 250 50, 50 50, 50 300))";
    String expected = "MULTIPOLYGON (((100 200, 200 200, 200 150, 200 100, 100 100, 100 200)), ((50 300, 250 300, 250 150, 250 50, 50 50, 50 300), (200 150, 200 200, 100 200, 100 100, 200 100, 200 150)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  public void testPolygonsWithCollapsedConnection()
  throws ParseException
  {
    String wkt0 = "POLYGON ((50 150, 50 50, 100 50, 100 150, 150 150, 150 50, 200 50, 200 150, 50 150))";
    String wkt1 = "POLYGON ((0 200, 300 200, 300 0, 0 0, 0 200))";
    String expected = "MULTIPOLYGON (((50 150, 100 150, 100 50, 50 50, 50 150)), ((150 150, 200 150, 200 50, 150 50, 150 150)), ((0 200, 300 200, 300 0, 0 0, 0 200), (100 150, 50 150, 50 50, 100 50, 100 150), (200 50, 200 150, 150 150, 150 50, 200 50)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  public void testMultiPolygonWithTouchingHoleContainingTouchingPolygon()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((300 300, 300 0, 0 0, 0 300, 300 300), (200 200, 150 50, 0 0, 50 150, 200 200)), ((100 100, 100 150, 200 200, 150 100, 100 100)))";
    String wkt1 = null;
    String expected = "MULTIPOLYGON (((100 150, 200 200, 150 100, 100 100, 100 150)), ((0 300, 300 300, 300 0, 0 0, 0 300), (0 0, 150 50, 200 200, 50 150, 0 0)))";
    checkOverlay(wkt0, wkt1, expected);
  }

  public void testPolygonOverlayCollapsedCrossingPolygon()
  throws ParseException
  {
    String wkt0 = "POLYGON ((0 200, 200 200, 200 0, 0 0, 0 200))";
    String wkt1 = "POLYGON ((50 50, 100 150, 250 150, 100 150, 50 50))";
    String expected = "POLYGON ((0 200, 200 200, 200 150, 200 0, 0 0, 0 200))";
    checkOverlay(wkt0, wkt1, expected);    
  }
  
  public void testCollapsedHoleInfiniteLoop()
  throws ParseException
  {
    String wkt0 = "POLYGON ((148.85736 -37.31646, 148.8575 -37.31646, 148.8575 -37.31661, 148.85736 -37.31661, 148.85736 -37.31646), (148.8574648 -37.3165694, 148.8574352 -37.316536, 148.8574352 -37.3165359, 148.8574085 -37.3164978, 148.8574352 -37.3165359, 148.8574352 -37.316536, 148.8574648 -37.3165694))";
    String wkt1 = null;
    String expected = "POLYGON ((148.85736 -37.31646, 148.8575 -37.31646, 148.8575 -37.31661, 148.85736 -37.31661, 148.85736 -37.31646))";
    checkOverlay(wkt0, wkt1, 10000000, expected);    
  }
  
  public void testGridAWithPolyContainedInB()
  throws ParseException
  {
    String wkt0 = "MULTIPOLYGON (((100 400, 200 400, 200 300, 100 300, 100 400)), ((300 400, 300 300, 200 300, 200 400, 300 400)), ((300 200, 200 200, 200 300, 300 300, 300 200)), ((100 200, 100 300, 200 300, 200 200, 100 200)), ((400 400, 400 300, 300 300, 300 400, 400 400)), ((400 200, 300 200, 300 300, 400 300, 400 200)), ((400 100, 300 100, 300 200, 400 200, 400 100)), ((200 100, 200 200, 300 200, 300 100, 200 100)), ((100 100, 100 200, 200 200, 200 100, 100 100)))";
    String wkt1 = "POLYGON ((155 338, 320 360, 340 140, 150 170, 155 338))";
    String expected = "MULTIPOLYGON (((153.86904761904762 300, 150.89285714285714 200, 100 200, 100 300, 153.86904761904762 300)), ((200 162.10526315789474, 200 100, 100 100, 100 200, 150.89285714285714 200, 150 170, 200 162.10526315789474)), ((200 200, 200 162.10526315789474, 150 170, 150.89285714285714 200, 200 200)), ((200 300, 200 200, 150.89285714285714 200, 153.86904761904762 300, 200 300)), ((200 344, 200 300, 153.86904761904762 300, 155 338, 200 344)), ((200 400, 200 344, 155 338, 153.86904761904762 300, 100 300, 100 400, 200 400)), ((300 146.31578947368422, 300 100, 200 100, 200 162.10526315789474, 300 146.31578947368422)), ((300 200, 300 146.31578947368422, 200 162.10526315789474, 200 200, 300 200)), ((300 357.3333333333333, 300 300, 200 300, 200 344, 300 357.3333333333333)), ((300 400, 300 357.3333333333333, 200 344, 200 400, 300 400)), ((320 360, 325.45454545454544 300, 300 300, 300 357.3333333333333, 320 360)), ((325.45454545454544 300, 334.54545454545456 200, 300 200, 300 300, 325.45454545454544 300)), ((334.54545454545456 200, 340 140, 300 146.31578947368422, 300 200, 334.54545454545456 200)), ((400 200, 400 100, 300 100, 300 146.31578947368422, 340 140, 334.54545454545456 200, 400 200)), ((400 300, 400 200, 334.54545454545456 200, 325.45454545454544 300, 400 300)), ((400 400, 400 300, 325.45454545454544 300, 320 360, 300 357.3333333333333, 300 400, 400 400)), ((200 300, 300 300, 300 200, 200 200, 200 300)))";
    checkOverlay(wkt0, wkt1, 10000000, expected);    
  }
  
  //========================================================
  
  void checkOverlay(String wkt0, String wkt1, String expectedWKT)
  throws ParseException
  {
    checkOverlay(wkt0, wkt1, 0, expectedWKT);
  }
  
  void checkOverlay(String wkt0, String wkt1, int precisionScaleFactor, String expectedWKT)
  throws ParseException
  {
    PrecisionModel precModel = null;
    if (precisionScaleFactor > 0)
      precModel =new PrecisionModel(precisionScaleFactor);
    
    Geometry g1 = reader.read(wkt0);
    Geometry g2 = null;
    if (wkt1 != null)
      g2 = reader.read(wkt1);
    
    Geometry result = PolygonOverlay.overlay(g1, g2, precModel);
    
    //System.out.println(result);
    
    Geometry expected = reader.read(expectedWKT);
    result.normalize();
    expected.normalize();
    boolean isExpectedResult = equalsExactComponents(result, expected, COMPARISON_TOLERANCE);
    if (! isExpectedResult) {
      System.out.println("Expected: ");
      System.out.println(expected);
      System.out.println("Actual: ");
      System.out.println(result);
    }
    assertTrue(isExpectedResult);
  }
  
  /*
  private Geometry OLDrunOverlay(Geometry g1, Geometry g2, PrecisionModel precModel)
  {
    //----- source
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
    if (precModel != null) {
      extracter.setPrecisionModel(precModel);
    }
    
    //----- sink
    GeometryCreatorSink polyCreator = new GeometryCreatorSink(geomFact);
    
    //---- create pipeline
    PolygonOverlay overlay = new PolygonOverlay(geomFact, precModel);
    overlay.init(extracter, polyCreator, true);

    //---- execute pipeline
    extracter.add(g1);
    if (g2 != null) extracter.add(g2);
    extracter.extract();
    
    //---- handle results
    Geometry result = polyCreator.getGeometry();
    return result;
  }
*/
  
  public static boolean equalsExactComponents(Geometry g1, Geometry g2, double tolerance)
  {
    if (g1.getNumGeometries() != g2.getNumGeometries()) return false;
    for (int i = 0; i < g1.getNumGeometries(); i++) {
      if (! g1.getGeometryN(i)
          .equalsExact(g2.getGeometryN(i), tolerance))
        return false;
    }
    return true;
  }
}
