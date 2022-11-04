package Acquisition.pamAudio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Opens a .sud audio file.
 * <p>
 * Sud files contain X3 compressed audio data. The sud
 * file reader opens files, creating a map of the file and saving
 * the map as a.sudx file so it can be read more rapidly when the file
 * is next accessed. 
 * <p>
 * The SudioAudioInput stream fully implements AudioInputStream and so 
 * sud files can be accessed using much of the same code as .wav files. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SudAudioFile extends WavAudioFile {
	
	
	public SudAudioFile() {
		super(); 
		fileExtensions = new ArrayList<String>(Arrays.asList(new String[]{".sud"})); 
	}

	@Override
	public String getName() {
		return "SUD";
	}
	
	
	@Override
	public AudioInputStream getAudioStream(File soundFile) {
		if (soundFile.exists() == false) {
			System.err.println("The sud file does not exist: " + soundFile);
			return null;
		}
		if (soundFile != null) {
			try {
				return new SudAudioFileReader().getAudioInputStream(soundFile);
			}
				// don't do anything and it will try the built in Audiosystem
			catch (UnsupportedAudioFileException e) {
				System.err.println("Could not open sud file: not a supported file " + soundFile.getName()); 

				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Could not open sud file: IO Exception: " + soundFile.getName()); 

				e.printStackTrace();
			}
		}
		return null;
	}

}
