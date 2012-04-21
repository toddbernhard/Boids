package boids;

import interfaces.Aware;
import interfaces.Flockable;
import interfaces.HasSprites;

import java.util.ArrayList;

import kinect.Kinect;

import processing.core.PConstants;
import processing.core.PVector;
import processing.core.PImage;
import simulation.Boid;
import simulation.Set;
import simulation.Sim;
import sprites.*;

import java.util.Random;


public class Fish extends Boid implements Aware, Flockable, HasSprites {
		
	public static final Boid.Type TYPE = Boid.Type.FISH;
	
	// Indices for the different styles
	public static final int STYLE1 = 0; // TODO use enum instead? handy to use names as index tho
	public static final int STYLE2 = 1;
	public static final int STYLE3 = 2;
	public static final int STYLE4 = 3;
	
	public static final float MAX_TURN_RATIO = (float) Math.tan( Math.toRadians(Set.FISH_MaxTurnAngle) );

	public static Integer[] COLOR_OFFSETS = { null, null, null, null };

	
	private final int FRAME_OFFSET;
	
	
	// T=tail, H=head
	public float T_LENGTH;
	public float T_ANGLE;
	public float H_WIDTH;
	public float H_LENGTH;
	
	public float AWARE_RADIUS;
	public float AWARE_CONE_LENGTH;
	public float AWARE_CONE_WIDTH_MAX;
	public float AWARE_CONE_WIDTH_MIN;
	
	public int head_color;
	public int style;
	
	public int offset;
	public static ArrayList<PImage> sprites;
	public static Sprite fishSprite;
	public static Sprite fishSprite2;
	public static Sprite fishSprite3;
	public static Sprite fishSprite4;
	
	public Fish( PVector position, PVector speed, int size, Sim simul, int color ) {
		super(position, speed, size);
		
		T_LENGTH = (float) Math.sqrt(10) * size;
		T_ANGLE = (float) Math.atan(3);
		H_WIDTH = 0.9f * size;
		H_LENGTH = size;
		Random generator = new Random();
		
		offset = generator.nextInt(1000);
		
		AWARE_RADIUS = 7 * size;
		AWARE_CONE_LENGTH = 3 * AWARE_RADIUS;
		AWARE_CONE_WIDTH_MAX = AWARE_RADIUS;
		AWARE_CONE_WIDTH_MIN = 0.4f * AWARE_RADIUS;
		
		FRAME_OFFSET = Sim.rand.nextInt( Set.FISH_ShimmerCycle );
		
		style = color;
		
		if( Set.SHOW_Sprites ) {
			
			float scale = (float)0.5;
			if(fishSprite == null){
				fishSprite = new Sprite(simul, Set.FISH1_Path, Set.FISH1_Rows, Set.FISH1_Cols, 0);
				fishSprite.setFrameSequence(0, 24);
				fishSprite.setAnimInterval(.05);
				fishSprite.setScale(scale);
			}
			if(fishSprite2 == null){
				fishSprite2 = new Sprite(simul, Set.FISH2_Path, Set.FISH2_Rows, Set.FISH2_Cols, 0);
				fishSprite2.setFrameSequence(0, 24);
				fishSprite2.setAnimInterval(.05);
				fishSprite2.setScale(scale);
			}
			if(fishSprite3 == null)
			{
				fishSprite3 = new Sprite(simul, Set.FISH3_Path, Set.FISH3_Rows, Set.FISH3_Cols, 0);
				fishSprite3.setFrameSequence(0, 24);
				fishSprite3.setAnimInterval(.05);
				fishSprite3.setScale(scale);
			}
			if(fishSprite4 == null){
				fishSprite4 = new Sprite(simul, Set.FISH4_Path, Set.FISH4_Rows, Set.FISH4_Cols, 0);
				fishSprite4.setFrameSequence(0, 10);
				fishSprite4.setAnimInterval(.05);
				fishSprite4.setScale(scale*2);
			}
		}
		
		if(COLOR_OFFSETS[color] == null) {
			COLOR_OFFSETS[color] = simul.registerColors( createColors(color) );
		}
		
		//sprites = simul.loadSprites("../bin/fish0%d.png",5);
	}

	public Fish( PVector position, PVector speed, Sim s, int color ) {
		this( position, speed, 7, s, color );
	}	
	
	public Fish( float xpos, float ypos, float xspeed, float yspeed, int size, Sim s, int color ) {
		this( new PVector( xpos, ypos ), new PVector( xspeed, yspeed ), size, s, color );		
	}
	
	public Fish( float xpos, float ypos, float xspeed, float yspeed, Sim s, int color ) {
		this( new PVector( xpos, ypos ), new PVector( xspeed, yspeed ), s, color );		
	}
	
	/*
	 * Makes all the changes of a timestep. Accel is the acceleration vector on the fish
	 * during this timestep 
	 */
	@Override
	public int step( ArrayList<Boid> school ) {
	//public void update( PVector accel ) {
		
		PVector accel = calculateAccel( school );
		accel = avoidEgdes(accel);
		
		
		// If accel == null, skip down to end
		if( accel != null ) {
			
			PVector accelLocal = matrixMult( Fish.inverse(basis), accel );
				// accelLocal is now a cordinate vector in the fish's local coordinate system,
				// defined by the fish's basis			
			
			/*
			 * To limit it's max steering angle to a defined degree, we make sure the acceleration perpendicular 
			 * to its velocity is <= the parallel acceleration.
			 */
			
			// TODO should we be doing this w/ speed, not accel?
			// If turning angle is greater than the maximum allowed ...
			if( Math.abs( accelLocal.y)/Math.abs(accelLocal.x) > MAX_TURN_RATIO ) {

				
				// Sets y's magnitude to x's magnitude, keeping y's sign
				accelLocal.y = ((accelLocal.y < 0) ? -1 : 1) * Math.abs(accelLocal.x);
				
				// TODO put in settings for min coasting speed
				// If we are turning extremely slowly (and we're here because we want to turn hard),
				// speed up so we can turn
				if( accelLocal.mag() < Set.FISH_MaxAccel/3 ) {
					accelLocal.normalize();
					accelLocal.mult( Set.FISH_MaxAccel/3 );
				}
			}
			
			// If the desired acceleration is > max, set it to max
			if( accel.mag() > Set.FISH_MaxAccel ) {
				accel.normalize();
				accel.mult(Set.FISH_MaxAccel);
			}
			
			// not sure TODO
			if( speed.mag() + accelLocal.x < Set.FISH_MinSpeed) {
				accelLocal.x = -accelLocal.x;
			}
			
			// Update recentAccel.  basis * accelLocal -> accel in global
			recentAccel = matrixMult(basis, accelLocal); 	
			
			// update speed. 
			speed.add( recentAccel );
			
			// If the new speed is > max, set it to max
			if( speed.mag() > Set.FISH_MaxSpeed ) {
				speed.normalize();
				speed.mult(Set.FISH_MaxSpeed);
			}	
			
		} else { // Jumps to here if accel == null
			recentAccel = null;
		}
		
		
		// Update position
		position.add(speed);
		
		// Make sure the new position is onscreen; if not, wrap it
		if( position.x < 0 ) position.x += Set.SCREEN_Width;
		if( position.y < 0 ) position.y += Set.SCREEN_Height;
		position.x %= Set.SCREEN_Width;
		position.y %= Set.SCREEN_Height;
		
		// Update basis
		basis[0].set(speed);
		PVector.cross( basis[0], Z_VECTOR, basis[1]);
		basis[0].normalize();
		basis[1].normalize();
		
		// Update opacity
		color = Sim.colors.get( COLOR_OFFSETS[style]+(Sim.frameCounter+FRAME_OFFSET)%Set.FISH_ShimmerCycle );
		head_color = Sim.colors.get( Set.FISH_ShimmerCycle+COLOR_OFFSETS[style]+(Sim.frameCounter+FRAME_OFFSET)%Set.FISH_ShimmerCycle );
		
		// no change in school
		return 0;
	}
	
	/*
	 * Calculates 
	 */
	protected PVector calculateAccel( ArrayList<Boid> school ) {
		
		// Separation == sum[ (unit vector of diff. of position) / (magnitude of diff. of position)^2 ]
		// Cohesion   == avg[ position ] - (actual position)
		// Alignment  == avg[ velocity ] - (actual velocity) 
		PVector separation = new PVector( 0, 0 );  // separation is also our return value
		PVector cohesion = new PVector( 0, 0 );
		PVector alignment = new PVector( 0, 0 );
		PVector avoidance = new PVector( 0, 0 );
		
		int i;
		int nearbyCount = 0;
		PVector displaceVector = new PVector();
		float displaceMag;
		Boid other;
		
		for( i=0; i<school.size(); i++ ) {	
			other = school.get(i);
			
			if( ! other.equals(this) && isAwareOf(other) ) {

				displaceVector = PVector.sub( position, other.position );
				displaceMag = displaceVector.mag();
				
				// TODO use plug-in functions
				switch( other.getTYPE() ) {
				
				case FISH:
					
					// if same color or colors mingle...
					if( Set.FISH_StylesMingle || style == ((Fish)other).style ) {
						
						// Separation == sum[ (unit vector of diff. of position) / (magnitude of diff. of position)^2 ]
						displaceVector.normalize();  // = unit vector of diff. of position
						displaceVector.div( (float) Math.pow(displaceMag, 2) ); // divided by (magnitude)^2
						separation.add( displaceVector );  // sum them
						
						// Cohesion   == avg[ position ] - (actual position)
						cohesion.add( other.position ); // we will take the avg and subtract later
						
						// Alignment  == avg[ velocity ] - (actual velocity)
						alignment.add( other.speed ); // we will take the avg and subtract later
						
						nearbyCount++;  // count used in avg's
						
						break;
						
					} else { // if different colors, avoid them
						displaceVector.add(other.speed); // SAME AS OBSTACLE but we add 3 steps of speed to its location
						displaceVector.add(other.speed);
						displaceVector.add(other.speed);
						displaceVector.normalize();
						displaceVector = PVector.add( PVector.div( displaceVector, (float) Math.pow( displaceMag, 2 ) ),
													  PVector.div( displaceVector, 2*displaceMag ) );
						//vectToObst.div( distToObst );
						displaceVector.mult( Set.FISH_FearWeight );
						avoidance.add( displaceVector );
						
						break;
					}
					
				case OBSTACLE:
					displaceVector.normalize();
					displaceVector = PVector.add( PVector.div( displaceVector, (float) Math.pow( displaceMag, 2 ) ),
												  PVector.div( displaceVector, 2*displaceMag ) );
					//vectToObst.div( distToObst );
					displaceVector.mult( Set.FISH_FearWeight );
					avoidance.add( displaceVector );
					break;
				
				case PERSON:
					displaceVector.normalize();
					displaceVector.mult( Set.FISH_FearWeight );
					avoidance.add( displaceVector );
					break;
					
				case SHARK:		// SAME AS OBSTACLE but fear weight is 2x
					displaceVector.normalize();
					displaceVector = PVector.add( PVector.div( displaceVector, (float) Math.pow( displaceMag, 2 ) ),
												  PVector.div( displaceVector, 2*displaceMag ) );
					//vectToObst.div( distToObst );
					displaceVector.mult( 2*Set.FISH_FearWeight );
					avoidance.add( displaceVector );
					break;
					
				case FOOD:
					displaceVector.normalize();
					displaceVector.mult( Set.FISH_HungerWeight );
					avoidance.sub( displaceVector );
				
				}
			}
		}
		
		// Use pointCloud if we should
		if( Set.KINECT_On && Set.kinect_AffectsSim ) {
			Kinect kinect = Sim.kinect;
			for( i=0; i<kinect.goodPixels.length; i++ ) {
				if( kinect.pointCloud[kinect.goodPixels[i]] ) {
					displaceVector.x = position.x - kinect.mapKinectToSim_Col[kinect.goodPixels[i]%640];
					displaceVector.y = position.y - kinect.mapKinectToSim_Row[kinect.goodPixels[i]/640];
					if( displaceVector.mag() < AWARE_RADIUS ) {
						displaceVector.mult(kinect.pointCloudRepulsionMulti);
						avoidance.add( displaceVector );
					}
				}
			}
		}
		
		// if the fish is alone, no acceleration vector
		if( nearbyCount > 0 ) {
			
			//separation.normalize();
			
			// Cohesion   == avg[ position ] - (actual position)
			cohesion.div( nearbyCount );   // divide to get avg[ position ]
			cohesion.sub( position ); // subtract
			
			//cohesion.normalize();
			//cohesion.mult( Set.FISH_MaxSpeed );
			//cohesion.sub( speed );
			//cohesion.normalize();
			
			// Alignment  == avg[ velocity ] - (actual velocity)
			alignment.div( nearbyCount ); // divide to get avg[ speed ]
			alignment.sub( speed );  // subtract
			
			//alignment.normalize();
			
			// Weight them all
			separation.mult( getSEPARATION_WEIGHT() );
			cohesion.mult( getCOHESION_WEIGHT() );
			alignment.mult( getALIGNMENT_WEIGHT() );

			// Add them together. choosing separation for the sum is arbitrary
			separation.add(cohesion);
			separation.add(alignment);
		}
		return PVector.add( separation, avoidance );
	}
	
	@Override
	public void drawBoid(Sim sim) {
		//============DISPLAY EXTRAS============
		// We draw the awareness circle first so it is under the fish
		if (Set.SHOW_Awareness) {
			sim.stroke(255, 0, 0);
			sim.fill(255, 0, 0, 25);
			sim.ellipse(position.x, position.y,
					2 * AWARE_RADIUS, 2 * AWARE_RADIUS);
		}
		if (Set.SHOW_AwarenessCone) {
			sim.stroke(255, 0, 0);
			sim.fill(255, 0, 0, 25);

			PVector cone[] = {
					new PVector(0, -1 * AWARE_CONE_WIDTH_MAX),
					new PVector(AWARE_CONE_LENGTH, -1 * AWARE_CONE_WIDTH_MIN),
					new PVector(AWARE_CONE_LENGTH, AWARE_CONE_WIDTH_MIN),
					new PVector(0, AWARE_CONE_WIDTH_MAX) };

			cone = Boid.matrixMultParallel(basis, cone);

			sim.beginShape();
			sim.vertex(position.x + cone[0].x, position.y + cone[0].y);
			sim.vertex(position.x + cone[1].x, position.y + cone[1].y);
			sim.vertex(position.x + cone[2].x, position.y + cone[2].y);
			sim.vertex(position.x + cone[3].x, position.y + cone[3].y);
			sim.endShape();
		}

		//=========DRAW FISH===========
		if( Set.SHOW_Sprites ) {
			// Code to animate sprites
			
			float angle = (float) getHeading();
			
			float totalAccel = recentAccel.mag();
			
			if(style == STYLE1) //Balanced fish
			{
				fishSprite.setRot(angle);
				fishSprite.setXY(position.x, position.y);
				
				//fishSprite.setScale((float)AWARE_RADIUS/60);
				fishSprite.setScale(Set.FISH1_Scale);
				
				if(totalAccel < 1){
					fishSprite.setFrame((Sim.frameCounter+offset)/14 % 5);
				}
				else if(totalAccel < 2){
					fishSprite.setFrame((Sim.frameCounter+offset)/8 % 5);
				}
				else{
					fishSprite.setFrame((Sim.frameCounter+offset)/4 % 5);
				}
			}
			else if (style == STYLE2) //Slow moving fish (probably a large fish)
			{
				fishSprite2.setRot(angle);
				fishSprite2.setXY(position.x, position.y);
				
				//fishSprite2.setScale((float)AWARE_RADIUS/60);
				fishSprite2.setScale(Set.FISH2_Scale);
				
				if(totalAccel < 1){
					fishSprite2.setFrame((Sim.frameCounter+offset)/30 % 5);
				}
				else if(totalAccel < 2){
					fishSprite2.setFrame((Sim.frameCounter+offset)/10 % 5);
				}
				else{
					fishSprite2.setFrame((Sim.frameCounter+offset)/5 % 5);
				}
			}
			else if (style == STYLE3) //balanced, but frantic in a tight spot
			{
				fishSprite3.setRot(angle);
				fishSprite3.setXY(position.x, position.y);
				
				//fishSprite3.setScale((float)AWARE_RADIUS/60);
				fishSprite3.setScale(Set.FISH3_SCALE);
				
				if(totalAccel < 1){
					fishSprite3.setFrame((Sim.frameCounter+offset)/15 % 5);
				}
				else if(totalAccel < 2){
					fishSprite3.setFrame((Sim.frameCounter+offset)/8 % 5);
				}
				else{
					fishSprite3.setFrame((Sim.frameCounter+offset) % 5);
				}
			}
			else if (style == STYLE4) // slowish, but frantic when in a tight space
			{
				fishSprite4.setRot(angle);
				fishSprite4.setXY(position.x, position.y);
				
				//fishSprite4.setScale((float)AWARE_RADIUS/60);
				fishSprite4.setScale(Set.FISH4_SCALE);
				
				if(totalAccel < 1){
					fishSprite4.setFrame((Sim.frameCounter+offset)/20 % 5);
				}
				else if(totalAccel < 2){
					fishSprite4.setFrame((Sim.frameCounter+offset)/10 % 5);
				}
				else{
					fishSprite4.setFrame((Sim.frameCounter+offset) % 5);
				}
			}

			//fishSprite.setZorder(frameCounter%62);
			S4P.drawSprites();
		
			
		} else {
			// Code to draw the fish from scratch
			
			// head are the three points that define the head
			PVector head[] = {
					new PVector((float) (H_WIDTH * Math.cos(getHeading()
							+ PConstants.PI / 2)),
							(float) (H_WIDTH * Math.sin(getHeading() + PConstants.PI / 2))),
					new PVector(
							(float) (H_LENGTH * Math.cos(getHeading())),
							(float) (H_LENGTH * Math.sin(getHeading()))),
					new PVector((float) (H_WIDTH * Math.cos(getHeading()
							- PConstants.PI / 2)),
							(float) (H_WIDTH * Math.sin(getHeading() - PConstants.PI / 2))) };

			
			// Scales the width of head inversely and length directly with %speed
			float fracSpeed = (float) speed.mag() / getMAX_SPEED();

			if (speed.mag() > 1) {
				head[0].mult(1.1f - .3f * fracSpeed);
				head[1].mult(.9f + .5f * fracSpeed);
				head[2].mult(1.1f - .3f * fracSpeed);
			}

			// tail are the two points that define the tail
			PVector tail[] = {
					new PVector((float) (T_LENGTH * Math.cos(getHeading()
										+ PConstants.PI / 2 + T_ANGLE)),
								(float) (T_LENGTH * Math.sin(getHeading()
										+ PConstants.PI / 2 + T_ANGLE))),
					new PVector((float) (T_LENGTH * Math.cos(getHeading()
										- PConstants.PI / 2 - T_ANGLE)),
								(float) (T_LENGTH * Math.sin(getHeading()
										- PConstants.PI / 2 - T_ANGLE))) };

		/*
		 * PVector accel = recentAccel;
		 * 
		 * 
		 * if( accel != null ) { accel =
		 * Boid.matrixMult(Boid.inverse(basis), recentAccel );
		 * accel.normalize(); accel.mult((float)T_LENGTH/2); if( accel.x >
		 * 0 ) { accel.x = - accel.x; } accel.y *= .5f; //if( accel.x >
		 * -.5f*T_LENGTH ) { // accel.x = (float) (-.5*T_LENGTH); //}
		 * } else { accel = new PVector( -1, 0 ); } accel =
		 * Boid.matrixMult(basis, accel);
		 * 
		 * //PVector accel = recentAccel; PVector tail[] = { PVector.sub(
		 * accel, head[2] ), PVector.sub( accel, head[0] ) };
		 * 
		 * tail[0].normalize(); tail[1].normalize(); tail[0].mult( (float)
		 * T_LENGTH ); tail[1].mult( (float) T_LENGTH );
		 */

		
			// Draw head
			sim.fill(head_color);
			sim.stroke(head_color);

			// Connect the dots
			sim.beginShape();
			sim.vertex((int) (position.x + head[0].x),
					(int) (position.y + head[0].y));
			sim.vertex((int) (position.x + head[1].x),
					(int) (position.y + head[1].y));
			sim.vertex((int) (position.x + head[2].x),
					(int) (position.y + head[2].y));
			sim.vertex((int) (position.x + head[0].x),
					(int) (position.y + head[0].y));
			sim.endShape();

			// Draw body
			sim.fill(color);
			sim.stroke(color);

			// Connect the dots
			sim.beginShape();
			sim.vertex((int) (position.x + head[0].x),
					(int) (position.y + head[0].y));
			sim.vertex((int) (position.x + head[2].x),
					(int) (position.y + head[2].y));
			sim.vertex((int) (position.x + tail[0].x - H_LENGTH/2),
					(int) (position.y + tail[0].y - H_WIDTH/2));
/*			sim.vertex((int) (position.x + tail[1].x),
					(int) (position.y + tail[1].y));
			sim.vertex((int) (position.x + head[0].x),
					(int) (position.y + head[0].y));
				*/
			/*
			 * wispy fish
			sim.vertex((int) (position.x + head[0].x),
					(int) (position.y + head[0].y));
			sim.vertex((int) (position.x + head[1].x),
					(int) (position.y + head[1].y));
			sim.vertex((int) (position.x + tail[0].x),
					(int) (position.y + tail[0].y));
			*/
			sim.endShape();

		}
		// END DRAW FISH	
		
		//====DISPLAY EXTRAS====
		// Draws each fish's basis vectors
		if (Set.SHOW_Bases) {
			sim.stroke(255, 255, 0);
			sim.line(position.x, position.y, position.x + 20
					* basis[0].x, position.y + 20 * basis[0].y);
			sim.line(position.x, position.y, position.x + 15
					* basis[1].x, position.y + 15 * basis[1].y);
		}

		// Draws each fish's acceleration and velocity vectors
		if (Set.SHOW_KinematicVectors) {
			sim.stroke(255, 255, 0);
			sim.line(position.x, position.y, position.x + 10
					* speed.x, position.y + 10 * speed.y);
			if (recentAccel != null) {
				sim.stroke(0, 0, 255);
				sim.line(position.x, position.y, position.x + 35
						* recentAccel.x, position.y + 35
						* recentAccel.y);
			}
		}

	}
	public static int[][] createColors( int style ){
		
		// if COLOR_OFFSET is already set, we don't need to do it again
		//if( COLOR_OFFSET != null) { return null; }
		
		int[][] answer = new int[Set.FISH_ShimmerCycle*2][4]; 
		
		
		for( int i=0; i<Set.FISH_ShimmerCycle; i++ ) {
			answer[i][0] = Set.FISH_Styles[style][0];
			answer[i][1] = Set.FISH_Styles[style][1];
			answer[i][2] = Set.FISH_Styles[style][2];
			answer[i][3] = (int) (255-Set.FISH_ShimmerDepth + Set.FISH_ShimmerDepth * Math.abs( ( Math.cos(
									Boid.redoRangeERROR( i, 0, (float) (2*PConstants.PI), 0, Set.FISH_ShimmerCycle )
								  		) ) ) );
		}
		
		for( int i=Set.FISH_ShimmerCycle; i<2*Set.FISH_ShimmerCycle; i++ ) {
			answer[i][0] = Set.FISH_Styles[style][3];
			answer[i][1] = Set.FISH_Styles[style][4];
			answer[i][2] = Set.FISH_Styles[style][5];
			answer[i][3] = (int) (255-Set.FISH_ShimmerDepth + Set.FISH_ShimmerDepth * Math.abs( ( Math.cos(
									Boid.redoRangeERROR( i, 0, (float) (2*PConstants.PI), 0, Set.FISH_ShimmerCycle )
								  		) ) ) );
		}
		return answer;
	}

	/*
	public static void receiveColors(int startIndex, int size) {
		COLOR_OFFSET = startIndex;
	}
	*/
	
	@Override
	public boolean isAwareOf(Boid boid) {
		
		switch( boid.getTYPE() ) {
		
		//case YELLOW_FISH:
		//	return ( PVector.sub( position, boid.position ).mag() < AWARE_RADIUS );
		
		case FISH:
			return ( PVector.sub( position, boid.position ).mag() < AWARE_RADIUS );
		
		case OBSTACLE:
			
			PVector displacement = PVector.sub( boid.position, position );
			if( displacement.mag() < AWARE_RADIUS + 3*boid.size ) {
				return true;
			}
			
			
			displacement = Boid.matrixMult(basis, displacement);
			
			if( displacement.x + 3*boid.size < AWARE_CONE_LENGTH ) {
				
				float yLimit = AWARE_CONE_WIDTH_MAX + (AWARE_CONE_WIDTH_MIN-AWARE_CONE_WIDTH_MAX)/AWARE_CONE_LENGTH;
				if( displacement.y + 3*boid.size < Math.abs( yLimit ) ) {
					return true;
				}
			
			}
			return false;
			
		case PERSON:
			
			displacement = PVector.sub( boid.position, position );
			if( displacement.mag() < AWARE_RADIUS + 3*boid.size ) {
				return true;
			}
			
			
			displacement = Boid.matrixMult(basis, displacement);
			
			if( displacement.x + 3*boid.size < AWARE_CONE_LENGTH ) {
				
				float yLimit = AWARE_CONE_WIDTH_MAX + (AWARE_CONE_WIDTH_MIN-AWARE_CONE_WIDTH_MAX)/AWARE_CONE_LENGTH;
				if( displacement.y + 3*boid.size < Math.abs( yLimit ) ) {
					return true;
				}
			
			}
			return false;
		
		case SHARK:
			return ( PVector.sub( position, boid.position ).mag() < AWARE_RADIUS + 3*boid.size );
		
		case FOOD:
			return ( PVector.sub( position, boid.position ).mag() < 4 * AWARE_RADIUS );
		}
		
		return false;
		
	}
	
	@Override
	public float getFEAR_WEIGHT() {
		return Set.FISH_FearWeight;
	}

	@Override
	public float getALIGNMENT_WEIGHT() {
		return Set.FISH_AlignmentWeight;
	}

	@Override
	public float getCOHESION_WEIGHT() {
		return Set.FISH_CohesionWeight;
	}

	@Override
	public float getSEPARATION_WEIGHT() {
		return Set.FISH_SeparationWeight;
	}
	
	@Override
	public Boid.Type getTYPE() {
		return TYPE;
	}
	
	@Override
	public float getMAX_ACCEL() {
		return Set.FISH_MaxAccel;
	}
	
	@Override
	public float getMAX_SPEED() {
		return Set.FISH_MaxSpeed;
	}
	
	// This is used only when creating a new fish
	public static float getMAX_SPEED_static() {
		return Set.FISH_MaxSpeed;
	}

	@Override
	public float getMIN_ACCEL() {
		return Set.FISH_MinAccel;
	}

	@Override
	public float getMIN_SPEED() {
		return Set.FISH_MinSpeed;
	}
	
	@Override
	public float getAWARE_RADIUS() {
		return AWARE_RADIUS;
	}

}