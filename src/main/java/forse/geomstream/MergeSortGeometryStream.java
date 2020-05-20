package forse.geomstream;

import org.locationtech.jts.geom.Geometry;

/**
 * A merge-sorting {@link GeometryStream}.
 * Input streams are assumed to be sorted 
 * with geometry envelopes in order of ascending X.
 * An exception is thrown if this constraint is found to be violated.
 * 
 * @author Martin Davis
 *
 */
public class MergeSortGeometryStream 
implements GeometryStream
{
  private static final double DOUBLE_NEG_MAX_VALUE = -Double.MAX_VALUE;
  
  private GeometryStream[] stream = new GeometryStream[2];
  private Geometry[] buffer = new Geometry[2];
  
  private int streamNum = 0;
  private int minBufferIndex = -1;
  private double minGeometryX = DOUBLE_NEG_MAX_VALUE;

  private int polyCount = 0;
  private long ptCount = 0;
  
  public MergeSortGeometryStream() {
  }

  public int getGeometryCount() { return polyCount; }
  public long getCoordinateCount() { return ptCount; }
  
  public void add(GeometryStream geomStream)
  {
    stream[streamNum++] = geomStream;
  }
  
  /**
   * 
   * @throws IllegalStateException if an input stream is not sorted correctly
   */
  public Geometry next()
  {
    fillBuffer();
    
    // no input left to process
    if (isBufferEmpty())
      return null;
    
    
    updateMinBufferValue();
    Geometry geom = removeFromBuffer(minBufferIndex);
    polyCount++;
    ptCount += geom.getNumPoints();
    return geom;
  }
  
  private double minGeometryX()
  {
    return minGeometryX;
  }
  
  private void fillBuffer()
  {
    fillBuffer(0);
    fillBuffer(1);
  }
  
  private void fillBuffer(int i)
  {
    if (buffer[i] != null) return;
    if (stream[i] == null) return;
    buffer[i] = stream[i].next();
    if (buffer[i] == null) {
      stream[i] = null;
    }
  }
  
  private void updateMinBufferValue()
  {
    int minIndex = -1;
    double minX = Double.MAX_VALUE;
    for (int i = 0; i < buffer.length; i++) {
      if (buffer[i] != null) {
        double minBufX = buffer[i].getEnvelopeInternal().getMinX();
        if (minBufX < minX) {
          minX = minBufX;
          minIndex = i;
        }
      }
    }
    if (minX < minGeometryX) {
      throw new IllegalStateException("Input Geometries are not sorted in X order"
          + " (current  X = " + minGeometryX + ", next = " + minX + " )");
    }
    minGeometryX = minX;
    minBufferIndex = minIndex;
  }
  
  private boolean isBufferEmpty()
  {
    return buffer[0] == null && buffer[1] == null;
  }
  
  private Geometry removeFromBuffer(int bufferIndex)
  {
    Geometry g = buffer[bufferIndex];
    // clear it from buffer
    buffer[bufferIndex] = null;
    return g;
  }
  
}
