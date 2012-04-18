/*
  Part of the Sprites for Processing library 
  	http://sprites4processing.lagers.org.uk
	http://code.google.com/p/sprites4processing/svn/trunk

  Copyright (c) 2009 Peter Lager

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
 */

package sprites;

import java.awt.event.MouseEvent;
import java.lang.reflect.Method;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * This class represents a sprite based upon a bitmap image file 
 * (eg  jpeg, gif, png)
 * <br>
 * It provides methods to set the sprite's position, velocity, acceleration,
 * scale and rotation.
 * <br>
 * Four types of collision detection is provided <br>
 * (1) Collision circles <br>
 * (2) Image border overlap (boxes) <br>
 * (3) Pixel level - collision based on overlapping non-transparent pixels <br>
 * (4) Overlap - similar to image border but there must be a user defined percentage overlap<br><br>
 * 
 * Methods 2, 3 & 4 will not work if the image has been rotated, attempting to use
 * these on a rotated image will cause method 1 to be used instead.<br>
 * Method 3 will not work with scaled images, attempting to use this method on a 
 * scaled image will cause method 2 to be used. <br>
 * In method 1 the collision detection radius is calculated as (width+height)/2 <br><br>
 * 
 * 
 * @author Peter Lager
 *
 */
public class Sprite implements Comparable<Object>, PConstants, SConstants {

	public PApplet app;

	// Various constants to handle rotation directions
	//	public static final int CLOCKWISE = +1;
	//	public static final int SHORTEST = 0;			// DEFAULT SETTING
	//	public static final int ANTICLOCKWISE = -1;

	/**
	 * Action sprite takes at domain edges
	 */
	public static final int HALT = 0;
	public static final int REBOUND = 1;

	public static int ALPHALEVEL = 20;

	/**
	 * INTERNAL USE ONLY
	 * This holds a reference to the Sprite that currently has the focus.
	 */
	protected static Sprite focusIsWith; // READ ONLY

	/*
	 * INTERNAL USE ONLY
	 * When the mouse button is pressed over a sprite these are used
	 * to remember the mouse position so when a sprite is being dragged
	 * its movement is relative to its starting position.
	 */
	protected int mdx = Integer.MAX_VALUE, mdy = Integer.MAX_VALUE;
	protected double moffX, moffY;
	
	// Current XY positional factors
	protected double x, y;						// World position
	protected double vx, vy;					// Velocities
	protected double ax, ay;					// Acceleration
	protected double rot;						// Image rotation (radians)
//	protected boolean alignToHeading = false;	// Align imgae with

	protected double scale = 1.0f;				// Image scale
	protected Integer zOrder = 0;				// Z order for drawing (low Z first)

	protected double colRadius;					// Collision radius

	protected boolean dead = false;				// is the sprite dead?
	protected boolean visible = true;			// Is the sprite visible

	protected Domain domain = null;				// The domain where the sprite exists
	protected int domainAction = REBOUND;		// Action when domain boundary reached

	// interval in seconds between bitmap image changes
	protected double animInterval;					// time between each frame
	protected double animTime;						// time since last frame change
	protected int animDir = 0;						// frame advance direction (either -1 reverse or 1 forwards)
	protected int frameCurrent = 0;					// current frame to display
	protected int frameBegin = 0; 					// start frame for anim sequence
	protected int frameEnd = 0;						// end frame for anim sequence
	protected int nbrRepeats = Integer.MAX_VALUE;	// The number of times an animation is to be repeated
	protected PImage[] frames;						// The animation frames (there will be at least 1
	protected PImage[] colFrames = null;			// Image used to show collision detection zone

	protected ImageInfo info;
	/*
	 * These variables relate to the image
	 */
	protected double halfWidth, width;
	protected double halfHeight, height;

	protected int hit_x, hit_y;

	/** The object to handle the event */
	protected Object eventHandlerObject = null;
	/** The method in eventHandlerObject to execute */
	protected Method eventHandlerMethod = null;
	/** the name of the method to handle the event */ 
	protected String eventHandlerMethodName;

	// The event type use READ ONLY
	public int eventType = 0;

	// True when being dragged
	private boolean beingDragged = false;
	// True if this sprite can be dragged
	protected boolean draggable = false;

	/** The user can attach any object for additional sprite information */
	public Object tag = null;
	/** The user can specify an integer for additional sprite information */
	public int tagNo = 0;
	
	/**
	 * Create a sprite based on an image file
	 * You can specify the order the sprites are drawn using zOrder - the
	 * higher the value the nearer the viewer.
	 * 
	 * @param theApplet
	 * @param imageFname
	 * @param zOrder
	 */
	public Sprite(PApplet theApplet, String imageFname, int zOrder){
		app = theApplet;
		info = S4P.getImageInfo(app, imageFname, 1, 1);
		ctorCore(imageFname, 1, 1, zOrder);
	}

	/**
	 * Create a sprite based on an image file and an alphaMask file. <br>
	 * You can specify the order the sprites are drawn using zOrder - the
	 * higher the value the nearer the viewer.
	 * 
	 * @param theApplet
	 * @param imageFname
	 * @param alphaFname
	 * @param zOrder
	 */
	public Sprite(PApplet theApplet, String imageFname, String alphaFname, int zOrder){
		app = theApplet;
		info = S4P.getImageInfo(app, imageFname, alphaFname, 1, 1);
		ctorCore(imageFname, 1, 1, zOrder);
	}

	/**
	 * Create a sprite based on an image file. <br>
	 * The actual image can be made up of a number of tiled pictures. <br>
	 * For animation purposes the images should be ordered left to 
	 * right, top to bottom.
	 * You can also specify the order the sprites are drawn using zOrder - the
	 * higher the value the nearer the viewer.
	 * 
	 * @param theApplet
	 * @param imageFname
	 * @param cols
	 * @param rows
	 * @param zOrder the higher the z value the nearer the viewer
	 */
	public Sprite(PApplet theApplet, String imageFname, int cols, int rows, int zOrder){
		app = theApplet;
		info = S4P.getImageInfo(app, imageFname, cols, rows);
		ctorCore(imageFname, cols, rows, zOrder);
	}

	/**
	 * Create a sprite based on an image file and an alphaMask file. <br>
	 * The actual image can be made up of a number of tiled pictures. <br>
	 * For animation purposes the images should be ordered left to 
	 * right, top to bottom.
	 * You can also specify the order the sprites are drawn using zOrder - the
	 * higher the value the nearer the viewer.
	 * 
	 * @param theApplet
	 * @param imageFname
	 * @param alphaFname
	 * @param cols
	 * @param rows
	 * @param zOrder
	 */
	public Sprite(PApplet theApplet, String imageFname, String alphaFname, int cols, int rows, int zOrder){
		app = theApplet;
		info = S4P.getImageInfo(app, imageFname, alphaFname, cols, rows);
		ctorCore(imageFname, cols, rows, zOrder);
	}

	/**
	 * INETRNAL USE ONLY
	 * Core coding for constructors
	 * 
	 * @param imageFname
	 * @param cols
	 * @param rows
	 * @param zOrder the higher the z value the nearer the viewer
	 */
	private void ctorCore(String imageFname, int cols, int rows, int zOrder){
		frames = S4P.getFrames(info);
		width = frames[0].width;
		height = frames[0].height;
		// Next are used in collision detection
		halfWidth = width/2;
		halfHeight = height/2;
		colRadius = (width + height)/4;
		this.zOrder = zOrder;
		S4P.registerSprite(this);	
	}

	protected void calcCollisionImage(){
		colFrames = new PImage[frames.length];
		for(int f = 0; f < colFrames.length; f++){
			colFrames[f] = new PImage((int)width, (int)height, ARGB);
			colFrames[f].loadPixels();
			frames[f].loadPixels();
			for(int p = 0; p < frames[f].pixels.length; p++){
				if( app.alpha(frames[f].pixels[p]) < ALPHALEVEL )
					colFrames[f].pixels[p] = 0;
				else
					colFrames[f].pixels[p] = S4P.colColor;
			}
			colFrames[f].updatePixels();
		}
	}

	/**
	 * If set to true it will look for a method with the format<br>
	 * <pre>void handleSpriteEvents(Sprite sprite) </pre><br>
	 * in your sketch to handle mouse events PRESSED, RELEASED,
	 * CLICKED and DRAGGED <br>
	 * Inside the handleSpriteEvents method you can test for event type: <br>
	 * <pre>if(sprite.eventType == Sprite.PRESSED) ...</pre><br>
	 * and so on. <br>
	 * 
	 * @param mouse_repond true or false
	 */
	public void respondToMouse(boolean mouse_repond){
		try{
			app.unregisterMouseEvent(this);
		}
		catch(Exception e){
		}
		if(mouse_repond){
			app.registerMouseEvent(this);
			if(this.eventHandlerObject == null){
				createEventHandler(app, "handleSpriteEvents", new Class[]{ Sprite.class });
			}
		}
	}
	
	/**
	 * Attempt to create the default event handler for the sprite class. 
	 * The default event handler is a method that returns void and has a single
	 * parameter of type sprite and a method called handleSpriteEvents.
	 * 
	 * @param handlerObj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 * @param parameters the parameter classes.
	 */
	@SuppressWarnings("unchecked")
	protected void createEventHandler(Object handlerObj, String methodName, Class[] parameters){
		try{
			eventHandlerMethod = handlerObj.getClass().getMethod(methodName, parameters );
			eventHandlerObject = handlerObj;
			eventHandlerMethodName = methodName;
		} catch (Exception e) {
			SMessenger.message(MISSING, this, new Object[] {methodName, parameters});
			eventHandlerObject = null;
		}
	}

	/**
	 * Attempt to create the mouse event handler for the sprite class. <br>
	 * The event handler is a method that returns void and has a single parameter
	 * of type Sprite and a method name. 
	 * 
	 * @param obj the object to handle the event
	 * @param methodName the method to execute in the object handler class
	 */
	public void addEventHandler(Object obj, String methodName){
		try{
			eventHandlerObject = obj;
			eventHandlerMethodName = methodName;
			eventHandlerMethod = obj.getClass().getMethod(methodName, new Class[] {this.getClass() } );
		} catch (Exception e) {
			SMessenger.message(NONEXISTANT, this, new Object[] {methodName, new Class[] { this.getClass() } } );
			eventHandlerObject = null;
			eventHandlerMethodName = "";
		}
	}

	/**
	 * Attempt to fire an event for this sprite.
	 * 
	 * The method called must have a single parameter which is the object 
	 * firing the event.
	 * If the method to be called is to have different parameters then it should
	 * be overridden in the child class
	 * The method 
	 */
	protected void fireEvent(){
		if(eventHandlerMethod != null){
			try {
				eventHandlerMethod.invoke(eventHandlerObject, new Object[] { this });
			} catch (Exception e) {
				SMessenger.message(EXCP_IN_HANDLER, eventHandlerObject, 
						new Object[] {eventHandlerMethodName, e } );
			}
		}		
	}

	/**
	 * Restore images back to their original state. Useful if
	 * you have used the bite() method.
	 */
	public void restoreImages(){
		frames = S4P.getFrames(info);	
	}

	/**
	 * Update the positions of all the sprites.
	 * 
	 * @param deltaTime the time in seconds since last called
	 */
	public void update(double deltaTime){
		updatePosition(deltaTime);
		updateImageAnimation(deltaTime);
	}

	/**
	 * Update the sprites position based on the time since last call
	 * 
	 * @param deltaTime
	 */
	protected void updatePosition(double deltaTime){
		vx += ax * deltaTime;
		vy += ay * deltaTime;
		x += vx * deltaTime;
		y += vy * deltaTime;
		if(domain != null)
			keepInsideDomain();
	}

	/**
	 * Calculates if part or all of the sprite is in the visible
	 * portion of the world.
	 *  
	 * @return true if part or all of the sprite is on screen 
	 */
	public boolean isOnScreem(){
		if(x+width*scale < S4P.screenDomain.left 
				|| x-width*scale >S4P.screenDomain.right
				|| y+height*scale < S4P.screenDomain.top
				|| y-height*scale > S4P.screenDomain.bottom)
			return false;

		return true;
	}

	/**
	 * Updates the image to be displayed
	 * 
	 * @param deltaTime
	 */
	protected void updateImageAnimation(double deltaTime){
		if(animInterval > 0.0 && nbrRepeats > 0){
			animTime += deltaTime;
			while(animTime > animInterval){
				animTime -= animInterval;
				frameCurrent += animDir;
				if(animDir == 1 && frameCurrent > frameEnd){
					frameCurrent = frameBegin;
					nbrRepeats--;
				}
				else if(animDir == -1 && frameCurrent < frameEnd){
					frameCurrent = frameBegin;
					nbrRepeats--;
				}
			}
			if(nbrRepeats <= 0){
				nbrRepeats = 0;
				this.animInterval = 0.0f;
			}
		}	
	}

	protected void keepInsideDomain(){
		double ws2 = width*scale*0.5f;
		double hs2 = height*scale*0.5f;
		switch(domainAction){
		case REBOUND:
			if(x-ws2 < domain.left && vx < 0){
				//				x = domain.left + ws2;					
				vx = Math.abs(vx);
				ax = -ax;
			}
			else if(x+ws2 > domain.right && vx > 0){
				//				x = domain.right - ws2;
				vx = -Math.abs(vx);
				ax = -ax;
			}
			if(y-hs2 < domain.top && vy < 0){
				//				y = domain.top + hs2;
				vy = Math.abs(vy);
				ay = -ay;
			}
			else if(y+hs2 > domain.bottom && vy > 0){
				//					y = domain.bottom - hs2;
				vy = -Math.abs(vy);
				ay = -ay;	
			}
			break;
		case HALT:
			// Bring sprite inside domain to prevent repeated test
			// with stationary sprites
			if(x-ws2 < domain.left){
				x = domain.left + ws2;
				vx = vy = ax = ay = 0;
			}
			else if(x+ws2 > domain.right){
				x = domain.right - ws2;
				vx = vy = ax = ay = 0;
			}

			if(y-hs2 < domain.top){
				y = domain.top + hs2;
				vx = vy = ax = ay = 0;
			}
			else if(y+hs2 > domain.bottom){
				y = domain.bottom - hs2;
				vx = vy = ax = ay = 0;
			}
			break;
		}
	}

	/**
	 * Set the sprite's movement domain. Normally you would ensure that the sprite 
	 * starts <b>inside</b> the domain. The last attribute defines what happens 
	 * to the sprite when it reaches the domain boundary - at present there are 
	 * 2 options REBOUND and HALT. <br>
	 * The domain boundaries are specified by the top-left and bottom-right corner
	 * co-ordinates.
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @param action
	 */
	public void setDomain(float left, float top, float right, float bottom, int action){
		domain = new Domain(left,top,right,bottom);
		domainAction = action;
	}

	/**
	 * Set the sprite's movement domain. Normally you would ensure that the sprite 
	 * starts <b>inside</b> the domain. The last attribute defines what happens 
	 * to the sprite when it reaches the domain boundary - at present there are 
	 * 2 options REBOUND and HALT. <br>
	 * 
	 * @param domain
	 * @param action
	 */
	public void setDomain(Domain domain, int action){
		this.domain = domain;
		domainAction = action;
	}

	/**
	 * Get the sprite's movement domain.
	 * 
	 * @return the movement domain
	 */
	public Domain getDomain(){
		return domain;
	}

	/**
	 * Removes the current domain so the sprite's movement is no
	 * longer constrained.
	 */
	public void clearDomain(){
		domain = null;
		domainAction = REBOUND;
	}

	/**
	 * If you have said that this sprite is to respond to the mouse
	 * then this method will be called every loop.
	 * 
	 * @param event
	 */
	public void mouseEvent(MouseEvent event){
		if(!visible || dead)
			return;

		boolean mouseOver = isOver(app.mouseX, app.mouseY);
		switch(event.getID()){
		case MouseEvent.MOUSE_PRESSED:
			if(mouseOver && (focusIsWith == null || (focusIsWith != null && zOrder > focusIsWith.zOrder))){
				mdx = app.mouseX;
				mdy = app.mouseY;
				moffX = x - mdx;
				moffY = y - mdy;
				focusIsWith = this;
				// May become true but will soon be set to false when
				// we loose focus
				eventType = PRESSED;
				fireEvent();
				beingDragged = true;
			}
			break;
		case MouseEvent.MOUSE_CLICKED:
			if(focusIsWith == this){
				//				x = app.mouseX;
				//				y = app.mouseY;					
				// This sprite does not keep the focus when clicked
				focusIsWith = null;
				mdx = mdy = Integer.MAX_VALUE;
				eventType = CLICKED;
				fireEvent();
			}
			break;
		case MouseEvent.MOUSE_RELEASED:
			if(focusIsWith == this){
				if(mouseHasMoved(app.mouseX, app.mouseY)){
					mdx = mdy = Integer.MAX_VALUE;
					focusIsWith = null;
					beingDragged = false;
					eventType = RELEASED;
					fireEvent();
				}
			}
			break;
		case MouseEvent.MOUSE_DRAGGED:
			if(focusIsWith == this && draggable){
				beingDragged = true;
				PointD2D p = S4P.pixel2world(app.mouseX + moffX, app.mouseY + moffY);
				x = p.x;
				y = p.y;
				eventType = DRAGGED;
				fireEvent();
			}
			break;
		}
	}

	/**
	 * Draw this sprite
	 * 
	 */
	public void draw(){
		if(visible && !dead){
			app.pushMatrix();
			app.translate((float)x, (float)y);
			app.scale((float)scale);
			app.rotate((float)rot);
			app.imageMode(CENTER);
			app.image(frames[frameCurrent],0,0,(float)width,(float)height);
			if(S4P.collisionAreasVisible)
				drawCollisionArea();
			app.popMatrix();
		}
	}

	/**
	 * Draw the collision area that will be used for this area
	 * 
	 */
	protected void drawCollisionArea(){
		app.pushStyle();
		app.noStroke();
		app.fill(S4P.colColor);
		if(isOver(app.mouseX, app.mouseY)){
			app.tint(app.red(S4P.selColor),app.green(S4P.selColor),app.blue(S4P.selColor),0xff);
			app.fill(S4P.selColor);
		}

		if(rot != 0){
			app.ellipseMode(CENTER);
			app.ellipse(0, 0, (float)colRadius*2, (float)colRadius*2);
		}
		else if(scale != 1){
			app.rectMode(CENTER);
			app.rect(0, 0, (float)width, (float)height);
		}
		else {
			if(colFrames == null)
				calcCollisionImage();
			app.image(colFrames[frameCurrent], 0, 0, (float)width, (float)height);							
		}
		//		app.noTint();
		app.popStyle();
	}

	/**
	 * Used on the MOUSE_RELEASED event 
	 * @param x
	 * @param y
	 * @return
	 */
	protected boolean mouseHasMoved(int x, int y){
		return (mdx != x || mdy != y);
	}

	/**
	 * This method will test if a screen position is over a sprite. <br>
	 * @param mx mouse x co-ordinate
	 * @param my mouse x co-ordinate
	 * @return true if the position mx, my is over a sprite
	 */
	public boolean isOver(int mx, int my){
		// Calculate world position of mx, my
		PointD2D p = S4P.pixel2world(mx, my);
		if(rot != 0){
			return (p.x-x)*(p.x-x)+(p.y-y)*(p.y-y) < colRadius*colRadius*scale*scale;
		}
		else if(scale != 1){
			return(p.x>x-halfWidth*scale && p.x<x+halfWidth*scale 
					&& p.y>y-halfHeight*scale&& p.y<y+halfHeight*scale);
		}
		else if(Math.abs(p.x-x)<halfWidth && Math.abs(p.y-y)<halfHeight){
			PImage c = frames[frameCurrent];
			int xx = (int)(p.x-x+halfWidth);
			int yy = (int)(p.y-y+halfHeight);
			int pn = yy * c.width + xx;
			c.loadPixels();
			if(app.alpha(c.pixels[pn]) > ALPHALEVEL){
				return true;
			}
		}
		return false;
	}

	/**
	 * See if the sprite's collision circles overlap i.e. collision
	 * 
	 * @param spriteB
	 * @return true if sprite collision circles overlap (collision)
	 */
	public boolean cc_collision(Sprite spriteB) {
		if(!visible || !spriteB.visible) return false;

		double d;
		d = (x-spriteB.x)*(x-spriteB.x) + (y-spriteB.y)*(y-spriteB.y);
		d -= ((colRadius*scale+spriteB.colRadius*spriteB.scale)
				*(colRadius*scale+spriteB.colRadius*spriteB.scale));
		return (d < 0.0f);
	}

	/**
	 * See if the spites have a none transparent pixel that collide <br>
	 * A pixel is transparent if its alpha component < ALPHALEVEL
	 * @param spriteB
	 * @return true if the sprites collide at the pixel level
	 */
	
	/**
	 * This method makes a circular area of pixels centered around hit_x/hit_y 
	 * transparent. This works fine with sprites using per-pixel transparency.
	 *
	 * @param biteRadius
	 */
	public void bite(int biteRadius) {
		bite(hit_x, hit_y, biteRadius);
	}

	/**
	 * This method makes a circular area of pixels centered around x/y 
	 * transparent. This works fine with sprites using per-pixel transparency.
	 *
	 * @param x
	 * @param y
	 * @param biteRadius
	 */
	public void bite(int x, int y, int biteRadius) {
		int x1 = x - biteRadius;
		int y1 = y - biteRadius;
		int x2 = x + biteRadius;
		int y2 = y + biteRadius;
		if(x1 < 0) x1 = 0;
		if(y1 < 0) y1 = 0;
		if(x2 >= width) x2 = (int) width;
		if(y2 >= height) y2 = (int) height;

		int br2 = biteRadius * biteRadius;

		int imgWidth = (int)width;

		for(int h = 0; h < frames.length; h++) {
			frames[h].loadPixels();
			for(int px = x1; px < x2; px++) {
				for (int py = y1; py < y2; py++) {
					if((px-x)*(px-x) + (py-y)*(py-y) <= br2) {
						frames[h].pixels[px + py * imgWidth] &= 0x00ffffff;
					}
				}
			}
			frames[h].updatePixels();
		}
	}



	public void setAnimInterval(double interval){
		animTime = 0;
		nbrRepeats = Integer.MAX_VALUE;
		animInterval = interval;
	}

	public void setAnimInterval(double interval, int nrepeats){
		animTime = 0;
		nbrRepeats = nrepeats;
		animInterval = interval;
	}

	/**
	 * When there are multiple frames then this can be used to animate through
	 * some or all of the images. Animation is repeated for the number of
	 * times specified.
	 * 
	 * @param firstFrame start with this frame
	 * @param lastFrame go back to firstFrame after this frame
	 * @param interval time in seconds between frames
	 * @param nrepeats how many times to repaet this
	 */
	public void setFrameSequence(int firstFrame, int lastFrame){
		frameBegin = PApplet.constrain(firstFrame, 0, frames.length - 1);
		frameEnd = PApplet.constrain(lastFrame, 0, frames.length - 1);
		if(frameEnd > frameBegin){
			animDir = 1;
			frameCurrent = PApplet.constrain(frameCurrent, frameBegin, frameEnd);
		}
		else {
			animDir = -1;
			frameCurrent = PApplet.constrain(frameCurrent, frameEnd, frameBegin);
		}
	}

	/**
	 * Sets up the animation sequence.
	 * @param firstFrame first frame number in sequence
	 * @param lastFrame last frame in sequence
	 * @param interval time in seconds to display each frame
	 */
	public void setFrameSequence(int firstFrame, int lastFrame, double interval){
		setFrameSequence(firstFrame, lastFrame);
		animInterval = interval;
	}

	/**
	 * 
	 * @param firstFrame first frame number in sequence
	 * @param lastFrame last frame in sequence
	 * @param interval time in seconds to display each frame
	 * @param nrepeats number of times to repeat sequence
	 */
	public void setFrameSequence(int firstFrame, int lastFrame, double interval, int nrepeats){
		setFrameSequence(firstFrame, lastFrame);
		nbrRepeats = nrepeats;
		animInterval = interval;
	}

	/**
	 * Stop the image animation at the current frame
	 */
	public void stopImageAnim(){
		animInterval = 0.0f;
	}

	/**
	 * Returns true if the image is currently being animated
	 */
	public boolean isImageAnimating(){
		return (animInterval > 0.0f);
	}

	/**
	 * Sets the frame to be displayed - will stop image animation
	 * @param frameNo
	 */
	public void setFrame(int frameNo){
		animInterval = 0.0f;
		frameCurrent = PApplet.constrain(frameNo, 0, frames.length);	
	}

	/**
	 * Get the frame number for the current frame.
	 */
	public int getFrame(){
		return frameCurrent;
	}

	/**
	 * Rotate the image
	 * @param d s
	 */
	public void setRot(double d){
		this.rot = d;
	}

	/**
	 * Get the sprite's rotation angle
	 * 
	 * @return rotation angle in radians
	 */
	public double getRot(){
		return rot;
	}

	/**
	 * Set the scale the image is to be displayed at.
	 * 
	 * @param scale
	 */
	public void setScale(float scale){
		this.scale = scale;
	}

	/**
	 * Get the scale used for sizing the sprite.
	 */
	public double getScale(){
		return scale;
	}

	/**
	 * Set the sprite's world x/y position
	 * 
	 * @param x
	 * @param y
	 */
	public void setXY(double x, double y){
		this.x = x;
		this.y = y;
	}

	/**
	 * Set the sprite's world x position
	 * @param x
	 */
	public void setX(double x){
		this.x = x;
	}

	/**
	 * Set the sprite's world y position
	 * @param y
	 */
	public void setY(double y){
		this.y = y;
	}

	/**
	 * Get the scaled height
	 */
	public double getHeight(){
		return height * scale;
	}

	/**
	 * Get the scaled width
	 */
	public double getWidth(){
		return width * scale;
	}

	/**
	 * Set the sprite's visibility
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible){
		this.visible = visible;
	}

	/**
	 * Is this sprite visible?
	 */
	public boolean isVisible(){
		return visible;
	}

	/**
	 * Sets the order that sprites are drawn the low number are drawn before
	 * high numbers. This method will assume that you want to sort them 
	 * immediately after the 
	 * 
	 * @param zOrder  the new z order value
	 */
	public void setZorder(int zOrder){
		this.zOrder = zOrder;
		S4P.sortZorder();
	}

	/**
	 * Can change the z order value woth/without resorting
	 * @param zOrder the new z order value
	 * @param sort
	 */
	public void setZorder(int zOrder, boolean sort){
		this.zOrder = zOrder;
		if(sort)
			S4P.sortZorder();
	}

	/**
	 * get the current zorder
	 * @return the current z order value
	 */
	public int getZorder(){
		return zOrder;
	}

	public int compareTo(Object o) {
		return this.zOrder.compareTo(((Sprite)o).zOrder);
	}

}
