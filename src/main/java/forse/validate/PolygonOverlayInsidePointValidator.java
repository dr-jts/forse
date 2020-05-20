package forse.validate;

import java.util.*;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.GeometryCombiner;
import org.locationtech.jts.geom.util.GeometryMapper;
import org.locationtech.jts.geom.util.GeometryMapper.MapOp;
import org.locationtech.jts.operation.overlay.validate.OffsetPointGenerator;

/**
 * Validates the result of a polygonal overlay
 * by testing that every resultant polygon
 * is covered by one or more input polygons.
 * Uses interior points to determine covers relationship.
 * This is quite slow.
 * 
 * @author Martin Davis
 *
 */
public class PolygonOverlayInsidePointValidator 
{
  private static final double OFFSET_DIST = 0.0001;
  
  private Geometry failedPts;
  
  public PolygonOverlayInsidePointValidator() {
    super();
  }

  public boolean isValid(Geometry input, Geometry result)
  {
    return isValid(input, null, result);
  }
  
  public boolean isValid(Geometry input1, Geometry input2, Geometry result)
  {
    compute(input1, input2, result);
    return ! failedPts.isEmpty();
  }
  
  public MultiPoint getFailurePts(GeometryFactory geomFact)
  {
    return (MultiPoint) failedPts;
  }
  
  
  
  static class InteriorPointMapOp implements MapOp
  {
    public Geometry map(Geometry g)
    {
        return g.getInteriorPoint();
    }
  }
  
  private void compute(Geometry input1, Geometry input2, Geometry result)
  {
    // find input inside points not covered by result
    Geometry interiorPts = insidePoints(input1);
    if (input2 != null) {
      interiorPts = interiorPts.union(insidePoints(input2));
    }
    Geometry uncoveredInput = differencePoints(interiorPts, result);
    if (! uncoveredInput.isEmpty()) {
      failedPts = uncoveredInput;
      return;
    }
    
    // find result inside points not covered by inputs
    Geometry inputAll = GeometryCombiner.combine(input1, input2);
    Geometry resultInteriorPts = insidePoints(result);
    Geometry uncoveredResult = differencePoints(resultInteriorPts, inputAll);
    failedPts = uncoveredResult;
  }

  private Geometry differencePoints(Geometry points, Geometry polys)
  {
    Coordinate[] coords = points.getCoordinates();
    CoordinateList insidePts = new CoordinateList(coords);
   
    // sort in Coordinate order (which implies sorting along X axis)
    Collections.sort(insidePts);
    
    removeCoveredPoints(insidePts, polys);
    return polys.getFactory().createMultiPoint(insidePts.toCoordinateArray());
  }

  private Geometry insidePoints(Geometry input1)
  {
    Geometry interiorPts = GeometryMapper.map(input1, new InteriorPointMapOp());
    return interiorPts;
  }
  
  private void removeCoveredPoints(List probePts, Geometry polyCollection)
  {
    lastPct = 0;
    for (int i = 0; i < polyCollection.getNumGeometries(); i++) {
      Polygon poly = (Polygon) polyCollection.getGeometryN(i);
      removeCoveredPoints(probePts, poly);
      int pct = (100 * i) / polyCollection.getNumGeometries();
      reportPercent(pct);
    }
  }
  
  private int lastPct = 0;
  
  private void reportPercent(int pct)
  {
    if (pct <= lastPct) return;
    System.out.println("Percent done = " + pct);
    lastPct = pct;
  }
  
  private void removeCoveredPoints(List probePts, Polygon poly)
  {
    Envelope env = poly.getEnvelopeInternal();
    IndexedPointInAreaLocator loc = new IndexedPointInAreaLocator(poly);
    int n = probePts.size();
    int i = 0;
    while (i < probePts.size()) {
      Coordinate probePt = (Coordinate) probePts.get(i);
      
      // can quit if remaining pts are further right than geometry
      if (probePt.x > env.getMaxX())  return;
      
      // continue if point is not in envelope
      if (! env.contains(probePt)) {
        i++;
        continue;
      }
      else if (Location.EXTERIOR != loc.locate(probePt)) {
        // point is in poly - remove it from list
        probePts.remove(i);
        // DON'T increment i, since array is now shorter
        continue;
      }
      i++;
    }
    
  }
  
}
