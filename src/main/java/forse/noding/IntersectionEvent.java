package forse.noding;

import org.locationtech.jts.geom.Coordinate;
import forse.*;
import forse.lineseg.LineSeg;

public class IntersectionEvent 
extends Event
{
  
  public IntersectionEvent(Coordinate location, LineSeg seg) {
    super(location, seg);
    this.seg = seg;
  }

  public int priority() { return PRIORITY_INTERSECTION; }
  
  public String toString()
  {
  	return "Intersection - " + location + " in " + seg;
  }

}
