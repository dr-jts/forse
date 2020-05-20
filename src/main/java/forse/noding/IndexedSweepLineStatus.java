package forse.noding;

import java.util.*;
import forse.*;
import forse.lineseg.LineSeg;

import org.locationtech.jts.index.bintree.*;

public class IndexedSweepLineStatus 
implements SweepLineStatus
{
  /**
   * Current implementation uses a BinTree (Binary Interval Tree)
   * containing intervals representing the vertical extent of segments. 
   */
  private Bintree tree = new Bintree();

  // Kept for statistics only
  private int maxSegs = 0;

  
  public IndexedSweepLineStatus() {
  }

  private static Interval interval(LineSeg seg)
  {
    return new Interval(seg.getMinY(), seg.getMaxY());
  }
  
  private static Interval interval(LineSeg seg, double expandBy)
  {
    return new Interval(seg.getMinY() - expandBy, seg.getMaxY() + expandBy);
  }
  
  public void insert(LineSeg seg)
  {
    tree.insert(interval(seg), seg); 
    
    // this hugely slows down the computation!
//    if (maxSegs < tree.size())
//    	maxSegs = tree.size();
  }
  
  public void remove(LineSeg seg)
  {
    tree.remove(interval(seg), seg);
  }
  public List query(LineSeg querySeg)
  {
    return query(querySeg, 0.0);
  }
  
  /**
   * Queries for all {@link LineSeg}s whose
   * vertical extent intersects the 
   * vertical extent of querySeg, 
   * optionally expanded up and down by a given amount.
   * 
   * @return a list of LineSegs
   * 
   */
  public List<LineSeg> query(LineSeg querySeg, double expandBy)
  {
    List<LineSeg> result = new ArrayList<LineSeg>();
    List<LineSeg> candidates = tree.query(interval(querySeg, expandBy));
    for (LineSeg seg : candidates) {
    	// heuristic to skip a common case - don't intersect with itself
    	if (querySeg == seg)
    		continue;

    	if (seg.envelopeIntersects(querySeg, expandBy))
    		result.add(seg);
    }
    return result;
  }
  
  public int getMaximumSize() { return maxSegs; }
  
  /**
   * Gets the current minimum X value of the items in the status structure.
   */
  public double minimumActiveX()
  {
    //throw new UnsupportedOperationException("not yet implemented");
    double minx = Double.MAX_VALUE;
    for (Iterator i = tree.iterator(); i.hasNext(); ) {
      LineSeg seg = (LineSeg) i.next();
      if (seg.getMinX() < minx)
        minx = seg.getMinX();

    }
    return minx;
  }
}
