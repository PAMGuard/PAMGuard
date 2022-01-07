package SoundRecorder;

import javax.sound.sampled.AudioFileFormat;

/**
 * Writes audio data into a file.
 * <p>
 * The PamRecorder process blocks the data from multiple channels
 * and sends it to PamAudioFileStorage as an array of double arrays.
 * 
 * @author Doug Gillespie
 *
 */
public interface RecorderStorage {
	
	/**
	 * Open audio file storage
	 * @param fileType Type of file storage (e.g. AU, AIFF, WAVE)
	 * @param recordingStart Start time of recording in milliseconds. The storage
	 * system should base a file name on the time
	 * @param sampleRate Sample rate for the recording
	 * @param nChannels Number of channels in the recording
	 * @param bitDepth Number of bits (e.g. 8, 16, 24)
	 * @return true if OK
	 */
	boolean openStorage(AudioFileFormat.Type fileType, long recordingStart, 
			float sampleRate, int nChannels, int bitDepth);
	
	/**
	 * Reopens the recording storage in a new file (if appropriate)
	 * This is done in the RecorderStorage class rather than as separate
	 * calls to closeStorage and openStorage in 
	 * 
	 * @param recordingStart new recording start time in milliseconds
	 * @return true if OK/ 
	 */
	boolean reOpenStorage(long recordingStart);
	
	/**
	 * Adds data to the store. Data are in a double array
	 * newData[channels][samples]. The number of channels must match the 
	 * number of channels in the call to openStorage and the number of
	 * samples must be the same for all channels. 
	 * @param dataTimeMillis the time of the data
	 * @param newData array of arrays of double data arranged by channel and sample
	 * @return true if OK.
	 */
	boolean addData(long dataTimeMillis, double[][] newData);
	
	/**
	 * 
	 * Closes the storage (and stops recording).
	 * The input and output data streams are flushed and
	 * closed. This automatically causes the write thread
	 * to terminate.
	 * @return true if OK
	 */
	boolean closeStorage();
	
	/**
	 * Gets the store file name. The file name must be set to null
	 * when the file is closed. 
	 * @return file name (or name of other store type)
	 */
	String getFileName();
	
	/**
	 * Get the file size in bytes/ Note that this is the physical size of the file
	 * which may not relate to the file length if a compressed recording system is used. 
	 * @return file size in bytes
	 */
	long getFileSizeBytes();
	
	/**
	 * Get the number of frames in the current file. 
	 * @return the number of frames in the current file. 
	 */
	long getFileFrames();
	
	
	/**
	 * Get the file length in milliseconds
	 * @return the file length in milliseconds.
	 */
	long getFileMilliSeconds();
	
	/**
	 * Get the time the current file started at. 
	 * @return time file started at or -1 if no current file.  
	 */
	long getFileStartTime();
	
	/**
	 * Get the absolute maximum file size in bytes for this type of storage
	 * @return Maximum size. Return Long.MAX_VALUE if it's indefinite. 
	 */
	long getMaxFileSizeBytes();
	
}
