package forse.noding;


import org.locationtech.jts.geom.PrecisionModel;

import forse.RunOverlayUtil;

public class RunSweepLineNoderFile 
{
  public static void main(String[] args) throws Exception
  {
  	RunSweepLineNoderFile test = new RunSweepLineNoderFile();
    try {
      test.run();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  void run() throws Exception {
    RunOverlayUtil.nodeFile("C:\\data\\martin\\proj\\jts\\testing\\overlay\\leduc\\dataset_extract1.wkt", 
        new PrecisionModel(10));
	}


}
