package imgprocessing;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import de.yadrone.base.ARDrone;
import de.yadrone.base.command.VideoChannel;
import de.yadrone.base.command.VideoCodec;
import de.yadrone.base.video.ImageListener;
import drone.GUI;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ImageProcessing implements ImageListener {

    BufferedImage currentFrame;
    List<MatOfPoint> cont = new ArrayList<>();
    private ArrayList<OpenCVListener> listener = new ArrayList<OpenCVListener>();



    public ImageProcessing() {

    }

    private void processFrame() {
        BufferedImage imageToShow = null;
        Mat frame;

        if (currentFrame != null) {
            frame = imageToMat(currentFrame);
            //here goes processing of image
//            frame = detectRedCircles(frame);
            detectWhiteRect(frame);
        }


    }

    private Mat imageToMat(BufferedImage image) {
        Mat result = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
        result.put(0, 0, data.getData());

        return result;

    }

    private BufferedImage matToImage(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        BufferedImage img = null;

        try {
            img = ImageIO.read(new ByteArrayInputStream(buffer.toArray()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error matToImage");
        }

        return img;

    }

    private Mat detectRedCircles(Mat frame) {
        Mat origFrame = frame;
        Mat bgrImage = frame;
        Mat hsvImage = new Mat();
        Imgproc.medianBlur(bgrImage, bgrImage, 3);
        //convert hsv
        Imgproc.cvtColor(bgrImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        // Threshold the hsv image, keeping red pixels.

        Mat lowerRedHueRange = new Mat();
        Mat upperRedHueRange = new Mat();
        Core.inRange(hsvImage,
                new Scalar(0, 100, 100),
                new Scalar(10, 255, 255),
                lowerRedHueRange);
        Core.inRange(hsvImage,
                new Scalar(160, 100, 100),
                new Scalar(179, 255, 255),
                upperRedHueRange);

        Mat redHueImage = new Mat();
        Core.addWeighted(lowerRedHueRange,
                1.0,
                upperRedHueRange,
                1.0,
                0.0,
                redHueImage);

        Mat circles = new Mat();
        Imgproc.HoughCircles(redHueImage,
                circles,
                Imgproc.CV_HOUGH_GRADIENT,
                1,
                redHueImage.rows() / 8,
                100, 20, 0, 0);


        for (int i = 0; i < circles.cols(); i++){
            double[] c = circles.get(0, i);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            //DENNE POINT KAN MULIGVIS BRUGES TIL AT FLYVE IMOD.
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(origFrame,
                    center,
                    radius,
                    new Scalar(0, 255, 0),
                    5);
            System.out.println("x: " + center.x + " y: " + center.y);


        }
        return origFrame;

    }

    private Mat detectWhiteRect(Mat frame){
        Mat grayFrame = new Mat();
        Mat blurredImage = new Mat();
        Mat binarizedImage = new Mat();
        //TODO: DO FOR WHITE RECT ONLY
        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        //equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);
        //Gaussian Blur
        Imgproc.GaussianBlur(grayFrame, blurredImage, new Size(5, 5), 0);
        Imgproc.threshold(blurredImage, binarizedImage, 60, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binarizedImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        this.cont = contours;

        //draw rect on paper

//        Scalar green = new Scalar(81, 190, 0);
//        for (MatOfPoint contour: contours){
//            RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
//            drawRotatedRect(binarizedImage, rotatedRect, green, 4);
//        }

        return binarizedImage;
    }

    private void drawRotatedRect(Mat frame, RotatedRect rect, Scalar color, int thickness){
        Point[] vertices = new Point[4];
        rect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(frame, Arrays.asList(points), -1, color, thickness);

    }

    public void addListener(OpenCVListener listener)
    {
        this.listener.add(listener);
    }

    @Override
    public void imageUpdated(BufferedImage image) {
        currentFrame = image;
        processFrame();

        for (int i=0; i < listener.size(); i++)
        {
            listener.get(i).onRect(cont);
        }

    }
}

