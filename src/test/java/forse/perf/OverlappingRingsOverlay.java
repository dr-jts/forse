package forse.perf;

import java.util.List;
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
 * Generates an X-sorted stream of annular circles, and overlays them.
 * Noding is validated. Stress-tests topology creation, including holes and
 * faces created by holes covered by other polygons.
 * 
 * @author Martin Davis
 * 
 */
public class OverlappingRingsOverlay implements GeometryStream
{
  static final double ANNULAR_FRACTION = 0.6;
  
  private static GeometryFactory geomFact = new GeometryFactory();

  private GeometryStreamSegmentSource segSrc;

  private GeometryCreatorSink gcSink;

  public OverlappingRingsOverlay()
  {

  }

  public void evaluateAll(int numCircles)
  {
    init(numCircles);
    segSrc.process();
    Geometry result = gcSink.getGeometry();
    System.out.println(result);
  }
  
  public void init(int numCircles) {
    GeometryStream geomStream = new RingGeometryStream(numCircles);

    segSrc = new GeometryStreamSegmentSource(geomStream);

    // ----- sink
    gcSink = new GeometryCreatorSink(geomFact);
    // StatisticsGeometrySink gcSink = new StatisticsGeometrySink();

    // ---- create pipeline
    PrecisionModel precModel = new PrecisionModel(1000000);
    PolygonOverlay overlay = new PolygonOverlay(geomFact, precModel);
    overlay.init(segSrc, gcSink, true);
  }

  public Geometry next()
  {
    fillNext();
    if (gcSink.size() == 0)
      return null;
    
    // pop the next output geometry and return it
    List<Geometry> geoms = gcSink.getGeometryList();
    Geometry g = geoms.get(0);
    geoms.remove(0);
    return g;
  }
  
  private void fillNext()
  {
    /**
     * Push segments into pipeline until a geometry is ready to extract.
     */
    while (gcSink.size() == 0) {
      boolean isMore = segSrc.processOne();
      if (! isMore) {
        segSrc.close();
        break;
      }
    }
  }
  
  static class RingGeometryStream implements GeometryStream
  {
    private int numCircles = 20;

    private int count = 0;

    private double height = 100;

    private double radius = 10;

    private double innerRadius = ANNULAR_FRACTION * radius;

    private double xIncrement = 2;

    private Random rand = new Random(1);

    private double x = 0;

    public RingGeometryStream(int numCircles)
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
      Geometry circle = createRing(x, y);
      // System.out.println(circle);
      x += xIncrement;
      return circle;
    }

    private Geometry createRing(double x, double y)
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
