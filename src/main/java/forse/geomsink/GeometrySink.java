package forse.geomsink;

import org.locationtech.jts.geom.Geometry;

public interface GeometrySink 
{
  void process(Geometry geom);
  void close();
}
