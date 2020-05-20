package forse.lineseg;

public class TopologyLabel 
{
  public static final byte BOTH_EXTERIOR = 0;
  public static final byte RIGHT_INTERIOR = 1;
  public static final byte LEFT_INTERIOR = 2;
  public static final byte BOTH_INTERIOR = 3;
  
  public static byte flip(byte sideLoc)
  {
    if (sideLoc == RIGHT_INTERIOR) return LEFT_INTERIOR;
    if (sideLoc == LEFT_INTERIOR) return RIGHT_INTERIOR;
    return sideLoc;
  }

  public static byte merge(byte label1, byte label2)
  {
    // TODO: is there a faster/simpler way?  (eg if chain?)
    return (byte) (label1 | label2);
  }
  
  public static byte depth(byte label)
  {
    // depth moving from left to right
    switch (label) {
    case BOTH_EXTERIOR: return 0;
    case BOTH_INTERIOR: return 0;
    case RIGHT_INTERIOR: return 1;
    case LEFT_INTERIOR: return -1;
    }
    return 0;
  }
  
  public static boolean isInterior(byte label)
  {
    return label == LEFT_INTERIOR
    || label == RIGHT_INTERIOR
    || label == BOTH_INTERIOR;
    
  }
  
  public static boolean isRightInterior(byte label)
  {
    return label == RIGHT_INTERIOR 
        || label == BOTH_INTERIOR;
  }
  
  public static boolean isLeftInterior(byte label)
  {
    return label == LEFT_INTERIOR 
        || label == BOTH_INTERIOR;
  }
  
  public static boolean isRightExterior(byte label)
  {
    return label == LEFT_INTERIOR 
        || label == BOTH_EXTERIOR;
  }
  
  public static boolean isLeftExterior(byte label)
  {
    return label == RIGHT_INTERIOR 
        || label == BOTH_EXTERIOR;
  }
}
