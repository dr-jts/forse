package forse.cmd;

import forse.geomsink.GeometrySink;
import forse.lineseg.LineSeg;
import forse.lineseg.SegmentSink;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jtstest.function.FunctionsUtil;

public class GeometryMonitorSink 
implements SegmentSink, GeometrySink
{

  GeometryFactory geomFact = new GeometryFactory();
  
  public GeometryMonitorSink() {
    super();
  }

  public void close() {
    
  }

  public void process(LineSeg seg)
  {
    process(geomFact.createLineString(
        new Coordinate[] { seg.getCoordinate(0), seg.getCoordinate(1) }
        ));
  }
  
  public void process(Geometry geom) {
    FunctionsUtil.showIndicator(geom);
    //OperationMonitorManager.indicator = geom;
  }


}
