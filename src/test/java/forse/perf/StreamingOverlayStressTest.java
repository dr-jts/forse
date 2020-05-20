package forse.perf;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class StreamingOverlayStressTest
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(StreamingOverlayStressTest.class);
  }
  
  public StreamingOverlayStressTest(String name) { super(name); }

  public void testOverlay()
  {
    StreamingCirclesOverlayer overlay = new StreamingCirclesOverlayer();
    overlay.evaluate(100);
  }

}
