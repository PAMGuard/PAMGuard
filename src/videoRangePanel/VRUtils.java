package videoRangePanel;

import java.awt.Point;

import PamUtils.LatLong;

/**
 * Some useful functions for the video range module. 
 * @author Jamie Macaulay
 *
 */
public class VRUtils {
	
	/**
	 * Convert a LatLong array to a double array with one dimensions i.e. latitude or longitude
	 * @param array - the LatLong array to convert
	 * @param lat - true for latitude or false for longitude. 
	 * @return - double array of either x or y values
	 */
	public static double[] latLong2Array(LatLong[] points, boolean lat) {
		double[] array = new double[points.length]; 
		for (int i=0; i<points.length; i++) {
			if (lat) array[i]=points[i].getLatitude();
			else array[i]=points[i].getLongitude();
		}
		return array;
	}
	
	/**
	 * Convert a point array to a double array with one dimensions i.e. x or y
	 * @param array - the point array to convert
	 * @param x - true for x or false for y. 
	 * @return - double array of either x or y values
	 */
	public static double[] points2Array(Point[] points, boolean x) {
		double[] array = new double[points.length]; 
		for (int i=0; i<points.length; i++) {
			if (x) array[i]=points[i].getX();
			else array[i]=points[i].getY();
		}
		return array;
	}

}
