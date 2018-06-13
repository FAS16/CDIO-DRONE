package drone;

import org.opencv.core.Core;

import de.yadrone.apps.paperchase.controller.PaperChaseKeyboardController;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;
import de.yadrone.base.command.VideoChannel;
import de.yadrone.base.exception.ARDroneException;
import de.yadrone.base.exception.IExceptionListener;
import de.yadrone.base.navdata.AttitudeListener;
import imgprocessing.CircleDetector;
import imgprocessing.QRCodeScanner;

public class DroneMain {

	public final static int TOLERANCE = 40;
	public final static int IMG_WIDTH = 640;
	public final static int IMG_HEIGHT = 360;

	QRCodeScanner qrCodescanner;
	CircleDetector circleDetector;
	IARDrone drone = null;
	GUI gui;
	CommandManager cmd;
	AutonomousController controller;
	private boolean autonomousControl = false;

	public DroneMain() {
		// calibrate commando
		drone = new ARDrone();
		drone.start();
		cmd = drone.getCommandManager();
		cmd.flatTrim();
		cmd.setVideoChannel(VideoChannel.HORI);

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

	}

	public void enableAutoControl(boolean enable) {
		System.out.println("MasterDrone enableAutoControler: " + enable);
		if (enable) {
			qrCodescanner.addListener(controller);
			new Thread(controller).start(); // Alternative: controller.start();
		} else {
			controller.stopController();
			qrCodescanner.removeListener(controller);
		}
		this.autonomousControl = enable;
	}

	public boolean getAutonomousControl() {
		return this.autonomousControl;
	}
	
	public int getAltitude() {
		return controller.getAltitude();
	}

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new DroneMain();
		
	}

}
