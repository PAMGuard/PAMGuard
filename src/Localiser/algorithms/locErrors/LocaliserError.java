package Localiser.algorithms.locErrors;

import pamMaths.PamVector;

/**
 * Interface for loclaiser error. . 
 * <p>
 * Perhaps the most interesting aspect of localisation algorithm is how it calculates error. For example MCMC creates a cloud of points,
 * which can be any shape- the density of the points corresponds to the error surface. Simplex looks at the curvature of a chi2 surface,
 *  hyperbolic might try to bootstrap loclisations with tweaked input params from probability functions. There are multiple ways to calulate 
 *  error..
 *  <p>
 *  The error class is an abstracted method of returning errors. It can be very simple, e.g. just a set of two or three vectors. Or it can be more complex,
 *  e.g. sampling a cloud of points, or storing errors in a 3D rotated ellipse. What is common across all subclasses is that error can be extracted in any 3D direction. 
 *  <p>
 *  As error methods may be very different, xml is used to store error in the database. It is up to the specific subclass to define it's own xml for adequate storage. . 
 *  #
 * @author Jamie Macualay
 *
 */
public interface LocaliserError {
	
	/**
	 * Unit vector in the x direction
	 */
	public static final double[] xdir={1,0,0};
	
	/**
	 * Unit vector in the y direction
	 */
	public static final double[] ydir={0,1,0};
	
	/**
	 * Unit vector in the z direction
	 */
	public static final double[] zdir={0,0,1};
	
	/**
	 * PamVector in the x direction (unit vecor)
	 */
	public static final PamVector xdirVec= new PamVector(xdir);
	
	/**
	 * PamVector in the y direction (unit vecor)
	 */
	public static final PamVector ydirVec= new PamVector(ydir);

	/**
	 * PamVector in the y direction (unit vector)
	 */
	public static final PamVector zdirVec= new PamVector(zdir);

	
	/**
	 * Return the error in a particular direction. The direction is based on the 3D Cartesian co-ordinate grid the localisation 
	 * took place in. 
	 * @param errorDirection - the direction of the error to find
	 * @return the value of the error in the direction specified. 
	 */
	public double getError(PamVector errorDirection);  
	
	/**
	 * Get the real world error direction. e.g. if the error is relative to north this will be [0,1,0] as per the PAMGuard co-ordinate system. 
	 * If the error is relative to a boat heading then the vector will describe the direction of the boat. Used primarily to geo reference errors. 
	 * @return the real world error direction. 
	 */
	public PamVector getErrorDirection();
	

	/**
	 * 
	 * @return Error in a JSON format for writing to database. 
	 */
	public String getJsonErrorString(); 

	
	/**
	 * Functions fro drawing the localisation error
	 * @return a class containing fucntion to draw localisation error with. 
	 */
	public LocErrorGraphics getErrorDraw();

	/**
	 * A description of the loclaisation error. 
	 * @return locisation error. 
	 */
	public String getStringResult();
	
	/**
	 * @return the total of the magnitude of the error in all directions. 
	 */
	public double getErrorMagnitude();

//	/**
//	 * Indicates whether the error can be calculated for any angle. An error may only have a set of discrete 
//	 * Measurements, in which case isMultiVector is false. 
//	 * @return true if error can be calculated for any angle. 
//	 */
//	public boolean isMultiVector(); 
		

	
//	/**
//	 * Convenience function to get unit vector for error estimate in a particular direction. 
//	 * @param heading - the heading in RADIANS. 
//	 * @param pitch - the pitch in RADIANS
//	 * @return a vector pointing in the specified direction. 
//	 */
//	public static PamVector getUnitVector(double heading, double pitch) {
//		PamVector vector = new PamVector(xdir);
//		vector=PamVector.rotateVector(vector, heading, pitch);
//		vector.normalise(); 
//		return vector;
//	} 
//	
//	/**
//	 * Convenience function to get unit vector for error estimate in a particular direction. 
//	 * @param heading - the heading in RADIANS. The pitch is 0;
//	 * @return a vector pointing in the specified direction. 
//	 */
//	public static PamVector getUnitVector(double heading) {
//		return getUnitVector(heading, 0);
//	} 

}
