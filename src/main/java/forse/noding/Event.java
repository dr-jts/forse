package forse.noding;

import java.util.Comparator;

import forse.lineseg.LineSeg;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.util.Debug;

public abstract class Event
implements Comparable
{
  protected static final int PRIORITY_INTERSECTION = 0;
  protected static final int PRIORITY_END = 1;
  
	protected LineSeg seg;
  protected Coordinate location;
  
  public Event(Coordinate location, LineSeg seg)
  {
    this.location = location;
    this.seg = seg;
  }
  
  public abstract int priority();
  
  public Coordinate getLocation()
  {
    return location;
  }
  
  public LineSeg getSegment()
  {
  	return seg;
  }
  
  public boolean equalsAll(Event ev)
  {
    if (ev.getClass() != getClass()) return false;
    if (! location.equals2D(ev.location)) return false;
    if (! seg.equals(ev.seg)) return false;
    return true;
  }
  
  public int compareTo(Object o)
  {
    if (this == o) return 0;
    Event ev = (Event) o;
    
    /**
     * Following logic ensure that SegEndEvents always sort 
     * AFTER intersection events in the same column.
     * This is necessary because in some cases 
     * involving a nearly vertical segment 
     * travelling SE,
     * an intersection can produce a point with the same
     * X value as the endpoint, but with a larger
     * Y value - thus being greater in lexocographic order.
     * The intersection point event would then normally be sorted after
     * the segment end event, which
     * would lead to a failure during segment chopping. 
     * Sorting segment ends after intersections 
     * in the same column avoids this problem.
     * This does not affect the correctness of the noding.
     * Performance is reduced very slightly, since
     * segments are kept in the SweepLineStatus for longer.
     */
    if (location.x == ev.location.x) {
      if (priority() > ev.priority()) 
        return 1;
      if (priority() < ev.priority()) 
        return -1;
      // else if priorities are equal, use normal comparison logic
    }
    return location.compareTo(ev.location);
  }
  
  /**
   * Compares both location and segment of two events.
   * 
   * @author Martin Davis
   *
   */
  public static class LocSegComparator implements Comparator
  {
    public int compare(Object o1, Object o2) 
    {
      Event e1 = (Event) o1;
      Event e2 = (Event) o2;
      if (e1 == e2) return 0;

      
     int locComp = e1.location.compareTo(e2.location);
     if (locComp != 0) return locComp;
     return e1.seg.compareTo(e2.seg);
    }
 }
}
