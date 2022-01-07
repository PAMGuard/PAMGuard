package PamguardMVC;

/**
 * Interface to add to data units that hold a time-frequency contour. <p>
 * this can be used by the beam former (or potentially other localisers or 
 * displays) to work within a restricted and varying frequency range. 
 * @author Doug Gillespie
 *
 */
public interface TFContourProvider {

	/**
	 * Gets time-frequency contour information in units of 
	 * milliseconds and frequency
	 * @return TFContour data object. 
	 */
	public TFContourData getTFContourData(); 
	
}
