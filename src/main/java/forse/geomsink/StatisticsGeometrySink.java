package forse.geomsink;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.util.Memory;
import org.locationtech.jts.util.Stopwatch;

public class StatisticsGeometrySink
implements GeometrySink
{
  private int polyCount = 0;
  private int ptCount = 0;
  private Stopwatch sw;
  
  public StatisticsGeometrySink()
  {
     sw = new Stopwatch();
  }
  
  public void process(Geometry g)
  {
    polyCount++;
    ptCount += g.getNumPoints();
  	// do nothing!
    
    if (polyCount % 10000 == 0) report();
  }
  
  public void close()
  {
    report();
    int polyPerSec = (int) (polyCount / (sw.getTime() / 1000.0));
    System.out.println("Throughput: " + polyPerSec + " poly/sec");
  	// do nothing!
  }
  
  private void report()
  {
    System.out.println("Polygons: " + polyCount 
        + "   Pts: " + ptCount
        + " ---- " + Memory.allString());   
  }
}
