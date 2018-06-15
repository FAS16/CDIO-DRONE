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


    public void detectWhiteRect(Mat frame) {
        Mat src = frame;
        Mat grayFrame = new Mat();
        Mat grad = new Mat();
        Mat blurredImage = new Mat();
        Mat binarizedImage = new Mat();
        Mat hsv = new Mat();
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;
        // convert the frame in gray scale
        Imgproc.GaussianBlur(src, src, new Size(5, 5), 0);
        //Imgproc.medianBlur(src, src, 11);
        //Imgproc.pyrMeanShiftFiltering(src, src, 31 ,91);

        //Imgproc.cvtColor(src, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(src, hsvImage, Imgproc.COLOR_BGR2HSV);

        Mat lowerBlueHueRange = new Mat();
        Mat upperBlueHueRange = new Mat();
        Core.inRange(hsvImage,
                new Scalar(0, 0, 200),
                new Scalar(180, 255, 255),
                lowerBlueHueRange);

//        Mat blueHueImage = new Mat();
//        Core.addWeighted(lowerBlueHueRange,
//                1.0,
//                upperBlueHueRange,
//                1.0,
//                0.0,  blueHueImage);

        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();

        //Imgproc.Scharr( src_gray, grad_x, ddepth, 1, 0, scale, delta, Core.BORDER_DEFAULT );
        Imgproc.Sobel(lowerBlueHueRange, grad_x, ddepth, 1, 0, 3, scale, delta, Core.BORDER_DEFAULT);
        //Imgproc.Scharr( src_gray, grad_y, ddepth, 0, 1, scale, delta, Core.BORDER_DEFAULT );
        Imgproc.Sobel(lowerBlueHueRange, grad_y, ddepth, 0, 1, 3, scale, delta, Core.BORDER_DEFAULT);

        Core.convertScaleAbs(grad_x, abs_grad_x);
        Core.convertScaleAbs(grad_y, abs_grad_y);
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);
        Imgproc.threshold(grad, grad, 20, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(grad, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        //draw rect on paper

        Scalar green = new Scalar(81, 190, 0);
        for (MatOfPoint contour : contours) {
            MatOfPoint2f contour2f = new MatOfPoint2f(contour.toArray());
            MatOfPoint2f approx = new MatOfPoint2f();
            double epsilon = 0.1 * Imgproc.arcLength(contour2f, true);

            if (epsilon > 1) {
                Imgproc.approxPolyDP(contour2f, approx, epsilon, true);
                MatOfPoint points = new MatOfPoint(approx.toArray());

                //Rectangle Checks - Points, area, convexity
                if (points.total() == 4 && Math.abs(Imgproc.contourArea(points)) > 1000 && Imgproc.isContourConvex(points)) {
                    double cos = 0;
                    double mcos = 0;
                    for (int sc = 2; sc < 5; sc++) {
                        // TO-DO Figure a way to check angle
                        if (cos > mcos) {
                            mcos = cos;
                        }
                    }
                    if (mcos < 0.3) {
                        // Get bounding rect of contour
                        Rect rect = Imgproc.boundingRect(points);

                        if (rect.height*1.2 > rect.width) {

                            System.out.println(contours.indexOf(contour) + "| x: " + rect.x + " + width(" + rect.width + "), y: " + rect.y + "+ width(" + rect.height + ")");

                            Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(20, 20, 20), -1, 4, 0);
                            Imgproc.drawContours(frame, contours, contours.indexOf(contour), new Scalar(0, 255, 0, .8), 2);

                            // Highgui.imwrite("detected_layers"+i+".png", originalImage);
                        }
                    }
                }
            }
        }
    }


//                double area = Imgproc.contourArea(contour);
//                if (area > 1000) {
//                    RotatedRect rotatedRect = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
//                    drawRotatedRect(frame, rotatedRect, green, 4);
//                }

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

        // Threshold the hsv image, keeping blue pixels.

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