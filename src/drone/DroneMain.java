package drone;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.command.VideoChannel;
import de.yadrone.base.exception.ARDroneException;
import de.yadrone.base.exception.IExceptionListener;
import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.navdata.BatteryListener;
import imgprocessing.ImageProcessing;
import imgprocessing.QRCodeScanner;
import org.opencv.core.Core;


public class DroneMain {

    public final static int IMG_WIDTH = 640; //
    public final static int IMG_HEIGHT = 360;
    public final static int TOLERANCE = 35;
    QRCodeScanner qrCodescanner;
    ImageProcessing imageProcessing;
    IARDrone drone = null;
    GUI gui;
    CommandManager cmd;

    public DroneMain() {

        drone = new ARDrone();
        drone.start();
        cmd = drone.getCommandManager();
        cmd.flatTrim();
        cmd.setVideoChannel(VideoChannel.HORI);

        gui = new GUI((ARDrone) drone, this);

        this.qrCodescanner = new QRCodeScanner();
        this.qrCodescanner.addListener(gui);

        this.imageProcessing = new ImageProcessing();
        this.imageProcessing.addListener(gui);

        drone.getVideoManager().addImageListener(gui);
        drone.getVideoManager().addImageListener(qrCodescanner);
        drone.getVideoManager().addImageListener(imageProcessing);

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


//			cmd.takeOff().doFor(5000);
//			cmd.hover().doFor(1000);
//			cmd.up(80).doFor(2000);
//			cmd.landing();
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new DroneMain();
    }

}
