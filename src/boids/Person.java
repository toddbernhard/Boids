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
	public int step(ArrayList<Boid> school) {
		
		position.add( speed );
		
		if( position.x < 0 || position.x > Set.screen_Width ||
			position.y < 0 || position.y > Set.screen_Height   ) {
			
			float odds = Sim.rand.nextFloat();
			
			position.x = Set.screen_Width/3 + Sim.rand.nextInt(Set.screen_Width/3);
			
			// 65% come from the top
			if( odds < .25 ) {
			// Entering from TOP
				position.y = 0;
			}
			// 35% from bottom
			else {
			// Entering from BOTTOM
				position.y = Set.screen_Height;
			}
			
			speed.x = 2*(Sim.rand.nextFloat()-.5f)*Set.PERSON_HorizSpeedVar;
			speed.y = (position.y!=0?-1:1)*Boid.redoRangeERROR( Sim.rand.nextFloat(), Set.PERSON_MinSpeed, Set.PERSON_MaxSpeed);
			
		}
		
		// no change in school
		return 0;
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

	@Override
	public void drawBoid(Sim sim) {
		sim.stroke(0);
		sim.fill(200);

		sim.ellipse(position.x, position.y, size, size);
	}
}
