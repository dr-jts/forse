package forse.lineseg;

public class DebugSegmentSink
implements SegmentSink
{
  public void process(LineSeg seg)
  {
  	System.out.println(seg);
  }
  
  public void close()
  {
  	// do nothing!
  }
}
