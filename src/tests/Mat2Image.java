package tests;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class Mat2Image {
    Mat mat = new Mat();
    BufferedImage img;
    byte[] dat;
    public Mat2Image() {
    }
    public Mat2Image(Mat mat) {
        getSpace(mat);
    }
    public void getSpace(Mat mat) {
        this.mat = mat;
        int w = mat.cols(), h = mat.rows();
        if (dat == null || dat.length != w * h * 3)
            dat = new byte[w * h * 3];
        if (img == null || img.getWidth() != w || img.getHeight() != h
                || img.getType() != BufferedImage.TYPE_3BYTE_BGR)
            img = new BufferedImage(w, h,
                    BufferedImage.TYPE_3BYTE_BGR);
    }
    public BufferedImage getImage(Mat mat){
        getSpace(mat);
        mat.get(0, 0, dat);
        img.getRaster().setDataElements(0, 0,
                mat.cols(), mat.rows(), dat);
        return img;
    }


    public void detectWhiteRect(Mat frame){
        Mat grayFrame = new Mat();
        Mat blurredImage = new Mat();
        Mat binarizedImage = new Mat();
        //TODO: DO FOR WHITE RECT ONLY
        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        //equalize the frame histogram to improve the result
//        Imgproc.equalizeHist(grayFrame, grayFrame);
        //Gaussian Blur
        Imgproc.GaussianBlur(grayFrame, blurredImage, new Size(5, 5), 0);
        Imgproc.threshold(blurredImage, binarizedImage, 127, 255, Imgproc.THRESH_BINARY);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(binarizedImage, contours, new Mat(), Imgproc.CHAIN_APPROX_SIMPLE , Imgproc.CHAIN_APPROX_SIMPLE);

        //draw rect on paper

        Scalar green = new Scalar(81, 190, 0);
        for (MatOfPoint contour: contours){
            RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            drawRotatedRect(frame, rotatedRect, green, 4);
        }

    }

    private void drawRotatedRect(Mat frame, RotatedRect rect, Scalar color, int thickness){
        Point[] vertices = new Point[4];
        rect.points(vertices);
        MatOfPoint points = new MatOfPoint(vertices);
        Imgproc.drawContours(frame, Arrays.asList(points), -1, color, thickness);

    }

    public void detectRedCircles(Mat frame) {
        Mat origFrame = frame;
        Mat bgrImage = frame;
        Mat hsvImage = new Mat();
        Imgproc.medianBlur(bgrImage, bgrImage, 5);
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
                100, 20, 50, 200);


        for (int i = 0; i < circles.cols(); i++) {
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

    }
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}