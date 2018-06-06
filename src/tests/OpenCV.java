package tests;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.video.ImageListener;

public class OpenCV extends JFrame {
	
private BufferedImage image = null;
    
    public OpenCV(final IARDrone drone){
        super("YADrone Tutorial");
        
        setSize(640,360);
        setVisible(true);
        
        drone.getVideoManager().addImageListener(new ImageListener() {
            public void imageUpdated(BufferedImage newImage)
            {
                image = newImage;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        repaint();
                    }
                });
            }
        });

        
        
    }
    
    public void paint(Graphics g)
    {
        if (image != null)
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
    }
	
	

}
