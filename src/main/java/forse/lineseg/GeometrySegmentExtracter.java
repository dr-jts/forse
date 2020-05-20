package forse.lineseg;

import java.util.*;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;
import org.locationtech.jts.util.Stopwatch;

/**
 * Extracts line segments from input data as {@link LineSegs}s,
 * and labels them with topological information.
 * 
 * @author mbdavis
 *
 */
public class GeometrySegmentExtracter 
{
  public static void extract(Geometry geom, List<LineSeg> segList)
  {
    GeometrySegmentExtracter extracter = new GeometrySegmentExtracter(segList);
    extracter.add(geom);
  }
  
  public static void extract(Geometry geom, PrecisionModel precisionModel, List<LineSeg> segList)
  {
    GeometrySegmentExtracter extracter = new GeometrySegmentExtracter(segList);
    extracter.setPrecisionModel(precisionModel);
    extracter.add(geom);
  }
  
  public static void extract(Collection geoms, PrecisionModel precisionModel, List<LineSeg> segList)
  {
    GeometrySegmentExtracter extracter = new GeometrySegmentExtracter(segList);
    extracter.setPrecisionModel(precisionModel);
    extracter.add(geoms);
  }
  

  private List<LineSeg> segs = new ArrayList<LineSeg>();
  // default: do not round
  private PrecisionModel precisionModel = null;
  
  public GeometrySegmentExtracter(List<LineSeg> segList) {
    segs = segList;
  }
  
  /**
   * Sets the {@link PrecisionModel} to use for rounding the input coordinates.
   * 
   * The default is to use the coordinates as they are provided.
   * 
   * @param precModel the precision model to use
   */
  public void setPrecisionModel(PrecisionModel precisionModel)
  {
    this.precisionModel = precisionModel;
  }
  
  public void add(Collection<Geometry> geoms)
  {
    for (Iterator<Geometry> i = geoms.iterator(); i.hasNext(); ) {
      Geometry g = (Geometry) i.next();
      add(g);
    }
  }
  
  public void add(Geometry geom)
  {
    if (geom.isEmpty()) return;
    if (geom instanceof GeometryCollection) { 
      addGeometryCollection((GeometryCollection) geom);
    }
    else if (geom instanceof Polygon) {
      addPolygon((Polygon) geom);
    }
    else 
      addLines(LinearComponentExtracter.getLines(geom));
  }
  
  public void addGeometryCollection(GeometryCollection gc)
  {
    for (int i = 0; i < gc.getNumGeometries(); i++) {
      add(gc.getGeometryN(i));
    }
  }
  
  public void addPolygon(Polygon poly)
  {
    add(poly.getExteriorRing().getCoordinates(), true);
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      add(poly.getInteriorRingN(i).getCoordinates(), false);
    }
  }
  
  public void addLines(Collection<LineString> lines)
  {
    for (Iterator<LineString> i = lines.iterator(); i.hasNext(); ) {
      LineString line = (LineString) i.next();
      add(line.getCoordinates(), TopologyLabel.BOTH_EXTERIOR);
    }
  }
  
  public void add(Coordinate[] linePts, boolean interiorRightIfCW)
  {
    add(linePts, orientedLabel(linePts, interiorRightIfCW));
  }
  
  public void add(Coordinate[] linePts, byte sideLoc)
  {
    for (int i = 1; i < linePts.length; i++) {
      segs.add(new LineSeg(round(linePts[i-1]), round(linePts[i]), sideLoc));
    }
  }
  
  public static byte orientedLabel(Coordinate[] pts, boolean interiorRightIfCW)
  {
    boolean isCW = ! CGAlgorithms.isCCW(pts);
    if (isCW) {
      return interiorRightIfCW
          ? TopologyLabel.RIGHT_INTERIOR 
          : TopologyLabel.LEFT_INTERIOR;
    }
    return interiorRightIfCW
        ? TopologyLabel.LEFT_INTERIOR 
        : TopologyLabel.RIGHT_INTERIOR;
  }
  
  private Coordinate round(Coordinate p)
  {
    // return the input if not rounding
    if (precisionModel == null) return p;
    // round using the precision model provided
    Coordinate pRounded = new Coordinate(p);
    precisionModel.makePrecise(pRounded);
    return pRounded;
  }
  
}
