package drone;

import java.util.Timer;


import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import de.yadrone.apps.paperchase.PaperChase;
import de.yadrone.base.IARDrone;
import drone.CurrentState;
import de.yadrone.base.command.CommandManager;
import de.yadrone.base.command.LEDAnimation;

public class StateController {
	
	
	
	private final static int SPEED = 5;
	private final static int SLEEP = 500;

	public CurrentState state;
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

	public void handleState(CurrentState state) {
		if (state == CurrentState.TakeOff) {
			
			takeOff();

		} else if (state == CurrentState.Hovering) {
			
			try {
				hover();
			} catch (InterruptedException e) {
			
				e.printStackTrace();
			}

		} else if (state == CurrentState.SearchingForQR) {
			
			try {
				searchForQR();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			

		} 
//		else if (state == State.LostQR) {
//
//		} else if (state == State.ValidatingQR) {
//
//		} 
		else if (state == CurrentState.CentralizeQR) {

		} 
//		else if (state == State.SearchingForCircle) {
//
//		} else if (state == State.CentralizeCircle) {
//
//		} 
		
		else if (state == CurrentState.PassingPort) {

		} else if (state == CurrentState.Finished) {

		}

	}

	private void takeOff() {
		cmd.takeOff();
		//fly to height ############
		System.out.println("CURRENT STATE: Take off");
		state = CurrentState.Hovering;
		
	}
	
	private void hover() throws InterruptedException {
		drone.hover();
//		cmd.hover();
//		aController.sleep(100);
		System.out.println("CURRENT STATE: Hovering");
		state = CurrentState.SearchingForQR;
	}
	
	private void searchForQR() throws InterruptedException {
		System.out.println("CURRENT STATE: Searching for QR");
		
		
		Result tag = aController.getTag();
		if(tag != null) {
			System.out.println("FOUND QR");
			this.state = CurrentState.ValidatingQR;
			return;
		}
		
		// Hvis den ikke kan findes, skal dronen rykkes sig hensigtsmæssigt ift banen.
		System.out.println("NO QR FOUND - LOOKING TO THE RIGHT");
		cmd.spinRight(70).doFor(30);
		aController.sleep(800);		
		
	}
	
	private void centralizeQR() throws InterruptedException{
		String tagText;
		ResultPoint[] points;
		Result tag = aController.getTag();
		
		synchronized(tag)
		{
			points = tag.getResultPoints();	
			tagText = tag.getText();
		}
		
		int imgCenterX = PaperChase.IMAGE_WIDTH / 2;
		int imgCenterY = PaperChase.IMAGE_HEIGHT / 2;
		
		float x = points[1].getX();
		float y = points[1].getY();
		
		float orientation = aController.getTagOrientation();
		
		if ((orientation > 10) && (orientation < 180))
		{
			System.out.println("STATECONTROLLER: Spin left");
			drone.getCommandManager().spinLeft(SPEED * 2);
			Thread.currentThread().sleep(SLEEP);
		}
		else if ((orientation < 350) && (orientation > 180))
		{
			System.out.println("STATECONTROLLER: Spin right");
			drone.getCommandManager().spinRight(SPEED * 2);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (x < (imgCenterX - PaperChase.TOLERANCE))
		{
			System.out.println("STATECONTROLLER: Go left");
			drone.getCommandManager().goLeft(SPEED);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (x > (imgCenterX + PaperChase.TOLERANCE))
		{
			System.out.println("STATECONTROLLER: Go right");
			drone.getCommandManager().goRight(SPEED);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (y < (imgCenterY - PaperChase.TOLERANCE))
		{
			System.out.println("STATECONTROLLER: Go forward");
			drone.getCommandManager().forward(SPEED);
			Thread.currentThread().sleep(SLEEP);
		}
		else if (y > (imgCenterY + PaperChase.TOLERANCE))
		{
			System.out.println("STATECONTROLLER: Go backward");
			drone.getCommandManager().backward(SPEED);
			Thread.currentThread().sleep(SLEEP);
		}
		else
		{
			System.out.println("PaperChaseAutoController: Tag centered");
			drone.getCommandManager().setLedsAnimation(LEDAnimation.BLINK_GREEN, 10, 5);
			cmd.hover();
			this.state = CurrentState.PassingPort;
		}
	}
	

}
