package imgprocessing;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import de.yadrone.base.video.ImageListener;


public class CircleDetector implements ImageListener {
	
	private static final double DP = 1.1; // Basically tolerance
	private static final int MIN_DIST = 50; // Minimum distance between center points
	private static final int BLUR = 9; // Blur amount, removes noise - must be uneven
	
	private long imageCount = 0;
	// Used for only checking specific frames -> skipping frames.
	private final int skipValueForFrames = 5; 
	
	private ArrayList<CircleListener> listeners = new ArrayList<CircleListener>();
	
	/*
	 * Finds the circles in an image
	 * Takes the image where to detect the circles as an argument
	 * returns a array containing the data from the found circles.
	 */
	public static Circle[] findCircles(Mat image) {
		Size imgSize = new Size(0,0);
		if (image.size().height > 1200)
			Imgproc.resize(image, image, imgSize, 0.5,0.5,1);
		
		Mat gray = image.clone();
		// Convert to grayscale
		Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
		
		// Detect Circles
		Mat circles = new Mat();
		Size s = new Size(BLUR,BLUR);
		Imgproc.GaussianBlur(gray, gray, s, 2);
		Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, DP, MIN_DIST);
		
		Circle[] ret = new Circle[circles.cols()];
		double[] data;
		
		for (int i = 0; i < circles.cols(); i++) {
			data = circles.get(0, i);
			ret[i] = new Circle(data[0], data[1], data[2]);
		}
		
		return ret;
	}
	
	
	public static Circle[] findCircles(BufferedImage bi){
		 Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		  byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		  mat.put(0, 0, data);
		  return findCircles(mat);
	}
	


	@Override
	public void imageUpdated(BufferedImage img) {
		// We don't need to find circles in every frame
		if ((imageCount++ % skipValueForFrames) != 0)
			return;
		Circle[] circles = findCircles(img);
		for (CircleListener listener : listeners)
			listener.circlesUpdated(circles);
	}
	
	public void addListener(CircleListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeListener(CircleListener listener) {
		this.listeners.remove(listener);
	}

}
