package forse.lineseg;

import java.util.*;

import org.locationtech.jts.algorithm.CGAlgorithms;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.*;
import org.locationtech.jts.util.Stopwatch;

/**
 * Extracts line segments from input data as {@link LineSegs}s,
 * labels them with topological information,
 * sorts the segments, removes duplicates,
 * and passes them to a {@link SegmentSink}.
 * 
 * @author mbdavis
 *
 */
public class GeometrySegmentExtracterSource 
implements SegmentSource
{
  private SegmentSink segSink;
  private List<LineSeg> segs = new ArrayList<LineSeg>();
  // default: do not round
  private PrecisionModel precisionModel = null;

  public GeometrySegmentExtracterSource() {
  }

  public GeometrySegmentExtracterSource(PrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  public void setSink(SegmentSink segSink)
  {
  	this.segSink = segSink;
  }
  
  /**
   * Sets the {@link PrecisionModel} to use for rounding the input coordinates.
   * 
   * The default is to use the coordinates as they are provided.
   * 
   * @param precModel the precision model to use
   */
  public void setPrecisionModel(PrecisionModel precisionModel)
  {
    this.precisionModel = precisionModel;
  }
  
  public void add(Collection<?> geoms)
  {
    GeometrySegmentExtracter.extract(geoms, precisionModel, segs);
  }
  
  public void add(Geometry geom)
  {
    if (geom == null) return;
    GeometrySegmentExtracter.extract(geom, precisionModel, segs);
  }
    
  public void extract()
  {
    Collections.sort(segs);
    
    //System.out.println("GeometrySegmentExtracterSource: Sorted");
    //Stopwatch sw = new Stopwatch();
    
    //TODO: do merging on-the-fly during processing phase
    List<LineSeg> uniqueSegs = mergeDuplicates(segs);
    // free the segs array since it is no longer needed
    segs = null;
    
    for (Iterator<LineSeg> i = uniqueSegs.iterator(); i.hasNext(); ) {
    	segSink.process((LineSeg) i.next());
    }
    segSink.close();
    //System.out.println("GeometrySegmentExtracterSource: finished in " + sw.getTimeString());
  }
  
  public static List<LineSeg> mergeDuplicates(List<LineSeg> sortList)
  {
    List<LineSeg> unique = new ArrayList<LineSeg>();
    LineSeg prev = null;
    int size = sortList.size();
    for (int i = 0; i < size; i++) {
      LineSeg curr = (LineSeg) sortList.get(i);
      if (prev == null || curr.compareTo(prev) != 0) {
        prev = curr;
        unique.add(curr);
      }
      else {
        // segs are equal, so merge into previous seg (which is already stored in array)
        prev.merge(curr);       
      }
    }
    return unique;
  }

}
