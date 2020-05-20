package forse.geomsink;

import java.util.*;

import forse.lineseg.LineSeg;
import forse.lineseg.SegmentSink;

import org.locationtech.jts.geom.*;

public class GeometryCreatorSink 
implements SegmentSink, GeometrySink
{
	private GeometryFactory geomFact;
	private List<Geometry> geoms = new ArrayList<Geometry>();
	
	public GeometryCreatorSink(GeometryFactory geomFact)
	{
		this.geomFact = geomFact;
	}
	
  public void process(LineSeg seg)
  {
  	geoms.add(geomFact.createLineString(new Coordinate[] {
  			seg.getCoordinate(0), seg.getCoordinate(1)
  	}));
  }
  
  public void process(Geometry geom)
  {
    // add all components, which allows MultiPolygons to be handled correctly
    for (int i = 0; i < geom.getNumGeometries(); i++)
      geoms.add(geom.getGeometryN(i));
  }
  
  public void close()
  {
  	
  }
  
  public Geometry getGeometry()
  {
    return getGeometry(false);
  }
  
  /**
   * Gets a geometry containing the results of the upstream process.
   * 
   * Creates a GeometryCollection by default.
   * 
   * @return a Geometry of the requested type
   */
  public Geometry getGeometry(boolean asPolygonal)
  {
    if (geoms.size() <= 1)
      return geomFact.buildGeometry(geoms);
    if (asPolygonal) {
      return geomFact.createMultiPolygon(
          GeometryFactory.toPolygonArray(geoms));
    }
    return geomFact.createGeometryCollection(
        GeometryFactory.toGeometryArray(geoms));
  }
  
  public MultiLineString getLinearGeometry()
  {
    return geomFact.createMultiLineString(GeometryFactory.toLineStringArray(geoms));
  }
  
  public List<Geometry> getGeometryList()
  {
    return geoms;
  }
  
  public int size()
  {
    return geoms.size();
  }
}
