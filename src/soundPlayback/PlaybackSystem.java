package soundPlayback;

import java.util.ArrayList;

import PamDetection.RawDataUnit;
import soundPlayback.fx.PlaybackSettingsPane;
import soundPlayback.swing.PlaybackDialogComponent;

/**
 * Interface for soundplayback systems. 
 * 
 * @author Doug Gillespie
 *
 */
public abstract class PlaybackSystem {

	public ArrayList<PlaybackChangeObserver> changeObservers = new ArrayList<PlaybackChangeObserver>();
	
	public abstract boolean prepareSystem(PlaybackControl playbackControl, int nChannels, float sampleRate);
	
	public abstract boolean unPrepareSystem();
	
	public abstract int getMaxChannels();
	
	public abstract boolean playData(RawDataUnit[] data, double gain);
	
	/**
	 * Get the swing component for the playback system. 
	 * @return the swing component for the playback system. 
	 */
	public abstract PlaybackDialogComponent getDialogComponent();
	
	/**
	 * 
	 * @return
	 */
	public abstract PlaybackSettingsPane getSettingsPane();

	
	/**
	 * 
	 * @return System name
	 */
	public abstract String getName();
	
	/**
	 * Must use the same sample rate for output as for input when running
	 * in real time. 
	 * @return true if must use same sample rate for O as for I in real time. 
	 */
	public boolean mustSameIOSampleRate() {
		return true;
	}
	
	public void addChangeObserver(PlaybackChangeObserver playbackChangeObserver) {
		if (changeObservers.contains(playbackChangeObserver)) return;
		changeObservers.add(playbackChangeObserver);
	}
	
	public void removeChangeObserver(PlaybackChangeObserver playbackChangeObserver) {
		changeObservers.remove(playbackChangeObserver);
	}
	
	public void notifyObservers() {
		for (PlaybackChangeObserver pbo:changeObservers) {
			pbo.playbackChange();
		}
	}
	
	public PlayDeviceState getDeviceState() {
		return null;
	}

	/**
	 * @return the name of the actual playback device
	 */
	public String getDeviceName() {
		return getName();
	}
}
