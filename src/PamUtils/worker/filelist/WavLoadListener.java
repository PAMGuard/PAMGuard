package PamUtils.worker.filelist;


/**
 * Listener for the loading of a single wav file
 */
public interface WavLoadListener {
	
	/**
	 * Update the loading of a wav file. 
	 * @param message - an update message	
	 * @param percentComplete - the percent complete from 0.0 to 1.0
	 */
	public void updateWavLoad(String message, double percentComplete);
	
//	/**
//	 * Update the loading of a wav file. 
//	 * @param message - an update message	
//	 * @param count - load count e.g. number of chunks in a compressed file. 
//	 */
//	public void updateWavLoad(String message, int count);


}
