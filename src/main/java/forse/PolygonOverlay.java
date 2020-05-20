package forse;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import forse.geomsink.GeometryCreatorSink;
import forse.geomsink.GeometrySink;
import forse.lineseg.GeometrySegmentExtracterSource;
import forse.lineseg.SegmentSource;
import forse.noding.SweepLineNoder;
import forse.noding.SweepLineNodingValidator;
import forse.polygonize.SweepLinePolygonizer;

/**
 * Performs Polygon Overlay.
 * 
 * @author Martin Davis
 *
 */
public class PolygonOverlay 
{
  /**
   * Overlay two sets of input polygons,
   * using an optional precision model.
   * <p>
   * If a precision model is supplied, 
   * the input is snapped to the precision model 
   * during processing.
   * <p>
   * Invalid noding is checked for automatically,
   * and an {@link InvalidNodingException} is thrown if encountered.
   * 
   * @param g1 a geometry containing polygons
   * @param g2 a geometry containing polygons (may be null)
   * @param precModel the precision model to use (may be null)
   * @return the collection of resultant polygons
   * @throws InvalidNodingException if invalid noding is found
   */
  public static Geometry overlay(Geometry g1, Geometry g2, 
      PrecisionModel precModel) 
  {
    return overlay(g1, g2, precModel, true, false, false);
  }
  
  /**
   * Union all the polygons in one or two geometries.
   * <p>
   * If a precision model is supplied, 
   * the input is snapped to the precision model 
   * during processing.
   * 
   * @param g1 a geometry containing polygons
   * @param g2 a geometry containing polygons (may be null)
   * @param precModel the precision model to use (may be null)
   * @param unionCreateHolesAsPolys whether to create polygons for holes
   * @return the union MultiPolygon
   * @throws InvalidNodingException if invalid noding is found
   */
  public static Geometry union(Geometry g1, Geometry g2, 
      PrecisionModel precModel,
      boolean unionCreateHolesAsPolys) 
  {
    return overlay(g1, g2, precModel, true, true, unionCreateHolesAsPolys);
  }
  
  /**
   * Overlay two sets of input polygons,
   * using an optional precision model.
   * <p>
   * If a precision model is supplied, 
   * the input is snapped to the precision model 
   * during processing.
   * 
   * @param g1 a geometry containing polygons
   * @param g2 a geometry containing polygons (may be null)
   * @param precModel the precision model to use (may be null)
   * @param isValidating whether to validate that the noding is correct
   * @param isUnion
   * @param unionCreateHolesAsPolys
   * @return the collection of resultant polygons
   * @throws InvalidNodingException if invalid noding is found
   */
  public static Geometry overlay(Geometry g1, Geometry g2, 
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
    
    //---- create pipeline
    PolygonOverlay overlay = new PolygonOverlay(geomFact, precModel);
    overlay.init(extracter, gcSink, isValidating);
    //----- set overlay options
    overlay.setUnion(isUnion);
    overlay.setUnionCreateHolesAsPolys(unionCreateHolesAsPolys);
    
    //--- set up the geometry input
    extracter.add(g1);
    extracter.add(g2);
    
    //---- run the process
    extracter.extract();
    
    //---- get results
    return gcSink.getGeometry(true);
  }

  private GeometryFactory geomFactory;
  private PrecisionModel precisionModel = null;
  private SweepLineNodingValidator validator = null;
  private SweepLinePolygonizer polygonizer;
  
  public PolygonOverlay(GeometryFactory geomFactory) 
  {
    this(geomFactory, null);
  }

  public PolygonOverlay(GeometryFactory geomFactory, PrecisionModel precisionModel) 
  {
    this.geomFactory = geomFactory;
    this.precisionModel = precisionModel;
  }

  /**
   * 
   * @param inputSegSrc
   * @param geomSink
   * @param isValidating whether to validate that the noding is correct
   */
  public void init(SegmentSource inputSegSrc,
      GeometrySink geomSink,
      boolean isValidating)
  {
    SweepLineNoder noder = new SweepLineNoder(precisionModel);
    inputSegSrc.setSink(noder);
    
    SegmentSource outputSegSrc = noder; 
    if (isValidating) {
      validator = new SweepLineNodingValidator(true);
      noder.setSink(validator);
      outputSegSrc = validator;
    }
    
    polygonizer = new SweepLinePolygonizer(geomFactory);
    outputSegSrc.setSink(polygonizer);
    polygonizer.setSink(geomSink);
  }

  /**
   * Sets whether the overlay process should compute the
   * union of the input polygons.
   * 
   * @param isUnion whether to compute a union
   */
  public void setUnion(boolean isUnion)
  {
    polygonizer.setUnion(isUnion);
  }
  
  /**
   * Sets whether the union process should create polygons
   * for holes which are created by the input polygons.
   * <p>
   * Holes may created in the union by:
   * <ul>
   * <li>Holes in input polygons which are not fully covered by other input polygons
   * <li>A hole left by an area surrounded by a contiguous "ring" of input polygons,
   * and which is not covered by other input polygons
   * </ul>
   * 
   * @param isUnionCreateHolesAsPolys
   */
  public void setUnionCreateHolesAsPolys(boolean isUnionCreateHolesAsPolys)
  {
    polygonizer.setUnionCreateHolesAsPolys(isUnionCreateHolesAsPolys);
  }
  
  public SweepLinePolygonizer getPolygonizer()
  {
    return polygonizer;
  }

  public SweepLineNodingValidator getValidator()
  {
    return validator;
  }

  public boolean isNodingValid()
  {
    if (validator != null && ! validator.getGeometry().isEmpty()) {
      return false;
    }
    return true;
  }
}
