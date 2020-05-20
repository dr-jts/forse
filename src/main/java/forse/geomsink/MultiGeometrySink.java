package forse.geomsink;

import forse.cmd.GeometryMonitorSink;

import org.locationtech.jts.geom.Geometry;

public class MultiGeometrySink
implements GeometrySink
{
  public static GeometrySink create(GeometrySink sink1,
      GeometrySink sink2)
  {
    return new MultiGeometrySink(sink1, sink2);
  }

  private GeometrySink sink1;
  private GeometrySink sink2;
  
  public MultiGeometrySink(GeometrySink sink1, GeometrySink sink2) {
    this.sink1 = sink1;
    this.sink2 = sink2;
  }

  public void process(Geometry geom)
  {
    sink1.process(geom);
    sink2.process(geom);
  }
  public void close()
  {
    sink1.close();
    sink2.close();
  }


}
