package geoMag;

import weka.core.SingleIndex;
import GPS.GpsData;

/**
 * Wrapper class around other available magnetic variation models so that I can switch 
 * to other models easily should the need arise. Currently just works with the
 * TSAGeoMag class.
 * @author Doug Gillespie
 *
 */
public class MagneticVariation {

	private TSAGeoMag tsaGeoMag = new TSAGeoMag();
	
	private double millisPerYear = 365.25*3600.*24*1000;
	
	private static MagneticVariation singleInstance;
	
	private MagneticVariation() {
		
	}
	
	public static MagneticVariation getInstance() {
		if (singleInstance == null) {
			singleInstance = new MagneticVariation();
		}
		return singleInstance;
	}
	
	/**
	 * Get the magnetic variation for a GPS location and time 
	 * @param gpsData gps data (contains time and position information)
	 * @return magnetic variation in degrees
	 */
	public double getVariation(GpsData gpsData) {
		return getVariation(gpsData.getTimeInMillis(), gpsData.getLatitude(), gpsData.getLongitude());
	}
	
	/**
	 * Return the magnetic variation for a time and place
	 * @param timeMilliseconds time in Java milliseconds
	 * @param dLat latitude in decimal degrees
	 * @param dLong longitude in decimal degrees
	 * @return magnetic variation in degrees
	 */
	public double getVariation(long timeMilliseconds, double dLat, double dLong) {
		double year = millisToYear(timeMilliseconds);
		return tsaGeoMag.getDeclination(dLat, dLong, year, 0);
	}
	
	/**
	 * Convert java milliseconds into a year for use in the tsaGeoMag routines.  
	 * @param millis Java time in milliseconds
	 * @return time in years. 
	 */
	private double millisToYear(long millis) {
		double years = millis / millisPerYear;
		return years + 1970;
	}
}
