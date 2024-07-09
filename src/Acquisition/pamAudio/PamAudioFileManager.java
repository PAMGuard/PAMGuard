package Acquisition.pamAudio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.codehaus.plexus.util.FileUtils;

import Acquisition.sud.SudAudioFile;

/**
 * Central class for opening sound files.
 * <p>
 * PamAudioFieManager holds a list of PamAudioFile classes. Each PamAudioFile
 * can open a certain type of sound file e.g. flac or raw wav files.
 * PamAudioFieManager provides functions around the list to open files, provide
 * file filters etc.
 * 
 * @author Jamie Macaulay
 *
 */
public class PamAudioFileManager {

	/**
	 * Instance of the PamAudioFieManager.
	 */
	private static PamAudioFileManager pamAudioFileManager;

	/**
	 * The pam audio file types.
	 */
	private ArrayList<PamAudioFileLoader> pamAudioFileTypes;

	/**
	 * The default file loader for raw files. A reference to this is kept so that
	 * file loaders can preferentially select raw files over compressed file if they
	 * have the same name e.g. if using a folder of uncompressed SoundTrap data that
	 * contains both .sud and .wav files then only .wav files should be read.
	 */
	private WavAudioFile rawFileLoader;

	public PamAudioFileManager() {
		pamAudioFileTypes = new ArrayList<PamAudioFileLoader>();

		/***** Add new audio file types here *****/
		pamAudioFileTypes.add(rawFileLoader = new WavAudioFile());
		pamAudioFileTypes.add(new FlacAudioFile());
		pamAudioFileTypes.add(new SudAudioFile());

	}

	/**
	 * Get the audio file loader for a file.
	 * 
	 * @param soundFile - the sound file
	 * @return the audio file loader.
	 */
	public PamAudioFileLoader getAudioFileLoader(File soundFile) {
		for (int i = 0; i < pamAudioFileTypes.size(); i++) {
			if (isExtension(soundFile, pamAudioFileTypes.get(i))) {
				return pamAudioFileTypes.get(i);
			}
		}
		return null;
	}

	/**
	 * Check if a file has an extension supported by a PamAudioFile.
	 * 
	 * @param file
	 * @param pamAudioFile
	 */
	public boolean isExtension(File file, PamAudioFileLoader pamAudioFile) {
		for (int i = 0; i < pamAudioFile.getFileExtensions().size(); i++) {
			if (isSoundFile(file, pamAudioFile.getFileExtensions().get(i))) {
				return true;
			}
		}
		return false;
	}

	public boolean isSoundFile(File soundFile, String soundExtension) {
		if (soundFile == null) {
			return false;
		}

		String extension = FileUtils.getExtension(soundFile.getName());
		if (extension == null) {
			return false;
		}
		extension = extension.toLowerCase();
		soundExtension = soundExtension.toLowerCase();
		//System.out.println("Sound Extension: " + soundExtension + " File extension: " + extension);
		return (soundExtension.equals(extension) || soundExtension.equals("." + extension));
	}

	/**
	 * Open an audio input stream. If the file is a Wav file, then it will attempt
	 * to read the file with PAMGuards bespoke audio stream reader. This includes
	 * support for wav files which are > 2GByte in size and also works for floating
	 * point 32 bit files (which the Java one doesn't). If that fails, or if its not
	 * a wav file, then the standard java AudioInputStream is used.
	 * 
	 * @param file file to open
	 * @return a new audio input stream
	 * @throws UnsupportedAudioFileException thrown if it can't understand the audio
	 *                                       format.
	 * @throws IOException
	 */
	public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
		// if (file != null && isWavFile(file) && file.length() > largeFileSize) {
		if (file.exists() == false) {
			System.err.println("PamAudioFileManager: the input file was null");
			return null;
		}

		AudioInputStream stream = null;
		for (int i = 0; i < pamAudioFileTypes.size(); i++) {
//			System.out.println(file.getName() + "   " + pamAudioFileTypes.get(i).getName()); 
			if (isExtension(file, pamAudioFileTypes.get(i))) {
				//System.out.println("Get stream for: " +pamAudioFileTypes.get(i).getName()); 
				stream = pamAudioFileTypes.get(i).getAudioStream(file);
				if (pamAudioFileTypes != null) {
					break;
				}
			}
		}

		if (stream == null) {
			// have a punt at opening as a default audiostream
			//System.out.println("Try default stream for: " +file.getName() ); 

			stream = new WavAudioFile().getAudioStream(file);
		}

		if (stream == null) {
			System.err.println("PamAudioFileManager: unable to open an AudioStream for " + file.getName() + " size: " + file.length());
		}

		return stream;

	}

	/**
	 * Get the instance of the PamAudioManager
	 */
	public static PamAudioFileManager getInstance() {
		if (pamAudioFileManager == null) {
			pamAudioFileManager = new PamAudioFileManager();
		}
		return pamAudioFileManager;
	}

	/**
	 * Get the audio file filter.
	 * 
	 * @return the audio file filter.
	 */
	public PamAudioFileFilter getAudioFileFilter() {
		return new PamAudioFileFilter(this);
	}

	/**
	 * Get the current audio file
	 * 
	 * @return a list of the current audio loaders.
	 */
	public ArrayList<PamAudioFileLoader> getAudioFileLoaders() {
		return this.pamAudioFileTypes;
	}
	
	/**
	 * Get the loaders which are needed to open a list of files
	 * @param  files - the files to find audio loaders for. 
	 * @return a list of the  audio loaders required for the file list
	 */
	public ArrayList<PamAudioFileLoader> getAudioFileLoaders(ArrayList<? extends File> files) {
		ArrayList<PamAudioFileLoader> audioLoaders = new ArrayList<PamAudioFileLoader>(); 
		PamAudioFileLoader loader;
		for (int i=0; i<files.size(); i++) {
			 loader =  getAudioFileLoader(files.get(i)); 
			 if (!audioLoaders.contains(loader)) {
				 audioLoaders.add(loader); 
			 }
		}
		return audioLoaders;
	}

	/**
	 * Get the default file loader for raw files. 
	 * @return the default file loader for raw files. 
	 */
	public WavAudioFile getRawFileLoader() {
		return rawFileLoader; 
	}

}
