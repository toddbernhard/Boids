package simulation;

import java.awt.Dimension;
import java.awt.Toolkit;

/*
 * Settings
 */

public class Set {

	/*========= HOW TO USE ===========
	 * Play with an *uncommented* config by changing the corresponding array values.
	 * Add columns or additional arrays as needed, just try not to break previous configs in the process.
	 * When you find a good set of parameters, save it by giving the index a comment.
	 */
	
	public static final int config_n = 
		//	0; // Test configuration with all options turned on
		//	1; // Kinect hidden, LOTS of fish, no borders.  could use for vestibule
		//	2; // Old demo 1: no kinect, red fish w/ sharks in small screen
		//	3; // Same as 1 but w/ setup mode and dense sampling for renders
		//	4; // Sprites test
			5; // test, very ense renders
			
						   // Format =    [ Scrn W, Scrn H, Edge, KinectConfig# ]
	private static final int[][] screen =  {{  800,    600,    50, 		1 },
										   {  800,    600,     50, 		2 },
										   {  800,    600,    50,		0 },
										   {  800,    600,     0, 		3 },
										   {  800,    600,    50,		0 },
										   {  800,    600,     0, 		4 }};

	
	private static final boolean[][] display_toggles =  
		//  In order, on/off:
		// AwareRadius, Obstacles, Sprites(vsDrawn), Fullscreen	
		{{ true, true, true, true },
		 { false,  true, false, true },
		 { false,  true, false, true },
		 { false,  true, false, true },
		 { false,  true,  true, false },
		 { false,  true, false, true }};
	
				    				// Format =  RedF #, BlueF #, GreenF #, YellowF #, Obst #, Peop # Shark # ]
	private static final int[][] populations = {{  20,     20,     20,        20,         2,      2,     4    },
												{ 400,    200,     50,        50,         0,      0,     0    },
												{ 400,      0,      0,         0,         0,      0,     4    },
												{ 400,    200,     50,        50,         0,      0,     0    },
												{ 40,      40,     40,        40,         0,      0,     4    },
												{ 100,    10,     10,        5,         0,      0,     0    }};
	
	
	// TODO NOT FINAL. be careful that we never set these. maybe use getter/setters, but don't want the function call
	// These two lines check Fullscreen display_toggle: if yes->null, if no->respective setting from this class
	public static int SCREEN_Width  = setScreenSize("width");
	public static int SCREEN_Height = setScreenSize("height");
	
	public static final int SCREEN_FrameRate  			= 30; // Maximum framerate
	public static final int[] SCREEN_BackgroundColor 	= { 0, 50, 150 };

	
	//public static final int 	KINECT_ConfigNumber 	= 2; // override
	public static final int 	KINECT_ConfigNumber		= screen[config_n][3];
	
							// Format =     [ On, SetupMode, Renderable, AffectsSim, MirrorHoriz, MirrorVert ]
	private static final boolean[][] KINECT_MODES = {{ false, false, false, false, false, false },	// Everything Off
													 { true,  true,  true,  true , false, false  },	// Everything On
											   		 { true, false, false, true , false, false  },	// GOOD: Hidden w/ no setup, sparse sampling
											   		 { true,  true,  true, true , false, false }, // setup mode, dense sampling
													 { true,  true,  true, true, true, false }};
	
							// Format =		[ SampleInterval ]
	private static final int[][] KINECT_INTS = {{  0 },
												{  4 },
												{ 13 },
												{ 5  },
												{  2 }};
	
	public static final boolean KINECT_On 				= KINECT_MODES[KINECT_ConfigNumber][0]; // Global on/off
	public static final boolean KINECT_SetupMode		= KINECT_On && KINECT_MODES[KINECT_ConfigNumber][1]; // Let's you play with the parameters
	
	public static final boolean KINECT_Renderable			= KINECT_On && (KINECT_SetupMode || KINECT_MODES[KINECT_ConfigNumber][2]); // Render the kinect in simulation
	public static final boolean KINECT_INIT_AffectsSim		= KINECT_On && KINECT_MODES[KINECT_ConfigNumber][3]; // Whether fish react to kinect
	
	public static final float   KINECT_DefaultFilter	= 70; // pixels w/ a larger stddev are filtered out
	public static final boolean KINECT_FancyStart		= true; // BROKEN

	
	public static final boolean JOGL_RenderShaders = false;
	// this controls the speed of the water in the background
	// bigger = faster
	public static final float JOGL_TimeIncrement = (float)0.01;
		
	public static final String JOGL_VertexPath = "/Users/vestibule/java_workspace/Boids/warping.vert";
	public static final String JOGL_FragmentPath = "/Users/vestibule/java_workspace/Boids/warping_water.frag";
		
	public static final boolean SHOW_Bases = false; // Local coordinate system for each fish
	public static final boolean SHOW_KinematicVectors = false; // Current speed and accel
	public static final boolean SHOW_AwarenessCone = false; // Forward cone to avoid things
	public static final boolean SHOW_ObstacleTarget = false; // The area obstacles aim for when spawned
	public static final boolean SHOW_Sprites = 			display_toggles[config_n][2]; // Whether we render sprites or draw by procedure
	public static final boolean SHOW_Fullscreen = 		display_toggles[config_n][3];
	
	
	public static final int NUMBER_People =     populations[config_n][5];
	
	
	// These are relative and unit-less, nominally pixels in some cases
	public static final float FISH_MaxAccel = (float) 1.5;
	public static final float FISH_MinAccel = 0;
	public static final float FISH_MaxSpeed = 7;
	public static final float FISH_MinSpeed = (float) 1;
	
	public static final float FISH_SeparationWeight = 10; // 15 is good alone
	public static final float FISH_CohesionWeight = (float) .001;
	public static final float FISH_AlignmentWeight = (float) .1;
	public static final float FISH_FearWeight = 25;
	public static final float FISH_HungerWeight = .7f;
	
	public static final int FISH_ShimmerCycle = 400; // frames per animation cycle
	public static final int FISH_ShimmerDepth = 200; // alpha channel varies from (255-Depth) to 255
	public static final int FISH_MaxSize = 7;
	public static final int FISH_MinSize = 5;
	public static final int FISH_MaxTurnAngle = 60;
	
	
	// These settings are for the fish spritesheets.  The rows and cols are not perfect, so the images
	// will need some tweaking.  Sorry about the paths, it made my computer happy...
	public static final String FISH1_Path = "/Users/vestibule/java_workspace/Boids/images/ninjaMan.png";
	public static final int FISH1_Rows = 7;
	public static final int FISH1_Cols = 5;
	public static final float FISH1_Scale = (float)0.7;
	
	public static final String FISH2_Path = "/Users/vestibule/java_workspace/Boids/images/spritesheet.png";
	public static final int FISH2_Rows = 8;
	public static final int FISH2_Cols = 8;
	public static final float FISH2_Scale = (float)0.8;
	
	public static final String FISH3_Path = "/Users/vestibule/java_workspace/Boids/images/cube.png";
	public static final int FISH3_Rows = 5;
	public static final int FISH3_Cols = 6;
	public static final float FISH3_SCALE = (float)2;
	
	public static final String FISH4_Path = "/Users/vestibule/java_workspace/Boids/images/gunman.png";
	public static final int FISH4_Rows = 9;
	public static final int FISH4_Cols = 5;
	public static final float FISH4_SCALE = (float)1.2;
	



											  // Body RGB   Head RGB
	public static final int[][] FISH_Styles = {{ 255,15,0,  255,160,0 },  // Red
		   									   { 100,100,255,  255,200,0 },	  // Blue
											   { 100,200,20, 80,150,255 }, // Green
								   			   { 255,210,0,    0,0,255 }};	  // Yellow
	public static final boolean FISH_StylesMingle = true;
 	
	
	public static final int FOOD_Size = 5;
	
	public static final float FOOD_MaxAccel = .2f; // Allows food to "float" a bit
	public static final float FOOD_MinAccel = 0;
	public static final float FOOD_MaxSpeed = 2;   // same
	public static final float FOOD_MinSpeed = 0;
	
	public static final float FOOD_EatenThreshold = 4;
		// to eat it, a fish must pass within this distance, measured center-to-center in pixels
	
	
	public static final float OBSTACLE_MaxAccel = 0; // Unused
	public static final float OBSTACLE_MinAccel = 0; // Unused
	public static final float OBSTACLE_MaxSpeed = (float) 3;
	public static final float OBSTACLE_MinSpeed = (float) .001;
	
	public static final int OBSTACLE_TargetSize = 350;	
	
	
	public static final float PERSON_MaxAccel = 0;
	public static final float PERSON_MinAccel = 0;
	public static final float PERSON_MaxSpeed = (float) 3;
	public static final float PERSON_MinSpeed = (float) .001;
	public static final float PERSON_HorizSpeedVar = (float) .2;
	
	
	public static final int SHARK_MaxAccel = 5;
	public static final int SHARK_MinAccel = 0;
	public static final int SHARK_MaxSpeed = 10;
	public static final int SHARK_MinSpeed = 2;
	
	public static final float SHARK_SeparationWeight = 0; // All these are unused for
	public static final float SHARK_CohesionWeight = 0;   // sharks
	public static final float SHARK_AlignmentWeight = 0;
	public static final float SHARK_FearWeight = 0;
	
	public static final int SHARK_SpeedCycle = 100;
	public static final int SHARK_HealthLevels = 100;
	
	
	/*--Live settings--*/
	
	public static boolean paused		= false;
	
	public static Integer screen_EdgeWidth  = screen[config_n][2]; // ID = 1

	public static Boolean show_Awareness = display_toggles[config_n][0]; // ID = 8 Circle of awareness
	public static Boolean show_Obstacle =  display_toggles[config_n][1]; // ID = 9 Normally on, but if you want to have invisible obstacles
	
	public static Integer number_FishRed =    populations[config_n][0]; // ID=2
	public static Integer number_FishBlue =   populations[config_n][1]; // ID=3
	public static Integer number_FishGreen =  populations[config_n][2]; // ID=4
	public static Integer number_FishYellow = populations[config_n][3]; // ID=5
	public static Integer number_Obstacles =  populations[config_n][4]; // ID=6
	public static Integer number_Sharks =     populations[config_n][6]; // ID=7

	// Just a table of the various configurations. Gives the in-Sim pixel-coordinates of the top-left and bottom-right corners
	// Format = {x1,y1},{x2,y2}
	public static final int[][][] kinect_CoordTable 	= { {{-180,-450},{SCREEN_Width+500,SCREEN_Height+450}},  // Initial museum steup
														    {{   0,  0 },{SCREEN_Width,SCREEN_Height}} };// Full screen
	public static final int[][] kinect_Coord			= kinect_CoordTable[0];

	// id=10
	public static Boolean kinect_Render			= KINECT_On && (KINECT_SetupMode || KINECT_MODES[KINECT_ConfigNumber][1]); // Render the kinect in simulation
	// id=11
	public static Boolean kinect_AffectsSim		= KINECT_On && KINECT_MODES[KINECT_ConfigNumber][3]; // Whether fish react to kinect
	// id=12
	public static Boolean kinect_MirrorHoriz		= KINECT_MODES[KINECT_ConfigNumber][4];
	// id=13
	public static Boolean kinect_MirrorVert		= KINECT_MODES[KINECT_ConfigNumber][5];

	// id=14
	public static Integer kinect_FrameRatio		= 3;	// # of frames per Kinect update
	// id=15
	public static Integer kinect_SampleInterval   = KINECT_INTS[KINECT_ConfigNumber][0];  // uses only 1 pixel per interval in each dimension, so 3 --> 1/9 the pixels
	// id=16
	public static Integer kinect_CalibrationLevel = 200;	// Calibration sample size
	
	
	// Used to initialize the screen width and height. Allows fullscreen support
	private static int setScreenSize(String string) {
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
				
		if( string.equals("width") )   return (display_toggles[config_n][3]) ? dim.width  : screen[config_n][0];
		if( string.equals("height") )  return (display_toggles[config_n][3]) ? dim.height : screen[config_n][1];
		else return -1;
	
	}

}
 
