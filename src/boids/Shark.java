package boids;

import java.util.ArrayList;

import processing.core.PVector;

import simulation.Boid;
import simulation.Set;
import simulation.Sim;


public class Shark extends Boid {

	private static Boid.Type TYPE = Boid.Type.SHARK;
	
	private static Integer SPEED_COLOR_OFFSET = null;
	private static Integer HEALTH_COLOR_OFFSET = null;
	
	private final int FRAME_OFFSET;
	
	private float energy;
	private float hunger;
	public int energyColor;
	public int hungerColor;	

	public Shark(float xpos, float ypos, float xspeed, float yspeed, int size, Sim simul) {
		super(xpos, ypos, xspeed, yspeed, size);
		
		FRAME_OFFSET = Sim.rand.nextInt( Set.SHARK_SpeedCycle );
		
		if(SPEED_COLOR_OFFSET == null) {
			SPEED_COLOR_OFFSET = simul.registerColors( createColors() );
			HEALTH_COLOR_OFFSET = SPEED_COLOR_OFFSET + Set.SHARK_SpeedCycle;
		}
	}
	
	public Shark(float xpos, float ypos, float xspeed, float yspeed, Sim s ) {
		this( xpos, ypos, xspeed, yspeed, 20, s );
	}
	
	@Override
	public void step(ArrayList<Boid> flock) {
		
		Fish closest = null;
		float distance = 100000;
		float displaceMag;
		
		for( Object obj : flock.toArray() ) {
			if( obj instanceof Fish ) {
				
				displaceMag = PVector.sub( ((Fish) obj).position, position ).mag();
				if( displaceMag < distance ) {
					closest = (Fish) obj;
					distance = displaceMag;
				}
			
			}
		}
		
		if( closest != null) {
			
			float cyclePercent = (1 - (float) Math.pow( Math.abs( Math.cos( Boid.redoRangeERROR(
					(Sim.frameCounter+FRAME_OFFSET)%Set.SHARK_SpeedCycle, 0, (float) Math.PI, 0, Set.SHARK_HealthLevels ))) , .5 ));
			
			speed = PVector.sub( closest.position, position );
			speed.normalize();
			speed.mult( Set.SHARK_MinSpeed + (Set.SHARK_MaxSpeed-Set.SHARK_MinSpeed)* cyclePercent );
			
//			cyclePercent = (float) Math.abs( Math.cos( Boid.redoRange(
//					(Sim.frameCounter+FRAME_OFFSET)%LUNGE_CYCLE, 0, (float) Math.PI, 0, LUNGE_CYCLE )) );
			
			color = Sim.colors.get( (int) (SPEED_COLOR_OFFSET + (Set.SHARK_SpeedCycle-1)* cyclePercent) );
			//color = Sim.colors.get( (int) (HEALTH_COLOR_OFFSET + (Set.SHARK_HealthLevels-1)* cyclePercent) );

		}
		else
		{
			speed.normalize();
			speed.mult(2);
		}
			
		// Update position
		position.add(speed);
		
		// Make sure the new position is onscreen; if not, wrap it
		if( position.x < 0 ) position.x += Set.SCREEN_Width;
		if( position.y < 0 ) position.y += Set.SCREEN_Height;
		
		position.x %= Set.SCREEN_Width;
		position.y %= Set.SCREEN_Height;
		
		if( hunger < 1 ) hunger += (float) .1;
		if( energy < 1 ) energy += (float) .1;
		
		hungerColor = Sim.colors.get( (int) (HEALTH_COLOR_OFFSET + hunger*(Set.SHARK_HealthLevels-1)) );
		energyColor = Sim.colors.get( (int) (HEALTH_COLOR_OFFSET + energy*(Set.SHARK_HealthLevels-1)) );
		
	}

	
	public static int[][] createColors() {
		
		if( SPEED_COLOR_OFFSET != null) { return null; }
		
		int[][] answer = new int[Set.SHARK_SpeedCycle+Set.SHARK_HealthLevels][4]; 
		
		int i;
		float percent;
		
		for( i=0; i<Set.SHARK_SpeedCycle; i++ ) {
			answer[i][0] = (int) ( (163-151)*i/(float)Set.SHARK_SpeedCycle+151 );
			answer[i][1] = (int) ( ( 10-159)*i/(float)Set.SHARK_SpeedCycle+159 );
			answer[i][2] = (int) ( (  5-209)*i/(float)Set.SHARK_SpeedCycle+209 );
			answer[i][3] = 255;
		}
		
		for( /* i */; i<Set.SHARK_SpeedCycle+Set.SHARK_HealthLevels; i++ ) {
			percent = (i - Set.SHARK_SpeedCycle) / (float) Set.SHARK_HealthLevels;
			
			if( percent < .13 ) {
				answer[i][0] = 0;
				answer[i][1] = 207;
				answer[i][2] = 21;
			}
			else if( percent < .5 ) {
				answer[i][0] = (int) ( (255-  0)* (percent-.13)/(.5-.13) +  0 );
				answer[i][1] = (int) ( (164-207)* (percent-.13)/(.5-.13) +207 );
				answer[i][2] = (int) ( ( 28- 21)* (percent-.13)/(.5-.13) + 21 );
			}
			else if( percent < .87 ) {
				answer[i][0] = 255;
				answer[i][1] = (int) ( ( 0-164)* (percent-.5)/(.87-.5) +164 );
				answer[i][2] = (int) ( ( 0- 28)* (percent-.5)/(.87-.5) + 28 );
			}
			else {
				answer[i][0] = 255;
				answer[i][1] = 0;
				answer[i][2] = 0;
			}
			
			//System.out.println( percent+"  ("+answer[i][0]+","+answer[i][1]+","+answer[i][2]+")" );
			
			answer[i][3] = 255;
		}
		
		return answer;	
	}
	
	@Override
	public float getFEAR_WEIGHT() {
		return Set.SHARK_FearWeight;
	}

	@Override
	public float getMAX_ACCEL() {
		return Set.SHARK_MaxAccel;
	}

	@Override
	public float getMAX_SPEED() {
		return Set.SHARK_MaxSpeed;
	}

	@Override
	public float getMIN_ACCEL() {
		return Set.SHARK_MinAccel;
	}

	@Override
	public float getMIN_SPEED() {
		return Set.SHARK_MinSpeed;
	}

	@Override
	public Type getTYPE() {
		// TODO Auto-generated method stub
		return TYPE;
	}
}
