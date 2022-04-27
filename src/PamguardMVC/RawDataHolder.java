package PamguardMVC;

/**
 * Interface to be used by PamDataUnits which contain raw data which 
 * might get used by other processes. Examples include clicks and 
 * output from the clip generator. 
 * @author Doug Gillespie
 *
 */
public interface RawDataHolder {
	
	/**
	 * Get arrays of raw audio data, one per channel. Assume
	 * that the array matches getChannelMap() !
	 * @return arrays of raw data by channel
	 */
	public double[][] getWaveData();
	
	/**
	 * Get the raw data transforms class. This handles standard data transforms 
	 * that are often used in raw data units, e.g. calculating the spectrum, filtering
	 * waveforms, getting data as an int16 (short) array, etc. 
	 *  
	 * @return the data transforms object. 
	 */
	public RawDataTransforms getDataTransforms(); 

}
