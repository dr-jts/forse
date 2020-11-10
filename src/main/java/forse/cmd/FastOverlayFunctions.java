package forse.cmd;

import java.util.*;

import forse.LineNoder;
import forse.PolygonOverlay;
import forse.geomsink.GeometryCreatorSink;
import forse.geomsink.MultiGeometrySink;
import forse.lineseg.GeometrySegmentExtracterSource;
import forse.lineseg.LineSeg;
import forse.lineseg.MultiSegmentSink;
import forse.lineseg.SegmentDissolver;
import forse.lineseg.SegmentSource;
import forse.noding.SweepLineNoder;
import forse.noding.SweepLineNodingValidator;
import forse.polygonize.SweepLinePolygonizer;
import forse.validate.PolygonOverlayInsidePointValidator;
import forse.validate.PolygonOverlayProbeValidator;

import org.locationtech.jts.geom.*;

public class FastOverlayFunctions 
{
  /**
   * Utility function to dissolve segments.
   * Does not use streaming, just a sort and duplicate removal.
   * 
   * @param geom
   * @return
   */
  public static Geometry dissolveSegments(Geometry geom) {
    SegmentDissolver diss = new SegmentDissolver();
    diss.add(geom);
    List segs = diss.dissolve();
    return convertLineSeg(segs, geom.getFactory());
  }

  public static Geometry findDuplicateSegments(Geometry geom) {
    SegmentDissolver diss = new SegmentDissolver();
    diss.add(geom);
    Collection segs = diss.duplicates();
    return convertLineSeg(segs, geom.getFactory());
  }

  private static Geometry convertLineSeg(Collection segList, GeometryFactory geomFact)
  {
    List lines = new ArrayList();
    for (Iterator i = segList.iterator(); i.hasNext(); ) {
      LineSeg seg = (LineSeg) i.next();
      LineString line = geomFact.createLineString(
          new Coordinate[] { new Coordinate(seg.getX0(), seg.getY0()),
              new Coordinate(seg.getX1(), seg.getY1())
          });
             
      lines.add(line);
    }
    return geomFact.createMultiLineString(GeometryFactory.toLineStringArray(lines));
  }
  
  public static Geometry node(Geometry g1, Geometry g2)
  {
    return node(g1, g2, null, false);
  }
  
  public static Geometry nodeWithValidation(Geometry g1, Geometry g2)
  {
    return node(g1, g2, null, true);
  }
  
  public static Geometry nodeWithPrecision(Geometry g1, Geometry g2, double precisionScaleFactor)
  {
    return node(g1, g2, new PrecisionModel(precisionScaleFactor), false);
  }
  
  public static Geometry nodeWithPrecisionAndValidation(Geometry g1, Geometry g2, double precisionScaleFactor)
  {
    return node(g1, g2, new PrecisionModel(precisionScaleFactor), true);
  }
  
  private static Geometry node(Geometry g1, Geometry g2, PrecisionModel precModel, 
      boolean isValidating)
  {
    //---- segment source
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource(precModel);
        
    ///---- segment sink
    GeometryCreatorSink gcSink = new GeometryCreatorSink(new GeometryFactory());
    GeometryMonitorSink monSink = new GeometryMonitorSink();

    //--- create the noder
    LineNoder noder = new LineNoder(precModel);
    noder.init(extracter, MultiSegmentSink.create(gcSink, monSink), isValidating)  ;
    
    //---- run the process
    extracter.add(g1);
    extracter.add(g2);
    extracter.extract();
    
    return gcSink.getGeometry();
  }

  public static Geometry nodingValidator(Geometry g1, Geometry g2)
  {
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
    
    SweepLineNodingValidator validator = new SweepLineNodingValidator();
    
    extracter.setSink(validator);
    GeometryMonitorSink monSink = new GeometryMonitorSink();
    validator.setSink(monSink);
    
    if (g1 != null) extracter.add(g1);
    if (g2 != null) extracter.add(g2);
    extracter.extract();
    
    return validator.getGeometry();
  }
  
  private static Geometry overlay(Geometry g1, Geometry g2, 
      PrecisionModel precModel, 
      boolean isValidating,
      boolean isUnion,
      boolean unionCreateHolesAsPolys)
  {
    GeometryFactory geomFact = g1.getFactory();
    
    //----- source
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource(precModel);

    //----- sink
    GeometryCreatorSink gcSink = new GeometryCreatorSink(geomFact);
    GeometryMonitorSink monSink = new GeometryMonitorSink();
    
    //---- create pipeline
    PolygonOverlay overlay = new PolygonOverlay(geomFact, precModel);
    overlay.init(extracter, MultiGeometrySink.create(gcSink, monSink), isValidating);
    //----- set overlay options
    overlay.setUnion(isUnion);
    overlay.setUnionCreateHolesAsPolys(unionCreateHolesAsPolys);
    
    //---- execute process
    extracter.add(g1);
    extracter.add(g2);
    extracter.extract();
    
    //---- handle results
    return gcSink.getGeometry(true);
  }

  private static Geometry overlay(Geometry g1, Geometry g2, 
      PrecisionModel precModel) 
  {
    return overlay(g1, g2, precModel, true, false, false);
  }
  
  private static Geometry union(Geometry g1, Geometry g2, 
      PrecisionModel precModel,
      boolean unionCreateHolesAsPolys) 
  {
    return overlay(g1, g2, precModel, true, true, unionCreateHolesAsPolys);
  }

  public static Geometry overlay(Geometry g1, Geometry g2)
  {
    return overlay(g1, g2, null);
  }
  
  private static final boolean VALIDATE_NODING = true;
  
   public static Geometry overlayWithPrecision(Geometry g1, Geometry g2, double precisionScaleFactor)
  {
    return overlay(g1, g2, new PrecisionModel(precisionScaleFactor));
  }
  
  public static Geometry union(Geometry g1, Geometry g2)
  {
    return union(g1, g2, null, false);
  }
  
  public static Geometry unionWithHolesAsPolys(Geometry g1, Geometry g2)
  {
    return union(g1, g2, null, true);
  }
  
  public static Geometry unionWithHolesAsPolysPrec(Geometry g1, Geometry g2, double precisionScaleFactor)
  {
    return union(g1, g2, new PrecisionModel(precisionScaleFactor), true);
  }
  
  public static Geometry unionWithPrecision(Geometry g1, Geometry g2, double precisionScaleFactor)
  {
    return union(g1, g2, new PrecisionModel(precisionScaleFactor), false);
  }
  
  public static Geometry overlayWithProbeValidation(Geometry g1, Geometry g2)
  {
    GeometryFactory geomFact = g1.getFactory();
    
    Geometry result = overlay(g1, g2);
    
    System.out.println("Overlay complete - validating...");
    // test correctness of validation by perturbing result
    //result = removeSmallest(result);
    
    PolygonOverlayProbeValidator validator = new PolygonOverlayProbeValidator();
    boolean isValid = validator.isValid(g1, g2, result);
    if (! isValid) { 
      // return failure points as a geometry
      result = validator.getFailedProbePts(geomFact);
    }
    return result;
  }
  
  public static Geometry overlayWithInsidePointValidation(Geometry g1, Geometry g2, double precisionScaleFactor)
  {
    GeometryFactory geomFact = g1.getFactory();
    
    Geometry result = PolygonOverlay.overlay(g1, g2, new PrecisionModel(precisionScaleFactor));
    
    System.out.println("Overlay complete - validating...");
    // test correctness of validation by perturbing result
    //result = removeSmallest(result);
    
    PolygonOverlayInsidePointValidator validator = new PolygonOverlayInsidePointValidator();
    boolean isValid = validator.isValid(g1, g2, result);
    if (! isValid) { 
      //throw new IllegalStateException("Coverage is invalid");
      
      // return failure points as a geometry
      result = validator.getFailurePts(geomFact);
    }
    return result;
  }
  
  private static Geometry removeSmallest(Geometry polys)
  {
    int iMin = -1;
    double minArea = Double.MAX_VALUE;
    for (int i = 0; i < polys.getNumGeometries(); i++) {
      Geometry g = polys.getGeometryN(i);
      double area = g.getArea();
      if (area < minArea) {
        minArea = area;
        iMin = i;
      }
    }
    
    // remove iMin'th geometry
    List geoms = new ArrayList();
    for (int i = 0; i < polys.getNumGeometries(); i++) {
      Geometry g = polys.getGeometryN(i);
      if (i != iMin) {
        geoms.add(g);
      }
      else {
        System.out.println(g);
      }
    }
    return polys.getFactory().buildGeometry(geoms);
  }
}
