package kinect;

import java.util.ArrayList;

import processing.core.PConstants;

public class KinectConfig {
	
	public static final int MODE_Normal		 	= 0;
	public static final int MODE_StdDevAdjust	= 1;
	public static final int MODE_RangeAdjust	= 2;
	public static final int MODE_AlphaAdjust 	= 3;
	
	public int mode;
	public float stdDevThreshold;
	public int range;
	public int rangeSize;
	public int rangeAlpha;
	public boolean rangeBg;
	public ArrayList<Float> stdDevs;
	
	private Kinect kinect;
	
	public KinectConfig(Kinect kinect) {
		this.kinect = kinect;
		reset();
		
		stdDevs = new ArrayList<Float>();
	}
	
	public void reset() {
		mode = 0;
		kinect.filter = false;
		stdDevThreshold = 70;
		range = -1000;
		rangeSize = 800;
		rangeAlpha = 150;
		rangeBg = false;
	}

	public void keyPressed(int key, int keyCode) {
		
		if(keyCode == PConstants.UP) {
			
			if(mode == MODE_StdDevAdjust) {
				stdDevThreshold *= 1.5;
				System.out.printf("std-dev=%f\n", stdDevThreshold);
			}
			if(mode == MODE_RangeAdjust) {
				range += rangeSize/2;
				System.out.printf("range=[%d,%d]\n", range, range+rangeSize);
			}
			if(mode == MODE_AlphaAdjust) {
				rangeAlpha += 20;
				System.out.printf("alpha=%d", rangeAlpha);
			}
			
		} else if(keyCode == PConstants.DOWN) {
			
			if(mode == MODE_StdDevAdjust) {
				stdDevThreshold /= 1.5;
				System.out.printf("std-dev=%f\n", stdDevThreshold);
			}
			if(mode == MODE_RangeAdjust) {
				range -= rangeSize/2;
				System.out.printf("range=[%d,%d]\n", range, range+rangeSize);
			}
			if(mode == MODE_AlphaAdjust) {
				rangeAlpha -= 20;
				System.out.printf("alpha=%d", rangeAlpha);
			}
			
		} else if(keyCode == PConstants.RIGHT) {
			mode = MODE_StdDevAdjust;
			System.out.println("StdDev Adjust");
		
		} else if(keyCode == PConstants.LEFT) {
			mode = MODE_RangeAdjust;
			System.out.println("Range Adjust");
			
		} else if(keyCode == PConstants.ESC) {
			reset();
			System.out.println("Reset");
		} else {
			
			switch(key) {
			case 'h':
			case 'H':
				printHelp();
				break;
			case 'r':
			case 'R':
				reset();
				System.out.println("Reset");
				break;
			case 'b':
			case 'B':
			case '5':
				rangeBg = !rangeBg;
				System.out.println(rangeBg ? "rangeBg=ON" : "rangeBg=OFF");
			case 'f':
			case 'F':
			case '4':
				kinect.filter = !kinect.filter;
				System.out.println( kinect.filter ? "Filter=ON" : "Filter=OFF" );
				kinect.refreshGoodPixels();
				break;
			case 'n':
			case 'N':
			case '0':
				mode = MODE_Normal;
				System.out.println("Normal, blocking bad pixels");
				break;
			case '1':
				mode = MODE_StdDevAdjust;
				System.out.println("StdDev Adjust");
				break;
			case '2':
				mode = MODE_RangeAdjust;
				System.out.println("Range Adjust");
				break;
			case 'a':
			case 'A':
				mode = MODE_AlphaAdjust;
				System.out.println("Alpha Adjust");
				break;
			case '+':
			case '=':
				if( mode == MODE_RangeAdjust ) {
					rangeSize *= 1.5;
					System.out.printf("range=[%d,%d]\n", range, range+rangeSize);
				}
				break;
			case '-':
			case '_':
				if( mode == MODE_RangeAdjust ) {
					rangeSize /= 1.5;
					System.out.printf("range=[%d,%d]\n", range, range+rangeSize);
				}
				break;
			}
		}


	}

	private void printHelp() {
		System.out.println("" +
				"--------------------------------------------\n" +
				"KinectConfig  [ bernhard.todd@gmail.com ] \n" +
				"0,N   -> Normal Mode\n" +
				"1,>>  -> Adjust StdDev Threshold\n" +
				"2,<<  -> Adjust Range\n" +
				"3,A   -> Adjust Range alpha channel\n" +
				"4,F   -> Toggles filter\n" +
				"         (ignores pixels > StdDevThresh)\n" +
				"5,B   -> Toggles rendering over backround in Range\n" +
				"\n" +
				"in <Adjust> modes use, Up / Down to change,\n" +
				"in <Adjust Range> mode, use +/- to \"zoom\"\n" +
				"\n" +
				"R     -> Reset\n" +
				"H,    -> this menu\n" +
				"ESC -> EXIT the sim ");
		
	}
}
