package drone;

import java.awt.image.BufferedImage;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import de.yadrone.apps.paperchase.PaperChase;
import de.yadrone.base.IARDrone;
import de.yadrone.base.navdata.Altitude;
import de.yadrone.base.navdata.AltitudeListener;
import de.yadrone.base.video.ImageListener;
import imgprocessing.TagListener;

public class AutonomousController extends AbstractController implements TagListener, ImageListener {

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
	
	public boolean isTagCentered() {
		if (tag == null)
			return false;
		
		int imgCenterX = PaperChase.IMAGE_WIDTH / 2;
		int imgCenterY = PaperChase.IMAGE_HEIGHT / 2;
		
		ResultPoint[] points = tag.getResultPoints();
		boolean isCentered = ((points[1].getX() > (imgCenterX - PaperChase.TOLERANCE)) &&
			(points[1].getX() < (imgCenterX + PaperChase.TOLERANCE)) &&
			(points[1].getY() > (imgCenterY - PaperChase.TOLERANCE)) &&
			(points[1].getY() < (imgCenterY + PaperChase.TOLERANCE)));
		
		return isCentered;
	}

	@Override
	public void onTag(Result result, float orientation) {
		if(result == null) {
			return;
		}
		
		tag = result;
		tagOrientation = orientation;

	}

	@Override
	public void run() {
		this.doStop = false;
		stateController = new StateController(this, drone, drone.getCommandManager());
		stateController.state = CurrentState.TakeOff; 
		while (!doStop) // control loop
		{
			try {
				// reset if too old (and not updated)
				if ((tag != null) && (System.currentTimeMillis() - tag.getTimestamp() > 2000)) {
					System.out.println("Resetting tag");
					tag = null;
				}
				stateController.handleState(stateController.state);
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

}
