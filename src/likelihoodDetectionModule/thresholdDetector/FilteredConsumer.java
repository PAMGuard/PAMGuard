package likelihoodDetectionModule.thresholdDetector;

import likelihoodDetectionModule.normalizer.NormalizedDataUnit;

/** This interface abstracts the consumer of filtered event objects.  This could represent a display widget,
 *  a file writer, a database writer, etc. 
 * 
 * @author Dave Flogeras
 *
 */
public interface FilteredConsumer {

	/***
	 * This method is called when a new detection starts on the given channel and band
	 * 
	 * @param key The key associated with the starting detection
	 */
	public void startDetection( DetectionKey key );
	
	/***
	 * This method is called to append a DetectionEvent to a running detection
	 * pre-condition: startDetection has been called for the given key (channelMask and band)
	 * At least one update will be given per detection.
	 * 
	 * @param key The key of the detection
	 * @param ndu The data for the update.
	 * @return true if successfully updates, false otherwise (ie. an event arrived for a channelMask/band that wasn't
	 * previously started
	 * 
	 */
	public boolean updateDetection( DetectionKey key, NormalizedDataUnit ndu );
	
	/***
	 * This method is called to end an existing detection
	 * pre-condition: startDetection has been previously called with the same channelMask/band.
	 * Ignores the request if the key had not been started.
	 * 
	 * @param key The key associated with the ending detection.
	 */
	public void endDetection( DetectionKey key );
	

}
