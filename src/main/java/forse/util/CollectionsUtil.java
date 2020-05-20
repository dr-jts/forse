package forse.util;

import java.util.*;
import java.util.List;

public class CollectionsUtil 
{
  public static List removeDuplicates(List sortedList)
  {
    List unique = new ArrayList();
    Comparable prev = null;
    int size = sortedList.size();
    for (int i = 0; i < size; i++) {
      Comparable curr = (Comparable) sortedList.get(i);
      boolean isDifferent = prev == null || curr.compareTo(prev) != 0;
      if (isDifferent) {
        prev = curr;
        unique.add(curr);
      }
    }
    return unique;
  }
  
  public static Collection findDuplicates(List sortedList)
  {
    Set dup = new HashSet();
    Comparable prev = null;
    int size = sortedList.size();
    for (int i = 0; i < size; i++) {
      Comparable curr = (Comparable) sortedList.get(i);
      boolean isDifferent = prev == null || curr.compareTo(prev) != 0;
      if (isDifferent) {
        prev = curr;
      }
      else {
        dup.add(curr);
      }
    }
    return dup;
  }

}
