package forse.validate;

import java.util.*;

import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.overlay.validate.OffsetPointGenerator;

/**
 * Validates the result of a polygonal overlay
 * by testing that every input polygon
 * is covered by one or more result polygons.
 * Uses probe points to determined covers relationship.
 * This is quite slow.
 * 
 * @author Martin Davis
 *
 */
public class PolygonOverlayProbeValidator 
{
  private static final double OFFSET_DIST = 0.0001;
  
  private List probePts;
  
  public PolygonOverlayProbeValidator() {
    super();
  }

  public boolean isValid(Geometry input, Geometry result)
  {
    return isValid(input, null, result);
  }
  
  public boolean isValid(Geometry input1, Geometry input2, Geometry result)
  {
    compute(input1, input2, result);
    return probePts.size() == 0;
  }
  
  public MultiPoint getFailedProbePts(GeometryFactory geomFact)
  {
    return geomFact.createMultiPoint(CoordinateArrays.toCoordinateArray(probePts));
  }
  
  private void compute(Geometry input1, Geometry input2, Geometry result)
  {
    probePts = new ArrayList();
    
    createProbePoints(input1, probePts);
    if (input2 != null) 
      createProbePoints(input2, probePts);
    
    // sort in Coordinate order (which implies sorting along X axis)
    Collections.sort(probePts);
    
    removeCoveredPoints(probePts, result);
  }
  
  private void createProbePoints(Geometry polyCollection, List probePts)
  {
    for (int i = 0; i < polyCollection.getNumGeometries(); i++) {
      Polygon poly = (Polygon) polyCollection.getGeometryN(i);
      addProbePoints(poly, probePts);
    }
  }
  
  private void addProbePoints(Polygon poly, List probePts)
  {
    OffsetPointGenerator ptGen = new OffsetPointGenerator(poly);
    // only generate inside pts
    ptGen.setSidesToGenerate(false, true);
    List pts = ptGen.getPoints(OFFSET_DIST);
    probePts.addAll(pts);
  }
  
  private void removeCoveredPoints(List probePts, Geometry polyCollection)
  {
    for (int i = 0; i < polyCollection.getNumGeometries(); i++) {
      Polygon poly = (Polygon) polyCollection.getGeometryN(i);
      removeCoveredPoints(probePts, poly);
      int pct = (100 * i) / polyCollection.getNumGeometries();
      reportPercent(pct);
    }
  }
  
  private static int lastPct = 0;
  
  private static void reportPercent(int pct)
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
