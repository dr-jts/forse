package forse;

import forse.lineseg.LineSeg;

/**
 * Indicates that invalid noding was detected
 * in an arrangement of geometries.
 * The {@link LineSeg} which intersect improperly are reported.
 * 
 * @author Martin Davis
 *
 */
public class InvalidNodingException extends RuntimeException
{

  public InvalidNodingException(LineSeg seg1, LineSeg seg2)
  {
    super("Invalid noding found at: "
    + LineSeg.toWKT(seg1, seg2));
  }

}
