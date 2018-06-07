package drone;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.Result;

import de.yadrone.base.IARDrone;
import de.yadrone.base.video.ImageListener;
import imgprocessing.TagListener;

public class AutonomousController extends AbstractController implements TagListener, ImageListener {

	private Result tag;	
//	private Map<Integer, String> ports = new HashMap<>();
	private ArrayList<String> portsToFind = new ArrayList<String>();

	public AutonomousController(IARDrone drone) {
		super(drone);
		
//		ports.put(1, "P.00");
//		ports.put(2, "P.01");
//		ports.put(3, "P.02");
//		ports.put(4, "P.03");
//		ports.put(5, "P.04");
//		ports.put(6, "P.05");
		
		
	
		
		portsToFind.add("P.00");
		portsToFind.add("P.01");
		portsToFind.add("P.02");
		portsToFind.add("P.03");
		portsToFind.add("P.04");
		portsToFind.add("P.05");
		portsToFind.add("P.06");
	}

	@Override
	public void imageUpdated(BufferedImage image) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTag(Result result, float orientation) {
		if(result == null) {
			return;
		}
		
		tag = result;

	}

	@Override
	public void run() {
		
		while(!doStop) {
			
			
			
		}
		
		
	}

}
