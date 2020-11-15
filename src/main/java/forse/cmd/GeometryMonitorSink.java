package forse.cmd;

import forse.geomsink.GeometrySink;
import forse.lineseg.LineSeg;
import forse.lineseg.SegmentSink;

import java.awt.Color;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.util.TestBuilderProxy;
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
    if (! isActive()) return;
    
    Geometry line = geomFact.createLineString(
        new Coordinate[] { seg.getCoordinate(0), seg.getCoordinate(1) }
        );
    TestBuilderProxy.showIndicator(line, Color.CYAN);
    //FunctionsUtil.showIndicator(line, Color.CYAN);
  }
  
  private static boolean isActive() {
    return FunctionsUtil.isTestBuilderRunning();
  }

  public void process(Geometry geom) {
    if (! isActive()) return;
    
    TestBuilderProxy.showIndicator(geom, Color.CYAN);
    //FunctionsUtil.showIndicator(geom, Color.CYAN);
    //OperationMonitorManager.indicator = geom;
  }


}
