package simulation;

import SimpleOpenNI.*;
import processing.core.*;

public class Kinect {
	
	public static int MOTION_DETECTION = 1;
	
	public SimpleOpenNI  context;
	public int[] depthMap;
	public float[] scene;
	
	public Kinect(PApplet applet, int mode)
	{
	 // context = new SimpleOpenNI(this);
	  context = new SimpleOpenNI(applet,SimpleOpenNI.RUN_MODE_MULTI_THREADED);
	  // enable depthMap generation 
	  context.enableDepth();
	  scene = new float[context.depthMap().length];
	  for( float pixel : scene ) {
		  pixel = 0;
	  }
	}

	public void calibrate() {
		context.update();
		depthMap = context.depthMap();
		// If still in calibration period, calibrate
		if (Sim.frameCounter < Set.KINECT_CalibrationTime) {
			System.out.println("calibrating kinect...");

			// add a bit of this frame to scene total to create an average
			for (int i = 0; i < depthMap.length; i++) {
				scene[i] += depthMap[i] / (float) Set.KINECT_CalibrationTime;
			}
		} else if (Sim.frameCounter == Set.KINECT_CalibrationTime) {
			System.out.println("calibration DONE!");
		}
	}
	
	public void update()
	{
		// update the cam;
		context.update();
		depthMap = context.depthMap();
		for(int i=0; i<depthMap.length; i++) {
			depthMap[i] -= scene[i];
		}
/*  //else if(Sim.frameCounter%5 == 0) {
	  // update the cam;
//	  context.update();
	  depthMap = context.depthMap();
	  if(Sim.frameCounter%2==0) {
		  System.out.println("one");
	  } else {
		  System.out.println("two");
	  }
  }
  */
  
	}
}