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


  void runGio() throws Exception 
  {
    /**
     * 
     *  ==== IndexedSweepLineStatus ====
     *
     *  
     *  OVERLAY
     *  
Polygons: 1308401   Pts: 17342264 ---- Used: 26.02 MB   Free: 15.86 MB   Total: 41.87 MB
Throughput: 900 poly/sec
Input - Polygons: 100000   Pts: 8236950
  --  Time: 1453.406 s  Mem: Used: 26.07 MB   Total: 41.87 MB
  
  UNION
  
  
  */
    
    String dir = "C:\\data\\martin\\proj\\overlay\\testing\\gio\\";
    //overlay(dir + "selection_136MB_SORT.shp", null);
    overlay(dir + "bigselection4martin/1_SORT.shp", null);
  }
  

  void runMI() throws Exception 
  {
    /**
     * 
     *  ==== IndexedSweepLineStatus ====
     *  
Polygons: 10000   Pts: 599347 ---- Used: 46.65 MB   Free: 6.9 MB   Total: 53.54 MB
Polygons: 20000   Pts: 1197299 ---- Used: 53.69 MB   Free: 5.25 MB   Total: 58.93 MB
Polygons: 30000   Pts: 2015912 ---- Used: 47.84 MB   Free: 29.75 MB   Total: 77.58 MB
Polygons: 40000   Pts: 2201663 ---- Used: 51.66 MB   Free: 25.93 MB   Total: 77.58 MB
Polygons: 50000   Pts: 2375990 ---- Used: 75.97 MB   Free: 1655.5 KB   Total: 77.58 MB
Polygons: 60000   Pts: 2564986 ---- Used: 59.58 MB   Free: 18.01 MB   Total: 77.58 MB
Polygons: 70000   Pts: 2757956 ---- Used: 41.53 MB   Free: 36.06 MB   Total: 77.58 MB
Polygons: 80000   Pts: 2963535 ---- Used: 61.23 MB   Free: 16.36 MB   Total: 77.58 MB
Polygons: 90000   Pts: 3191969 ---- Used: 44.45 MB   Free: 33.14 MB   Total: 77.58 MB
Polygons: 100000   Pts: 3537475 ---- Used: 29.96 MB   Free: 47.63 MB   Total: 77.58 MB
Polygons: 100301   Pts: 3593589 ---- Used: 41.58 MB   Free: 36.01 MB   Total: 77.58 MB
Throughput: 582 poly/sec
Input - Polygons: 30327   Pts: 2955958
  --  Time: 172.297 s  Mem: Used: 41.58 MB   Total: 77.58 MB
      
  */
    String dir = "C:\\data\\martin\\proj\\jts\\testing\\overlay\\mapinfo\\";
    overlay(dir + "coverage_region.shp", dir+"uszip07d_region_SORT.shp");

  }
  
  void runTimberline() throws Exception 
  {
    String timberline = "C:\\data\\martin\\proj\\overlay\\testing\\timberline\\";
    /**
     * 
     *  ==== IndexedSweepLineStatus ====
    Polygons: 528263   Pts: 20699680 
    Throughput: 945 poly/sec
    Input - Polygons: 183031   Pts: 17663867
      --  Time: 559.187 s  Mem: Used: 44.43 MB   Total: 60.77 MB
      */
    overlay(timberline + "tbl_pem_SORT.shp", timberline+"fdp_dump_SORT.shp");

  }
  
  
  void runTSB() throws Exception 
  {

    /*
     * Input - Polygons: 223305   Pts: 16533213
     * 
     * ==== SimpleSweepLineStatus ====
    Result Polygons: 1292766 -- Time: 1642.235 s  Mem: Used: 138.84 MB   Total: 148.11 MB
    -- 787 poly/sec
    
     ==== IndexedSweepLineStatus ====
     Polygons: 1292766 Pts: 21905570 -- Time: 561.125 s  Mem: Used: 73.53 MB   Total: 99.75 MB
     -- 2281 poly/sec

     */
    //run(tsb + "pemdec_SORT.shp", tsb+"vri_tsa14_SORT.shp");
    
    /*
     * Input - Polygons: 133544   Pts: 6393829
     * 
     * ==== SimpleSweepLineStatus ====
     Segments: 3316089 -- Time: 295.906 s  Mem: Used: 7.42 MB   Total: 56.98 MB
     Polygons: 255057  -- Time: 389.406 s  Mem: Used: 56.59 MB   Total: 77.92 MB
       
     ==== IndexedSweepLineStatus ====
     Segments: 3316089 -- Time: 74.766 s  Mem: Used: 20.6 MB   Total: 54.79 MB
     Polygons: 255057  -- Time: 186.625 s  Mem: Used: 72.48 MB   Total: 76.97 MB
     -- 1371 poly/sec
     */
    //run(tsb+"pemdec_SORT.shp",tsb+"Lakes_Visuals_SORT.shp");

    /*
     * ==== SimpleSweepLineStatus ====
 
    Polygons: 10581 -- Time: 20.734 s  Mem: Used: 25.41 MB   Total: 30.77 MB
    -- 511 poly/sec
    
    ==== IndexedSweepLineStatus ====
    Polygons: 10581 --  Time: 18.641 s  Mem: Used: 18.55 MB   Total: 30.74 MB
    -- 568 poly/sec
     */
    String tsb = "C:\\data\\martin\\proj\\overlay\\testing\\tsb\\";
    overlay(tsb+"Lakes_ESA_Wildlife_SORT.shp",tsb+"Lakes_Visuals_SORT.shp");

/*
    run(
        "C:\\data\\martin\\proj\\jts\\testing\\tsb\\Lakes_ESA_Wildlife_SORT.shp",
        "C:\\data\\martin\\proj\\jts\\testing\\tsb\\LakesSouth_Corridors_SORT.shp");
     run(
        "C:\\data\\martin\\proj\\jts\\testing\\tsb\\Lakes_Visuals.shp",
        "C:\\data\\martin\\proj\\jts\\testing\\tsb\\Lakes_ESA_Wildlife.shp");
        */
	}

  void union(String file1, String file2) throws Exception 
  {
    run(file1, file2, true); 
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
        + overlay.getMerger().getGeometryCount()
        + "   Pts: " + overlay.getMerger().getCoordinateCount());
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
