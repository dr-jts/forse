package forse;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;

import forse.lineseg.GeometrySegmentExtracterSource;
import forse.lineseg.NullSegmentSink;
import forse.noding.SweepLineNodingValidator;

/**
 * Validates the noding of a set of geometries using FORSE.
 * 
 * @author Martin Davis
 *
 */
public class NodingValidator 
{
  /**
   * Validates the noding of the line segments in a pair of geometries.
   * 
   * @param g1 a geometry
   * @param g2 a geometry (may be null)
   * @return a collection of MultiLineStrings containing the pairs of invalid segments
   */
  public static GeometryCollection nodingValidator(Geometry g1, Geometry g2)
  {
    GeometrySegmentExtracterSource extracter = new GeometrySegmentExtracterSource();
    
    SweepLineNodingValidator validator = new SweepLineNodingValidator();
    
    extracter.setSink(validator);
    NullSegmentSink monSink = new NullSegmentSink();
    validator.setSink(monSink);
    
    extracter.add(g1);
    extracter.add(g2);
    extracter.extract();
    
    return validator.getGeometry();
  }

}
