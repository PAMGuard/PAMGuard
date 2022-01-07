package PamUtils;

import fftManager.Complex;

/**
 * Calculate the mean of a load of angles in degrees. <p>
 * This is tricky, since the mean of 359 and 1 is 0, so 
 * the first thing to do is to try to find the centre of where 
 * most angles are, then we calculate all angles relative to that 
 * reference point, and finally, get the mean. 
 * <p>Strategy is to calculate a unit vector for each bearing, then take 
 * the mean vector. If all bearings cancel each other out, then the bearing
 * returned will be NaN. 
 * <p> function has been put into a class like this so that it can go further 
 * and calculate the standard deviation as well should it feel like it. 
 * @author Doug Gillespie. 
 *
 */
public class BearingMean {

	private double[] allBearings;
	
	private double bearingMean;
	
	/**
	 * Resultant vector, r, required by most circular statistics
	 */
	Complex r = new Complex();
	
	public BearingMean(double[] bearings) {
		allBearings = bearings;
		double x = 0, y = 0;
		for (int i = 0; i < bearings.length; i++) {
			x += Math.cos(Math.toRadians(bearings[i]));
			y += Math.sin(Math.toRadians(bearings[i]));
		}
		r.real = x/bearings.length;
		r.imag = y/bearings.length;
		if (x == 0 && y == 0) {
			bearingMean = Double.NaN;
		}
		else {
			bearingMean = Math.toDegrees(Math.atan2(y, x));
		}
	}

	/**
	 * @return the bearingMean
	 */
	public double getBearingMean() {
		return bearingMean;
	}
	
	/**
	 * Get the standard deviation about the mean. 
	 * @return the standard deviation about the mean. 
	 */
	public double getBearingSTD() {
		if (allBearings == null) {
			return 0;
		}
		if (Double.isNaN(bearingMean)) {
			return Double.NaN;
		}
		// otherwise calculate it - know the mean, so calc is just the RMS
		double r = 0;
		double ca;
		for (int i = 0; i < allBearings.length; i++) {
			ca = PamUtils.constrainedAngle(allBearings[i], bearingMean+180.);
			r += ca*ca;
		}
		r /= allBearings.length;
		return Math.sqrt(r);
	}
	
	/**
	 * Magnitude of resultant vector r. 
	 * This quantity is used for calculating 
	 * circular variance, circular standard deviation
	 * @return R - the magnitude of the resultant vector
	 */
	public double getBearingR(){
		return r.mag();
	}
	
	/**
	 * The standard deviation of as 
	 * defined by Zar (1999) sqrt(2*(1-R))
	 * @return Circular standard deviation
	 */
	public double getBearingSTD2(){
		double R = getBearingR();
		return Math.toDegrees(Math.sqrt(2*(1-R)));
		
	}
}
