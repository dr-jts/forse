package forse.noding;

import java.util.*;
import forse.*;
import forse.lineseg.LineSeg;

public interface SweepLineStatus 
extends SweepLineFront
{
  void insert(LineSeg seg);  
  void remove(LineSeg seg); 
  
  /**
   * Gets all segments in status whose envelopes intersect
   * a query segment.
   * Clients can perform further testing to filter these
   * segments according to their requirements.
   * 
   * @param querySeg
   * @return
   */
  List query(LineSeg querySeg);
  List query(LineSeg querySeg, double expandBy);
  double minimumActiveX();
}
