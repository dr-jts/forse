package forse.geomstream;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.Memory;
import org.locationtech.jts.util.Stopwatch;

import forse.geomsink.StatisticsGeometrySink;
import forse.noding.SweepLineNoder;
import forse.polygonize.SweepLinePolygonizer;



public class RunStreamOverlayFile 
{
  public static void main(String[] args) throws Exception
  {
  	RunStreamOverlayFile test = new RunStreamOverlayFile();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private PrecisionModel precModel = new PrecisionModel(1000000000);
  private GeometryFactory geomFact = new GeometryFactory(precModel);
  private boolean isOutput = false;

  void run() throws Exception 
  {
	  runTest();
  }
  
  void runTest() throws Exception 
  {
    String dir = "D:/proj/jts/testing/pglist-20200708/";
    overlay(dir + "dbca-poly-sort.wkt", null, precModel );
    //overlay(dir + "dbca-poly-sort.wkt", dir + "dbca-poly-sort.wkt", precModel);
  }

  void overlay(String file1, String file2, PrecisionModel pm) throws Exception 
  {
    run(file1, file2, false, pm); 
  }
  
	void run(String file1, String file2, boolean isUnion, PrecisionModel pm) throws Exception 
  {
    System.out.println("File 1: " + file1);
    if (file2 != null) System.out.println("File 2: " + file2);

    GeometryStream gs1 = createStream(file1);
    GeometryStream gs2 = null;
    if (file2 != null)
      gs2 = createStream(file2);
    
    Stopwatch sw = new Stopwatch();

    overlay(gs1, gs2, pm);
    
    System.out.println("  --  Time: " + sw.getTimeString()
        + "  Mem: " + Memory.usedTotalString());
    System.out.println();
    
  }
  
  GeometryStream createStream(String filename)
  throws Exception
  {
    return new WKTGeometryStream(filename, geomFact);
  }
    
  void overlay(GeometryStream g1, GeometryStream g2, PrecisionModel pm)
  {
    PolygonOverlayGeometryStream overlay = new PolygonOverlayGeometryStream(geomFact, pm);
    overlay.setValidating(true);
    overlay.create(g1, g2);
    
    int count = 0;
    while (true) {
      Geometry g = overlay.next();
      if (isOutput)
    	  System.out.println(g);
      count++;
      if (g == null) break;
    }
    System.out.println("Input - Polygons: " 
        + overlay.getMerger().getGeometryCount()
        + "   Pts: " + overlay.getMerger().getCoordinateCount());
    System.out.println("Output - Polygons: " + count);
  }
  

	
  

}
