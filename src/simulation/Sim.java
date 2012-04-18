package simulation;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

import processing.core.*;
import processing.opengl.*;
import javax.media.opengl.GL;

import boids.*;
import sprites.*;

import kinect.Kinect;
import kinect.KinectConfig;


@SuppressWarnings("serial")
public class Sim extends PApplet{

	public static int frameCounter; // Used for animation and color cycles.
									// Shadows p5's frameCount, but is public
	
	public static Random rand; // for spawning
	public static ArrayList<Integer> colors; // A simulation-wide color palette.
											 // Boids register their colors on spawn.
	
	// TODO make this more dynamic, put in settings too
	public Sprite fishSprite;
	
	public ArrayList<Boid> school;

	public static Kinect kinect; // kinect handle

	// OpenGL shader stuff
	String vertexSource;
	String fragmentSource;
	int shaderProgram;
	float time;

	@Override
	public void setup() {
		frameRate(30);
		//fishpic.resize(20, 20);
		size(Set.SCREEN_Width, Set.SCREEN_Height, OPENGL);  // Set the screen size
				
		frameCounter = 0;
		rand = new Random();
		colors = new ArrayList<Integer>();
		school = new ArrayList<Boid>();

		if( Set.SHOW_Sprites ) {
			fishSprite = new Sprite(this, "/Users/vestibule/java_workspace/Boids/images/ninjaMan.png", 7, 5, 0);
			fishSprite.setFrameSequence(0, 24);
			fishSprite.setAnimInterval(.05);
			float scale = (float)0.5;
			fishSprite.setScale(scale);
		}
		
		if (Set.KINECT_On) {
			kinect = new Kinect(this, Kinect.MOTION_DETECTION);
			kinect.init();
		}

		if(Set.JOGL_RenderShaders) {
			try {
				vertexSource = readFileAsString(Set.JOGL_VertexPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				fragmentSource = readFileAsString(Set.JOGL_FragmentPath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// set up the shader
			PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
			GL gl = pgl.beginGL();

			// compiled shader handles
			int vert = gl.glCreateShader(GL.GL_VERTEX_SHADER);
			int frag = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);

			String[] vs = new String[1];
			vs[0] = vertexSource;

			String[] fs = new String[1];
			fs[0] = fragmentSource;

			gl.glShaderSource(vert, 1, vs, (IntBuffer) null);
			gl.glCompileShader(vert);

			gl.glShaderSource(frag, 1, fs, (IntBuffer) null);
			gl.glCompileShader(frag);

			shaderProgram = gl.glCreateProgram();
			gl.glAttachShader(shaderProgram, vert);
			gl.glAttachShader(shaderProgram, frag);
			gl.glLinkProgram(shaderProgram);
			gl.glValidateProgram(shaderProgram);

			// something to keep track of time
			// use timeIncrement in Set to control how fast this increases
			time = 0;
		}

		// Create new fish with random position in screen and random speed
		for (int i = 0; i < Set.NUMBER_FishRed + Set.NUMBER_FishBlue
				+ Set.NUMBER_FishGreen + Set.NUMBER_FishYellow; i++) {

			int color;
			if (i < Set.NUMBER_FishRed) {
				color = Fish.RED;
			} else if (i < Set.NUMBER_FishRed + Set.NUMBER_FishBlue) {
				color = Fish.BLUE;
			} else if (i < Set.NUMBER_FishRed + Set.NUMBER_FishBlue
					+ Set.NUMBER_FishGreen) {
				color = Fish.GREEN;
			} else {
				color = Fish.YELLOW;
			}

			// position = anywhere
			// speed = [-.25max, .25max]
			// size = see settings
			// color = see above
			school.add(new Fish(rand.nextFloat() * Set.SCREEN_Width, rand
					.nextFloat() * Set.SCREEN_Height, (rand.nextFloat() - .5f)
					* Set.FISH_MaxSpeed / 2, (rand.nextFloat() - .5f)
					* Set.FISH_MaxSpeed / 2, Set.FISH_MinSize
					+ rand.nextInt(Set.FISH_MaxSize - Set.FISH_MinSize), this,
					color));
			if (Set.SHOW_Groups) {
				// school.get(i).color = Boid.colors[i%Boid.colors.length];
			}
		}

		if (Set.NUMBER_Obstacles > 0) {
			for (int i = 0; i < Set.NUMBER_Obstacles; i++) {
				school.add(new Obstacle());
			}
		}

		if (Set.NUMBER_People > 0) {
			for (int i = 0; i < Set.NUMBER_People; i++) {
				school.add(new Person());
			}
		}

		if (Set.NUMBER_Sharks > 0) {
			for (int i = 0; i < Set.NUMBER_Sharks; i++) {
				school.add(new Shark(rand.nextFloat() * Set.SCREEN_Width, rand
						.nextFloat() * Set.SCREEN_Height, 0, 0, this));
			}
		}

		stroke(155, 0, 0);
		rectMode(CENTER);
		frameRate(30);
	}

	@Override
	public void draw() {
		// Display StdDev Adjust mode for Kinect
		if (Set.KINECT_SetupMode
				&& kinect.config.mode == KinectConfig.MODE_StdDevAdjust) {
			background(10, 40, 100); // Clear screen

			for (int i = 0; i < kinect.depthMap.length; i++) {
				if (kinect.config.stats[i].getStdDev() > kinect.filterThreshold) {
					
					if( kinect.filter ) stroke(80,50,50);
					else stroke(255);
					
					point(kinect.mapKinectToSim_Col[i % 640],
							kinect.mapKinectToSim_Row[i / 640]);
				}
			}
		} else

		// Display the simulation
		if (Set.paused == false) {
			frameCounter++;

			// If Kinect is used, update it
			if (kinect != null && frameCounter % Set.KINECT_FrameRatio == 0) {
				kinect.update();
			}

			background(0, 20, 80); // Clear screen

			// If we have obstacles and target is turned on, draw it
			if (Set.NUMBER_Obstacles > 0 && Set.SHOW_ObstacleTarget == true) {
				fill(150, 0, 0, 40);
				rect(Set.SCREEN_Width / 2, Set.SCREEN_Height / 2,
						Set.OBSTACLE_TargetSize, Set.OBSTACLE_TargetSize);
			}
			
			/*
			 * TODO Groups don't work
			 * if (Set.SHOW_Groups) {
			 * Boid.group(school); }
			 */

			for (int i = 0; i < school.size(); i++) {
				school.get(i).step(school);
				drawBoid(school.get(i));
			}

			// Display Range Adjust mode for Kinect 
			int color_index;
			if (Set.KINECT_On && (Set.kinect_Render ||
					(Set.KINECT_SetupMode && kinect.config.mode == KinectConfig.MODE_RangeAdjust) )) {

				for (int i = 0; i < kinect.goodPixels.length; i++) {

					if (kinect.diffMap[kinect.goodPixels[i]] > kinect.range 
								&& kinect.diffMap[kinect.goodPixels[i]] < kinect.range+kinect.rangeSize) {
						if (kinect.depthMap[kinect.goodPixels[i]] < Kinect.DEPTH_MIN) {
							color_index = 0;
						} else if (kinect.depthMap[kinect.goodPixels[i]] > Kinect.DEPTH_MAX) {
							color_index = Kinect.NUM_COLORS;
						} else {
							color_index = kinect.mapDepthToColor[kinect.depthMap[kinect.goodPixels[i]]];
						}

						stroke(colors.get(Kinect.COLOR_OFFSET + color_index));

						point(kinect.mapKinectToSim_Col[kinect.goodPixels[i] % 640],
								kinect.mapKinectToSim_Row[kinect.goodPixels[i] / 640]);
					}
				}
			}
		}

		
		// SLOW, TODO maybe cache a loop
		if (Set.JOGL_RenderShaders) {
			renderShaders();
		}

		frameCounter++;

		// Renders Kinect color spectrum at the bottom of screen
		// SLOW, but OKAY
		if( Set.KINECT_SetupMode && kinect.config.mode == KinectConfig.MODE_RangeAdjust)
		for (int i = 0; i < Set.SCREEN_Width; i++) {
			stroke(colors.get(Kinect.COLOR_OFFSET
					+ (int) Boid.redoRange(i, 0, Kinect.NUM_COLORS, 0,
							Set.SCREEN_Width)));
			line(i, Set.SCREEN_Height - 10, i, Set.SCREEN_Height);
		}

	}

	/*
	 * Implements 'pause' with the spacebar
	 */
	@Override
	public void keyPressed() {

		switch(key) {
		case ' ':
			Set.paused = !Set.paused;
			System.out.println(Set.paused ? "PAUSE" : "PLAY");
			return;
		case 'a':
			Set.kinect_AffectsSim = !Set.kinect_AffectsSim;
			System.out.println(Set.kinect_AffectsSim ? "sim using kinect" : "sim ignoring kinect");
			return;
		case 's':
			Set.kinect_Render = !Set.kinect_Render;
			System.out.println(Set.kinect_Render ? "showing kinect" : "hiding kinect");
			return;
		}
		if (keyCode == ESC) {
			System.out.println("Goodbye");
			System.exit(0);
		}

		if (Set.KINECT_SetupMode) {
			
			kinect.config.keyPressed(key, keyCode);

		}
	}

	/*
	 * Handles mouse clicks. Currently, LEFT -> new food; RIGHT -> pause
	 */
	@Override
	public void mouseClicked() {

		if (mouseButton == LEFT) {
			Food f = new Food(mouseX, mouseY);
			school.add(f);
			drawFood(f);
		} else if (mouseButton == RIGHT) {
			Set.paused = !Set.paused;
		}

	}

	private void drawBoid(Boid boid) {

		switch (boid.getTYPE()) {

		// case YELLOW_FISH: drawYellowFish( (YellowFish) boid); break;
		case FISH:
			drawFish((Fish) boid);
			break;

		case OBSTACLE:
			if (Set.SHOW_Obstacle) {
				drawObstacle((Obstacle) boid);
			}
			break;

		case PERSON:
			drawPerson((Person) boid);
			break;
		case SHARK:
			drawShark((Shark) boid);
			break;

		case FOOD:
			drawFood((Food) boid);
			break;

		}
	}

	private void drawObstacle(Obstacle obst) {
		stroke(0);
		fill(150, 0, 0);

		ellipse(obst.position.x, obst.position.y, obst.size, obst.size);
	}

	private void drawPerson(Person obst) {
		stroke(0);
		fill(200);

		ellipse(obst.position.x, obst.position.y, obst.size, obst.size);
	}

	/*
	 * Simply draws the fish
	 */
	private void drawFish(Fish fish) {

		//============DISPLAY EXTRAS============
		// We draw the awareness circle first so it is under the fish
		if (Set.SHOW_Awareness) {
			stroke(255, 0, 0);
			fill(255, 0, 0, 25);
			ellipse(fish.position.x, fish.position.y,
					2 * fish.getAWARE_RADIUS(), 2 * fish.getAWARE_RADIUS());
		}
		if (Set.SHOW_AwarenessCone) {
			stroke(255, 0, 0);
			fill(255, 0, 0, 25);

			PVector cone[] = {
					new PVector(0, -1 * fish.AWARE_CONE_WIDTH_MAX),
					new PVector(fish.AWARE_CONE_LENGTH, -1
							* fish.AWARE_CONE_WIDTH_MIN),
					new PVector(fish.AWARE_CONE_LENGTH,
							fish.AWARE_CONE_WIDTH_MIN),
					new PVector(0, fish.AWARE_CONE_WIDTH_MAX) };

			cone = Boid.matrixMultParallel(fish.basis, cone);

			beginShape();
			vertex(fish.position.x + cone[0].x, fish.position.y + cone[0].y);
			vertex(fish.position.x + cone[1].x, fish.position.y + cone[1].y);
			vertex(fish.position.x + cone[2].x, fish.position.y + cone[2].y);
			vertex(fish.position.x + cone[3].x, fish.position.y + cone[3].y);
			endShape();
		}

		//=========DRAW FISH===========
		if( Set.SHOW_Sprites ) {
			// Code to animate sprites
			
			float angle = atan2(fish.speed.x, fish.speed.y);
			fishSprite.setRot(angle);
			fishSprite.setXY(fish.position.x, fish.position.y);
			float totalAccel = fish.recentAccel.mag();
			
			if(totalAccel < 1)
			{
				fishSprite.setFrame(frameCounter/6 % 5);
			}
			else if(totalAccel < 2)
			{
				fishSprite.setFrame(frameCounter/3 % 5);
			}
			else
			{
				fishSprite.setFrame(frameCounter % 5);
			}
			//fishSprite.setZorder(frameCounter%62);
			S4P.drawSprites();
		
		} else {
			// Code to draw the fish from scratch
			
			// head are the three points that define the head
			PVector head[] = {
					new PVector((float) (fish.H_WIDTH * Math.cos(fish.getHeading()
							+ PI / 2)), (float) (fish.H_WIDTH * Math.sin(fish
									.getHeading() + PI / 2))),
					new PVector(
							(float) (fish.H_LENGTH * Math.cos(fish.getHeading())),
							(float) (fish.H_LENGTH * Math.sin(fish.getHeading()))),
					new PVector((float) (fish.H_WIDTH * Math.cos(fish.getHeading()
							- PI / 2)), (float) (fish.H_WIDTH * Math.sin(fish
									.getHeading() - PI / 2))) };

			
			// Scales the width of head inversely and length directly with %speed
			float fracSpeed = (float) fish.speed.mag() / fish.getMAX_SPEED();

			if (fish.speed.mag() > 1) {
				head[0].mult(1.1f - .3f * fracSpeed);
				head[1].mult(.9f + .5f * fracSpeed);
				head[2].mult(1.1f - .3f * fracSpeed);
			}

			// tail are the two points that define the tail
			PVector tail[] = {
					new PVector((float) (fish.T_LENGTH * Math.cos(fish.getHeading()
										+ PI / 2 + fish.T_ANGLE)),
								(float) (fish.T_LENGTH * Math.sin(fish.getHeading()
										+ PI / 2 + fish.T_ANGLE))),
					new PVector((float) (fish.T_LENGTH * Math.cos(fish.getHeading()
										- PI / 2 - fish.T_ANGLE)),
								(float) (fish.T_LENGTH * Math.sin(fish.getHeading()
										- PI / 2 - fish.T_ANGLE))) };

		/*
		 * PVector accel = fish.recentAccel;
		 * 
		 * 
		 * if( accel != null ) { accel =
		 * Boid.matrixMult(Boid.inverse(fish.basis), fish.recentAccel );
		 * accel.normalize(); accel.mult((float)Fish.T_LENGTH/2); if( accel.x >
		 * 0 ) { accel.x = - accel.x; } accel.y *= .5f; //if( accel.x >
		 * -.5f*Fish.T_LENGTH ) { // accel.x = (float) (-.5*Fish.T_LENGTH); //}
		 * } else { accel = new PVector( -1, 0 ); } accel =
		 * Boid.matrixMult(fish.basis, accel);
		 * 
		 * //PVector accel = fish.recentAccel; PVector tail[] = { PVector.sub(
		 * accel, head[2] ), PVector.sub( accel, head[0] ) };
		 * 
		 * tail[0].normalize(); tail[1].normalize(); tail[0].mult( (float)
		 * Fish.T_LENGTH ); tail[1].mult( (float) Fish.T_LENGTH );
		 */

		
			// Draw head
			fill(fish.head_color);
			stroke(fish.head_color);

			// Connect the dots
			beginShape();
			vertex((int) (fish.position.x + head[0].x),
					(int) (fish.position.y + head[0].y));
			vertex((int) (fish.position.x + head[1].x),
					(int) (fish.position.y + head[1].y));
			vertex((int) (fish.position.x + head[2].x),
					(int) (fish.position.y + head[2].y));
			vertex((int) (fish.position.x + head[0].x),
					(int) (fish.position.y + head[0].y));
			endShape();

			// Draw body
			fill(fish.color);
			stroke(fish.color);

			// Connect the dots
			beginShape();
			vertex((int) (fish.position.x + head[0].x),
					(int) (fish.position.y + head[0].y));
			vertex((int) (fish.position.x + head[2].x),
					(int) (fish.position.y + head[2].y));
			vertex((int) (fish.position.x + tail[0].x),
					(int) (fish.position.y + tail[0].y));
			vertex((int) (fish.position.x + tail[1].x),
					(int) (fish.position.y + tail[1].y));
			vertex((int) (fish.position.x + head[0].x),
					(int) (fish.position.y + head[0].y));
			endShape();

		}
		// END DRAW FISH	
		
		//====DISPLAY EXTRAS====
		// Draws each fish's basis vectors
		if (Set.SHOW_Bases) {
			stroke(255, 255, 0);
			line(fish.position.x, fish.position.y, fish.position.x + 20
					* fish.basis[0].x, fish.position.y + 20 * fish.basis[0].y);
			line(fish.position.x, fish.position.y, fish.position.x + 15
					* fish.basis[1].x, fish.position.y + 15 * fish.basis[1].y);
		}

		// Draws each fish's acceleration and velocity vectors
		if (Set.SHOW_KinematicVectors) {
			stroke(255, 255, 0);
			line(fish.position.x, fish.position.y, fish.position.x + 10
					* fish.speed.x, fish.position.y + 10 * fish.speed.y);
			if (fish.recentAccel != null) {
				stroke(0, 0, 255);
				line(fish.position.x, fish.position.y, fish.position.x + 35
						* fish.recentAccel.x, fish.position.y + 35
						* fish.recentAccel.y);
			}
		}

	}

	/*
	 * private void group( Fish[] school, ArrayList<ArrayList<Fish>> groups ) {
	 * 
	 * boolean placed; int i, j, k;
	 * 
	 * for( i=0; i<school.length; i++ ) { placed = false;
	 * 
	 * for( j=0; j<groups.size(); j++ ) { for( k=0; k<groups.get(j).size(); k++
	 * ) {
	 * 
	 * if( !placed && PVector.sub(school[i].position,
	 * groups.get(j).get(k).position ).mag() < 2*Fish.AWARE_RADIUS &&
	 * PVector.sub(school[i].speed, groups.get(j).get(k).position ).mag() < 20 )
	 * {
	 * 
	 * groups.get(j).add( school[i] ); placed = true; }
	 * 
	 * } }
	 * 
	 * if( !placed ) { groups.add( new ArrayList<Fish>() ); groups.get( j ).add(
	 * school[i] ); } }
	 * 
	 * for( i=0; i<groups.size(); i++ ) { for( j=0; j<groups.get(i).size(); j++
	 * ) {
	 * 
	 * switch( (Fish.colorCounter%21)/7 ) { case 0: groups.get(i).get(j).color =
	 * color( (Fish.colorCounter%7)*35 + 10, 0, 0 ); break; case 1:
	 * groups.get(i).get(j).color = color( 0, (Fish.colorCounter%7)*35 + 10, 0
	 * ); break; case 2: groups.get(i).get(j).color = color( 0, 0,
	 * (Fish.colorCounter%7)*35 + 10 ); break; }
	 * 
	 * } Fish.colorCounter++; } //System.out.println( groups.size() ); }
	 */

	/*
	 * private void updateGroups( Fish[] school, ArrayList<ArrayList<Fish>>
	 * groups ) {
	 * 
	 * boolean changed = true; int i, j;
	 * 
	 * while( changed ) {
	 * 
	 * changed = false;
	 * 
	 * for( i=0; i<groups.size(); i++ ) { for( j=0; j<groups.get(i).size(); j++
	 * ) {
	 * 
	 * 
	 * 
	 * } } }
	 * 
	 * }
	 */

	private void drawShark(Shark shark) {

		stroke(shark.color);

		// fill( shark.energyColor );
		// arc( shark.position.x, shark.position.y, (int) (shark.size*1.5),
		// (int) (shark.size*1.5), PI, PI+PI/2 );

		// fill( shark.hungerColor );
		// arc( shark.position.x, shark.position.y, (int) (shark.size*1.5),
		// (int) (shark.size*1.5), PI+PI/2, TWO_PI );

		fill(shark.color);
		ellipse(shark.position.x, shark.position.y, shark.size, shark.size);

	}

	private void drawFood(Food food) {

		if (food.eaten == true) {
			school.remove(food);
		}

		stroke(180, 150, 0);
		fill(180, 150, 0);
		ellipse(food.position.x, food.position.y, food.size, 2 * food.size);

	}

	private float lawOfCos_SideC(float sideA, float angleC, float sideB) {
		return (float) Math.sqrt(Math.pow(sideA, 2) + Math.pow(sideB, 2) - 2
				* sideA * sideB * ((float) Math.cos(angleC)));
	}

	private float lawOfCos_AngleC(float sideA, float sideB, float sideC) {
		return (float) Math
				.acos((Math.pow(sideA, 2) + Math.pow(sideB, 2) - Math.pow(
						sideC, 2)) / (2 * sideA * sideB));
	}

	// returns k*propToThis, unless that is less than min. If so, returns min
	private double proportion(double propToThis, double k, double min) {
		return ((k * propToThis) > min) ? (k * propToThis) : min;
	}

	/*
	 * public int createColor( int r, int g, int b, int a ) { return
	 * color(r,g,b,a); }
	 * 
	 * public int createColor( int r, int g, int b ) { return color(r,g,b,255);
	 * }
	 */

	/*
	 * private <U> void registerColors( U obj ) {
	 * 
	 * int[][] newColors = null ;
	 * 
	 * 
	 * if( obj instanceof YellowFish ) { newColors = YellowFish.requestColors();
	 * } else if( obj instanceof Fish ) { newColors = Fish.requestColors(); }
	 * else if( obj instanceof Shark ) { newColors = Shark.requestColors(); }
	 * 
	 * if( newColors != null ) {
	 * 
	 * int startIndex = colors.size(); int size = 0; // TODO remove size
	 * failsafe at end
	 * 
	 * for( int[] rgba : newColors ) {
	 * 
	 * colors.add( color( rgba[0], rgba[1], rgba[2], rgba[3] )); ++size; }
	 * 
	 * // changed these to non-static methods if( obj instanceof YellowFish ) {
	 * YellowFish.receiveColors( startIndex, size ); } else if( obj instanceof
	 * Fish ) { Fish.receiveColors( startIndex, size ); }
	 * 
	 * else if( obj instanceof Shark ) { Shark.receiveColors( startIndex, size
	 * ); }
	 * 
	 * //if( obj) }
	 * 
	 * }
	 */

	
	private void renderShaders() {
		time += Set.JOGL_TimeIncrement;

		PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
		GL gl = pgl.beginGL();

		gl.glUseProgram(shaderProgram);
		int loc = gl.glGetUniformLocation(shaderProgram, "screen");
		gl.glUniform1f(loc, Set.SCREEN_Width / Set.SCREEN_Height);

		loc = gl.glGetUniformLocation(shaderProgram, "time");
		gl.glUniform1f(loc, time);

		gl.glColor3f(1, 1, 1);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(-Set.SCREEN_Width, -Set.SCREEN_Height, -1);
		gl.glVertex3f(Set.SCREEN_Width, -Set.SCREEN_Height, -1);
		gl.glVertex3f(Set.SCREEN_Width, Set.SCREEN_Height, -1);
		gl.glVertex3f(-Set.SCREEN_Width, Set.SCREEN_Height, -1);
		gl.glEnd();

		gl.glUseProgram(0);

		pgl.endGL();
	}
	
	private static String readFileAsString(String filePath)
			throws java.io.IOException {
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return new String(buffer);
	}
	
	
	
	
	
	
	
	
	
	/*
	class Animation {
		  PImage[] images;
		  int imageCount;
		  int frame;
		  
		  Animation(String imagePrefix, int count) {
		    imageCount = count;
		    images = new PImage[imageCount];

		    for (int i = 0; i < imageCount; i++) {
		      // Use nf() to number format 'i' into four digits
		      String filename = imagePrefix + nf(i, 4) + ".png";
		      images[i] = loadImage(filename, "png");
		    }
		    for (int k = 0; k < imageCount; k++)
		    {
		    	images[k].resize(20, 20);
		    }
		  }

		  void display(float xpos, float ypos, float xvel, float yvel) {
			if(ypos>xpos ||(-xpos>ypos))
			{
				frame= 1;
			}
			else
			{
				frame=0;
			}
		    //frame = (frame+1) % imageCount;
			  image(images[frame], xpos, ypos);
		  }
		  
		  int getWidth() {
		    return images[0].width;
		  }
		}
*/
	public int registerColors( int[][] newColors ) {
		assert(newColors!=null);

		int startIndex = colors.size();

		for (int[] rgba : newColors) {
			colors.add(color(rgba[0], rgba[1], rgba[2], rgba[3]));
		}

		return startIndex;
	}
}
