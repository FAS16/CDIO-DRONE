package drone;

import java.util.Timer;

import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;

public class StateController {

	private State state;
	private IARDrone drone;
	private CommandManager cmd;
	private AutonomousController aController;

	private Timer timer;
	private int nextPort = 0;
	private final int maxPorts = 5;

	public StateController(AutonomousController ac, IARDrone drone, CommandManager cmd) {
		this.aController = ac;
		this.drone = drone;
		this.cmd = cmd;
		this.timer = new Timer();
	}

	public void handleState(State state) {

		if (state == State.TakeOff) {

		} else if (state == State.Hovering) {

		} else if (state == State.SearchingForQR) {

		} else if (state == State.LostQR) {

		} else if (state == State.ValidatingQR) {

		} else if (state == State.CentralizeQR) {

		} else if (state == State.SearchingForCircle) {

		} else if (state == State.CentralizeCircle) {

		} else if (state == State.PassingPort) {

		} else if (state == State.Finished) {

		}

	}

	private void takeOff() {
		cmd.takeOff();
		//fly to height ############
		System.out.println("CURRENT STATE: Take off");
		state = State.Hovering;
		
	}
	
	private void hover() throws InterruptedException {
		drone.hover();
//		cmd.hover();
//		aController.sleep(100);
		System.out.println("CURRENT STATE: Hovering");
		state = State.SearchingForQR;
	}
	
	

}
