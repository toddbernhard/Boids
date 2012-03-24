package interfaces;

import simulation.Boid;

//import

public interface Aware {

	boolean isAwareOf( Boid boid );
	float getAWARE_RADIUS();
	
}
