package forse.geomstream;

import org.locationtech.jts.geom.Geometry;

public class GeometryArrayGeometryStream 
  implements GeometryStream
  {
    Geometry[] geom;
    int i = 0;
    
    public GeometryArrayGeometryStream(Geometry[] geom)
    {
      this.geom = geom;
    }
    
    public Geometry next()
    {
      if (i >= geom.length)
        return null;
      return geom[i++];
    }
  }
