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

	private Result tag;
	private float tagOrientation;
	private ArrayList<String> portsToFind = new ArrayList<String>();
	
	protected double latestImgTime; // Not used yet
	private StateController stateController;
	private int altitude;
	private CommandManager cmd;
	private String nextQr; // Husk at implementere dette

	public AutonomousController(IARDrone drone) {
		super(drone);
		this.cmd = drone.getCommandManager();
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
	 * A tag is centered when Point 1 (the upper left) is near the center of the
	 * camera
	 */
	public boolean isTagCentered() {
		System.out.println("AutonomousController: Checking if tag is centered");

		if (tag == null)
			return false;

		int imgCenterX = DroneMain.IMG_WIDTH / 2;
		int imgCenterY = DroneMain.IMG_HEIGHT / 2;

//		ResultPoint[] points = tag.getResultPoints();
		Point centerOfQR = getCenterOfQr();
		boolean isCentered = ((centerOfQR.x > (imgCenterX - DroneMain.TOLERANCE))
				&& (centerOfQR.x < (imgCenterX + DroneMain.TOLERANCE))
				&& (centerOfQR.y > (imgCenterY - DroneMain.TOLERANCE))
				&& (centerOfQR.y < (imgCenterY + DroneMain.TOLERANCE)));

		System.out.println("AutonomousController: Is tag centered ? " + isCentered);

		return isCentered;
	}
	
	public Point getCenterOfQr() {
		Point center = null;
		
		ResultPoint[] points = tag.getResultPoints();
		
		float horiDistance = points[2].getX() - points[1].getX();
		
		float xOfCenter = points[1].getX() + (horiDistance/2);
		
		float vertDistance = points[1].getY() - points[0].getY();
		
		float yOfCenter = points[0].getY() + (vertDistance/2);
		
		center = new Point(xOfCenter, yOfCenter);
		
		return center;
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
			
			System.out.print(".");
			try {
				// reset if too old (and not updated)
				if ((tag != null) && (System.currentTimeMillis() - tag.getTimestamp() > 2000)) {
					System.out.println("AutonomousController: Resetting tag");
					tag = null;
				}

				else if (tag != null) {
					System.out.println("AutonomousController: QR-TAG FOUND");
					if (!isTagCentered()) {
						System.out.println("AutonomousController: QR-TAG is not centered - Trying to center");
						centralizeQR();

					} 

				}
				
//				if(tag == null) {
//					
//					System.out.println("AutonomousController: Tag is NULL");
//				}

			} catch (Exception e) {
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

	public void centralizeQR() throws InterruptedException {

		final int SPEED = 40;
		final int HOVER = 3000;
		final int DURATION = 40;

		ResultPoint[] points;
		String qrText;
		double distanceFromMiddle;

		synchronized (tag) {
			points = tag.getResultPoints();
			qrText = tag.getText();
		}

		// Camera center point
		double xOfCenter = DroneMain.IMG_WIDTH / 2;
		double yOfCenter = DroneMain.IMG_HEIGHT / 2;
		Point cameraCenter = new Point(xOfCenter, yOfCenter);
		System.out.println("Coordinates of camera center: ("+ cameraCenter.x +", "+ cameraCenter.y+")");

		// Tag (QR) center point
//		double xOfQr = points[1].getX();
//		double yOfQr = points[1].getY();
//		Point qrCenter = new Point(xOfQr, yOfQr);
		Point qrCenter = getCenterOfQr();
		System.out.println("Coordinates of QR-TAG center: ("+ qrCenter.x +", "+ qrCenter.y+")");
		
		// Moving drone according to QR and it's center
		
		if(getTagSize() < 50) {
			System.out.println("AutonomousController: TAG SIZE = "+ getTagSize());
			System.out.println("AutonomousController: going FORWARD towards QR-tag");
			cmd.forward(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(500);
			
		}
		 else if (qrCenter.x < (cameraCenter.x - DroneMain.TOLERANCE)) {
			System.out.println("AutonomousController: going LEFT towards QR-tag");
			// Go left
			cmd.goLeft(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(500);
			
		} else if (qrCenter.x > (cameraCenter.x + DroneMain.TOLERANCE)) {
			System.out.println("AutonomousController: going RIGHT towards QR-tag");
			// Go right
			cmd.goRight(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(500);
		} 
		
		else if (qrCenter.y < (cameraCenter.y - DroneMain.TOLERANCE)) {
			 System.out.println("AutonomousController: going UP towards QR-tag");
			// Go up
			cmd.up(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(500);
			
			
		} else if (qrCenter.y > (cameraCenter.y + DroneMain.TOLERANCE)) {
			System.out.println("AutonomousController: going DOWN towards QR-tag");
			// Go down
			cmd.down(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(500);
			
		}
		if(isTagCentered()) {
			System.out.println("AutonomousController: TAG CENTERED");
			flyThroughPort(qrText);
			drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);
		}

	}

	public void flyThroughPort(String qrText) {
		
//		if(qrText.equals(nextQr)) {
		System.out.println("AutonomousController: READY TO FLY THROUGH PORT");
		cmd.up(50*2).doFor(100);
		cmd.hover().doFor(2000);
		cmd.forward(50).doFor(200);
		cmd.hover().doFor(2000);
		cmd.landing();
//		}
		}
	
	public double getTagSize() {
		if (tag != null){
			ResultPoint[] points = tag.getResultPoints();
			return points[2].getX() - points[1].getX();
		}			
		else
			return 0.0;
	}

	private void centerTag() throws InterruptedException {

		final int SPEED = 5;
		final int SLEEP = 2000;
		final int DURATION = 500;

		String tagText;
		ResultPoint[] points;

		synchronized (tag) {
			points = tag.getResultPoints();
			tagText = tag.getText();
		}

		int imgCenterX = DroneMain.IMG_WIDTH / 2;
		int imgCenterY = DroneMain.IMG_HEIGHT / 2;

		float x = points[1].getX();
		float y = points[1].getY();

		if ((tagOrientation > 10) && (tagOrientation < 180)) {
			System.out.println("AutonomousController: Spin left");
			drone.getCommandManager().spinLeft(SPEED * 2).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		} else if ((tagOrientation < 350) && (tagOrientation > 180)) {
			System.out.println("AutonomousController: Spin right");
			drone.getCommandManager().spinRight(SPEED * 2).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		} else if (x < (imgCenterX - DroneMain.TOLERANCE)) {
			System.out.println("AutonomousController: Go left");
			drone.getCommandManager().goLeft(SPEED).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		} else if (x > (imgCenterX + DroneMain.TOLERANCE)) {
			System.out.println("AutonomousController: Go right");
			drone.getCommandManager().goRight(SPEED).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		} else if (y < (imgCenterY - DroneMain.TOLERANCE)) {
			System.out.println("AutonomousController: Go forward");
			drone.getCommandManager().forward(SPEED).doFor(DURATION);
			Thread.currentThread().sleep(SLEEP);
		} else if (y > (imgCenterY + DroneMain.TOLERANCE)) {
			System.out.println("AutonomousController: Go backward");
			drone.getCommandManager().backward(SPEED).doFor(DURATION);
			drone.getCommandManager().hover();
			Thread.currentThread().sleep(SLEEP);
		} else {
			System.out.println("AutonomousController: Tag centered");
			drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);

		}
	}

}
