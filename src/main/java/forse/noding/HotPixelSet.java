package forse.noding;

import java.util.*;


import org.locationtech.jts.geom.Coordinate;

/**
 * Provides an efficient container
 * for a set of Hot Pixels, 
 * under the conditions that there are
 * many duplicate entries, 
 * and that the HotPixels are reused many times. 
 * 
 * @author Martin Davis
 *
 */
public class HotPixelSet
implements Iterable
{
  private double scaleFactor;
  private Map<Coordinate, HotPixel> hotPtMap = new HashMap<Coordinate, HotPixel>();

  HotPixelSet(double scaleFactor)
  {
    this.scaleFactor = scaleFactor;
  }
  
  public void add(Coordinate hotPt)
  {
    if (hotPtMap.containsKey(hotPt))
      return;
    hotPtMap.put(hotPt, new HotPixel(hotPt, scaleFactor));
  }
  
  @Override
  public Iterator<HotPixel> iterator()
  {
    // TODO Auto-generated method stub
    return hotPtMap.values().iterator();
  }
}
