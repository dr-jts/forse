package forse.geomstream;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import forse.PolygonOverlay;
import forse.geomsink.GeometryCreatorSink;

public class PolygonOverlayGeometryStream implements GeometryStream
{
  private GeometryFactory geomFactory;
  private PrecisionModel precisionModel = null;
  private GeometryStreamSegmentSource segSrc;
  private MergeSortGeometryStream merger;
  private GeometryCreatorSink gcSink;
  private boolean isValidating = true;
  
  public PolygonOverlayGeometryStream(GeometryFactory geomFactory) 
  {
    this(geomFactory, null);
  }
  
  public PolygonOverlayGeometryStream(GeometryFactory geomFactory, PrecisionModel precisionModel) 
  {
    this.geomFactory = geomFactory;
    this.precisionModel = precisionModel;
  }

  public void setValidating(boolean isValidating) {
	  this.isValidating = isValidating;
  }
  
  public void create(GeometryStream g1, GeometryStream g2)
  {
    merger = new MergeSortGeometryStream();
    merger.add(g1);
    if (g2 != null) merger.add(g2);
    segSrc = new GeometryStreamSegmentSource(merger);

    gcSink = new GeometryCreatorSink(geomFactory);
    
    PolygonOverlay overlay = new PolygonOverlay(geomFactory, precisionModel);
    overlay.init(segSrc, gcSink, isValidating);
  }
  
  public MergeSortGeometryStream getMerger()
  {
    return merger;
  }

  @Override
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
    while (gcSink.size() == 0) {
      boolean isMore = segSrc.processOne();
      if (! isMore) {
        segSrc.close();
        break;
      }
    }
  }

}
