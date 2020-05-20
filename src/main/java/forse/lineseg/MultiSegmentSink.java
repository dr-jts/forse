package forse.lineseg;


public class MultiSegmentSink
implements SegmentSink
{
  public static MultiSegmentSink create(SegmentSink sink1, SegmentSink sink2)
  {
    return new MultiSegmentSink(sink1, sink2);
  }
  
  private SegmentSink sink1;
  private SegmentSink sink2;
  
  public MultiSegmentSink(SegmentSink sink1, SegmentSink sink2) {
    this.sink1 = sink1;
    this.sink2 = sink2;
  }

  public void process(LineSeg seg)
  {
    sink1.process(seg);
    sink2.process(seg);
  }
  public void close()
  {
    sink1.close();
    sink2.close();
  }

}
