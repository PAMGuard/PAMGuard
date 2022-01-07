package Localiser.algorithms.timeDelayLocalisers.bearingLoc;

import pamMaths.PamVector;

/**
 * Class to estimate bearings, with errors from a closely spaced 
 * hydrophone array
 * @author Doug Gillespie
 *
 */
public interface BearingLocaliser {

	/**
	 * Do any preparation necessary (e.g. creation of look up tables)
	 * 
	 * @param arrayElements list of hydrophone array elements in the
	 * sub array for this localiser. 
	 * @param timeMillis, current time for this localisation
	 * @param timingError expected timing error for each measurement (typically 
	 * 1/12 of an ADC bin, or less if interpolation is being used in the cross correlation function). 
	 */
	public void prepare(int[] arrayElements, long timeMillis, double timingError);
	
	/**
	 * @return the type of array - linear, planar, volumetric, etc. 
	 */
	public int getArrayType();
	
	/**
	 * Get a hydrophone map for this localiser 
	 * @return bitmap of used hydrophones. 
	 */
	public int getHydrophoneMap();
	
	/**
	 * Get the principle axis of the array
	 * @return for a linear array this will be a vector along the array;<p> 
	 * for a planar array, this will be the vector on the plane between two
	 * hydrophones which is closest to the y axis.
	 */
	public PamVector[] getArrayAxis();
	
	/**
	 * Calculate angles theta and phi, based on a set of delays. 
	 * The number of delays should be equal to n(n-1)/2 where n is
	 * the number of hydrophone elements in the sub array. 
	 * @param delays array of delay times. 
	 * @param timeMillis of this detection. This is relatively unused except in situation where hydrophone elements are moving relative to each other with time. 
	 * @return theta, phi and their estimated errors all in radians. 
	 * <p> Data are packed into a two row array, the first row of 
	 * which contains one or two angles, the second (optional) row
	 * contains the errors on those angles.  
	 */
	public double[][] localise(double[] delays, long timeMillis);
	
}
