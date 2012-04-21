package simulation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import processing.core.PApplet;

public class ApplicationWrapper extends Frame {
	
     public ApplicationWrapper() {
         super("Boids");
         
       //Fullscreen support
        if( Set.SHOW_Fullscreen ) {
         	 
         	 // Gets a handle on the screen
         	 GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
             if (gd.isFullScreenSupported()) { // if we can do fullscreen...
                 
            	 setUndecorated(true);         // ... remove window titlebar
            	 gd.setFullScreenWindow(this); // .. and make it fullscreen
            	 
            	 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            	 setBounds(0, 0, screenSize.width, screenSize.height);
            	 
             } else {
                 System.err.println("Full screen not supported");
            	 setResizable(false); // We don't yet support live resizing. Have to restart
                 setBounds(0, 0, Set.screen_Width, Set.screen_Height);
             }
         	 
         } else { // No fullscreen
        	 setResizable(false); // We don't yet support live resizing. Have to restart
         	 setBounds(0, 0, Set.screen_Width, Set.screen_Height);
         }

         PApplet embed = new Sim(this);
         add(embed, BorderLayout.CENTER);

         // important to call this whenever embedding a PApplet.
         // It ensures that the animation thread is started and
         // that other internal variables are properly set.
         embed.init();
     }
            
      public static void main(String[] s) {
         new ApplicationWrapper().setVisible(true);
    }
 }

/*
 * I tried to decide fullscreen or no based on settings, but I couldn't initialize the Sim.
 * The code below was my attempt. TODO
 */
/*
//Fullscreen support
if( Set.SHOW_Fullscreen ) {
	 
	 // Gets a handle on the screen
	 GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    if (gd.isFullScreenSupported()) { // if we can do fullscreen...
        
   	 setUndecorated(true);         // ... remove window titlebar
   	 gd.setFullScreenWindow(this); // .. and make it fullscreen
   	 
   	 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
   	 setBounds(0, 0, screenSize.width, screenSize.height);
   	 
    } else {
        System.err.println("Full screen not supported");
        setBounds(0, 0, Set.screen_Width, Set.screen_Height);
    }
	 
} else { // No fullscreen
	 setBounds(0, 0, Set.screen_Width, Set.screen_Height);
}
*/