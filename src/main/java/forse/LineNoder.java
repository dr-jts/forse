package forse;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.PrecisionModel;

import forse.geomsink.GeometryCreatorSink;
import forse.lineseg.GeometrySegmentExtracterSource;
import forse.lineseg.SegmentProcess;
import forse.lineseg.SegmentSink;
import forse.lineseg.SegmentSource;
import forse.noding.SweepLineNoder;
import forse.noding.SweepLineNodingValidator;

/**
 * Creates a Noding pipeline using FORSE.
 * 
 * @author Martin Davis
 *
 */
public class LineNoder 
{
  /**
   * Nodes the linear components of the input.
   * 
   * @param g1 a geometry containing linear components to node
   * @param precModel the precision model to use (if any)
   * @return a set of fully noded line segments
   * @throws InvalidNodingException if invalid noding occurs
   */
  public static MultiLineString node(Geometry g1, PrecisionModel precModel)
  {
    return node(g1, null, precModel, true);
  }
  
  /**
   * Nodes the linear components of the input.
   * <p>
   * If a precision model is supplied, 
   * the input is snapped to the precision model during processing.
   * 
   * @param g1 a geometry containing linear components to node
   * @param precModel the precision model to use (if any)
   * @param isValidating whether to validate that the noding is correct
   * @return a set of fully noded line segments
   * @throws InvalidNodingException if invalid noding occurs
   */
  public static MultiLineString node(Geometry g1, PrecisionModel precModel, 
      boolean isValidating)
  {
    return node(g1, null, precModel, isValidating);
  }
  
  /**
   * Nodes the linear components of the input.
   * <p>
   * If a precision model is supplied, 
   * the input is snapped to the precision model during processing.
   * 
   * @param g1 a geometry containing linear components to node
   * @param g2 a geometry containing linear components to node
   * @param precModel the precision model to use (if any)
   * @param isValidating whether to validate that the noding is correct
   * @return a set of fully noded line segments
   * @throws InvalidNodingException if invalid noding occurs
   */
  public static MultiLineString node(Geometry g1, Geometry g2, PrecisionModel precModel, 
      boolean isValidating)
  {
    //---- segment source
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource(precModel);
    ///---- segment sink
    GeometryCreatorSink gcSink = new GeometryCreatorSink(g1.getFactory());

    //--- create the noder
    LineNoder noder = new LineNoder(precModel);
    noder.init(extracter, gcSink, isValidating)  ;
    
    //--- set up the geometry input
    extracter.add(g1);
    extracter.add(g2);
    
    //---- run the process
    extracter.extract();
    
    return gcSink.getLinearGeometry();
  }

  private PrecisionModel precisionModel = null;
  private SweepLineNodingValidator validator = null;
  
  public LineNoder() 
  {
    this(null);
  }

  public LineNoder(PrecisionModel precisionModel) 
  {
    this.precisionModel = precisionModel;
  }

  /**
   * Sets up the noding pipeline.
   * <p>
   * Pipeline execution is triggered
   * by the inputSegSrc calling {@link SegmentSink#process(forse.lineseg.LineSeg)}
   * and finished by the inputSegSrc calling {@link SegmentSink#close()}.
   * 
   * @param inputSegSrc
   * @param segSink
   * @param isValidating
   */
  public void init(SegmentSource inputSegSrc,
      SegmentSink segSink,
      boolean isValidating)
  {
    SweepLineNoder noder = new SweepLineNoder(precisionModel);
    inputSegSrc.setSink(noder);
    
    SegmentProcess outputSegSrc = noder; 
    
    // add validator if requested
    if (isValidating) {
      validator = new SweepLineNodingValidator(true);
      noder.setSink(validator);
      outputSegSrc = validator;
    }
    // connect up pipeline   
    outputSegSrc.setSink(segSink);
  }
  
  public SweepLineNodingValidator getValidator()
  {
    return validator;
  }

  /**
   * 
   * @return
   */
  public boolean isNodingValid()
  {
    if (validator != null && ! validator.getGeometry().isEmpty()) {
      return false;
    }
    return true;
  }
}
