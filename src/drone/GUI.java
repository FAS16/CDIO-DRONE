package drone;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.VideoChannel;
import de.yadrone.base.navdata.BatteryListener;
import de.yadrone.base.video.ImageListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Created by fahadali on 06-06-2018.
 */

public class GUI extends JFrame {
	private BufferedImage image = null;
	private IARDrone drone;

	private int batteryPercantage;

	public GUI(final ARDrone drone) {
		super("Drone");
		this.drone = drone;
		setBatteryListener();

		setSize(640, 360);
		setVisible(true);

		setMouseListener();
		setWindowListener();

		drone.getVideoManager().addImageListener(new ImageListener() {
			public void imageUpdated(BufferedImage newImage) {
				image = newImage;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						repaint();
					}
				});
			}
		});

	}

	public void paint(Graphics g) {
		if (image != null)
			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
	}

	private void setBatteryListener() {
		drone.getNavDataManager().addBatteryListener(new BatteryListener() {

			public void batteryLevelChanged(int percentage) {
				batteryPercantage = percentage;
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

}