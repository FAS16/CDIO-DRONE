package tests;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;
import de.yadrone.base.command.CommandManager;
import de.yadrone.base.navdata.BatteryListener;
import de.yadrone.base.navdata.HDVideoStreamData;
import de.yadrone.base.navdata.NavDataManager;
import de.yadrone.base.navdata.VideoListener;
import de.yadrone.base.navdata.VideoStreamData;

public class FirstTest {
	
	static NavDataManager navData;
	static CommandManager cmd;
	
	
	public static void main(String[] args) {
		
		System.out.println("YOOO!");
//		final int SPEED = 5;
//		final int SLEEP = 1000;
//		final int DURATION = 500;
//		
//		// Loading native library - opencv .dylib-file
//		System.loadLibrary("opencv_java341");
//		
//		IARDrone drone = null;
//	    try
//	    {
//	        drone = new ARDrone();
//	        drone.start(); // Activates managers - CommandManager, NavDataManager osv.
//	        cmd = drone.getCommandManager();
//	        
//	        cmd.takeOff();
//	        cmd.hover().doFor(5000);
//	        cmd.landing();
//	        
////	        cmd.hover();
////	        cmd.goLeft(SPEED).doFor(DURATION);
////			cmd.hover();
//////			Thread.currentThread().sleep(SLEEP);
////	        cmd.landing();
//	        
//	        
////	        int speed = 30;
////	        navData = drone.getNavDataManager();
//	        
////	        navData.addBatteryListener(new BatteryListener() {
////				
////				@Override
////				public void voltageChanged(int arg0) {
////					// TODO Auto-generated method stub
////				}
////				
////				@Override
////				public void batteryLevelChanged(int percentage) {
////					System.out.println("## BATTERY ##: " + percentage + " %");
////					
////				}
////			});
//	    }
//	    catch (Exception exc)
//		{
//			exc.printStackTrace();
//		}
//		finally
//		{
//			// Stop drone after try-block
//			if (drone != null) {
//				drone.stop();
//			}	
//			System.exit(0);
//		}
//		
	}

}
