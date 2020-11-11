package forse.geomstream;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.util.Memory;
import org.locationtech.jts.util.Stopwatch;



public class RunStreamOverlayFile 
{
  static PrecisionModel DEMO_PM = new PrecisionModel(1000000000);
  static String DEMO_FILE_1 = "D:/proj/jts/testing/pglist-20200708/dbca-poly-sort.wkt";
  static String DEMO_FILE_2 = null;
	  
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
  
  private PrecisionModel precModel = DEMO_PM;
  private String filename1 = DEMO_FILE_1;
  private String filename2 = DEMO_FILE_2;
  
  private GeometryFactory geomFact = new GeometryFactory(precModel);
  private boolean isOutput = false;

  void run() throws Exception 
  {
	boolean isUnion = false;
    overlay(filename1, filename2, isUnion, precModel );
  }
  
  void overlay(String file1, String file2, boolean isUnion, PrecisionModel pm) throws Exception 
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
        + overlay.getGeometryCount()
        + "   Pts: " + overlay.getCoordinateCount());
    System.out.println("Output - Polygons: " + count);
  }
  

	
  

}
