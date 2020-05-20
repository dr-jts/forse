package forse.noding;

import java.util.*;

/**
 * Exposes an internal method of ArrayList.
 * 
 * @author mbdavis
 *
 */
public class ExtendedArrayList
extends ArrayList
{

  public ExtendedArrayList() {
    super();
  }

  public void removeRange(int from, int to)
  {
    super.removeRange(from, to);
  }
}
