package forse.noding;

import forse.lineseg.LineSeg;

import org.locationtech.jts.geom.Coordinate;

public class SegEndEvent 
extends Event
{
  
  public SegEndEvent(Coordinate location, LineSeg seg) {
    super(location, seg);
  }
  public int priority() { return PRIORITY_END; }

  public String toString()
  {
  	return "End - " + seg;
  }
}
