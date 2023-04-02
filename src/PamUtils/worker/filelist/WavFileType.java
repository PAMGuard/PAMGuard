package PamUtils.worker.filelist;

import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import Acquisition.pamAudio.PamAudioFileManager;
import Acquisition.pamAudio.PamAudioSystem;
import PamController.PamGUIManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class WavFileType extends File {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Audio information.  
	 */
	private AudioFormat audioInfo;
	
	/**
	 * True to use the file
	 */
	private BooleanProperty useFile = new SimpleBooleanProperty(true);

	/**
	 * The duration of the file in seconds
	 */
	private float durationInSeconds; 

	
	public WavFileType(File baseFile, AudioFormat audioInfo) {
		super(baseFile.getAbsolutePath());
		this.setAudioInfo(audioInfo);
	}


	/**
	 * Create a wave file type and automatically generate the audio format information. 
	 * @param baseFile - the sound file. 
	 */
	public WavFileType(File baseFile) {
		super(baseFile.getAbsolutePath());
		
		//this is a temporary hack as FX GUI uses the audioformat. Not neat. 
		if (PamGUIManager.isFX()) {
		this.setAudioInfo(getAudioFormat(baseFile));
		}
	}

	/**
	 * @return the audioInfo
	 */
	public AudioFormat getAudioInfo() {
		if (audioInfo == null) {
			audioInfo = getAudioFormat();
		}
		return audioInfo;
	}

	/**
	 * Get the audio format. 
	 * @return the audio format.
	 */
	private AudioFormat getAudioFormat() {
		return getAudioFormat(this);
	}
	
	/**
	 * Get the audio format. 
	 * @return the audio format.
	 */
	private AudioFormat getAudioFormat(File file) {
		try {
			AudioInputStream audioStream = PamAudioFileManager.getInstance().getAudioInputStream(file);
			
			AudioFormat audioFormat = audioStream.getFormat();
			
			//some additonal useful info
			 long audioFileLength = file.length();
			 durationInSeconds = (audioFileLength / (audioFormat.getFrameSize() * audioFormat.getFrameRate()));
			    
			    
			audioStream.close();
			return audioFormat;
		}
		catch (Exception Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Get the duration of the sound file in seconds
	 * @return the duration of the sound file in seconds.
	 */
	public float getDurationInSeconds() {
		return durationInSeconds;
	}


	/**
	 * @param audioInfo the audioInfo to set
	 */
	public void setAudioInfo(AudioFormat audioInfo) {
		this.audioInfo = audioInfo;
	}

	/**
	 * True to use the file in analysis
	 * @return true to use the file in analysis 
	 */
	public Boolean useWavFile() {
		return useFile.getValue();
	}
	
	/**
	 * The property for using wav files. 
	 * @return the boolean property for using the wav file. 
	 */
	public BooleanProperty useWavFileProperty() {
		return useFile;
	}

}
