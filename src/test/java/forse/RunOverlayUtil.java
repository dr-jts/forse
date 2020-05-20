package forse;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.util.Memory;
import org.locationtech.jts.util.Stopwatch;

import forse.geomsink.GeometryCreatorSink;
import forse.lineseg.GeometrySegmentExtracterSource;
import forse.noding.SweepLineNoder;



public class RunOverlayUtil 
{
  public static GeometryFactory geomFact = new GeometryFactory();
  public static WKTReader wktRdr = new WKTReader(geomFact);

  public static Geometry readFile(String filename)
  throws Exception
  {
    WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    List polyList = fileRdr.read();
    return geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(polyList));
  }

  public static Geometry readFileLogged(String filename)
  throws Exception
  {
    System.out.println("File 1: " + filename);
    WKTFileReader fileRdr = new WKTFileReader(filename, wktRdr);
    List polyList = fileRdr.read();
    Geometry g = geomFact.createGeometryCollection(GeometryFactory.toGeometryArray(polyList));
    System.out.println("Geometry 1: " + g.getNumPoints() + " pts");
    return g;
  }

  static Geometry nodeLogged(Geometry[] geom, 
      PrecisionModel pm) 
  throws Exception 
  {
    Stopwatch sw = new Stopwatch();
    Geometry result = node(geom, pm);
    //System.out.println(result);
    System.out.println("  --  Time: " + sw.getTimeString()
        + "  Mem: " + Memory.usedTotalString());
    System.out.println();
    return result;
  }
  
    
  public static Geometry node(Geometry[] geom, 
      PrecisionModel pm) 
  throws Exception 
  {
    
    // set up pipeline
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
    SweepLineNoder noder = new SweepLineNoder();
    if (pm != null) {
      extracter.setPrecisionModel(pm);
      noder.setPrecisionModel(pm);
    }
    GeometryCreatorSink gcSink = new GeometryCreatorSink(geomFact);
    
    extracter.setSink(noder);
    noder.setSink(gcSink);
    
    for (int i = 0; i < geom.length; i++) {
      extracter.add(geom[i]);
    }
    extracter.extract();
    
    Geometry result = gcSink.getGeometry();
    
    return result;
  }

  public static Geometry nodeFile(String filename, PrecisionModel pm)
  throws Exception
  {
    Geometry g = readFileLogged(filename);
    return nodeLogged(new Geometry[] { g }, pm);
  }
}
