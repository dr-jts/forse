package forse.geomstream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.util.LinearComponentExtracter;

import forse.lineseg.GeometrySegmentExtracter;
import forse.lineseg.LineSeg;
import forse.lineseg.SegmentSink;
import forse.lineseg.SegmentSource;
import forse.noding.SegmentStreamSorter;
import forse.noding.SweepLineFront;
import forse.util.CollectionsUtil;

public class GeometryStreamSegmentSource 
implements SegmentSource
{
  private static final double DOUBLE_NEG_MAX_VALUE = -Double.MAX_VALUE;
  
  private GeometryStream stream;
  private SegmentStreamSorter segSorter;

  private double minGeometryX = DOUBLE_NEG_MAX_VALUE;

  private int polyCount = 0;
  private long ptCount = 0;
  
  public GeometryStreamSegmentSource() {
    this(null);
  }
  
  public GeometryStreamSegmentSource(GeometryStream geomStream) {
    stream = geomStream;
    segSorter = new SegmentStreamSorter(new SweepLineFront()
        {
          public double minimumActiveX() {
            return minGeometryX();
          }
        });
  }

  public int getGeometryCount() { return polyCount; }
  public long getCoordinateCount() { return ptCount; }
  
  public void setSink(SegmentSink segSink)
  {
    segSorter.setSink(segSink);
  }
  
  /**
   * Process the input stream by extracting the segments
   * and passing them to the provided {@link SegmentSink}
   * in segment order.
   * 
   * @throws IllegalArgumentException if an input stream is not sorted correctly
   */
  public void process()
  {
    while (true) {
      if (! processOne())
        break;
    }
    segSorter.close();
  }

  public boolean processOne()
  {
    Geometry geom = stream.next();
    // no input left to process
    if (geom == null)
      return false;
    process(geom);
    return true;
  }
  
  public void close()
  {
    segSorter.close();
  }

  private double minGeometryX()
  {
    return minGeometryX;
  }
  
  private List<LineSeg> segList = new ArrayList();

  /**
   * 
   * @throws IllegalArgumentException if an input stream is not sorted correctly
   */
  private void process(Geometry geom)
  {
    polyCount += 1;
    ptCount += geom.getNumPoints();
    
    // assert - min x value never decreases
    double minx = geom.getEnvelopeInternal().getMinX();
    if (minx < minGeometryX) {
      throw new IllegalArgumentException("Input geometry stream is not sorted");
    }
    minGeometryX = minx;
    
    segList.clear();
    GeometrySegmentExtracter.extract(geom, segList);
    processSegs(segList);
  }
  
  private void processSegs(List segs)
  {
    Collections.sort(segs);
    
    // should not be any duplicate segments if geometry is valid
    // in any case they will be removed by segSorter
    
    for (Iterator i = segs.iterator(); i.hasNext(); ) {
      segSorter.process((LineSeg) i.next());
    }
  }

  
  
}
