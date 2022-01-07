package map3D.fx;


import javafx.geometry.Point3D;
import javafx.geometry.Point2D;

/**
 * Convenience extension of Point3D to easily get 2D points. 
 * @author Doug Gillespie
 */
public class MapPointData extends Point3D {

	public MapPointData(double x, double y, double z) {
		super(x, y, z);
	}

	/**
	 * Convert a 3D point to a 2D point
	 * @return 2D point. 
	 */
	public Point2D toPoint2D() {
		return new Point2D(getX(), getY());
	}
}
