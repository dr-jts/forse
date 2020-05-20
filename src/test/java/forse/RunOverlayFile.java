package forse;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Memory;
import org.locationtech.jts.util.Stopwatch;
import org.locationtech.jtstest.util.io.IOUtil;
//import test.jts.TestFiles;

public class RunOverlayFile 
{
  public static void main(String[] args) throws Exception
  {
  	RunOverlayFile test = new RunOverlayFile();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  private static GeometryFactory geomFact = new GeometryFactory();
  private static WKTReader wktRdr = new WKTReader(geomFact);

  void run() throws Exception 
  {
    runSewell();
//    run("C:\\data\\martin\\proj\\jts\\testing\\tsb\\Lakes_Visuals.wkt",
//    "C:\\data\\martin\\proj\\jts\\testing\\tsb\\Lakes_ESA_Wildlife.wkt");
    
//  run("C:\\data\\martin\\proj\\jts\\testing\\tsb\\Lakes_Visuals.wkt",
//  "C:\\data\\martin\\proj\\jts\\testing\\tsb\\Lakes_ESA_Wildlife.wkt");

//  	run(TestFiles.DATA_DIR + "uk.wkt");
//	run("C:\\data\\martin\\proj\\jts\\data\\southAfrica.wkt");
//	run("C:\\proj\\JTS\\test\\overlay\\britain.wkt");
//  	run("C:\\proj\\JTS\\test\\overlay\\britain_chop.wkt");
	}

  void runSewell() throws Exception 
  {
    String dir = "C:\\data\\martin\\proj\\overlay\\testing\\sewell\\sample_data\\";
    run(dir + "left_polygons_19.shp", 
        dir+"right_polygons_19.shp");
  }

  void run(String filename) 
  throws Exception 
  {
    WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    List polyList = fileRdr.read();
    
    Geometry[] geom = new Geometry[2];
    
    geom[0] = geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(polyList));
    run(geom);
  }
  
  void run(String file1, String file2) 
  throws Exception 
  {
    System.out.println("File 1: " + file1);
    System.out.println("File 2: " + file2);
    
    Geometry[] geom = new Geometry[2];
    // TODO: fix this
    //geom[0] = IOUtil.readGeometriesFromFile(file1, geomFact);
    //geom[1] = IOUtil.readGeometriesFromFile(file2, geomFact);
    
    System.out.println("Geometry 1: " + geom[0].getNumPoints() + " pts");
    System.out.println("Geometry 2: " + geom[1].getNumPoints() + " pts");

    run(geom);
  }
  
  static Geometry readFile(String filename)
  throws Exception
  {
    WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    List polyList = fileRdr.read();
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(polyList));
  }
  
  public void run(Geometry[] geom)
  {
    Stopwatch sw = new Stopwatch();

    PolygonOverlay.overlay(geom[0], geom[1], new PrecisionModel(100000000));
    
    System.out.println("  --  Time: " + sw.getTimeString()
        + "  Mem: " + Memory.usedTotalString());
    System.out.println();
  }
  
  /*
  private Geometry runOverlay(Geometry g1, Geometry g2, PrecisionModel precModel)
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
  



}
