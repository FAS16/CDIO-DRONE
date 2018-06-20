package drone;

import org.opencv.core.Core;

import de.yadrone.apps.paperchase.controller.PaperChaseKeyboardController;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.exception.ARDroneException;
import de.yadrone.base.exception.IExceptionListener;
import de.yadrone.base.navdata.AttitudeListener;
import imgprocessing.CircleDetector;
import imgprocessing.QRCodeScanner;

public class DroneMain {

	public final static int TOLERANCE = 35;
	public final static int IMG_WIDTH = 640;
	public final static int IMG_HEIGHT = 360;
	

	private QRCodeScanner qrCodescanner;
	private CircleDetector circleDetector;
	private IARDrone drone = null;
	private GUI gui;
	private AutonomousController controller;
	private boolean autonomousControl;

	public DroneMain() {
		drone = new ARDrone();
		drone.start();
		drone.getCommandManager().setMaxAltitude(1750);

		gui = new GUI((ARDrone) drone, this);

		// keyboard controller is always enabled and cannot be disabled (for safety
		// reasons)
		// Ships with Yadrone
		PaperChaseKeyboardController keyboardController = new PaperChaseKeyboardController(drone);
		keyboardController.start();

		controller = new AutonomousController(drone);

		this.qrCodescanner = new QRCodeScanner();
		this.qrCodescanner.addListener(gui);

		this.circleDetector = new CircleDetector();
		this.circleDetector.addListener(gui);

		drone.getVideoManager().addImageListener(controller);
		drone.getVideoManager().addImageListener(gui);
		drone.getVideoManager().addImageListener(qrCodescanner);
		drone.getVideoManager().addImageListener(circleDetector);

		drone.getNavDataManager().addAttitudeListener(new AttitudeListener() {
			public void attitudeUpdated(float pitch, float roll, float yaw) {
			}

			@Override
			public void attitudeUpdated(float pitch, float roll) {
			}

			@Override
			public void windCompensation(float pitch, float roll) {
			}
		});

		drone.addExceptionListener(new IExceptionListener() {
			public void exeptionOccurred(ARDroneException exc) {
				exc.printStackTrace();
			}
		});

		final int SPEED = 40;
		final int SLEEP = 3000;
		final int DURATION = 50;

		drone.getCommandManager().takeOff();
		drone.getCommandManager().hover().doFor(2000); // Must hover here, or else it will float around
		try {
			Thread.sleep(6000);
		} catch (InterruptedException ex) {
		}
		 this.enableAutoControl(true);

		// drone.getCommandManager().up(30).doFor(30);
		// controller.setAltitude(900);
		//

		// try { Thread.sleep(SLEEP); } catch (InterruptedException ex) { }
		// drone.getCommandManager().hover().doFor(4000);
		// drone.getCommandManager().up(SPEED*3).doFor(DURATION);
		// drone.getCommandManager().hover().doFor(2000);
		// drone.getCommandManager().forward(50).doFor(100);
		// drone.getCommandManager().hover(); // Husk at hover efter hver kommando

	}

	public void enableAutoControl(boolean enable) {
		System.out.println("MasterDrone enableAutoControler: " + enable);
		if (enable) {
			qrCodescanner.addListener(controller);
			controller.start(); // Alternative: controller.start();
		} else {
			controller.stopController();
			qrCodescanner.removeListener(controller);
		}
		this.autonomousControl = enable;
	}

	public int getAltitude() {
		return controller.getAltitude();
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new DroneMain();

	}

}
