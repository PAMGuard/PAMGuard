package Acquisition.pamAudio;

import java.io.File;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.filemaps.OfflineFileServer;
import pamScrollSystem.ViewLoadObserver;

/**
 * Interface for PAMGuard opening a sound file. 
 * 
 * @author Jamie Macaulay
 *
 */
public interface PamAudioFile {
	
	/**
	 * Get file extensions associated with the file type
	 * @return a list of the file extensions (e.g.".wav")
	 */
	public ArrayList<String> getFileExtensions(); 
	
	
	/**
	 * Get the name of the file. 
	 * @return the name of the file
	 */
	public String getName(); 
	

	/**
	 * Get the audio stream for the file
	 * @return the audio input stream. 
	 */
	public AudioInputStream getAudioStream(File soundFile);

	/**
	 * Load a section of audio data. 
	 * @param dataBlock - the data block. 
	 * @param offlineFileServer - the offline file server. 
	 * @param offlineDataLoadInfo - the offline data load info. 
	 * @return true if the file has been loaded
	 */
	boolean loadAudioData(OfflineFileServer offlineFileServer, PamDataBlock dataBlock,
			OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver); 
	

}
