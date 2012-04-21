package boids;

import java.util.ArrayList;

import processing.core.PVector;
import simulation.Boid;
import simulation.Set;
import simulation.Sim;


public class Obstacle extends Boid {

	
	private static final Boid.Type TYPE = Boid.Type.OBSTACLE;
	private static final PVector CENTER_VECTOR = new PVector( Set.screen_Width/2, Set.screen_Height/2 );
	
	
	public Obstacle() {
		super( new PVector(-1,-1), new PVector(0,0), 30 );
	}
	
	@Override
	public float getFEAR_WEIGHT() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Boid.Type getTYPE() {
		return TYPE;
	}

	@Override
	public int step(ArrayList<Boid> school) {
		
		position.add( speed );
		
		if( position.x < 0 || position.x > Set.screen_Width ||
			position.y < 0 || position.y > Set.screen_Height   ) {
			
			int sourcePixel = Sim.rand.nextInt( 2*(Set.screen_Height+Set.screen_Width) );
			
			if( sourcePixel < Set.screen_Width ) {
			// Entering from TOP	
				position.x = sourcePixel;
				position.y = 0;
				
			} else if( sourcePixel < Set.screen_Width+Set.screen_Height ) {
			// Entering from RIGHT
				position.x = Set.screen_Width;
				position.y = sourcePixel - Set.screen_Width;
				
			} else if( sourcePixel < 2*Set.screen_Width + Set.screen_Height ) {
			// Entering from BOTTOM
				position.x = sourcePixel - Set.screen_Width - Set.screen_Height;
				position.y = Set.screen_Height;
				
			} else {
			// Entereing from LEFT
				position.x = 0;
				position.y = sourcePixel - 2*Set.screen_Width - Set.screen_Height;
				
			}
			
			speed = PVector.sub( CENTER_VECTOR, position );
			speed.add( (Sim.rand.nextFloat()-.5f)*Set.OBSTACLE_TargetSize, (Sim.rand.nextFloat()-.5f)*Set.OBSTACLE_TargetSize, 0);
			speed.normalize();
			speed.mult( Boid.redoRangeERROR( Sim.rand.nextFloat(), Set.OBSTACLE_MinSpeed, Set.OBSTACLE_MaxSpeed) );
		}
		
		// no change in school
		return 0;
	}

	@Override
	public float getMAX_ACCEL() {
		return Set.OBSTACLE_MaxAccel;
	}

	@Override
	public float getMAX_SPEED() {
		return Set.OBSTACLE_MaxSpeed;
	}

	@Override
	public float getMIN_ACCEL() {
		return Set.FOOD_MinAccel;
	}

	@Override
	public float getMIN_SPEED() {
		return Set.OBSTACLE_MinSpeed;
	}

	@Override
	public void drawBoid(Sim sim) {
		sim.stroke(0);
		sim.fill(150, 0, 0);

		sim.ellipse(position.x, position.y, size, size);
	}
}
