package simulation;


/*
 * Settings
 */

public class Set {
	
	// TODO why is this here? where is it from
	//	public static final int AVOID_RADIUS = 10;

						   // Format =    [ Scrn W, Scrn H, Edge ]
	public static final int[][] screen =  {{  800,    600,    50 },
										   {  800,    600,    50 },
										   { 1200,    675,     0 },
										   {  800,    600,    50 },
										   {  800,    600,    50 }};

	
	//  In order, on/off: BasisVec, KinematicVec, AwareRadius, AwareCone, Groups, Obstacles, ObstTarget
	public static final boolean[][] display_toggles =  {{ false, false, false, false, false,  true, false },
														{ false, false, false, false, false,  true, false },
		/* NB: Groups is very buggy */					{ false, false, false, false, false,  true, false },
														{ false, false, false, false, false,  true, false },
														{ false, false, false, false, false,  true, false }};
	
				    				// Format =  RedF #, BlueF #, GreenF #, YellowF #, Obst #, Peop # Shark # ]
	public static final int[][] populations =  {{  75,     75,     75,        75,         0,      0,     3    },
												{ 400,      0,      0,         0,         0,      0,     4    },
												{ 200,      0,    300,         0,         0,      0,     2    },
												{ 200,      0,     25,         0,         0,      3,     2    },
												{ 200,      0,     50,         0,         0,      0,     0   }};
				    
	public static final int config_n = 4;

	public static final int SCREEN_Width = 			screen[config_n][0];
	public static final int SCREEN_Height =			screen[config_n][1];
	public static final int SCREEN_EdgeWidth =		screen[config_n][2];
	public static final int SCREEN_FrameRate =		30; // Maximum framerate

								// Format =     [ On, SetupMode, Render, AffectsSim ]
	private static final boolean[][] kinect = {{ false, false,   false,   false },
											   {  true,  true,   true ,   false },
											   {  true,  false,   true,   true  }};
	public static final int 	KINECT_ConfigNumber 	= 2;
	public static final boolean KINECT_On 				= kinect[KINECT_ConfigNumber][0]; // Global on/off
	public static final boolean KINECT_SetupMode		= KINECT_On && kinect[KINECT_ConfigNumber][1]; // Let's you play with the parameters
	
	public static final boolean KINECT_INIT_Render			= KINECT_On && (KINECT_SetupMode || kinect[KINECT_ConfigNumber][2]); // Render the kinect in simulation
	public static final boolean KINECT_INIT_AffectsSim		= KINECT_On && kinect[KINECT_ConfigNumber][3]; // Whether fish react to kinect
	
	public static final int 	KINECT_CalibrationLevel = 10;	// Calibration sample size
	public static final int		KINECT_FrameRatio		= 3;	// # of frames per Kinect update
	public static final int 	KINECT_SampleInterval   = 8;  // uses only 1 pixel per interval in each dimension, so 3 --> 1/9 the pixels
	public static final float   KINECT_DefaultFilter	= 70; // pixels w/ a larger stddev are filtered out
	public static final boolean KINECT_FancyStart		= false; // BROKEN

	// Just a table of the various configurations. Gives the in-Sim pixel-coordinates of the top-left and bottom-right corners
	// Format = {x1,y1},{x2,y2}
	public static final int[][][] KINECT_CoordTable 	= { {{-175,-500},{SCREEN_Width,SCREEN_Height}},  // Initial museum steup
														    {{   0,  0 },{SCREEN_Width,SCREEN_Height}} };// Full screen
	public static final int[][] KINECT_Coord			= KINECT_CoordTable[1];

	
	public static final boolean JOGL_RenderShaders = false;
	// this controls the speed of the water in the background
	// bigger = faster
	public static final float JOGL_TimeIncrement = (float)0.002;
		
	public static final String JOGL_VertexPath = "/Users/vestibule/java_workspace/Boids/warping.vert";
	public static final String JOGL_FragmentPath = "/Users/vestibule/java_workspace/Boids/warping_water.frag";
		
	
	
	public static final boolean SHOW_Bases =            display_toggles[config_n][0]; // Local coordinate system for each fish
	public static final boolean SHOW_KinematicVectors = display_toggles[config_n][1]; // Current speed and accel
	public static final boolean SHOW_Awareness =        display_toggles[config_n][2]; // Circle of awareness
	public static final boolean SHOW_AwarenessCone =    display_toggles[config_n][3]; // Forward cone to avoid things
	public static final boolean SHOW_Groups =           display_toggles[config_n][4]; // Haven't tested this in a while
	public static final boolean SHOW_Obstacle =         display_toggles[config_n][5]; // Normally on, but if you want to have invisible obstacles
	public static final boolean SHOW_ObstacleTarget =   display_toggles[config_n][6]; // The area obstacles aim for when spawned

	public static final int NUMBER_FishRed =    populations[config_n][0];
	public static final int NUMBER_FishBlue =   populations[config_n][1];
	public static final int NUMBER_FishGreen =  populations[config_n][2];
	public static final int NUMBER_FishYellow = populations[config_n][3];
	public static final int NUMBER_Obstacles =  populations[config_n][4];
	public static final int NUMBER_People =     populations[config_n][5];
	public static final int NUMBER_Sharks =     populations[config_n][6];
	
	
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
	
											  // Body RGB   Head RGB
	public static final int[][] FISH_Styles = {{ 180,10,0,  180,120,0 },  // Red
		   									   { 100,100,255,  255,200,0 },	  // Blue
											   { 50,150,10, 50,100,200 }, // Green
								   			   { 200,170,0,    0,0,255 }};	  // Yellow
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
	

	public static boolean paused				= false;
	public static boolean kinect_Render			= KINECT_On && (KINECT_SetupMode || kinect[KINECT_ConfigNumber][1]); // Render the kinect in simulation
	public static boolean kinect_AffectsSim		= KINECT_On && kinect[KINECT_ConfigNumber][3]; // Whether fish react to kinect

}
 