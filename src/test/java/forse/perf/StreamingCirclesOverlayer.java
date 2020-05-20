package forse.perf;

import java.util.Random;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import forse.PolygonOverlay;
import forse.geomsink.GeometryCreatorSink;
import forse.geomstream.GeometryStream;
import forse.geomstream.GeometryStreamSegmentSource;

/**
 * Generates a sequence of annular circles as a stream, and overlays them.
 * Noding is validated. Stress-tests topology creation, including holes and
 * faces created by holes covered by other polygons.
 * 
 * @author Martin Davis
 * 
 */
public class StreamingCirclesOverlayer
{
  static final double ANNULAR_FRACTION = 0.6;
  
  private static GeometryFactory geomFact = new GeometryFactory();

  private GeometryStreamSegmentSource segSrc;

  public StreamingCirclesOverlayer()
  {

  }

  public void evaluate(int numCircles)
  {
    GeometryStream geomStream = new CircleGeometryStream(numCircles);

    segSrc = new GeometryStreamSegmentSource(geomStream);

    // ----- sink
    GeometryCreatorSink gcSink = new GeometryCreatorSink(geomFact);
    // StatisticsGeometrySink gcSink = new StatisticsGeometrySink();

    // ---- create pipeline
    PrecisionModel precModel = new PrecisionModel(1000000);
    PolygonOverlay overlay = new PolygonOverlay(geomFact, precModel);
    overlay.init(segSrc, gcSink, true);

    segSrc.process();

    Geometry result = gcSink.getGeometry();
    System.out.println(result);
  }

  static class CircleGeometryStream implements GeometryStream
  {
    private int numCircles = 20;

    private int count = 0;

    private double height = 100;

    private double radius = 10;

    private double innerRadius = ANNULAR_FRACTION * radius;

    private double xIncrement = 2;

    private Random rand = new Random(1);

    private double x = 0;

    public CircleGeometryStream(int numCircles)
    {
      this.numCircles = numCircles;
    }

    @Override
    public Geometry next()
    {
      if (count > numCircles)
        return null;
      count++;
      return createRandom();
    }

    private Geometry createRandom()
    {
      double y = height * rand.nextDouble();
      Geometry circle = createCircle(x, y);
      // System.out.println(circle);
      x += xIncrement;
      return circle;
    }

    private Geometry createCircle(double x, double y)
    {
      Geometry centre = geomFact.createPoint(new Coordinate(x, y));
      Polygon outer = (Polygon) centre.buffer(radius);
      Polygon inner = (Polygon) centre.buffer(innerRadius);
      Geometry circle = geomFact.createPolygon(
          (LinearRing) outer.getExteriorRing(),
          new LinearRing[] { (LinearRing) inner.getExteriorRing() });
      return circle;
    }

  }
}
