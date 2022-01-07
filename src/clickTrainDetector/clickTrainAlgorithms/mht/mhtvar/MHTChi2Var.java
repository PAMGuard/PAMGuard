package clickTrainDetector.clickTrainAlgorithms.mht.mhtvar;

import java.util.ArrayList;
import java.util.BitSet;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.layout.mht.MHTVarSettingsPane;

/**
 * A variable which is used to calculate chi2 value.
 * <p>
 * Multiple variables can be used to calculate the chi^2 value of a track. For
 * example a slowly changing ICI might be used or a combination of ICI,
 * amplitude, bearing and/or correlation. MHTChi2Var is a single variable which
 * can be extracted from a list of T data items and the chi2 value calculated.
 * <p>
 * All the MHT calculation are based somewhat on ICI because error values depend 
 * on the ICI value. For example a low ICI might mean that amplitude changes are likely
 * to be significantly less on a click by click basis. 
 * <p>
 * Note, these should be made serilizable. To save settings use the getSettingsObject() instead. 
 * 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public interface MHTChi2Var<T> extends Cloneable {
	
	/**
	 * Get the name of the chi^2 variable.  
	 * @return the name of the chi^2 variable. 
	 */
	public String getName();
	
	/**
	 * Get the data units for the MHTChi2 var.
	 * @return the units. 
	 */
	public String getUnits(); 

	/**
	 * Calculate the chi2 value for a particular variable from a list of data units in a track. 
	 * 
	 * @param mhtDataUnits
	 *            - a list of items in the track
	 * @param iciManager
	 *            - class with the the times of the data series starting from data unit 0 at time 0
	 *            in seconds. The time series may be based on simply the millisecond
	 *            time or a more accurate time using sample number and cross correlation. 
	 * @return chi2 value.
	 */
	public double calcChi2(ArrayList<T> mhtDataUnits, IDIManager iciManager);
	
	/**
	 * Update the existing chi2 value with a new data unit and track bitset.
	 * 
	 * @param newdataUnit 	- the new data unit.
	 * @param bitSet      	- the bitset for the track.
	 * @param bitcount		- the number of detections in the track.
	 * @param kcount      	- the current kcount, the total number of detections added to possibility mix. 
	 * @param iciManager  	- the IDI manager.
	 * @return the new chi^2 value
	 */
	public double updateChi2(PamDataUnit newdataUnit, BitSet bitSet, int bitcount, int kcount, IDIManager iciManager);

	/**
	 * Get the current chi^2 value i.e. the chi2 value since the last update. 
	 * @return the current chi^2 value
	 */
	public double getChi2();
	
	/**
	 * Get the error estimate for chi2 values. 
	 * @return the current chi^2 error. 
	 */
	public double getError();

	/**
	 * Get the settings pane with controls to change MHTChi2Var settings
	 * @return the MHTChiVar specific settings pane. 
	 */
	public MHTVarSettingsPane getSettingsPane(); 
	
	/**
	 * Get the settings object for the variable 
	 * @return the settings object. 
	 */
	public Object getSettingsObject();

	/**
	 * Set the settings object for saving the parameters. This is only used for
	 * saving params. 
	 * @param object - the settings object. 
	 */
	public void setSettingsObject(Object object);

	/**
	 * Resets the variable and clear chi2 values etc. 
	 */
	public void clear();

	/**
	 * Deep clone of the variable. 
	 * @return deep clone of the variable. 
	 */
	public MHTChi2Var<PamDataUnit> clone();

	/**
	 * Check whether a data block is compatible with this type of data unit. 
	 * @param parentDataBlock - the parent data block 
	 * @return true if the data block is allowed
	 */
	public boolean isDataBlockCompatible(PamDataBlock parentDataBlock);


}
