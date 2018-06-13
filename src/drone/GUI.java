package drone;

import de.yadrone.apps.paperchase.PaperChase;
import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.VideoChannel;
import de.yadrone.base.navdata.BatteryListener;
import de.yadrone.base.video.ImageListener;
import imgprocessing.OpenCVListener;
import imgprocessing.TagListener;

import javax.swing.*;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import org.opencv.core.MatOfPoint;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fahadali on 06-06-2018.
 */

public class GUI extends JFrame implements ImageListener, TagListener, OpenCVListener {
	
	private BufferedImage image = null;
	private Result result;
	private List<MatOfPoint> points;
	private IARDrone drone;
	private DroneMain main;
	private JPanel videoPanel;
	private String orientation;

	private int batteryPercentage;

	public GUI(final ARDrone drone, DroneMain main) {
		super("Drone");
		this.drone = drone;
		this.main = main;
		setBatteryListener();
		
		setSize(640, 360);
		setVisible(true);

		setMouseListener();
		setWindowListener();
		
		setLayout(new GridBagLayout());

		add(createVideoPanel(), new GridBagConstraints(0, 0, 1, 2, 1, 1, GridBagConstraints.FIRST_LINE_START,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pack();


//		drone.getVideoManager().addImageListener(new ImageListener() {
//			public void imageUpdated(BufferedImage newImage) {
//				image = newImage;
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						repaint();
//					}
//				});
//			}
//		});

	}
	
	private JPanel createVideoPanel() {
		
		videoPanel = new JPanel() {
			
			private Font font = new Font("SansSerif", Font.BOLD, 14);
			
			public void paint(Graphics g) {
				
				if(image != null) {
					
					// Drawing the camera image
					g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
					
					// Draw battery percentage
					g.setColor(Color.GREEN);
					g.setFont(font);
					g.drawString("Battery: " + batteryPercentage + "%", 0, 25);
					
					// Draw tolerance field (rectangle)
					g.setColor(Color.RED);
					
					int imgCenterX = DroneMain.IMG_WIDTH / 2;
    				int imgCenterY = DroneMain.IMG_HEIGHT / 2;
    				int tolerance = DroneMain.TOLERANCE;
    				
    				g.drawPolygon(new int[] {imgCenterX-tolerance, imgCenterX+tolerance, imgCenterX+tolerance, imgCenterX-tolerance}, 
						      		  new int[] {imgCenterY-tolerance, imgCenterY-tolerance, imgCenterY+tolerance, imgCenterY+tolerance}, 4);
    				
    				// Draw triangle if QR code is visible
					if(points != null){
						Object[] objects = points.toArray();

						// Printing array of objects
						for (Object obj : objects)
							System.out.print(obj + " ");
					}
    				if(result != null) {
    					
    					ResultPoint[] points = result.getResultPoints();
						ResultPoint a = points[1]; // top-left
						ResultPoint b = points[2]; // top-right
						ResultPoint c = points[0]; // bottom-left
						ResultPoint d = points.length == 4 ? points[3] : points[0]; // alignment
																					// point
																					// (bottom-right)

						g.setColor(Color.GREEN);

						g.drawPolygon(new int[] {(int)a.getX(),(int)b.getX(),(int)d.getX(),(int)c.getX()}, 
	  						      new int[] {(int)a.getY(),(int)b.getY(),(int)d.getY(),(int)c.getY()}, 4);
						
						g.setColor(Color.RED);
        				g.setFont(font);
        				g.drawString(result.getText(), (int)a.getX(), (int)a.getY());
        				g.drawString(orientation, (int)a.getX(), (int)a.getY() + 20);
        				
        				//  Nødvendigt?
        				if ((System.currentTimeMillis() - result.getTimestamp()) > 1000) {
							result = null;
						}
    					
    				}
					
				}
				
				else {
					// draw "Waiting for image"
					g.setColor(Color.RED);
					g.setFont(font);
					g.drawString("Waiting for image ...", 10, 20);
				}
				
			}
			
			
			
		};
		
		videoPanel.setSize(DroneMain.IMG_WIDTH, DroneMain.IMG_HEIGHT);
        videoPanel.setMinimumSize(new Dimension(DroneMain.IMG_WIDTH, DroneMain.IMG_HEIGHT));
        videoPanel.setPreferredSize(new Dimension(DroneMain.IMG_WIDTH, DroneMain.IMG_HEIGHT));
        videoPanel.setMaximumSize(new Dimension(DroneMain.IMG_WIDTH, DroneMain.IMG_HEIGHT));
		
		return videoPanel;
	}
	
	private long imageCount = 0;
	
	public void imageUpdated(BufferedImage newImage)
    {
		if ((++imageCount % 2) == 0)
			return;
		
    	image = newImage;
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				videoPanel.repaint();
			}
		});
    }
	
	@Override
	public void onTag(Result result, float v) {
		if (result != null) {
			this.result = result;
			this.orientation = v + " deg";
		}
	}
	
	

//	public void paint(Graphics g) {
//		if (image != null)
//			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
//	}

	private void setBatteryListener() {
		drone.getNavDataManager().addBatteryListener(new BatteryListener() {

			public void batteryLevelChanged(int percentage) {
				batteryPercentage = percentage;
			}

			@Override
			public void voltageChanged(int vbat_raw) {
			}
		});
	}

	private void setMouseListener() {

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				drone.getCommandManager().setVideoChannel(VideoChannel.NEXT);
			}
		});

	}

	private void setWindowListener() {

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				drone.stop();
				System.exit(0);
			}
		});

	}

	@Override
	public void onRect(List<MatOfPoint> points) {
		if (points != null) {
			this.points = points;
		}

	}
}