package forse;



import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

import forse.geomsink.DebugGeometrySink;
import forse.lineseg.GeometrySegmentExtracterSource;
import forse.noding.SweepLineNoder;
import forse.polygonize.SweepLinePolygonizer;

public class TestOverlay 
{
  public static void main(String[] args) throws Exception
  {
  	TestOverlay test = new TestOverlay();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader(geomFact);

  void run() throws Exception {
		run(triangleKite_topoException);
	}

	void run(String[] wkt) throws Exception 
  {
    Geometry g1 = null, g2 = null;
		g1 = reader.read(wkt[0]);

    if (wkt[1] != null) g2 = reader.read(wkt[1]);
		
		// set up pipeline
		GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
		SweepLineNoder noder = new SweepLineNoder();
		SweepLinePolygonizer polygonizer = new SweepLinePolygonizer(geomFact);
		
		extracter.setSink(noder);
		noder.setSink(polygonizer);
		polygonizer.setSink(new DebugGeometrySink());
		
		extracter.add(g1);
		if (g2 != null) extracter.add(g2);
		extracter.extract();

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

  String[] overlappingPolys_ResultMissingLeftPoly = {
      "POLYGON ((250 210, 280 105, 107 105, 250 210))",
      "POLYGON ((190 150, 327 169, 280 60, 190 150))"
  };

  String[] overlappingPolys_OuterHoleNotDiscarded = {
      "POLYGON ((120 330, 350 100, 420 190, 120 330)))",
      "POLYGON ((80 230, 190 90, 370 320, 80 230))"
  };

  String[] thinTriangle_topoException = {
      "MULTIPOLYGON (((1345691.9844329 615893.254651331, 1345690.68721311 615895.75171873, 1345700 615900, 1345696.05190682 615890.427798035, 1345691.9844329 615893.254651331)), ((1345516.80203441 615798.007655577, 1345516.71925431 615797.962647881, 1345516.69061909 615798.051527861, 1345691.9844329 615893.254651331, 1345516.80203441 615798.007655577)))",
      null
  };

  String[] triangleKite_topoException = {
      "POLYGON ((40 180, 220 280, 120 100, 40 180))",
      "POLYGON ((310 390, 207 338, 220 280, 271 258, 310 390))"
  };


}
