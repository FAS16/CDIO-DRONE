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
	private String nextPort; // Husk at implementere dette
	private boolean tagLost;
	private Command latestCommand;
	private int portCounter;
	private boolean tagSpotted;

	
	final int SPEED = 5; //10
	final int HOVER = 2500; 
	final int DURATION = 120; //100

	public AutonomousController(IARDrone drone) {
		super(drone);
		latestCommand = null;
		this.cmd = drone.getCommandManager();
		portsToFind.add("P.00");
		portsToFind.add("P.01");
		portsToFind.add("P.02");
		portsToFind.add("P.03");
		portsToFind.add("P.04");
//		portsToFind.add("P.05");
		portsToFind.add("P.06");
		portCounter = 0;
		nextPort = portsToFind.get(portCounter);
		System.out.println("AutonomousController:** Next port is "+ nextPort +" **");

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

		// ResultPoint[] points = tag.getResultPoints();
		Point centerOfQR = getCenterOfQr();
		boolean isCentered = ((centerOfQR.x > (imgCenterX - DroneMain.TOLERANCE))
				&& (centerOfQR.x < (imgCenterX + DroneMain.TOLERANCE))
				/*&& (centerOfQR.y > (imgCenterY - DroneMain.TOLERANCE))
				&& (centerOfQR.y < (imgCenterY + DroneMain.TOLERANCE))*/);

		System.out.println("AutonomousController: Is tag centered ? " + isCentered);

		return isCentered;
	}

	public Point getCenterOfQr() {
		Point center = null;
		ResultPoint[] points = tag.getResultPoints();

		float horiDistance = points[2].getX() - points[1].getX();
		float xOfCenter = points[1].getX() + (horiDistance / 2);

		float vertDistance = points[1].getY() - points[0].getY();
		float yOfCenter = points[0].getY() + (vertDistance / 2);

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
		// stateController = new StateController(this, drone,
		// drone.getCommandManager());
		// stateController.state = CurrentState.h;
		while (!doStop) // control loop
		{

			System.out.print(".");
			try {
				
				if(tag == null) {
//					searchForQr();
					
					System.out.println("AutonomousController: NO TAG FOUND - going FORWARD:");
					cmd.forward(10).doFor(185); // 10 - 185;
					cmd.hover().doFor(2000);
					cmd.hover();
					this.sleep(2500);
					}
				
				// reset if too old (and not updated), so it nullifies the tag
				if ((tag != null) && (System.currentTimeMillis() - tag.getTimestamp() > 1500)) {
					System.out.println("AutonomousController: Resetting tag");
					tag = null;
				}

				if (tag != null) {
					tagSpotted = true;
					tagLost = false;
					System.out.println("AutonomousController: QR-TAG FOUND - Trying to center");
					centralizeQR();
					this.sleep(3500);

				} else if (tag == null && latestCommand != null) {
					tagLost = true;
					// Drone lost, redo!
//					searchForLostQr();
				} 
//				else /*if (tag == null)*/ {
////					searchForQr();
//					System.out.println("AutonomousController: NO TAG FOUND - going FORWARD:");
//					cmd.forward(10).doFor(165);
//					cmd.hover().doFor(2000);
//					cmd.hover();
//					this.sleep(2000);
//				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void searchForQr() throws InterruptedException {
		
		boolean change = false;
		
		if(!change) {
			cmd.forward(25).doFor(75);
			cmd.hover().doFor(5000);
			this.sleep(1500);
			change = !change;
		} else if(change) {
			cmd.backward(25).doFor(75);
			cmd.hover().doFor(5000);
			this.sleep(1500);
			change = !change;
		}
		
	}

	private void searchForLostQr() throws InterruptedException {
		if (latestCommand == Command.FORWARD) {
			cmd.backward(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER/2);
			cmd.goRight(SPEED).doFor(DURATION);
			cmd.goLeft(SPEED).doFor(DURATION);
			cmd.hover();
//			this.sleep(1500);
//			this.latestCommand = null;

		} else if (latestCommand == Command.BACKWARD) {
			cmd.forward(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER/2);
			cmd.goRight(SPEED).doFor(DURATION);
			cmd.goLeft(SPEED).doFor(DURATION);
			cmd.hover();
//			this.sleep(1500);
//			this.latestCommand = null;

		} else if (latestCommand == Command.RIGHT) {
			cmd.backward(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER/2);
			cmd.goLeft(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
			cmd.hover();
//			this.sleep(1500);
//			this.latestCommand = Command.RIGHT;
		
		} else if (latestCommand == Command.LEFT) {
			cmd.backward(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER/2);
			cmd.goRight(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(1500);
//			this.latestCommand = null;

		} else if (latestCommand == Command.UP) {
			cmd.down(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(1500);
//			this.latestCommand = null;

		

		} else if (latestCommand == Command.DOWN) {
			cmd.up(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(1500);
//			this.latestCommand = null;		

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
		System.out.println("Coordinates of camera center: (" + cameraCenter.x + ", " + cameraCenter.y + ")");

		// Tag (QR) center point
		// double xOfQr = points[1].getX();
		// double yOfQr = points[1].getY();
		// Point qrCenter = new Point(xOfQr, yOfQr);
		Point qrCenter = getCenterOfQr();
		System.out.println("Coordinates of QR-TAG center: (" + qrCenter.x + ", " + qrCenter.y + ")");

		// Moving drone according to QR and it's center
		
		
		 


		
		
		 if (qrCenter.x < (cameraCenter.x - DroneMain.TOLERANCE) && getTagSize() > 30) {
			System.out.println("AutonomousController: going LEFT towards QR-tag");
			// Go left
			cmd.goLeft(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(500);
			this.latestCommand = Command.LEFT;
			//
		} else if (qrCenter.x > (cameraCenter.x + DroneMain.TOLERANCE)  && getTagSize() > 30) {
			System.out.println("AutonomousController: going RIGHT towards QR-tag");
			// Go right
			cmd.goRight(SPEED).doFor(DURATION);
			cmd.hover().doFor(HOVER);
//			this.sleep(500);
			this.latestCommand = Command.RIGHT;
		}
		
		else if (getTagSize() < 35) {
				System.out.println("AutonomousController: TAG SIZE = " + getTagSize());
				System.out.println("AutonomousController: going FORWARD towards QR-tag");
				cmd.forward(SPEED).doFor(DURATION);
				cmd.hover().doFor(HOVER);
//				this.sleep(500);
				this.latestCommand = Command.FORWARD;

			} else if (getTagSize() > 50 && getTagSize() < 70) {

				System.out.println("AutonomousController: TAG SIZE = " + getTagSize());
				System.out.println("AutonomousController: TOO CLOSE going BACKWARDS away from QR-tag");
				cmd.backward(SPEED).doFor(DURATION);
				cmd.hover().doFor(HOVER);
//				this.sleep(500);
				this.latestCommand = Command.BACKWARD;

			} 
//		else if (qrCenter.y < (cameraCenter.y - DroneMain.TOLERANCE)) {
//			System.out.println("AutonomousController: going UP towards QR-tag");
//			// Go up
//			cmd.up(SPEED).doFor(DURATION);
//			cmd.hover().doFor(HOVER);
////			this.sleep(500);
//			this.latestCommand = Command.UP;
//
//		} else if (qrCenter.y > (cameraCenter.y + DroneMain.TOLERANCE)) {
//			System.out.println("AutonomousController: going DOWN towards QR-tag");
//			// Go down
//			cmd.down(SPEED).doFor(DURATION);
//			cmd.hover().doFor(HOVER);
////			this.sleep(500);
//			this.latestCommand = Command.DOWN;

//		}
	else if (isTagCentered()) {
			cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 1);
			System.out.println("AutonomousController: TAG CENTERED");
			flyThroughPort();
			
		}

	}
	
	private void alignWithQr() {
		Point center = null;
		ResultPoint[] points = tag.getResultPoints();
		float horiDistance = points[2].getX() - points[1].getX();
		float vertDistance = points[1].getY() - points[0].getY();
		float y1 = points[1].getY();
		float y2 = points[2].getY();

//		System.out.println("Horizontal distance: " + horiDistance);
//		System.out.println("Vertical distance: " + vertDistance);
		
		System.out.println("Upper left y = " + y1);
		System.out.println("Upper right y = " + y2);
	}

	public void flyThroughPort() {
	
		if(getTag().getText().trim().equals(nextPort)) {
		System.out.println("AutonomousController: Port validated, access given to fly through");
		cmd.up(50 * 2).doFor(140); //-140
//		setAltitude(1300);
		cmd.hover().doFor(3000);
//		cmd.spinRight(10).doFor(20); // HUUUUSK
		cmd.forward(25).doFor(195); //215 - 20 - 200 - 225 - 25
		cmd.hover().doFor(3000);
		cmd.hover();
		cmd.down(50).doFor(200); //- 200
//		cmd.goRight(40).doFor(40).hover(); // kom væk fra bænken til venstre
		
//		if(nextPort.equals(portsToFind.get(0))) {
//			cmd.down(30).doFor(30);
//			cmd.hover().doFor(2000);
//			cmd.hover();
//			System.out.println("AutonomousController: 1");
//			cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 1);
//		}
//		else {
//			setAltitude(900);	
//			System.out.println("AutonomousController: 2");
//			cmd.setLedsAnimation(LEDAnimation.BLINK_ORANGE, 10, 1);
//		}
//		

		
		updateNextPort();
		tagSpotted = false;
		
		 }
		
		// }
	}
	
	private void updateNextPort() {
		++portCounter;
		nextPort = portsToFind.get(portCounter);
		System.out.println("AutonomousController:** Next port is "+ nextPort +" **");
		// Måske noget if til at spinne mere hvis det er p.02
		
		cmd.spinRight(50).doFor(40).hover();
	}
	
	public void adjustDrone() {
		
	}

	public double getTagSize() {
		if (tag != null) {
			ResultPoint[] points = tag.getResultPoints();
			return points[2].getX() - points[1].getX();
		} else
			return 0.0;
	}
	

	public void setAltitude(int altitude) {
		System.out.println("AutonomousController: setAltitude() START: " + altitude);
		while (true) {
			// Decrease altitude
			if (altitude + 50 < getAltitude()) { 
				cmd.down(30).doFor(30).hover();
				// Increase altitude
			} else if (altitude - 50 > getAltitude()) { 
				cmd.up(30).doFor(30).hover();
				
			} else {
				System.out.println("AutonomousController: setAltitude() DONE - Altitude = " + getAltitude()); // done
				return;
			}
		}
	}

}