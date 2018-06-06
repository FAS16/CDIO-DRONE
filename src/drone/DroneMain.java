package drone;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;
import de.yadrone.base.command.LEDAnimation;
import de.yadrone.base.exception.ARDroneException;
import de.yadrone.base.exception.IExceptionListener;
import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.navdata.BatteryListener;


public class DroneMain {

	public static void main(String[] args) {
		
		IARDrone drone = null;
		GUI gui;
		CommandManager cmd;
		try {
			drone = new ARDrone();
			drone.start();
			
			gui = new GUI((ARDrone) drone);

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

			cmd = drone.getCommandManager();
			cmd.takeOff();
			cmd.hover().doFor(10000);
			
		} catch (Exception exc) {

		}
		// finally
		// {
		// if (drone != null)
		// drone.stop();
		// System.exit(0);
		// }
	}

}
