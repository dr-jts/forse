package forse.geomstream;

import java.io.FileInputStream;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Memory;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile;

import forse.geomsink.StatisticsGeometrySink;
import forse.noding.SweepLineNoder;
import forse.polygonize.SweepLinePolygonizer;



public class TestStreamOverlayFile 
{
  public static void main(String[] args) throws Exception
  {
  	TestStreamOverlayFile test = new TestStreamOverlayFile();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
  private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader(geomFact);

  void run() throws Exception 
  {
    runSewell();
    //runGio();
    //runMI();
    //runTSB();
    //runTimberline();
  }
  
  void runSewell() throws Exception 
  {
    String dir = "C:\\data\\martin\\proj\\jts\\testing\\overlay\\sewell\\sample_data";
    overlay(dir + "left_polygons_19.shp", 
        dir+"right_polygons_19.shp");
  }

  void overlay(String file1, String file2) throws Exception 
  {
    run(file1, file2, false); 
  }
  
	void run(String file1, String file2, boolean isUnion) throws Exception 
  {
    System.out.println("File 1: " + file1);
    System.out.println("File 2: " + file2);

    GeometryStream gs1 = createShapefileStream(file1);
    GeometryStream gs2 = null;
    if (file2 != null)
      gs2 = createShapefileStream(file2);
    
    Stopwatch sw = new Stopwatch();

    overlay(gs1, gs2);
    
    System.out.println("  --  Time: " + sw.getTimeString()
        + "  Mem: " + Memory.usedTotalString());
    System.out.println();
    
  }
  
  GeometryStream createShapefileStream(String filename)
  throws Exception
  {
    Shapefile shpfile = new Shapefile(new FileInputStream(filename));
    return new ShapefileGeometryStream(shpfile, geomFact);
  }
    
  void overlay(GeometryStream g1, GeometryStream g2)
  {
    PolygonOverlayGeometryStream overlay = new PolygonOverlayGeometryStream(geomFact);
    overlay.create(g1, g2);
    
    int count = 0;
    while (true) {
      Geometry g = overlay.next();
      count++;
      if (g == null) break;
    }
    System.out.println("Input - Polygons: " 
        + overlay.getGeometryCount()
        + "   Pts: " + overlay.getCoordinateCount());
    System.out.println("Output - Polygons: " + count);
  }
  

  Geometry OLDoverlay(GeometryStream g1, GeometryStream g2, boolean isUnion)
  {
    MergeSortGeometryStream merger = new MergeSortGeometryStream();
    merger.add(g1);
    if (g2 != null) merger.add(g2);
    GeometryStreamSegmentSource segSrc = new GeometryStreamSegmentSource(merger);

    SweepLineNoder noder = new SweepLineNoder();
    segSrc.setSink(noder);
    
    //StatisticsSegmentSink statSegSink = new StatisticsSegmentSink();
    //noder.setSink(statSegSink);

    //*
    SweepLinePolygonizer polygonizer = new SweepLinePolygonizer(geomFact);
    polygonizer.setUnion(isUnion);
    polygonizer.setUnionCreateHolesAsPolys(isUnion);
    noder.setSink(polygonizer);
    
    StatisticsGeometrySink gcSink = new StatisticsGeometrySink();
    //GeometryCreatorSink gcSink = new GeometryCreatorSink(geomFact);
    polygonizer.setSink(gcSink);
    //*/
    
    segSrc.process();
    
    System.out.println("Input - Polygons: " 
        + merger.getGeometryCount()
        + "   Pts: " + merger.getCoordinateCount());
        
    return null;
  }
  

	
  

}
