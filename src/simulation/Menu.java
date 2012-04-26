package simulation;

import java.util.ArrayList;

import boids.Fish;
import boids.Obstacle;
import boids.Shark;

import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PVector;

public class Menu {
	
	public static final int GENERAL = 0;
	public static final int KINECT	= 1;
	public static final int CREDITS = 2;
	
	private int[][] modeColors = {{ 0, 150, 0 },
								  { 200, 10, 10 },
								  { 10, 10, 200 }};
	
	private static final int NUM_MODES = 3;
	
	private static final String systemFont = "HelveticaBold";
	private static final String ON  = "ON";
	private static final String OFF = "OFF";
	
	private Sim sim;
	
	private PFont titleFont, headingFont, menuFont;
	private ArrayList<Option> generalOptions, kinectOptions;
	private String[] modeStrings;
	
	private boolean needsKinectRefresh;
	private boolean needsKinectInit;
	private boolean needsPopulationCheck;
	
	private int mode;
	private int selected;
	
	public Menu(Sim simul) {
		this.sim = simul;
		titleFont = simul.createFont(systemFont, 36);
		menuFont = simul.createFont(systemFont, 16);
		headingFont = simul.createFont(systemFont, 20);
		mode = GENERAL;
		
		generalOptions = new ArrayList<Menu.Option>();
		kinectOptions = new ArrayList<Menu.Option>();
		modeStrings = new String[NUM_MODES];
		
		modeStrings[GENERAL] = "general";
		modeStrings[KINECT]  = "kinect";
		modeStrings[CREDITS] = "credits";
		
		generalOptions.add( new Option(2, "Red fish", Set.number_FishRed , 0, null) );
		generalOptions.add( new Option(3, "Blue fish", Set.number_FishBlue, 0, null) );
		generalOptions.add( new Option(4, "Green fish", Set.number_FishGreen, 0, null) );
		generalOptions.add( new Option(5, "Yellow fish", Set.number_FishYellow, 0, null) );
		generalOptions.add( new Option(6, "Obstacles", Set.number_Obstacles, 0, null) );
		generalOptions.add( new Option(7, "Sharks", Set.number_Sharks, 0, null) );
		generalOptions.add( new Option(1, "Bounce off edges", (Set.screen_EdgeWidth==0 ? false : true)) );
		generalOptions.add( new Option(8, "Show awareness circle", Set.show_Awareness) );
		generalOptions.add( new Option(9, "Show obstacles", Set.show_Obstacle) );
		
		kinectOptions.add( new Option(10, "Show the Kinect pointcloud", Set.kinect_Render) );
		kinectOptions.add( new Option(11, "Fish react to the pointcloud", Set.kinect_AffectsSim) );
		kinectOptions.add( new Option(12, "Flip pointcloud vertically", Set.kinect_MirrorVert) );
		kinectOptions.add( new Option(13, "Flip pointcloud horizontally", Set.kinect_MirrorHoriz) );
		kinectOptions.add( new Option(14, "Frames per refresh", Set.kinect_FrameRatio, 1, null) );
		kinectOptions.add( new Option(15, "Sample density", Set.kinect_SampleInterval, 1, 100) );
		kinectOptions.add( new Option(16, "Calibration level", Set.kinect_CalibrationLevel, 0, null) );

		selected = NUM_MODES;
		needsKinectInit = false;
		needsKinectRefresh = false;
		needsPopulationCheck = false;
	}
	
	public void drawSelf() {
		
		int w2 = Set.SCREEN_Width/2;
		int h2 = Set.SCREEN_Height/2;
		
		// Draw main box
		sim.fill(255,255,255,210);
		roundedRect( Set.SCREEN_Width/2-320, Set.SCREEN_Height/2-240, Set.SCREEN_Width/2+320, Set.SCREEN_Height/2+240, 70,60);
		
		// Draw title
		sim.fill(Sim.backgroundColor);
		sim.textFont(titleFont);
		sim.text("gone fishing",w2-100,h2-190);
		sim.textFont(headingFont);
		sim.text("options", w2-40,h2-170);
		
		// Draw submenu buttons
		int xoffset=0;
		for(int i=0; i<NUM_MODES; i++) {
			switch(i) {
			case GENERAL: xoffset = -220; break;
			case KINECT: xoffset = -28; break;
			case CREDITS: xoffset = 147; break;
			}
			if ( i == selected) {
				sim.fill(modeColors[i][0], modeColors[i][1], modeColors[i][2], 100);
			} else {
				sim.fill(modeColors[i][0], modeColors[i][1], modeColors[i][2]);
			}
			roundedRect(w2-250+180*i, h2-150, w2-110+180*i, h2-120, 5, 5);
			sim.fill(255);
			sim.text(modeStrings[i], w2+xoffset, h2-128);
		}
	
		
		// Draw submenu
		
		String valStr;
		switch(mode) {
		
		case GENERAL:
			sim.textFont(menuFont);
			for(int i=0; i<generalOptions.size(); i++) {
				if( generalOptions.get(i).boolValue != null ) {
					valStr =  generalOptions.get(i).boolValue ?	valStr = ON : OFF;
				} else {
					valStr = generalOptions.get(i).intValue.toString();
				}
				
				if( selected == i+NUM_MODES ) {
					sim.fill(50);
					sim.rect(w2-200, h2-50+30*i, w2+200, h2-80+30*i);
					sim.fill(255);
				} else {
					sim.fill(0);
				}
				sim.text(generalOptions.get(i).text, w2-180, h2-60+30*i);
				sim.text(valStr, w2+150, h2-60+30*i);
			}
			break;

		case KINECT:
			sim.textFont(menuFont);
			for(int i=0; i<kinectOptions.size(); i++) {
				if( kinectOptions.get(i).boolValue != null ) {
					valStr =  kinectOptions.get(i).boolValue ?	valStr = ON : OFF;
				} else {
					valStr = kinectOptions.get(i).intValue.toString();
				}
				
				if( selected == i+NUM_MODES ) {
					sim.fill(50);
					sim.rect(w2-200, h2-50+30*i, w2+200, h2-80+30*i);
					sim.fill(255);
				} else {
					sim.fill(0);
				}
				sim.text(kinectOptions.get(i).text, w2-180, h2-60+30*i);
				sim.text(valStr, w2+150, h2-60+30*i);
			}
			break;
			
		case CREDITS:
			break;
		default:
			System.out.printf("Error: invalid Menu mode %d\n", mode);
			sim.fill(0);
			sim.textFont(headingFont);
			sim.text(String.format("Error: invalid Menu mode %d", mode));
		}
	}

	public void roundedRect( int x1, int y1, int x2, int y2, int Xradius, int Yradius ) {
		
		sim.noStroke();
		
		sim.rect( x1, y1+Yradius, x2, y2-Yradius );
		sim.rect( x1+Xradius, y1, x2-Xradius, y1+Yradius );
		sim.rect( x1+Xradius, y2-Yradius, x2-Xradius, y2 );
		
		sim.arc( x1+Xradius, y1+Yradius, Xradius*2, Yradius*2, PConstants.PI, PConstants.TWO_PI-PConstants.PI/2 );
		sim.arc( x2-Xradius, y1+Yradius, Xradius*2, Yradius*2, PConstants.TWO_PI-PConstants.PI/2, PConstants.TWO_PI );
		sim.arc( x1+Xradius, y2-Yradius, Xradius*2, Yradius*2, PConstants.PI/2, PConstants.PI );
		sim.arc( x2-Xradius, y2-Yradius, Xradius*2, Yradius*2, 0, PConstants.PI/2 );
	}
	
	public void keyPressed(char key, int keyCode) {
		
		if(key == ' ') {
			closeMenu();
			Set.paused = false;
			return;
		}
		
		int greatestIndex = NUM_MODES-1;
		if( mode == GENERAL )  greatestIndex += generalOptions.size();
		else if( mode == KINECT ) greatestIndex += kinectOptions.size();
		
		switch(keyCode) {
		
		case PConstants.UP:
			if(selected < NUM_MODES) {
				selected = greatestIndex;
			} else if(selected == NUM_MODES) {
				selected = mode;
			} else {
				selected--;
			}
			break;
		case PConstants.DOWN:
			if(selected < NUM_MODES) {
				selected = NUM_MODES;
			} else if(selected == greatestIndex ) {
				selected = mode;
			} else {
				selected++;
			}
			break;
		case PConstants.LEFT:
			if(selected < NUM_MODES) {
				selected--;
			} else {
				changeOption("left");
			}
			break;
		case PConstants.RIGHT:
			if(selected < NUM_MODES) {
				selected++;
			} else {
				changeOption("right");
			}
			break;
		case PConstants.ENTER:
			if(selected < NUM_MODES) {
				mode = selected;
				return;
			} else {
				changeOption("enter");
			}
		}
		
		if( selected < 0 ) {
			selected = greatestIndex;
		}
		else if( selected > greatestIndex ) {
			selected = 0;
		}
		
	}

	private void changeOption(String string) {
		Option chosen = (mode==0 ? generalOptions : kinectOptions).get(selected-NUM_MODES);
		
		if(chosen.boolValue != null) {
			chosen.boolValue = !chosen.boolValue;
		} else {
			if(string.equals("left"))  chosen.intValue -= 1;
			else if( string.equals("right"))  chosen.intValue += 1;
			
			if( chosen.intMaxMin[0] != null && chosen.intValue < chosen.intMaxMin[0]) chosen.intValue = chosen.intMaxMin[0];
			else if( chosen.intMaxMin[1] != null && chosen.intValue < chosen.intMaxMin[1]) chosen.intValue = chosen.intMaxMin[1];
		}
		
		
		switch(chosen.ID) {
		case 1: // edge width
			Set.screen_EdgeWidth = chosen.boolValue ? 50 : 0;
			break;
		
		case 2: // fish red
			Set.number_FishRed = chosen.intValue;
			needsPopulationCheck = true;
			break;
		case 3: // fish blue
			Set.number_FishBlue = chosen.intValue;
			needsPopulationCheck = true;
			break;
		case 4: // fish green
			Set.number_FishGreen = chosen.intValue;
			needsPopulationCheck = true;
			break;
		case 5: // fish yellow
			Set.number_FishYellow = chosen.intValue;
			needsPopulationCheck = true;
			break;
		case 6: // obstacles
			Set.number_Obstacles = chosen.intValue;
			needsPopulationCheck = true;
			break;
		case 7: // sharks
			Set.number_Sharks = chosen.intValue;
			needsPopulationCheck = true;
			break;
		
		case 8:	// show awareness
			Set.show_Awareness = chosen.boolValue;
			break;
		case 9: // show obstacle
			Set.show_Obstacle = chosen.boolValue;
			break;
		
		case 10: // kinect render
			Set.kinect_Render = chosen.boolValue;
			break;
			
		case 11: // affects sim
			Set.kinect_AffectsSim = chosen.boolValue;
			break;
			
		case 12: // mirroe horiz
			Set.kinect_MirrorHoriz = chosen.boolValue;
			Sim.kinect.createKinectToSimMap();
			break;
			
		case 13: // mirror vert
			Set.kinect_MirrorVert = chosen.boolValue;
			Sim.kinect.createKinectToSimMap();
			break;
			
		case 14: // frame ratio
			Set.kinect_FrameRatio = chosen.intValue;
			break;
			
		case 15: // kinect sample interval
			Set.kinect_SampleInterval = chosen.intValue;
			needsKinectRefresh = true;
			break;
			
		case 16: // calibration level
			Set.kinect_CalibrationLevel  = chosen.intValue;
			needsKinectInit = true;
			break;
		
		}
	}
	
	public void closeMenu() {
		if( needsKinectInit ) {
			Sim.kinect.init();
			needsKinectInit = false;
		}
		
		if( needsKinectRefresh ) {
			Sim.kinect.refreshGoodPixels();
			needsKinectRefresh = false;
		}
		
		if( needsPopulationCheck ) {
			int fishes[] = {0,0,0,0};
			int obstacles = 0;
			int sharks = 0;
			
			Boid boid;
			for( int i=0; i<sim.school.size(); i++) {
				boid = sim.school.get(i);
				
				switch(boid.getTYPE()) {
				case FISH:
					int color = ((Fish) boid).style;
					
					if( color == 0 && fishes[color] == Set.number_FishRed ) {
						sim.school.remove(i);
						i--;
					} else if( color == 1 && fishes[color] == Set.number_FishBlue ) {
						sim.school.remove(i);
						i--;
					} else if( color == 2 && fishes[color] == Set.number_FishGreen ) {
						sim.school.remove(i);
						i--;
					} else if( color == 3 && fishes[color] == Set.number_FishYellow ) {
						sim.school.remove(i);
						i--;
					} else {
						fishes[color]++;
					}
					
					break;
					
				case OBSTACLE:
					if( obstacles == Set.number_Obstacles ) {
						sim.school.remove(i);
					} else {
						obstacles++;
					}
					break;
				case SHARK:
					if( sharks == Set.number_Sharks ) {
						sim.school.remove(i);
					} else {
						sharks++;
					}
					break;
				}
			}
			
			PVector position = new PVector();
			for( int i=fishes[0]; i<Set.number_FishRed; i++) {
				int sourcePixel = Sim.rand.nextInt( 2*(Set.SCREEN_Height+Set.SCREEN_Width) );
				
				if( sourcePixel < Set.SCREEN_Width ) {
				// Entering from TOP	
					position.x = sourcePixel;
					position.y = 0;
					
				} else if( sourcePixel < Set.SCREEN_Width+Set.SCREEN_Height ) {
				// Entering from RIGHT
					position.x = Set.SCREEN_Width;
					position.y = sourcePixel - Set.SCREEN_Width;
					
				} else if( sourcePixel < 2*Set.SCREEN_Width + Set.SCREEN_Height ) {
				// Entering from BOTTOM
					position.x = sourcePixel - Set.SCREEN_Width - Set.SCREEN_Height;
					position.y = Set.SCREEN_Height;
					
				} else {
				// Entereing from LEFT
					position.x = 0;
					position.y = sourcePixel - 2*Set.SCREEN_Width - Set.SCREEN_Height;
					
				}
				sim.school.add(new Fish(position, new PVector((sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2, (sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2), Set.FISH_MinSize
						+ sim.rand.nextInt(Set.FISH_MaxSize - Set.FISH_MinSize), sim,
						0));
			}
			
			for( int i=fishes[1]; i<Set.number_FishBlue; i++) {
				int sourcePixel = Sim.rand.nextInt( 2*(Set.SCREEN_Height+Set.SCREEN_Width) );
				
				if( sourcePixel < Set.SCREEN_Width ) {
				// Entering from TOP	
					position.x = sourcePixel;
					position.y = 0;
					
				} else if( sourcePixel < Set.SCREEN_Width+Set.SCREEN_Height ) {
				// Entering from RIGHT
					position.x = Set.SCREEN_Width;
					position.y = sourcePixel - Set.SCREEN_Width;
					
				} else if( sourcePixel < 2*Set.SCREEN_Width + Set.SCREEN_Height ) {
				// Entering from BOTTOM
					position.x = sourcePixel - Set.SCREEN_Width - Set.SCREEN_Height;
					position.y = Set.SCREEN_Height;
					
				} else {
				// Entereing from LEFT
					position.x = 0;
					position.y = sourcePixel - 2*Set.SCREEN_Width - Set.SCREEN_Height;
					
				}
				sim.school.add(new Fish(position, new PVector((sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2, (sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2), Set.FISH_MinSize
						+ sim.rand.nextInt(Set.FISH_MaxSize - Set.FISH_MinSize), sim,
						1));
			}
			
			for( int i=fishes[2]; i<Set.number_FishGreen; i++) {
				int sourcePixel = Sim.rand.nextInt( 2*(Set.SCREEN_Height+Set.SCREEN_Width) );
				
				if( sourcePixel < Set.SCREEN_Width ) {
				// Entering from TOP	
					position.x = sourcePixel;
					position.y = 0;
					
				} else if( sourcePixel < Set.SCREEN_Width+Set.SCREEN_Height ) {
				// Entering from RIGHT
					position.x = Set.SCREEN_Width;
					position.y = sourcePixel - Set.SCREEN_Width;
					
				} else if( sourcePixel < 2*Set.SCREEN_Width + Set.SCREEN_Height ) {
				// Entering from BOTTOM
					position.x = sourcePixel - Set.SCREEN_Width - Set.SCREEN_Height;
					position.y = Set.SCREEN_Height;
					
				} else {
				// Entereing from LEFT
					position.x = 0;
					position.y = sourcePixel - 2*Set.SCREEN_Width - Set.SCREEN_Height;
					
				}
				sim.school.add(new Fish(position, new PVector((sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2, (sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2), Set.FISH_MinSize
						+ sim.rand.nextInt(Set.FISH_MaxSize - Set.FISH_MinSize), sim,
						2));
			}
			
			System.out.println("should be="+Set.number_FishYellow+" is="+fishes[3]);
			for( int i=fishes[3]; i<Set.number_FishYellow; i++) {
				int sourcePixel = Sim.rand.nextInt( 2*(Set.SCREEN_Height+Set.SCREEN_Width) );
				
				if( sourcePixel < Set.SCREEN_Width ) {
				// Entering from TOP	
					position.x = sourcePixel;
					position.y = 0;
					
				} else if( sourcePixel < Set.SCREEN_Width+Set.SCREEN_Height ) {
				// Entering from RIGHT
					position.x = Set.SCREEN_Width;
					position.y = sourcePixel - Set.SCREEN_Width;
					
				} else if( sourcePixel < 2*Set.SCREEN_Width + Set.SCREEN_Height ) {
				// Entering from BOTTOM
					position.x = sourcePixel - Set.SCREEN_Width - Set.SCREEN_Height;
					position.y = Set.SCREEN_Height;
					
				} else {
				// Entereing from LEFT
					position.x = 0;
					position.y = sourcePixel - 2*Set.SCREEN_Width - Set.SCREEN_Height;
					
				}
				sim.school.add(new Fish(position, new PVector((sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2, (sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2), Set.FISH_MinSize
						+ sim.rand.nextInt(Set.FISH_MaxSize - Set.FISH_MinSize), sim,
						3));
			}
			
			for( int i=sharks; i<Set.number_Sharks; i++) {
				int sourcePixel = Sim.rand.nextInt( 2*(Set.SCREEN_Height+Set.SCREEN_Width) );
				
				if( sourcePixel < Set.SCREEN_Width ) {
				// Entering from TOP	
					position.x = sourcePixel;
					position.y = 0;
					
				} else if( sourcePixel < Set.SCREEN_Width+Set.SCREEN_Height ) {
				// Entering from RIGHT
					position.x = Set.SCREEN_Width;
					position.y = sourcePixel - Set.SCREEN_Width;
					
				} else if( sourcePixel < 2*Set.SCREEN_Width + Set.SCREEN_Height ) {
				// Entering from BOTTOM
					position.x = sourcePixel - Set.SCREEN_Width - Set.SCREEN_Height;
					position.y = Set.SCREEN_Height;
					
				} else {
				// Entereing from LEFT
					position.x = 0;
					position.y = sourcePixel - 2*Set.SCREEN_Width - Set.SCREEN_Height;
					
				}
				sim.school.add(new Shark(position.x, position.y, (sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2, (sim.rand.nextFloat() - .5f)
						* Set.FISH_MaxSpeed / 2, Set.FISH_MinSize
						+ sim.rand.nextInt(Set.FISH_MaxSize - Set.FISH_MinSize), sim ) );
			}
			
			for( int i=obstacles; i<Set.number_Obstacles; i++) {

				sim.school.add(new Obstacle());
			}
		}
	}


	public class Option {
		
		public int ID;
		public String text;
		public Boolean boolValue;
		public Integer intValue;
		public Integer[] intMaxMin;
		public Fish fish;
		
		public Option(int id, String text, boolean bool) {
			this.ID = id;
			this.text = text;
			this.boolValue = bool;
			this.intValue = null;
			this.intMaxMin = null;
		}
		
		public Option(int id, String text, int num, Integer min, Integer max) {
			this.ID = id;
			this.text = text;
			this.boolValue = null;
			this.intValue = num;
			this.intMaxMin = new Integer[2];
			
			intMaxMin[0] = min;
			intMaxMin[1] = max;
			
		}
	}
	
}
