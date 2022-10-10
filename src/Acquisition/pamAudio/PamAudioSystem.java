package Acquisition.pamAudio;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

//import org.kc7bfi.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.sound.spi.FlacAudioFileReader;


public class PamAudioSystem {

	private static final long largeFileSize = 01L<<31;

	/**
	 * Open an audio input stream. If the file is a Wav file, then it will attempt to read the 
	 * file with PAMGuards bespoke audio stream reader. This includes support for wav files which 
	 * are > 2GByte in size and also works for floating point 32 bit files (which the Java one doesn't). 
	 * If that fails, or if its not a wav file, then the standard java AudioInputStream 
	 * is used. 
	 * @param file file to open
	 * @return a new audio input stream
	 * @throws UnsupportedAudioFileException thrown if it can't understand the audio format. 
	 * @throws IOException
	 */
	public static AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
		//		if (file != null && isWavFile(file) && file.length() > largeFileSize) {
		if (file.exists() == false) return null;
		if (file != null && isWavFile(file)) {
			try {
				return WavFileInputStream.openInputStream(file);
			}
			catch (UnsupportedAudioFileException e) {
				// don't do anything and it will try the built in Audiosystem
			}
		}
		else if (file != null && isFlacFile(file)) {
			try {
				return new FlacAudioFileReader().getAudioInputStream(file);
			}
			catch (UnsupportedAudioFileException e) {
				
			}
		}
		else if (file != null && isSudFile(file)) {
			try {
				return new SudAudioFileReader().getAudioInputStream(file);
			}
			catch (UnsupportedAudioFileException e) {
				//e.printStackTrace();
			}
		}
		try {
		return AudioSystem.getAudioInputStream(file);
		}
		catch (Exception e) {
			System.out.println("Error in audio file " + file.getName() + ":  " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
//	public static write(File outputfile) {
//		
//		return AudioSystem.write(stream, fileType, out)
//	}

	private static boolean isWavFile(File file) {
		String name = file.getName();
		if (name.length() < 4) {
			return false;
		}
		String end = name.substring(name.length()-4).toLowerCase();
		return (end.equals(".wav"));
	}
	
	private static boolean isFlacFile(File file) {
		String name = file.getName();
		if (name.length() < 5) {
			return false;
		}
		String end = name.substring(name.length()-5).toLowerCase();
		return (end.equals(".flac"));
	}
	
	/**
	 * Check whether a file is a .sud file. This is the file format used
	 * by SoundTraps which contains X3 compressed data. 
	 * @param file - the file to check. 
	 * @return  true if a .sud file. 
	 */
	private static boolean isSudFile(File file) {
		String name = file.getName();
		if (name.length() < 4) {
			return false;
		}
		String end = name.substring(name.length()-4).toLowerCase();
		return (end.equals(".sud"));
	}


}
