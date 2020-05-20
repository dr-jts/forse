package forse.noding;

import java.util.*;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.util.Debug;


public class SimpleEventQueue
implements EventQueue
{
  private ArrayList events = new ArrayList();
  private Comparator eventComparator = null;
  
  public SimpleEventQueue() {
  }

  public SimpleEventQueue(Comparator eventComparator) {
    this.eventComparator = eventComparator;
  }

  public void add(Event ev)
  {
    int insertIndex = -1;
    if (eventComparator == null)
      insertIndex = Collections.binarySearch(events, ev);
    else 
      insertIndex = Collections.binarySearch(events, ev, eventComparator);
    
    if (insertIndex >= 0) {
      // Found event with identical location
      // Note that sets of events with the same location will not be ordered
      
      // Heuristic: check if event after insert is exactly the same - if so, don't insert
      Event evInList = (Event) events.get(insertIndex);
      if (evInList.equalsAll(ev))
        return;
      
      events.add(insertIndex, ev);
    }
    else {
      // Location not found - insert at returned insertion point
      events.add(-(insertIndex + 1), ev);      
    }
  }

  public Event peek()
  {
    if (events.isEmpty())
      return null;
    Event ev = (Event) events.get(0);
    return ev;
  }
  
  public Event peek(int i)
  {
    if (events.isEmpty())
      return null;
    Event ev = (Event) events.get(i);
    return ev;
  }
  
  public Event pop()
  {
    Event ev = (Event) events.get(0);
    events.remove(0);
    return ev;
  }
  
  public void pop(int n)
  {
    for (int i = 0; i < n; i++)
      events.remove(0);
  }
  
  public void popBefore(double x)
  {
    while (events.size() > 0) {
      Event ev = (Event) events.get(0);
      if (ev.getLocation().x >= x)
        return;
      events.remove(0);
    }
  }
  
  public boolean isEmpty()
  {
    return events.isEmpty();
  }

  public int size() {
    return events.size();
  }

  /**
   * Computes the column size,
   * i.e. the number of items in the queue 
   * with the same X ordinate as the leading item.
   * 
   * @return the column size
   */
  public int columnSize()
  {
    if (events.isEmpty())
      return 0;
    double colOrdinate = peek().getLocation().x;
    int count = 0;
    while (size() > count
        && peek(count).getLocation().x == colOrdinate) {
      count++;
    }
    return count;
  }

}
