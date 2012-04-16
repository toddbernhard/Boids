package boids;

import java.util.ArrayList;

import processing.core.PVector;
import simulation.Boid;
import simulation.Set;
import simulation.Sim;


public class Obstacle extends Boid {

	
	private static final Boid.Type TYPE = Boid.Type.OBSTACLE;
	private static final PVector CENTER_VECTOR = new PVector( Set.SCREEN_Width/2, Set.SCREEN_Height/2 );
	
	
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
	public void step(ArrayList<Boid> school) {
		
		position.add( speed );
		
		if( position.x < 0 || position.x > Set.SCREEN_Width ||
			position.y < 0 || position.y > Set.SCREEN_Height   ) {
			
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
			
			speed = PVector.sub( CENTER_VECTOR, position );
			speed.add( (Sim.rand.nextFloat()-.5f)*Set.OBSTACLE_TargetSize, (Sim.rand.nextFloat()-.5f)*Set.OBSTACLE_TargetSize, 0);
			speed.normalize();
			speed.mult( Boid.redoRangeERROR( Sim.rand.nextFloat(), Set.OBSTACLE_MinSpeed, Set.OBSTACLE_MaxSpeed) );
			
			/*
			int i = rand.nextInt(8);
			
			switch( i ) {
			case 0: // NORTH
				position.x = SCREEN_WIDTH/2;
				position.y = 0;
				speed.x = 0;
				speed.y = OBSTACLE_SPEED;
				break;
			case 1: // EAST
				position.x = SCREEN_WIDTH;
				position.y = SCREEN_HEIGHT/2;
				speed.x = -OBSTACLE_SPEED;
				speed.y = 0;
				break;
			case 2: // SOUTH
				position.x = SCREEN_WIDTH/2;
				position.y = SCREEN_HEIGHT;
				speed.x = 0;
				speed.y = -OBSTACLE_SPEED;
				break;
			case 3: // WEST
				position.x = 0;
				position.y = SCREEN_HEIGHT/2;
				speed.x = OBSTACLE_SPEED;
				speed.y = 0;
				break;
			case 4: // NORTH EAST
				position.x = SCREEN_WIDTH;
				position.y = 0;
				speed.x = -SCREEN_WIDTH;
				speed.y = SCREEN_HEIGHT;
				speed.normalize();
				speed.mult( 2*OBSTACLE_SPEED );
				break;
			case 5: // SOUTH EAST
				position.x = SCREEN_WIDTH;
				position.y = SCREEN_HEIGHT;
				speed.x = -SCREEN_WIDTH;
				speed.y = -SCREEN_HEIGHT;
				speed.normalize();
				speed.mult( 2*OBSTACLE_SPEED );
				break;
			case 6: // SOUTH WEST
				position.x = 0;
				position.y = SCREEN_HEIGHT;
				speed.x = SCREEN_WIDTH;
				speed.y = -SCREEN_HEIGHT;
				speed.normalize();
				speed.mult( 2*OBSTACLE_SPEED );
				break;
			case 7: // NORTH WEST
				position.x = 0;
				position.y = 0;
				speed.x = SCREEN_WIDTH;
				speed.y = SCREEN_HEIGHT;
				speed.normalize();
				speed.mult( 2*OBSTACLE_SPEED );
				break;
			}
			*/
		}
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
}
