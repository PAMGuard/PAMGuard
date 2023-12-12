package Acquisition.pamAudio;

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


	public SudAudioFileReader() {
		 sudParams = new SudParams();
		 //set up the sud params for default. i.e. just read files and
		 //don't save any decompressed or meta data.
//		 sudParams.saveWav = false;
//		 sudParams.saveMeta = false;
		 sudParams.setFileSave(false, false, false, false);
		 sudParams.zeroPad = true;
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
		
//		System.out.println("Get SUD getAudioInputStream");
 
		try {
			sudAudioInputStream = SudAudioInputStream.openInputStream(file, sudParams, mapListener, false);
		} catch (Exception e) {
			String msg = String.format("Corrupt sud file %s: %s", file.getName(), e.getMessage());
			throw new UnsupportedAudioFileException(msg);
		}
		return sudAudioInputStream;
	}

}
