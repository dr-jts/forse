package forse.perf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Memory;
import org.locationtech.jts.util.Stopwatch;

import forse.lineseg.GeometrySegmentExtracterSource;
import forse.lineseg.OrderValidatingSegmentSink;
import forse.noding.SweepLineNoder;

public class TransformingOverlayFilePerfTest 
{
  public static void main(String[] args) throws Exception
  {
  	TransformingOverlayFilePerfTest test = new TransformingOverlayFilePerfTest();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  private static final PrecisionModel precModel = new PrecisionModel(100);
  private static final GeometryFactory geomFact = new GeometryFactory(precModel);
  private static final WKTReader wktRdr = new WKTReader();

  static final double OFFSET_FACTOR = 0.1;
  	
  static final int NUM_REPLICATES = 4;

  void run() throws Exception {
//  	run(TestFiles.DATA_DIR + "africa.wkt", 4);
  	run("C:\\data\\martin\\proj\\jts\\testing\\timberline\\fdp_dump_extract.wkt", 1);
//  	run("C:\\proj\\Timberline\\data\\tbl_pem.wkt", 1);
	}

  
	void run(String filename, int numReplicates) throws Exception 
	{
		System.out.println("Input file: " + filename);
		
    WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    List polyList = fileRdr.read();
    
    Envelope env = geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(polyList))
    									.getEnvelopeInternal();
    
    List polyListRepl = replicateSquare(polyList, numReplicates, env.getWidth(), env.getHeight());
    
    Geometry[] geom = new Geometry[2];
    
    geom[0] = geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(polyListRepl));
    
    // detach input data
    polyListRepl = null;
    polyList = null;
    
		double offsetX = OFFSET_FACTOR * geom[0].getEnvelopeInternal().getWidth();
		double offsetY = OFFSET_FACTOR * geom[0].getEnvelopeInternal().getHeight();
		
		AffineTransformation trans = AffineTransformation.translationInstance(offsetX, offsetY);
		geom[1] = trans.transform(geom[0]);
		
		System.out.println("Num polygons: " + (geom[0].getNumGeometries() + geom[1].getNumGeometries()));
		System.out.println("Num pts: " + (geom[0].getNumPoints() + geom[1].getNumPoints()));

		
//		System.out.println(g1);
//		System.out.println(g2);
		
		Stopwatch sw = new Stopwatch();
		
		System.out.println("  --  Start Mem: " + Memory.usedTotalString());

		runOverlay(geom);
		
		System.out.println("  --  Time: " + sw.getTimeString()
				+ "  Mem: " + Memory.usedTotalString());
		System.out.println();
	}
	
	void runOverlay(Geometry[] geom) {
		// set up pipeline
		GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
		SweepLineNoder noder = new SweepLineNoder();
		
//		GeometryCreatorSink endSink = new GeometryCreatorSink(geomFact);
//		NullSegmentSink endSink = new NullSegmentSink();
		OrderValidatingSegmentSink endSink = new OrderValidatingSegmentSink();
		
		extracter.setSink(noder);
		noder.setSink(endSink);
		
		extracter.add(geom[0]);
		geom[0] = null;
		extracter.add(geom[1]);
		geom[1] = null;
		extracter.extract();
		
//		System.out.println(gcSink.getGeometry());
	}

	List replicateSquare(List geoms, int num, double offsetX, double offsetY)
	{
		int numY = (int)Math.sqrt((double)num);
		int numX = num / numY;
		
		List result = new ArrayList();
		for (int i = 0; i < numX; i++) {
			for (int j = 0; j < numY; j++) {
				result.addAll(replicateOffset(geoms, i * offsetX, j * offsetY));
			}
		}
		return result;
	}
	
	List replicateOffset(List geoms, double offsetX, double offsetY)
	{
		AffineTransformation trans = AffineTransformation.translationInstance(offsetX, offsetY);

		List result = new ArrayList();
		for (Iterator i = geoms.iterator(); i.hasNext(); ) {
			Geometry g = (Geometry) i.next();
			result.add(trans.transform(g));
		}
		return result;
	}


}
