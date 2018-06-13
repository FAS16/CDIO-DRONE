package imgprocessing;

import org.opencv.core.Point;

/*This class defines a circle
 * [x,y] is the coordiantes of the center of the circle
 * R r is the radius.
 */
public class Circle {

	public double x, y, r;

	public Circle(double x, double y, double r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	public Circle(int x, int y, int r) {
		this.x = (double) x;
		this.y = (double) y;
		this.r = (double) r;
	}

	public Point getPoint() {
		return new Point(this.x, this.y);
	}

	public double getRadius() {
		return this.r;
	}

}
