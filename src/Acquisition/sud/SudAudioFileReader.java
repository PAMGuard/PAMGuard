package Acquisition.sud;

import java.io.File;
import java.io.IOException;
import org.pamguard.x3.sud.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Audio file reader for a .sud file. .sud files are a file format used by SoundTraps which contain
 * blocks of X3 compressed data. 
 * @author Jamie Macaulay
 *
 */
public class SudAudioFileReader {
	
	/**
	 * The current sud audio input stream
	 */
	SudAudioInputStream sudAudioInputStream; 
	
	/**
	 * Parameters for opening .sud files. 
	 */
	SudParams sudParams; 

	/**
	 * Constructor to create an Sud Audio reader with a
	 * default true to zeropad sud files. 
	 * */
	public SudAudioFileReader() {
		this(true); 
	} 
	
	
	/**
	 * Constructor to create an Sud Audio reader. Allows the option of zero padding.
	 * Zero padding fills gaps in sud files with zeros - these gaps are usually due
	 * to errors in the recording hardware.Without zero pad then time drift within a
	 * file can be difficult to predict, however zero padding means the sample
	 * numbers in other files e.g. csv sensor files will not align.
	 * 
	 * @param zeroPad - true to zero pad sud files.
	 */
	public SudAudioFileReader(boolean zeroPad) {
		 sudParams = new SudParams();
		 //set up the sud params for default. i.e. just read files and
		 //don't save any decompressed or meta data.
//		 sudParams.saveWav = false;
//		 sudParams.saveMeta = false;
		 sudParams.setFileSave(false, false, false, false);
		 sudParams.zeroPad = zeroPad;

	} 
	
	/**
	 * Get the audio input streamn. 
	 * @param file - the .sud file to open.
	 * @return the sud AudioStream. 
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {

		try {
			sudAudioInputStream = SudAudioInputStream.openInputStream(file, sudParams, false); 
		} catch (Exception e) {
			String msg = String.format("Corrupt sud file %s: %s", file.getName(), e.getMessage());
			throw new UnsupportedAudioFileException(msg);
		} 
		return sudAudioInputStream;
	}
	/**
	 * Get the audio input stream for a sud file.
	 * @param file - the .sud file to open.
	 * @param mapListener- a listener for the sud file maps - can be null.
	 * @return the sud AudioStream.
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 */
	public AudioInputStream getAudioInputStream(File file, SudMapListener mapListener) throws UnsupportedAudioFileException, IOException {
		
		//System.out.println("Get SUD getAudioInputStream" + sudParams.zeroPad);
 
		try {
			sudAudioInputStream = SudAudioInputStream.openInputStream(file, sudParams, mapListener, false);
		} catch (Exception e) {
			String msg = String.format("Corrupt sud file %s: %s", file.getName(), e.getMessage());
			throw new UnsupportedAudioFileException(msg);
		}
		return sudAudioInputStream;
	}

}
