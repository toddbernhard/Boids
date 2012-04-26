package simulation;
import java.util.ArrayList;


import processing.core.PVector;

public abstract class Boid {

	public static enum Type { FISH, OBSTACLE, SHARK, PERSON, FOOD };
	
	// Used in taking cross-products, for constructing basis
	public static final PVector Z_VECTOR = new PVector( 0, 0, 1 );
	
	public final int ID;
	
	public static int colorCounter = 0;
	public static int boidCounter = 0;
	
	public PVector position;// In global
	public PVector speed;	// In global
	public PVector recentAccel; // Used in animating, in global
	public PVector[] basis; // Basis of local grid
	public int size;
	public boolean grouped; // Whether or not this boid has been grouped this anim cycle
	public int color;
	public int opacity;
	

	public Boid( PVector position, PVector speed, int size ) {
		
		ID = ++boidCounter;
		
		this.position = position;
		this.speed = speed;
		recentAccel = null;
		
		basis = new PVector[2];
		basis[0] = new PVector();
		basis[1] = new PVector();
		basis[0].set( speed );
		PVector.cross(Z_VECTOR, speed, basis[1]);
		basis[0].normalize();
		basis[1].normalize();
		
		this.size = size;
		grouped = false;		
	}
	
	public Boid( float xpos, float ypos, float xspeed, float yspeed, int size ) {
		this( new PVector( xpos, ypos ), new PVector( xspeed, yspeed), size );		
	}
	
	/*
	 * Makes all the changes of a timestep. Accel is the acceleration vector on the fish
	 * during this timestep. Returns net change in size of school during the step.
	 * EX. nothing changed == 0 (most), food is eaten == -1, new fish spawned == +#new, etc 
	 */
	public abstract int step( ArrayList<Boid> flock );
	
	public abstract void drawBoid(Sim sim);
	
	public abstract Boid.Type getTYPE();
	public abstract float getMAX_ACCEL();
	public abstract float getMIN_ACCEL();
	public abstract float getMAX_SPEED();
	public abstract float getMIN_SPEED();
	public abstract float getFEAR_WEIGHT();
	
	
	protected PVector avoidEgdes( PVector accel ) {
		
		if( position.x < Set.screen_EdgeWidth ) {
			accel.x += redoRangeERROR( 1/(float)Math.pow( 1 + position.x, 3), 0, 1.5f*getMAX_ACCEL() ); 
		}
		else if( Set.SCREEN_Width - position.x < Set.screen_EdgeWidth ) {
			accel.x -= redoRangeERROR( 1/(float)Math.pow( 1 + Set.SCREEN_Width-position.x, 3), 0, 1.5f*getMAX_ACCEL() ); 
		}
		
		if( position.y < Set.screen_EdgeWidth ) {
			accel.y += redoRangeERROR( 1/(float)Math.pow( 1 + position.y, 1), 0, 1.5f*getMAX_ACCEL() ); 
		}
		else if( Set.SCREEN_Height - position.y < Set.screen_EdgeWidth ) {
			accel.y -= redoRangeERROR( 1/(float)Math.pow( 1 + Set.SCREEN_Height-position.x, 3), 0, 1.5f*getMAX_ACCEL() ); 
		}
		
		return accel;
		
	}

	
	protected void groupHelper( ArrayList<Boid> school, ArrayList<Boid> group ) {
		
		group.add( this );
		grouped = true;
		
		for( Boid boid : school ) {
			/*if(    boids[i].grouped == false
				&& PVector.sub(boids[index].position, boids[i].position ).mag() < Fish.AWARE_RADIUS 
				&& PVector.sub(boids[index].speed,    boids[i].speed ).mag() < 5) {
			*/
			if(    boid.grouped == false
				//&& PVector.sub(boids[index].speed,    boids[i].speed ).mag() < 5
				&& (   PVector.sub(position, boid.position ).mag() < 1.5*50
					|| Math.abs(position.x-boid.position.x-Set.SCREEN_Width) < 3*50
					|| Math.abs(position.y-boid.position.y-Set.SCREEN_Height) < 3*50 )  ) {
				
				boid.groupHelper( school, group);
				
			}
		}
		
	}
	/*
	 * Convenience function that returns the angle from the x-axis to the velocity vector.
	 * Return values from -PI to +PI
	 */
	public double getHeading() {
		return Math.atan2(speed.y, speed.x);
	}
		
	/*
	 * Creates a vector that is the orthogonal projection of "of" onto "onto"
	 */
	public static PVector orthProj( PVector of, PVector onto) {
		return PVector.mult(onto, PVector.dot(of, onto)/onto.mag() );
	}
	
	/*
	 * Inverts a 2x2 matrix. matrix is a an array of PVectors of length 2, so that:
	 * 		matrix[0] = { a, b }  and matrix[1] = { c, d }
	 * and we are inverting the matrix
	 *  	{ { a, c },
	 *  	  { b, d } }
	 * We return:
	 * 				{ {  d, -c } ,
	 *  1/(ad-bc) *   { -b,  a } }
	 *  
	 *  NOTE: ad-bc != 0 !!  (meaning matrix[0] and matrix[1] must be linearly indep!)
	 */
	public static PVector[] inverse( PVector[] matrix ) {
		
		float a, b, c, d, det;
		
		a = matrix[0].x;
		b = matrix[0].y;
		c = matrix[1].x;
		d = matrix[1].y;
		
		det = a*d - c*b; // the determinant
		
		if( a*d-c*b == 0 ) {  // If determinant is 0, inverse doesn't exist. Return null
			return null;
		}
		
		PVector[] inverse = new PVector[2];
		
		inverse[0] = new PVector( d/det, -c/det );
		inverse[1] = new PVector( -b/det, a/det );
		
		return inverse;
		
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		return ( obj instanceof Boid && ID == ((Boid) obj).ID );
	}

	public static PVector matrixMult( PVector[] matrix, PVector vector ) {
		
		return new PVector( matrix[0].x*vector.x + matrix[0].y*vector.y,
							matrix[1].x*vector.x + matrix[1].y*vector.y  );
	
	}
	
	public static PVector[] matrixMultParallel( PVector[] matrix, PVector[] arrayOfVectors ) {
		
		PVector[] answer = new PVector[ arrayOfVectors.length ];
		
		int length = arrayOfVectors.length;
		
		for( int i=0; i<length; i++ ) {
			answer[i] = matrixMult( matrix, arrayOfVectors[i] );
		}
		
		return answer;
	}
	
	// ERROR because I switched targetMax w/ targetMin so in effect it maps
	//    [ sourceMin, sourceMax ] --> [ targetMax, targetMax+Min ]
	//                          instead of
	//    [ sourceMin, sourceMax ] --> [ targetMin, targetMax ]
	// Before this was discovered, enough constants had been tweaked to compensate
	// that it was not removed.  Use redoRange for expected behavior 
	public static float redoRangeERROR( float value, float targetMin, float targetMax,
								   				float sourceMin, float sourceMax ) {
		return (value-sourceMin) * ((targetMax-targetMin)/(sourceMax-sourceMin)) + targetMax;
	}
	
	// ERROR because I switched targetMax w/ targetMin so in effect it maps
	//    [ sourceMin, sourceMax ] --> [ targetMax, targetMax+Min ]
	//                          instead of
	//    [ sourceMin, sourceMax ] --> [ targetMin, targetMax ]
	// Before this was discovered, enough constants had been tweaked to compensate
	// that it was not removed.  Use redoRange for expected behavior 
	public static float redoRangeERROR( float value, float targetMin, float targetMax ) {
		return redoRangeERROR( value, targetMin, targetMax, 0, 1 );
	}
	
	public static float redoRange( float value, float targetMin, float targetMax,
								   				float sourceMin, float sourceMax ) {
		if( value < sourceMin || value > sourceMax ) {
			//System.out.printf("warning: value %f outside range [%f,%f]\n",value,sourceMin,sourceMax);
		}
		return (value-sourceMin) * ((targetMax-targetMin)/(sourceMax-sourceMin)) + targetMin;
	}
	
	public static float redoRange( float value, float targetMin, float targetMax ) {
		return redoRange( value, targetMin, targetMax, 0, 1 );
	}
}
