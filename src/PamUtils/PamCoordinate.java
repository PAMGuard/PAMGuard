package PamUtils;

/**
 * Interface to put behind several existing PAM objects so that 
 * they can start working in a common way without having 
 * to totally rewrite everything. Applying initially to 
 * Coordinate3d and PamVector.
 * <p>
 * While dealing with x,y,z,it is possible that, particularly in the GeneralProjector
 * class (that this is going to be used with) these dimensions might get used 
 * for other coordinate frames. 
 * @author dg50
 *
 */
public interface PamCoordinate {

//	/**
//	 * @return the x coordinate
//	 */
//	double getX(); 
//
//	/**
//	 * @return the y coordinate
//	 */
//	double getY();
//
//	/**
//	 * @return the z coordinate (+ is up)
//	 */
//	double getZ();
//	
	/**
	 * Get an indexed coordinate. 
	 * @param iCoordinate coordinate index. 
	 * @return coordinate value. 
	 */
	double getCoordinate(int iCoordinate);
	
	/**
	 * Set a coordinate value
	 * @param iCoordinate index of coordinate (0,1,2)
	 * @param value value to set. 
	 */
	void setCoordinate(int iCoordinate, double value);
	
	/**
	 * @return the number of coordinates. 
	 */
	int getNumCoordinates();
	
	
}
