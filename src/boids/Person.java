package boids;

import java.util.ArrayList;

import processing.core.PVector;
import simulation.Boid;
import simulation.Set;
import simulation.Sim;


public class Person extends Boid {

	
	private static final Boid.Type TYPE = Boid.Type.PERSON;

	
	public Person() {
		super( new PVector(-1,-1), new PVector(0,0), 40 );
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
			
			float odds = Sim.rand.nextFloat();
			
			position.x = Set.SCREEN_Width/3 + Sim.rand.nextInt(Set.SCREEN_Width/3);
			
			// 65% come from the top
			if( odds < .25 ) {
			// Entering from TOP
				position.y = 0;
			}
			// 35% from bottom
			else {
			// Entering from BOTTOM
				position.y = Set.SCREEN_Height;
			}
			
			speed.x = 2*(Sim.rand.nextFloat()-.5f)*Set.PERSON_HorizSpeedVar;
			speed.y = (position.y!=0?-1:1)*Boid.redoRangeERROR( Sim.rand.nextFloat(), Set.PERSON_MinSpeed, Set.PERSON_MaxSpeed);
			
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
		return Set.PERSON_MaxAccel;
	}

	@Override
	public float getMAX_SPEED() {
		return Set.PERSON_MaxSpeed;
	}

	@Override
	public float getMIN_ACCEL() {
		return Set.PERSON_MinAccel;
	}

	@Override
	public float getMIN_SPEED() {
		return Set.PERSON_MinSpeed;
	}
}
