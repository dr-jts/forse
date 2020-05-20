package forse.geomstream;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class TestStreamOverlay 
{
  public static void main(String[] args) throws Exception
  {
  	TestStreamOverlay test = new TestStreamOverlay();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  private GeometryFactory geomFact = new GeometryFactory();
  private WKTReader reader = new WKTReader(geomFact);

  void run() throws Exception {
		run(boxes1, boxes2);
	}

	void run(String[] wkt1, String[] wkt2) throws Exception 
  {
    Geometry[] g1 = readWKT(wkt1);
    Geometry[] g2 = readWKT(wkt2);
    
    Geometry geom = overlay(g1, g2);
    System.out.println(geom);
  }
  
  Geometry overlay(Geometry[] g1, Geometry[] g2)
  {
    return overlay(new GeometryArrayGeometryStream(g1),
        new GeometryArrayGeometryStream(g2));
  }
  
  Geometry overlay(GeometryStream g1, GeometryStream g2)
  {
    PolygonOverlayGeometryStream overlay = new PolygonOverlayGeometryStream(geomFact);
    overlay.create(g1, g2);
    
    List geoms = new ArrayList();
    while (true) {
      Geometry g = overlay.next();
      if (g == null) break;
      geoms.add(g);
    }
    return geomFact.buildGeometry(geoms);
  }
    
  Geometry[] readWKT(String[] wkt)
  throws ParseException 
  {
    Geometry[] geom = new Geometry[wkt.length];
    for (int i = 0; i < wkt.length; i++) {
      geom[i] = reader.read(wkt[i]);
    }
    return geom;
	}

  // These MUST be sorted in X order
  String[] boxes1 = {
      "POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))",
      "POLYGON ((100 100, 100 0, 200 0, 200 100, 100 100))"
  };
  String[] boxes2 = {
      "POLYGON ((50 50, 50 150, 150 150, 150 50, 50 50))",
      "POLYGON ((150 150, 150 50, 250 50, 250 150, 150 150))"
  };
	
  

}
