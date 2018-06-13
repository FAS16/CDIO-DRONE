package drone;

import java.awt.image.BufferedImage;

import java.util.ArrayList;

import org.opencv.core.Point;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.video.ImageListener;

import imgprocessing.*;

public class AutonomousController extends AbstractController implements TagListener, ImageListener {
	private final static int SPEED = 5;
	private final static int SLEEP = 2000;
	private final static int DURATION = 500;
	
	private Result tag;
	private ArrayList<String> portsToFind = new ArrayList<String>();
	private float tagOrientation;
	protected double latestImgTime; // Not used yet
	private StateController stateController;
	private int altitude;

	public AutonomousController(IARDrone drone) {
		super(drone);

		portsToFind.add("P.00");
		portsToFind.add("P.01");
		portsToFind.add("P.02");
		portsToFind.add("P.03");
		portsToFind.add("P.04");
		portsToFind.add("P.05");
		portsToFind.add("P.06");

		setAltitudeListener();
	}

	@Override
	public void imageUpdated(BufferedImage image) {
		this.latestImgTime = System.currentTimeMillis();

	}

	/*
	 * A tag i centered when Point 1 (the upper left) is near the center of the
	 * camera
	 */
	public boolean isTagCentered() {
		System.out.println("AutonomousController: Checking if tag is centered");

		if (tag == null)
			return false;

		int imgCenterX = DroneMain.IMG_WIDTH / 2;
		int imgCenterY = DroneMain.IMG_HEIGHT / 2;

		ResultPoint[] points = tag.getResultPoints();
		boolean isCentered = ((points[1].getX() > (imgCenterX - DroneMain.TOLERANCE))
				&& (points[1].getX() < (imgCenterX + DroneMain.TOLERANCE))
				&& (points[1].getY() > (imgCenterY - DroneMain.TOLERANCE))
				&& (points[1].getY() < (imgCenterY + DroneMain.TOLERANCE)));

		System.out.println("AutonomousController: Is tag centered ? " + isCentered);

		return isCentered;
	}

	@Override
	public void onTag(Result result, float orientation) {
		if (result == null) {
			return;
		}

		tag = result;
		tagOrientation = orientation;

	}

	@Override
	public void run() {
		this.doStop = false;
		stateController = new StateController(this, drone, drone.getCommandManager());
		// stateController.state = CurrentState.h;
		while (!doStop) // control loop
		{
			try {
				// reset if too old (and not updated)
				if ((tag != null) && (System.currentTimeMillis() - tag.getTimestamp() > 2000)) {
					System.out.println("Resetting tag");
					tag = null;
				}

				if (tag != null) {
					if (!isTagCentered()) {
						System.out.println("AutonomousController: Tag is not centered - Trying to center");
						centerTag();
						
					}

				}

//				stateController.handleState(stateController.state);
				this.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private void setAltitudeListener() {
		drone.getNavDataManager().addAltitudeListener(new AltitudeListener() {
			@Override
			public void receivedAltitude(int a) {
				altitude = a;
			}

			@Override
			public void receivedExtendedAltitude(Altitude d) {
			}
		});
	}

	public int getAltitude() {
		return altitude;
	}

	public float getTagOrientation() {
		return tagOrientation;
	}

	public Result getTag() {
		return tag;
	}

	
	private void centerTag() throws InterruptedException
	{
		String tagText;
		ResultPoint[] points;
		
		synchronized(tag)
		{
			points = tag.getResultPoints();	
			tagText = tag.getText();
		}
		
		int imgCenterX = DroneMain.IMG_WIDTH/ 2;
		int imgCenterY = DroneMain.IMG_HEIGHT / 2;
		
		float x = points[1].getX();
		float y = points[1].getY();
		
		if ((tagOrientation > 10) && (tagOrientation < 180))
		{
			System.out.println("AutonomousController: Spin left");
			drone.getCommandManager().spinLeft(SPEED * 2).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		}
		else if ((tagOrientation < 350) && (tagOrientation > 180))
		{
			System.out.println("AutonomousController: Spin right");
			drone.getCommandManager().spinRight(SPEED * 2).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		}
		else if (x < (imgCenterX - DroneMain.TOLERANCE))
		{
			System.out.println("AutonomousController: Go left");
			drone.getCommandManager().goLeft(SPEED).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		}
		else if (x > (imgCenterX + DroneMain.TOLERANCE))
		{
			System.out.println("AutonomousController: Go right");
			drone.getCommandManager().goRight(SPEED).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		}
		else if (y < (imgCenterY - DroneMain.TOLERANCE))
		{
			System.out.println("AutonomousController: Go forward");
			drone.getCommandManager().forward(SPEED).doFor(DURATION);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (y > (imgCenterY + DroneMain.TOLERANCE))
		{
			System.out.println("AutonomousController: Go backward");
			drone.getCommandManager().backward(SPEED).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		}
		else
		{
			System.out.println("AutonomousController: Tag centered");
			drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);
			
		}
	}
	
	public void centralize() throws InterruptedException {
		
	}

}
