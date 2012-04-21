package boids;

import java.util.ArrayList;

import processing.core.PVector;

import simulation.Boid;
import simulation.Set;
import simulation.Sim;

public class Food extends Boid {

	public static final Boid.Type TYPE = Boid.Type.FOOD;

	
	public boolean eaten = false;
	
	
	public Food( float xpos, float ypos ) {
		super(xpos, ypos, 0, 0, Set.FOOD_Size);
	}
	
	@Override
	public float getFEAR_WEIGHT() {
		return 0;
	}

	@Override
	public float getMAX_ACCEL() {
		return Set.FOOD_MaxAccel;
	}

	@Override
	public float getMAX_SPEED() {
		return Set.FOOD_MaxSpeed;
	}

	@Override
	public float getMIN_ACCEL() {
		return Set.FOOD_MinAccel;
	}

	@Override
	public float getMIN_SPEED() {
		return Set.FOOD_MinSpeed;
	}

	@Override
	public Type getTYPE() {
		return TYPE;
	}

	@Override
	public int step(ArrayList<Boid> flock) {
	
		speed.add( (Sim.rand.nextFloat()*2-1)*Set.FOOD_MaxAccel, (Sim.rand.nextFloat()*2-1)*Set.FOOD_MaxAccel, 0 );
		
		if( speed.mag() > Set.FOOD_MaxSpeed) {
			speed.normalize();
			speed.mult( Set.FOOD_MaxSpeed );
		}
		
		position.add( speed );
	
		if(position.x < 0) position.x += Set.screen_Width;
		if(position.y < 0) position.y += Set.screen_Height;
		if(position.x > Set.screen_Width  ) position.x %= Set.screen_Width;
		if(position.y > Set.screen_Height ) position.x %= Set.screen_Height;
	
		int flockSize = flock.size();
		Boid other;
		for( int i=0; i<flockSize; i++) {
			other = flock.get(i);
			if( !other.equals(this) &&
				PVector.sub( other.position, position).mag() <= Set.FOOD_EatenThreshold ) {
				eaten = true;
			}
		}
		
		if( eaten ) {
			flock.remove(this);
			return -1; //removed boid from flock
		}
		
		return 0; // no change to school
		
		
	}

	@Override
	public void drawBoid(Sim sim) {
		sim.stroke(180, 150, 0);
		sim.fill(180, 150, 0);
		sim.ellipse(position.x, position.y, size, 2 * size);

	}
}
