package forse.perf;

import org.locationtech.jts.geom.Geometry;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class StreamingOverlayStressTest
extends TestCase
{
  private static final int RUN_SIZE = 1000;

  public static void main(String args[]) {
    TestRunner.run(StreamingOverlayStressTest.class);
  }
  private boolean isOutput = true;
  
  public StreamingOverlayStressTest(String name) { super(name); }
  
  public void testOverlay()
  {
    OverlappingRingsOverlay overlay = new OverlappingRingsOverlay();
    overlay.init(RUN_SIZE);
    while (true) {
      Geometry g = overlay.next();
      if (g == null) break;
      if (isOutput )
        System.out.println(g);
    }

  }
}
