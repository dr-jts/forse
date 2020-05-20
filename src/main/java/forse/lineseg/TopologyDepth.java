package forse.lineseg;

public class TopologyDepth
{
  /**
   *  
   * A positive depth indicates that the area to the right of the edge
   * is in the interior of one or more polygons.
   * 
   * Depth zero does not indicate either interior or exterior,
   * since topology collapse can cause this to happen
   * for edges which border an exterior face. 
   * 
   * A negative depth indicates that the area MAY be an exterior area,
   * but this must be confirmed by traversing all edges between it and
   * the world face (since there may be a larger polygon that completely
   * covers the area).
   * 
   * @param topoDepth
   * @return
   */
  public static boolean isInteriorRight(int topoDepth)
  {
    return topoDepth > 0;
  }
}
