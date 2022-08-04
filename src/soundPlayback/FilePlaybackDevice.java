package soundPlayback;

import PamDetection.RawDataUnit;
import PamView.dialog.PamDialogPanel;

/**
 * Interface to device types that can be used to play
 * back sound when acquisition is reading audio files. 
 * 
 * For real time acquisition, playback must generally be over 
 * the same device as is used for input. however, for file 
 * acquisition, playback can be over anything. Hence each 
 * playback device may easily end up being used in two 
 * different says - for playback of data from that particular
 * device and also for playback of file data.  
 * 
 * @author Doug Gillespie
 *
 */
public interface FilePlaybackDevice {

	/**
	 * 
	 * @return a name for this type of playback device
	 */
	public String getName();
	
	/**
	 * 
	 * @return a list of device names
	 */
	public String[] getDeviceNames();
	
	/**
	 * Get's the number of playback channels for a given device. 
	 * @param devNum device number
	 * @return number of channels
	 */
	public int getNumPlaybackChannels(int devNum);
	

	/**
	 * Prepare playback
	 * @param playbackParameters parameters
	 * @return true if all Ok. S
	 */
	public boolean preparePlayback(PlaybackParameters playbackParameters);
	
	/**
	 * Play some data. The length of the data array must
	 * correspond to the number of channels. 
	 * @param data array of raw audio data units. 
	 * @return true if played OK.
	 */
	public boolean playData(RawDataUnit[] data);

	/**
	 * Stop playback, clean up buffers, etc. 
	 * @return true if all cleaned up Ok. 
	 */
	public boolean stopPlayback();
	
	/**
	 * Get device status information. 
	 * @return
	 */
	public PlayDeviceState getDeviceState();

	public String getDeviceName();
	
	/**
	 * Get a settings panel for additional options. Can be null. 
	 * @return settings panel or null for additional options. 
	 */
	public PamDialogPanel getSettingsPanel();
	
}
