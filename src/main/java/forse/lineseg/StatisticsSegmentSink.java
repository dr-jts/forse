package forse.lineseg;

import org.locationtech.jts.util.Memory;

public class StatisticsSegmentSink
implements SegmentSink
{
  private int count = 0;

  public void process(LineSeg seg)
  {
    count++;
    // do nothing!
    
    if (count % 10000 == 0) report();
  }
  
  public void close()
  {
    report();
  	// do nothing!
  }
  public void report()
  {
    System.out.println("Segments: " + count + " ---- " + Memory.allString());
  }
}
