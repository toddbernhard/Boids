package simulation;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import processing.core.PApplet;

public class ApplicationWrapper extends Frame {
	
     public ApplicationWrapper() {
         super("Boids");
         
         GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
         if (gd.isFullScreenSupported()) {
             setUndecorated(true);
        	 gd.setFullScreenWindow(this);
         } else {
             System.err.println("Full screen not supported");
         }

         setLayout(new BorderLayout());
         setBounds(0, 0, Set.SCREEN_Width, Set.SCREEN_Height);

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