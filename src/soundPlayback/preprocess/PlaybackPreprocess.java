package soundPlayback.preprocess;

import PamDetection.RawDataUnit;

public interface PlaybackPreprocess {
	
	/**
	 * Reset or prepare the process <br>
	 * Will almost definitely need another less abstract process for setting more specific 
	 * task parameters. 
	 * @param channelMap map of channels that are going to get processed. 
	 */
	public void reset(double inputSampleRate, int channelMap);
	
	/**
	 * Process a data unit. 
	 * @param inputDataUnit source data unit. 
	 * @param mustCopy data must not be modified in place. This will be set true for the rist unit
	 * in a chain in the playback system (generally a decimator) after that it will be false meaning
	 * that processes are free to modify the data in place so long as it remains consistent in length, etc. 
	 * @return a Raw data unit which may or may not be the same as the input one. 
	 */
	public RawDataUnit processDataUnit(RawDataUnit inputDataUnit, boolean mustCopy);
	
	/**
	 * Get a component to include in the PAMGuard side panel. 
	 * @return Component for the side panel
	 */
	public PreprocessSwingComponent getSideParComponent();
	
	/**
	 * Get a node to include in the PAMGuard side panel. 
	 * @return pane for the side panel
	 */
	public PreProcessFXPane getSideParPane();
	
	/**
	 * return true if the preprocess is doing anything. 
	 * @return Component for the side panel
	 */
	public boolean isActive();
	

}
