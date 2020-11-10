package forse.geomstream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;

public class WKTGeometryStream implements GeometryStream {

  GeometryFactory geomFact;
  private String filename;
  private BufferedReader bufferedReader;
  private WKTReader wktReader;

  public WKTGeometryStream(String filename, GeometryFactory geomFact) 
    throws IOException
  {
    this.filename = filename;
    this.geomFact = geomFact;
    init();
    FileReader reader = new FileReader(filename);
	bufferedReader = new BufferedReader(reader);
	wktReader = new WKTReader(geomFact);
  }

  private void init() {
  }

  public Geometry next() 
  {
    try {
        if (isAtEndOfFile(bufferedReader)) {
        	return null;
        }
        Geometry g = wktReader.read(bufferedReader);
        return g;
    }
    catch (Exception ex) {
      throw new IllegalStateException(ex.getMessage());
    } 
  }
    
	/**
	 * Tests if reader is at EOF, and skips any leading whitespace
	 */
	private boolean isAtEndOfFile(BufferedReader bufferedReader) throws IOException {
	  // skip whitespace
	  int ch;
	  do {
	    bufferedReader.mark(1);
	    ch = bufferedReader.read();
	    // EOF reached
	    if (ch < 0) return true;
	  } while (Character.isWhitespace(ch));
	  bufferedReader.reset();
	  
	  return false;
	}
}
