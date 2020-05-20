package forse.noding;

import java.util.*;
import forse.*;
import forse.lineseg.LineSeg;

/**
 * A simple sweep-line status structure.
 * Operations are O(n) in the size of the structure.
 * 
 * @author Martin Davis
 *
 */
public class SimpleSweepLineStatus 
implements SweepLineStatus
{
	private int maxSegs = 0;
	
  private ArrayList segs = new ArrayList();
  
  public SimpleSweepLineStatus() {
  }

  public void insert(LineSeg seg)
  {
    segs.add(seg); 
    if (maxSegs < segs.size())
    	maxSegs = segs.size();
  }
  
  public void remove(LineSeg seg)
  {
    segs.remove(seg);
  }
  
  public List query(LineSeg querySeg)
  {
    return query(querySeg, 0.0);
  }
  
  public List query(LineSeg querySeg, double expandBy)
  {
    List result = new ArrayList();
    for (Iterator i = segs.iterator(); i.hasNext(); ) {
    	LineSeg seg = (LineSeg) i.next();
    	// heuristic to skip a common case - don't intersect with itself
    	if (querySeg == seg)
    		continue;
    	
    	if (seg.envelopeIntersects(querySeg))
    		result.add(seg);
    }
    return result;
  }
  
  public int getMaximumSize() { return maxSegs; }
  
  public double minimumActiveX()
  {
    double minX = Double.MAX_VALUE;
    for (Iterator i = segs.iterator(); i.hasNext(); ) {
      NodedLineSeg seg = (NodedLineSeg) i.next();
      double segMinX = seg.getNodeMinX();
      if (segMinX < minX)
        minX = segMinX;
    }
    return minX;
  }
}
