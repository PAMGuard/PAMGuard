package Acquisition.pamAudio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.codehaus.plexus.util.FileUtils;
import Acquisition.offlineFuncs.AquisitionLoadPoint;
import PamDetection.RawDataUnit;
//import PamUtils.CPUMonitor;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.OfflineDataMap;
import dataMap.filemaps.FileDataMapPoint;
import dataMap.filemaps.OfflineFileServer;
import pamScrollSystem.ViewLoadObserver;
import wavFiles.ByteConverter;

/**
 * Wav audio file - opens any raw audio file. 
 * <p>
 * This should be used a standard class for opening audio files
 * 
 * @author Jamie Macaulay, Doug Gillepsie
 *
 */
public class WavAudioFile implements PamAudioFileLoader {

	/**
	 * The audio input stream of the last loaded file. 
	 */
	private AudioInputStream audioInputStream;

	/**
	 * The audioformat of the last laoded file. 
	 */
	private AudioFormat audioFormat;

	/**
	 * Get the file extensions associated with loading these data. 
	 */
	protected ArrayList<String> fileExtensions; 

	public WavAudioFile() {
		fileExtensions = new ArrayList<String>(Arrays.asList(new String[]{".wav", ".aif", ".aiff"})); 
	}

	@Override
	public ArrayList<String> getFileExtensions() {
		return fileExtensions;
	}

	@Override
	public String getName() {
		return "WAV";
	}

	@Override
	public boolean loadAudioData(OfflineFileServer offlineFileServer, PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {

		//		Debug.out.println("OfflineFileServer: Load Wav Data: " + offlineDataLoadInfo.getCurrentObserver().getObserverName() );
		OfflineDataMap<FileDataMapPoint> dataMap = offlineFileServer.getDataMap();
		Iterator<FileDataMapPoint> mapIt = dataMap.getListIterator();
		FileDataMapPoint mapPoint = offlineFileServer.findFirstMapPoint(mapIt, offlineDataLoadInfo.getStartMillis(), offlineDataLoadInfo.getEndMillis());

		if (openSoundFile(mapPoint.getSoundFile()) == false) {
			System.out.println("Could not open sound file " + mapPoint.getSoundFile().getAbsolutePath());
			return false;
		}
		if (offlineDataLoadInfo.cancel) {
			return false;
		}
		
		File soundFile;

		ByteConverter byteConverter = ByteConverter.createByteConverter(audioFormat);
		long currentTime = mapPoint.getStartTime();
		long prevFileEnd = mapPoint.getEndTime();
		boolean fileGap = false;
		int newSamples; 
		double[][] doubleData;
		int nChannels = audioFormat.getChannels();
		int blockSamples = Math.max((int) audioFormat.getSampleRate() / 10, 1000);
		int frameSize = audioFormat.getFrameSize();
		if (frameSize < 0) {
			frameSize = audioFormat.getChannels()*audioFormat.getSampleSizeInBits()/8;
		}
		if (frameSize <= 0) {
			System.out.println("The frame size is less than zero " + mapPoint.getSoundFile().getAbsolutePath());
			return false;
		}
		byte[] inputBuffer = new byte[blockSamples * frameSize];
		int bytesRead = 0;
		long totalSamples = 0;
		//		long fileSamples = 0;
		long millisecondsGaps = 0;
		long ms;

		RawDataUnit newDataUnit;
		long skipped = 0; 
		if (currentTime < offlineDataLoadInfo.getStartMillis()) {
			// need to fast forward in current file. 
			long skipBytes = (long) (((offlineDataLoadInfo.getStartMillis()-currentTime)*audioFormat.getSampleRate()*audioFormat.getFrameSize())/1000.);
			try {

				//System.out.println("Skipped " + skipped+  " " + skipBytes + " " + audioInputStream.available());
//				CPUMonitor cpuMonitor = new CPUMonitor();
//				cpuMonitor.start();
				skipped = audioInputStream.skip(skipBytes);
//				cpuMonitor.stop();
//				System.out.println(cpuMonitor.getSummary("Sound skip: " + skipBytes + " bytes "));
				//System.out.println("Offline " + (offlineDataLoadInfo.getStartMillis()-currentTime) + " ms : frame size: " + audioFormat.getFrameSize());

			} catch (IOException e) {
				/**
				 * The datamap point may be longer than the actual file here ? In any case, with the 
				 * NEMO data which is 5 mins per hour, this get's hit for the file before the 
				 * file we want every time !
				 */
				//				System.out.println("End of audio file " + mapPoint.getSoundFile().getName());
				e.printStackTrace();
			}
			currentTime = offlineDataLoadInfo.getStartMillis();
		}
		ms = currentTime;
		while (ms < offlineDataLoadInfo.getEndMillis() && currentTime < offlineDataLoadInfo.getEndMillis()) {
			if (offlineDataLoadInfo.cancel) {
				//add the position we got to 
				offlineDataLoadInfo.setLastLoadInfo(new AquisitionLoadPoint(ms, bytesRead)); 


				break;
			}
			try {
				if (inputBuffer.length<audioInputStream.available()) {
					bytesRead = audioInputStream.read(inputBuffer);
				}
				else {
					bytesRead = 0; //force new file to load. 
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
			if (bytesRead <= 0) {
				skipped = 0 ; //reset ot zero because were not skipping anyu bytes here. 
				/*
				 *  that's the end of that file, so get the next one if there
				 *  is one, if not then break.
				 */
				if (mapIt.hasNext() == false) {
					break;
				}
				mapPoint = mapIt.next();
				fileGap = (mapPoint.getStartTime() - prevFileEnd) > 1000;
				//				if (fileGap) {
				//					System.out.println(String.format("Sound file gap %3.3fs from %s to %s", 
				//							(double) (mapPoint.getStartTime()-prevFileEnd) / 1000.,
				//							PamCalendar.formatTime(prevFileEnd), PamCalendar.formatTime(mapPoint.getStartTime())));
				//				}
				prevFileEnd = mapPoint.getEndTime();
				if (!fileGap) { // don't carry on if there is a file gap
					if (openSoundFile(mapPoint.getSoundFile()) == false) {
						break;
					}
					// try again to read data. 
					try {
						bytesRead = audioInputStream.read(inputBuffer);
					} catch (IOException e) {
						e.printStackTrace();
					}		
					if (bytesRead <= 0) {
						break;
					}
				}
			}
			newSamples = bytesRead / frameSize;
			doubleData = new double[nChannels][newSamples];
			int convertedSamples = byteConverter.bytesToDouble(inputBuffer, doubleData, bytesRead);
			ms = offlineFileServer.getOfflineRawDataStore().getParentProcess().absSamplesToMilliseconds(totalSamples);
			ms = currentTime + (long)(totalSamples * 1000 / (double) audioFormat.getSampleRate());

			for (int ichan = 0; ichan < nChannels; ichan++) {

				newDataUnit = new RawDataUnit(ms, 1 << ichan, totalSamples, newSamples);
				newDataUnit.setFileSamples(totalSamples + skipped / frameSize); //set the number samples into the wav file. 
				newDataUnit.setRawData(doubleData[ichan], true);

				//System.out.println("New wav data: " + PamCalendar.formatDateTime(newDataUnit.getTimeMilliseconds()));
				offlineFileServer.getRawDataBlock().addPamData(newDataUnit);
			}
			if (fileGap) {
				currentTime = mapPoint.getStartTime();
				totalSamples = 0;
				//				fileSamples = 0;
			}

			totalSamples += newSamples;
			//			fileSamples += newSamples;
		}

		//		System.out.println("Finished loading wav: " + offlineDataLoadInfo.getCurrentObserver().getObserverName() );

		return false;
	}


	/**
	 * Open a sound file. 
	 * @param soundFile
	 * @return
	 */
	private boolean openSoundFile(File soundFile) {

		audioInputStream = getAudioStream(soundFile);
		if (audioInputStream == null) return false;
		audioFormat = audioInputStream.getFormat();

		return true;
	}


	@Override
	public AudioInputStream getAudioStream(File soundFile) {
		if (soundFile.exists() == false) return null;
		if (soundFile != null && isSoundFile(soundFile)) {
			try {
				return WavFileInputStream.openInputStream(soundFile);
			}
			catch (UnsupportedAudioFileException | IOException e) {
				e.printStackTrace(); 
				// don't do anything and it will try the built in Audiosystem
				System.err.println("Could not open wav file: trying default audio stream: " + soundFile.getName()); 
			}
		}
		try {
			return AudioSystem.getAudioInputStream(soundFile);
		}
		catch (Exception e) {
			System.out.println("Error in audio file " + soundFile.getName() + ":  " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}


	public boolean isSoundFile(File soundFile) {
		String extension = FileUtils.getExtension(soundFile.getName()); 
		return (extension.equals(".wav"));
	}


}
