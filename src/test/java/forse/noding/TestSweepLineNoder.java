package forse.noding;



import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;

import forse.LineNoder;

public class TestSweepLineNoder 
{
  public static void main(String[] args) throws Exception
  {
  	TestSweepLineNoder test = new TestSweepLineNoder();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader();

  void run() throws Exception {
//    run(partiallyCoincidentEdges);
    run(badSnapRoundEdges2, badSnapRoundPM2);
	}

  void run(String[] wkt) throws Exception
  {
    run(wkt, null);
  }
  
  void run(String[] wkt, PrecisionModel pm) throws Exception {
    Geometry g1 = reader.read(wkt[0]);
    Geometry g2 = reader.read(wkt[1]);
    
    MultiLineString noded = LineNoder.node(g1, g2, pm, true);
    System.out.println(noded);
  }

	String[] simpleOverlap = {
			"POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))",
			"POLYGON ((20 30, 20 200, 150 200, 150 30, 20 30))"
	};
	
	String[] overlappingTriangles = {
			"POLYGON ((10 10, 10 70, 40 40, 10 10))",
			"POLYGON ((20 10, 20 70, 50 40, 20 10))"
	};
	
  String[] partiallyCoincidentEdges = {
      "POLYGON ((0 0, 0 60, 60 60, 60 0, 0 0))",
      "POLYGON ((60 20, 60 90, 130 90, 130 20, 60 20))"
  };

  String[] badSnapRoundEdges = {
      "LINESTRING (146.47713 -37.27558, 146.47715 -37.27576)",
      "LINESTRING (146.47716 -37.27576, 146.47713 -37.27559)"
  };
  PrecisionModel badSnapRoundPM = new PrecisionModel(100000);

  String[] badSnapRoundEdges2 = {
      "LINESTRING (13 -58, 15 -76)",
      "LINESTRING (16 -76, 13 -59)"
  };
  PrecisionModel badSnapRoundPM2 = new PrecisionModel(1);

}
