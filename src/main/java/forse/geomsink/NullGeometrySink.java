package forse.geomsink;

import org.locationtech.jts.geom.Geometry;

public class NullGeometrySink
implements GeometrySink
{
  private int count = 0;
  
  public void process(Geometry g)
  {
    // do nothing!
  }
  
  public void close()
  {
  	// do nothing!
  }
}
