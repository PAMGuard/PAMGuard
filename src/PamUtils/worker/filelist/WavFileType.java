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
	 * Used for HARP data where we'll be making multiple WavFileType objects
	 * for a single duty cycled HARP .x.wav file. Number of samples to offset by
	 * and the total number of samples to read before restarting. 
	 */
	private long samplesOffset;
	
	private long maxSamples;
	
	private long startMilliseconds; 
	
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
	 * Simple constructor to use with a single string file name
	 * @param newFile
	 */
	public WavFileType(String newFile) {
		this(new File(newFile));
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
	public AudioFormat getAudioFormat(File file) {
		try {
			AudioInputStream audioStream = PamAudioFileManager.getInstance().getAudioInputStream(file);
			if (audioStream == null) {
				return null;
			}
			
			AudioFormat audioFormat = audioStream.getFormat();
			
			//some additonal useful info
			 long audioFileLength = file.length();
			 if (maxSamples > 0) {
				 durationInSeconds = (float) maxSamples / audioFormat.getSampleRate();
			 }
			 else {
				 durationInSeconds = (audioFileLength / (audioFormat.getFrameSize() * audioFormat.getFrameRate()));
			 }
			    
			    
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


	/**
	 * HARP data, samples to skip before this chunk. 
	 * @return the samplesOffset
	 */
	public long getSamplesOffset() {
		return samplesOffset;
	}


	/**
	 * HARP data, samples to skip before this chunk. 
	 * @param samplesOffset the samplesOffset to set
	 */
	public void setSamplesOffset(long samplesOffset) {
		this.samplesOffset = samplesOffset;
	}


	/**
	 * HARP data maximum samples in this chunk. 
	 * @return the maxSamples
	 */
	public long getMaxSamples() {
		return maxSamples;
	}


	/**
	 * HARP data maximum samples in this chunk. 
	 * @param maxSamples the maxSamples to set
	 */
	public void setMaxSamples(long maxSamples) {
		this.maxSamples = maxSamples;
		if (audioInfo != null) {
			durationInSeconds = (float) maxSamples / audioInfo.getSampleRate();
		}
	}


	/**
	 * Chunk start in milliseconds. If zero, will process file name for a 
	 * time as usual. 
	 * @return the startMilliseconds
	 */
	public long getStartMilliseconds() {
		return startMilliseconds;
	}


	/**	 
	 * Chunk start in milliseconds. If zero, will process file name for a 
	 * time as usual. 
	 * @param startMilliseconds the startMilliseconds to set
	 */
	public void setStartMilliseconds(long startMilliseconds) {
		this.startMilliseconds = startMilliseconds;
	}


	@Override
	public int compareTo(File pathname) {
		if (pathname instanceof WavFileType == false) {
			return super.compareTo(pathname);
		}
		long thisT = this.startMilliseconds;
		WavFileType oth = (WavFileType) pathname;
		long thatT = oth.getStartMilliseconds();
		if (thisT != 0 && thatT != 0) {
			return (int) Math.signum(thisT-thatT);
		}
		// otherwise use file names
		String thisN = this.getName();
		String thatN = oth.getName();
		return thisN.compareTo(thatN);
	}

}
