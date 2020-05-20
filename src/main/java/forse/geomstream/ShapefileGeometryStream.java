package forse.geomstream;

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jtstest.testbuilder.io.shapefile.Shapefile;
import org.locationtech.jtstest.testbuilder.io.shapefile.ShapefileException;

public class ShapefileGeometryStream implements GeometryStream {
  Shapefile shapefile;

  GeometryFactory geomFact;

  public ShapefileGeometryStream(Shapefile shapefile, GeometryFactory geomFact) 
    throws IOException,ShapefileException,Exception
    {
    this.shapefile = shapefile;
    this.geomFact = geomFact;
    shapefile.readStream(geomFact);
  }

  public Geometry next() 
  {
    try {
      return shapefile.next();
    }
    catch (IOException ex) {
      throw new IllegalStateException(ex.getMessage());
    }
  }
}
