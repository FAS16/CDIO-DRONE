package imgprocessing;

import org.opencv.core.MatOfPoint;

import java.util.List;

public interface OpenCVListener {
    void onRect(List<MatOfPoint> points);


}
