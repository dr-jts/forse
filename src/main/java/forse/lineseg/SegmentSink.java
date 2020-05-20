package forse.lineseg;

public interface SegmentSink 
{
  void process(LineSeg seg);
  void close();
}
