package forse.polygonize;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;

/**
 * NOT USED
 * - doesn't handle branching gores
 * - inverted hole removal handles gore removal anyway (including branching ones)
 * 
 * Removes gores from rings.
 * Gores are sequences of vertices of 
 * odd length where the leading n/2 vertices
 * are the mirror image of the last n/2 vertices
 * (in other words, vertex palindromes,
 * which must necessarily be of odd length).
 * They are self-intersections, and hence are invalid in rings.
 * They occur after polygonization as 
 * a result of "inside dangles" in faces.
 * 
 * @author Martin Davis
 *
 */
public class GoreRemover
{
  //TODO: make iterative to remove branching gores
  
  public static Coordinate[] remove(Coordinate[] ring)
  {
    GoreRemover gr = new GoreRemover(ring);
    //return gr.getResult();
    
    // DDDDDDDDDDDDDDDDDDDD
    // Testing for performance impact
    return ring;
  }
  
  private Coordinate[] inputRing;
  private int ringLen;

  public GoreRemover(Coordinate[] inputRing)
  {
    this.inputRing = inputRing;
    ringLen = inputRing.length - 1;
  }
  
  public Coordinate[] getResult()
  {
    boolean foundGore = false;
    for (int i = 0; i < ringLen; i++) {
      if (inputRing[i] == null)
        continue;
      if (removeGore(i))
        foundGore = true;
    }
    if (foundGore) {
      return collapseRing(inputRing);
    }
    return inputRing;
  }

  private boolean removeGore(int i)
  {
    int iprev = i;
    int inext = i;
    /**
     * Records the last computed inext value.
     * The current inext coordinate is not removed, because
     * it forms the "base" of the gore,
     * so is required to be kept as a vertex.
     */
    int inextPrev = i;
    
    boolean found = false;
    do {
      iprev = prev(iprev);
      inext = next(inext);
      if (inputRing[iprev] == null || inputRing[inext] == null) 
        return found;
      // TODO: check for ends wrapping
      if (! inputRing[iprev].equals2D(inputRing[inext])) {
        return found;
      }
      inputRing[iprev] = null;
      inputRing[inextPrev] = null;
      inputRing[i] = null;
      found = true;
      inextPrev = inext;
    } while(true);
  }
  
  private int next(int i)
  {
    return (i + 1) % ringLen;
  }

  private int prev(int i)
  {
    int prev = i - 1;
    if (prev < 0)
      return ringLen - 1;
    return prev;
  }

  private Coordinate[] collapseRing(Coordinate[] ring)
  {
    CoordinateList coordList = new CoordinateList();
    // don't copy source closing coord - one will be added if needed
    for (int i = 0; i < ringLen; i++) {
      if (ring[i] != null) coordList.add(ring[i], false);
    }
    coordList.closeRing();
    return coordList.toCoordinateArray();
  }

}
