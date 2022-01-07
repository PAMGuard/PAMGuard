package soundPlayback;

import PamguardMVC.PamObserver;

/**
 * Provide data for sound playback in a suitable format. 
 * example use is from the click detector which will 
 * regenerate raw audio data from clicks, filling in spaces
 * between clicks with blanks. 
 * @author Doug Gillespie
 *
 */
public interface PlaybackDataServer {
	

	/**
	 * 
	 * Request playback data. <p>
	 * This will automatically get called in a separate worker thread
	 * so no need to rethread in the concrete subclass of this.
	 * @param dataObserver destination for new RawDataUnits
	 * @param progressMonitor progress monitor - should be notified in AWT thread. 
	 * @param startMillis start time in millis
	 * @param endMillis end time in millis. 
	 */
	public void orderPlaybackData(PamObserver dataObserver, 
			PlaybackProgressMonitor progressMonitor, float playbackRate, 
			long startMillis, long endMillis);
	
	/**
	 * Cancel data loading. 
	 */
	public void cancelPlaybackData();
	
	/**
	 * Get the true sample rate of the data
	 * @return true sample rate in the data to play back. 
	 */
	public double getDataSampleRate();
	
}
