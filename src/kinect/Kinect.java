package kinect;

import java.util.ArrayList;

import processing.core.PApplet;
import simulation.Set;
import SimpleOpenNI.SimpleOpenNI;

public class Kinect {
	
	public static int MOTION_DETECTION = 1;
	
	public SimpleOpenNI  context;
	public int[] diffMap; // current difference btw real and scene
	public int[] staticScene; // picture of the empty scene
	public int[] goodPixels;
	public boolean filter = true;
	public KinectConfig config;
	
	public Kinect(PApplet applet, int mode)
	{
		// If we'll need a config, create one
		if( Set.KINECT_SetupMode ) {
			config = new KinectConfig(this);
		}
		
		// SimpleOpenNI handle
		context = new SimpleOpenNI(applet,SimpleOpenNI.RUN_MODE_MULTI_THREADED);
	 
		// enable depthMap generation 
		context.enableDepth();
	  
		// initialize the static scene
		staticScene = new int[context.depthMap().length];
		for( int pixel : staticScene ) {
			pixel = 0;
		}
	}

	public void init() {
		
		System.out.println("calibrating kinect...");
		
		//Fetch data once
		context.update();
		diffMap = context.depthMap();
		
		//Get ready for the rest
		int perc10 = Set.KINECT_CalibrationLevel/10;
		RunningStat[] stats = new RunningStat[diffMap.length];
		for(int j=0; j<diffMap.length; j++) {
			stats[j] = new RunningStat();
		}
		
		//Take the samples and process
		for(int i=0; i<Set.KINECT_CalibrationLevel; i++) {
			if( i%perc10 == 0) {
				System.out.printf("%d",i/perc10);
			}
			
			//Fetch data
			context.update();
			diffMap = context.depthMap();
			
			//Add it up
			for (int j = 0; j < diffMap.length; j++) {
				stats[j].addSample( diffMap[j] );
			}
			
		}
		
		//If we're in config mode, save the data
		if( config != null ) {
			for (int j = 0; j < diffMap.length; j++) {
				config.stdDevs.add( (float) stats[j].getStdDev() );
			}
		}
		
		//Save the scene
		for (int j = 0; j < diffMap.length; j++) {
			staticScene[j] = (int) stats[j].getMean();
		}
		
		refreshGoodPixels();
		
		System.out.println("\nCalibration DONE!");
		
	}

	public void refreshGoodPixels() {
		
		if( Set.KINECT_SetupMode && filter ) {
			ArrayList<Integer> stack = new ArrayList<Integer>();
			
			for(int i=0; i<diffMap.length; i++) {
				if( config.stdDevs.get(i) <= config.stdDevThreshold ) {
					stack.add(i);
				}
			}
			goodPixels = new int[stack.size()];
			Object[] Ostack = stack.toArray();
			for(int i=0; i<goodPixels.length; i++) {
				goodPixels[i] = (Integer) Ostack[i];
			}
		}
		
		else {
			
			goodPixels = new int[diffMap.length];
			for(int i=0; i<diffMap.length; i++) {
				goodPixels[i] = i;
			}
		}
		
	}

	/*
	 * BROKEN
	 */
	public void initFancy(int count) {
		
		// Print start message
		if( count == 0 ) {
			System.out.println("calibrating kinect...");
		}
		
		// Print % msgs
		int perc10 = Set.KINECT_CalibrationLevel/10;
		if( count%perc10 == 0) {
			System.out.printf("%d",count/perc10);
		}
		
		//Fetch data
		context.update();
		diffMap = context.depthMap();

		// add a bit of this frame to scene total to create an average
		for (int j = 0; j < diffMap.length; j++) {
			staticScene[j] += diffMap[j];
		}
		
		// Finish up
		if( count == Set.KINECT_CalibrationLevel ) {
			for (int j = 0; j < diffMap.length; j++) {
				staticScene[j] /= Set.KINECT_CalibrationLevel;
			}
			// Print exit
			System.out.println("\nCalibration DONE!");
		}
		
	}
	
	public void update()
	{
		// update the cam;
		context.update();
		diffMap = context.depthMap();
		for(int i=0; i<diffMap.length; i++) {
			diffMap[i] -= staticScene[i];
		}
  
	}
}