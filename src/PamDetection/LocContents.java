package PamDetection;

/**
 * Class for holding information on what a type of information a localisation has. 
 * This is the typical class used throughout PAMGuard but specialised localisers might using a different subclass of LocalisationInfo.
 * 
 * @author Jamie Macaulay
 *
 */
public class LocContents implements LocalisationInfo {
	
	/**Flags for what type of 'Raw' localisation information from a detection**/

	/**
	 * Time delays are present 
	 */
	static public final int HAS_TIMEDELAYS = 0x1;
	
	/**
	 * Has an echo measurement 
	 */
	static public final int HAS_ECHO = 0x2;
	
	/**
	 * Localisation has a a bearing value. 
	 * <br>
	 * This is either 2 bearings representing a hyperbole, 2 bearings
	 * from a planar array or 1 3D bearing.  
	 */
	static public final int HAS_BEARING = 0x4;

	/**
	 * There is an error value associated with a bearing result
	 */
	static public final int HAS_BEARINGERROR = 0x8; 

	/**Flags to say what localisation results are present**/
	
	/**
	 * The localisation has a range value
	 */
	static public final int HAS_RANGE = 0x10;
	
	/**
	 * The localisation has a depth value
	 */
	static public final int HAS_DEPTH = 0x20;
	
	/**
	 * The localisation has an associated range error 
	 */
	static public final int HAS_RANGEERROR = 0x40;
	
	/**
	 * The localisation has a depth error  
	 */
	static public final int HAS_DEPTHERROR = 0x80;
	
	/**
	 * The localisation is geo-referenced i.e. has a latitude and longitude 
	 */
	static public final int HAS_LATLONG = 0x100;
	
	/**
	 * The localisation has a 2D location relative to the hydrophone array 
	 */
	static public final int HAS_XY = 0x200;
	
	/**
	 * The localisation has a 3D location relative co-ordinate frame of the hydrophone array. 
	 */
	static public final int HAS_XYZ = 0x400;
	
	/**
	 * The localisation has ambiguity i.e. multiple results are present.  
	 */
	static public final int HAS_AMBIGUITY = 0x800;
	/**
	 * Errors parallel and perpendicular to the ships track.
	 */
	static public final int HAS_PERPENDICULARERRORS = 0x100;
	
	
	/**
	 * bitmap of flags saying what's in the localisation information
	 */
	private int locContents = 0;
	
	public LocContents(int cont) {
		locContents=cont; 
	}

	/**
	 * 
	 * @param locContents a set of flags specifying which data are available within this localisation object.
	 */
	public void setLocContent(int locContents) {
		this.locContents = locContents;
	}
	
	/**
	 * The integer specifying which flags are present in the loclaisation. 
	 * @return bitmap of localisation contents. 
	 */
	public int getLocContent( ) {
		return locContents;
	}
	
	/**
	 * 
	 * @param flagsToAdd localisation flags to add to existing flags. 
	 */
	public void addLocContent(int flagsToAdd) {
		locContents |= flagsToAdd;
	}
	
	/**
	 * 
	 * @param flagsToRemove bitmap of localisation flags to remove. 
	 * @return new or remaining localisation content flags. 
	 */
	public int removeLocContent(int flagsToRemove) {
		locContents &= (~flagsToRemove);
		return locContents;
	}

	/**
	 * Check that the localisation has specific content. 
	 * @param requiredContent specified content
	 * @return true if specified content exists, false otherwise. 
	 */
	public boolean hasLocContent(int requiredContent) {
		return ((requiredContent & locContents) >= requiredContent);
	}

	
	
	

}
