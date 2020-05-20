package forse.geomsink;

import org.locationtech.jts.geom.Geometry;

public class DebugGeometrySink
implements GeometrySink
{
  public void process(Geometry geom)
  {
  	System.out.println(geom);
  }
  
  public void close()
  {
  	// do nothing!
  }
}
