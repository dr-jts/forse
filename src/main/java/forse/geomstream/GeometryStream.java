package forse.geomstream;

import org.locationtech.jts.geom.Geometry;

/**
 * An interface representing a stream of Geometrys.
 * 
 * 
 * @author Martin Davis
 *
 */
public interface GeometryStream 
{
  /**
   * Returns the next geometry in the stream, if any,
   * or else null if the stream is empty.
   * 
   * @return null if no more geometries in stream
   */
  Geometry next();
}
