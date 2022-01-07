package clickTrainDetector.clickTrainAlgorithms.mht;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;

/**
 * Simple class for holding a few bits of information on the track 
 * @author Jamie Macaulay
 *
 */
public class TrackDataUnits {

	public TrackDataUnits(ArrayList<PamDataUnit> dataUnits, PamDataUnit lastUnit) {
		this.dataUnits=dataUnits; 
		this.lastUnit=lastUnit;
	}
	
	/**
	 * List of data units in the track
	 */
	public ArrayList<PamDataUnit> dataUnits;
	
	/**
	 * The chi2 value fo the track
	 */
	public double chi2Value = 0;
	
	/**
	 * The last data unit so far analysed by the algorithm. Note that this
	 * may or may not be in the dataUnits. 
	 */
	@SuppressWarnings("rawtypes")
	public PamDataUnit lastUnit; 
}