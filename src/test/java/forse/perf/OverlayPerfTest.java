package forse.perf;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Memory;
import org.locationtech.jts.util.Stopwatch;

import forse.geomsink.GeometryCreatorSink;
import forse.lineseg.GeometrySegmentExtracterSource;
import forse.lineseg.NullSegmentSink;
import forse.noding.SweepLineNoder;
import forse.polygonize.SweepLinePolygonizer;

public class OverlayPerfTest 
{
  public static void main(String[] args) throws Exception
  {
  	OverlayPerfTest test = new OverlayPerfTest();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  private static final PrecisionModel precModel = new PrecisionModel(100);
  private static final GeometryFactory geomFact = new GeometryFactory(precModel);
  private static final WKTReader reader = new WKTReader();

  void run() throws Exception {
		run(4);
//		if(true) return;
		
		run(10);
		run(20);
		run(50);
		run(100);
		run(200);
//		run(500);
	}

	void run(int numOnSide) throws Exception 
	{
		System.out.println("Num polygons: " + numOnSide * numOnSide);
		
		double size = 100.0;
//		int numOnSide = 20;
		int nSegs = 10;
		
		double offset = size / numOnSide / 2;
		
		// hold geoms in an array to allow them to be freed by called method
		Geometry geom[] = new Geometry[2];
		geom[0] = createCoverage( 
				new Coordinate(0,0),           
				size, numOnSide, nSegs);
		geom[1] = createCoverage( 
				new Coordinate(offset,offset), 
				size, numOnSide, nSegs);
		
		System.out.println("Num pts: " + geom[0].getNumPoints());

		
//		System.out.println(g1);
//		System.out.println(g2);
		
		Stopwatch sw = new Stopwatch();
		
		System.out.println("  --  Start        ---- Mem: " + Memory.usedTotalString());

		runOverlay(geom);
		
		System.out.println("  --  Time: " + sw.getTimeString()
				+ " ---- Mem: " + Memory.usedTotalString());
		System.out.println();
	}
	
  void runOverlay(Geometry[] geom) {
    // set up pipeline
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
    SweepLineNoder noder = new SweepLineNoder();
    extracter.setSink(noder);
    
    SweepLinePolygonizer polygonizer = new SweepLinePolygonizer(geomFact);
    noder.setSink(polygonizer);
    
    GeometryCreatorSink gcSink = new GeometryCreatorSink(geomFact);
    polygonizer.setSink(gcSink);
    
    extracter.add(geom[0]);
    geom[0] = null;
    extracter.add(geom[1]);
    geom[1] = null;
    extracter.extract();
    
//    System.out.println(gcSink.getGeometry());
  }
  void OLDrunOverlay(Geometry[] geom) {
    // set up pipeline
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
    SweepLineNoder noder = new SweepLineNoder();
    
//    GeometryCreatorSink endSink = new GeometryCreatorSink(geomFact);
    NullSegmentSink endSink = new NullSegmentSink();
    
    extracter.setSink(noder);
    noder.setSink(endSink);
    
    extracter.add(geom[0]);
    geom[0] = null;
    extracter.add(geom[1]);
    geom[1] = null;
    extracter.extract();
    
//    System.out.println(gcSink.getGeometry());
  }

	Geometry createCoverage(Coordinate base, double size, int numOnSide, int nSegs)
	{
		PolygonCoverageBuilder builder = new PolygonCoverageBuilder(geomFact);
		List geoms = builder.build(base, size, numOnSide, nSegs);
		return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(geoms));
	}


}
