package forse.perf;

import org.locationtech.jts.geom.Geometry;

public class StreamingOverlayStressTest
{
  private static final int RUN_SIZE = 1000;
  
  private static final String TYPE_OVERLAY = "overlay";
  private static final String TYPE_DATA = "data";

  public static void main(String args[]) {
    StreamingOverlayStressTest test = new StreamingOverlayStressTest();
  
    String type = TYPE_OVERLAY;
    if (args.length >= 1) {
      type = args[0];
    }
    int size = RUN_SIZE;
    if (args.length >= 2) {
      size = Integer.parseInt(args[1]);
    }
    
    test.run(type, size);
  }

  private boolean isOutput = true;
  
  public StreamingOverlayStressTest() {
    
  }
  
  private void run(String type, int size) {
    if (type.equalsIgnoreCase(TYPE_DATA)) {
      runData(size);
    }
    else {
      runOverlay(size);
    }
  }
  
  public void runOverlay(int size)
  {
    OverlappingRingsOverlay overlay = new OverlappingRingsOverlay();
    overlay.init(size);
    while (true) {
      Geometry g = overlay.next();
      if (g == null) break;
      if (isOutput )
        System.out.println(g);
    }
  }
  public void runData(int size)
  {
    OverlappingRingsOverlay.RingGeometryStream stream = new OverlappingRingsOverlay.RingGeometryStream(size);
    while (true) {
      Geometry g = stream.next();
      if (g == null) break;
      if (isOutput )
        System.out.println(g);
    }
  }

}
