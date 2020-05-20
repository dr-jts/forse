package forse.lineseg;

import java.util.*;

import forse.util.CollectionsUtil;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;

/**
 * Dissolves a set of line segments to remove duplicates.
 * Uses a sort followed by duplicate removal.
 * 
 * @author Martin Davis
 *
 */
public class SegmentDissolver 
{
  private List segs = new ArrayList();
  
  public SegmentDissolver() {
  }

  public void add(Geometry geom)
  {
    addLines(LinearComponentExtracter.getLines(geom));
  }
  
  public void addLines(Collection lines)
  {
    for (Iterator i = lines.iterator(); i.hasNext(); ) {
      LineString line = (LineString) i.next();
      add(line.getCoordinates());
    }
  }
  
  public void add(Coordinate[] linePts)
  {
    for (int i = 1; i < linePts.length; i++) {
      segs.add(new LineSeg(linePts[i-1], linePts[i]));
    }
  }
  
  public List dissolve()
  {
    Collections.sort(segs);
    List dissolved = CollectionsUtil.removeDuplicates(segs);
    return dissolved;
  }
  public Collection duplicates()
  {
    Collections.sort(segs);
    return CollectionsUtil.findDuplicates(segs);
  }
  
}
