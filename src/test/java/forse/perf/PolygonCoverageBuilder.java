package forse.perf;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;



public class PolygonCoverageBuilder
{
  List geoms = new ArrayList();
  private GeometryFactory geomFact;
  PrecisionModel pm;

  public PolygonCoverageBuilder(GeometryFactory geomFact)
  {
    this.geomFact = geomFact;
    pm = geomFact.getPrecisionModel();
  }

  public List build(Coordinate base, double size, int numOnSide, int nSegs)
  {
    for (int i = 0; i < numOnSide; i++) {
      for (int j = 0; j < numOnSide; j++) {
        Coordinate sqBase = new Coordinate(base.x + size * i, base.y + size * j);
        geoms.add(buildPolygon(i, j, sqBase, size, nSegs));
      }
    }
    return geoms;
  }


  private Geometry buildPolygon(int i, int j, Coordinate base, double size, int nSegs)
  {
    Coordinate base2 = new Coordinate(base.x + size, base.y);

    CoordinateList coordList = new CoordinateList();
    coordList.add(buildVeeLine(base, nSegs, size, false), false);
//    coordList.add(buildSegmentedLine(base, base2, nSegs, size, false), false);

    Coordinate[] pts2 = buildVeeLine(base2, nSegs, size, false);
    CoordinateArrays.reverse(pts2);
    coordList.add(pts2, false);

    coordList.closeRing();

    Polygon sq = geomFact.createPolygon(
        geomFact.createLinearRing(coordList.toCoordinateArray()), null);

    return sq;
  }

  Coordinate[] buildWavyLine(Coordinate base, int nSegs, double size, boolean isHorizontal)
  {
    Coordinate[] pts = new Coordinate[nSegs + 1];
    pts[0] = base;
    if (isHorizontal) {
    	pts[nSegs] = new Coordinate(base.x + size, base.y);
    }
    else
    	pts[nSegs] = new Coordinate(base.x, base.y + size);    	

    double offset = 0.1 * size;
    
    for (int i = 1; i < nSegs; i++ ) {
      double dx = i * (size / nSegs);
      double dy = offset * ((i % 2) == 0 ? 1 : -1);
      double xFinal = base.x + dx;
      double yFinal = base.y + dy;
      if (! isHorizontal) {
      	xFinal = base.x + dy;
      	yFinal = base.y + dx;
      }
      pts[i] = new Coordinate(pm.makePrecise(xFinal), pm.makePrecise(yFinal) );
    }
    return pts;
  }
  Coordinate[] buildVeeLine(Coordinate base, int nSegs, double size, boolean isHorizontal)
  {
    Coordinate[] pts = new Coordinate[nSegs + 1];
    pts[0] = base;
    if (isHorizontal) {
    	pts[nSegs] = new Coordinate(base.x + size, base.y);
    }
    else
    	pts[nSegs] = new Coordinate(base.x, base.y + size);    	

    double offset = 0.1 * size;
    
    for (int i = 1; i < nSegs; i++ ) {
      double dx = i * (size / nSegs);
      double dy = offset * (nSegs/2 - i);
      double xFinal = base.x + dx;
      double yFinal = base.y + dy;
      if (! isHorizontal) {
      	xFinal = base.x + dy;
      	yFinal = base.y + dx;
      }
      pts[i] = new Coordinate(pm.makePrecise(xFinal), pm.makePrecise(yFinal) );
    }
    return pts;
  }
}