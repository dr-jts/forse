package forse.noding;

import org.locationtech.jts.geom.Coordinate;

import forse.lineseg.LineSeg;
import junit.framework.TestCase;
import junit.textui.TestRunner;

public class HotPixelTest
extends TestCase
{
  public static void main(String args[]) {
    TestRunner.run(HotPixelTest.class);
  }

  public HotPixelTest(String name) { super(name); }

  // Hot Pixels are open along top and right, so this should have no intersection
  public void testIntersectsURCorner()
  {
    run(0,0,1, 0,1, 1,0, false);
  }

  public void testIntersectsLLCorner()
  {
    run(1,1,1, 0,1, 1,0, true);
  }

  private void run(double x, double y, double pixelSize, 
      double p0x, double p0y, double p1x, double p1y,
      boolean expectedResult)
  {
    HotPixel hp = new HotPixel(new Coordinate(x, y), pixelSize);
    LineSeg seg = new LineSeg(new Coordinate(p0x, p0y), new Coordinate(p1x, p1y));
    boolean result = hp.intersects(seg);
    assertTrue(result == expectedResult);
  }
}
