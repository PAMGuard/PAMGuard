package Acquisition.offlineFuncs;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.metadata.Metadata;
import org.jflac.metadata.StreamInfo;
import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.util.ByteData;

import Acquisition.filedate.FileDate;
import Acquisition.pamAudio.PamAudioSystem;
import PamController.OfflineFileDataStore;
import PamDetection.RawDataUnit;
import PamUtils.PamAudioFileFilter;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.OfflineDataMap;
import dataMap.filemaps.FileDataMapPoint;
import dataMap.filemaps.OfflineFileServer;
import pamScrollSystem.ViewLoadObserver;
import wavFiles.ByteConverter;

/**
 * This has been split off from OfflineFileServer so that OfflineFileServer can be used with 
 * other file types. 
 * @author dg50
 *
 */
public class OfflineWavFileServer extends OfflineFileServer<FileDataMapPoint> {

	private FileDate fileDate;

	private ByteConverter byteConverter;

	private AudioInputStream audioInputStream;
	private AudioFormat audioFormat;

	public OfflineWavFileServer(OfflineFileDataStore offlineRawDataStore, FileDate fileDate) {
		super(offlineRawDataStore);
		this.fileDate = fileDate;
	}

	public FileDate getFileDate() {
		return fileDate;
	}

	@Override
	public long[] getFileStartandEndTime(File file) {
		long[] t = new long[2];
		t[0] = fileDate.getTimeFromFile(file);
		return t;
	}

	@Override
	public FileDataMapPoint createMapPoint(File file, long startTime, long endTime) {
		if (endTime == 0) {
			endTime = startTime;
		}
		FileDataMapPoint mapPoint = new FileDataMapPoint(file, startTime, endTime);
		return mapPoint;
	}

	@Override
	public OfflineDataMap<FileDataMapPoint> createDataMap(OfflineFileServer<FileDataMapPoint> offlineFileServer,
			PamDataBlock pamDataBlock) {
		return new WavFileDataMap(offlineFileServer, pamDataBlock);
	}

	@Override
	public FileFilter getFileFilter() {
		return new PamAudioFileFilter();
	}

	@Override
	public void sortMapEndTimes() {
		OfflineDataMap<FileDataMapPoint> dataMap = this.getDataMap();
		Iterator<FileDataMapPoint> it = dataMap.getListIterator();
		FileDataMapPoint mapPoint;
		File file;
		int totalPoints = dataMap.getNumMapPoints();
		int opened = 0;
		while (it.hasNext()) {
			mapPoint = it.next();
			if (mapPoint.getMatchedPoint() != null) {
				// this shows that the map point has come back from the serialised file
				// so the time information will already be ok. 
				/*
				 * There was a bug #283, not entirely understood where wav files were being set with the
				 * same end and start times. I think this is because they are initially listed that
				 * way and then updated, but for some reason the update hadn't worked. In any case, 
				 * this will solve it, forcing it to re-get the wav duration if the current duration
				 * is zero. 
				 */
				if (mapPoint.getEndTime() > mapPoint.getStartTime()) {
					continue;
				}
			}
			file = mapPoint.getSoundFile();/*
			 * Now need to open file file as an input stream (what a bore !)
			 */
			AudioInputStream audioStream;
			long fileSamples = 0;
			long fileMillis = 0;
			if (file.getName().toLowerCase().endsWith(".flac")) {
				try {
					AudioInputStream flacFormat = new FlacAudioFileReader().getAudioInputStream(file);
					FLACDecoder flacDecoder = new FLACDecoder(flacFormat);
					Metadata metaData[] = flacDecoder.readMetadata();
					if (metaData != null) {
						for (int i = 0; i < metaData.length; i++) {
							if (StreamInfo.class.isAssignableFrom(metaData[i].getClass())) {
								fileSamples = ((StreamInfo) metaData[i]).getTotalSamples();
								float sampleRate = ((StreamInfo) metaData[i]).getSampleRate();
								fileMillis = (long) (fileSamples*1000/sampleRate);
								mapPoint.setEndTime(mapPoint.getStartTime() + fileMillis);
								break;
							}
						}						
					}
					flacFormat.close();
				} catch (UnsupportedAudioFileException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				try {
					audioStream = PamAudioSystem.getAudioInputStream(file);
					if (audioStream == null) {
						it.remove();
						continue;
					}
					AudioFormat audioFormat = audioStream.getFormat();
					audioStream.close();
					fileSamples = audioStream.getFrameLength();
					float sampleRate = audioFormat.getSampleRate();
					fileMillis = (long) (fileSamples*1000/sampleRate);
					mapPoint.setEndTime(mapPoint.getStartTime() + fileMillis);
				} catch (UnsupportedAudioFileException e) {
					System.out.println("UnsupportedAudioFileException reading file " + file.getName());
					//					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("IOException reading file " + file.getName());
					//					e.printStackTrace();
				} catch (Exception e) {
					System.out.println("Other Exception reading file " + file.getName());
					e.printStackTrace();
				}

			}
		}

	}

	private boolean loadWavData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) {

		//		Debug.out.println("OfflineFileServer: Load Wav Data: " + offlineDataLoadInfo.getCurrentObserver().getObserverName() );
		OfflineDataMap<FileDataMapPoint> dataMap = getDataMap();
		Iterator<FileDataMapPoint> mapIt = dataMap.getListIterator();
		FileDataMapPoint mapPoint = findFirstMapPoint(mapIt, offlineDataLoadInfo.getStartMillis(), offlineDataLoadInfo.getEndMillis());
		if (openSoundFile(mapPoint.getSoundFile()) == false) {
			System.out.println("Could not open .wav sound file " + mapPoint.getSoundFile().getAbsolutePath());
			return false;
		}
		File soundFile;

		byteConverter = ByteConverter.createByteConverter(audioFormat);
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
				skipped = audioInputStream.skip(skipBytes);
				//				Debug.out.println("Skipped " + skipped);
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
				bytesRead = audioInputStream.read(inputBuffer);
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
			ms = getOfflineRawDataStore().getParentProcess().absSamplesToMilliseconds(totalSamples);
			ms = currentTime + (long)(totalSamples * 1000 / (double) audioFormat.getSampleRate());

			for (int ichan = 0; ichan < nChannels; ichan++) {

				newDataUnit = new RawDataUnit(ms, 1 << ichan, totalSamples, newSamples);
				newDataUnit.setFileSamples(totalSamples + skipped / frameSize); //set the number samples into the wav file. 
				newDataUnit.setRawData(doubleData[ichan], true);

				//System.out.println("New wav data: " + PamCalendar.formatDateTime(newDataUnit.getTimeMilliseconds()));
				getRawDataBlock().addPamData(newDataUnit);
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

	private boolean loadFlacData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) { 
		//		System.out.println(String.format("Load flac data from "));
		/*
		 * Only seem able to read whole flac files - so read the whole file
		 * and just output samples of interest to data units. A bit crude, 
		 * but will probably not be too slow. 
		 */
		OfflineDataMap<FileDataMapPoint> dataMap = this.getDataMap();
		Iterator<FileDataMapPoint> mapIt = dataMap.getListIterator();
		FileDataMapPoint mapPoint;
		File soundFile;
		mapPoint = findFirstMapPoint(mapIt, offlineDataLoadInfo.getStartMillis(), offlineDataLoadInfo.getEndMillis());
		if (mapPoint == null) {
			return false;
		}
		while (mapPoint != null) {

			//		AudioInputStream inputStream;
			//		try {
			//			 inputStream = new FlacAudioFileReader().getAudioInputStream(mapPoint.getSoundFile());
			//		} catch (UnsupportedAudioFileException | IOException e1) {
			//			// TODO Auto-generated catch block
			//			e1.printStackTrace();
			//		}
			FileInputStream fileStream;
			try {
				fileStream = new FileInputStream(mapPoint.getSoundFile());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return false;
			}
			FLACDecoder flacDecoder = new FLACDecoder(fileStream);
			//		Metadata metaData[] = null;
			//		try {
			//			metaData = flacDecoder.readMetadata();
			//		} catch (IOException e) {
			//			// TODO Auto-generated catch block
			//			e.printStackTrace();
			//		}
			flacDecoder.addPCMProcessor(new FlacPCM(mapPoint, dataBlock, fileStream, 
					offlineDataLoadInfo, loadObserver));
			//		StreamInfo streamInfo = flacDecoder.getStreamInfo(); // returns null !
			try {
				flacDecoder.decode();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//			e.printStackTrace();
				return false;
			}
			if (mapIt.hasNext() == false) {
				break;
			}
			mapPoint = mapIt.next();
			if (mapPoint.getStartTime() > offlineDataLoadInfo.getEndMillis()) {
				break;
			}
		}
		return true;
	}

	/**
	 * Only seem to be able to 
	 * @author doug
	 *
	 */
	private class FlacPCM implements PCMProcessor {

		private FileInputStream fileStream;
		private int frameSize;
		private long fileSamples;
		private int nChannels;
		private FileDataMapPoint mapPoint;
		private int totalSamples;
		private int sampleRate;
		private ViewLoadObserver loadObserver;
		private PamRawDataBlock dataBlock;
		private OfflineDataLoadInfo offlineDataLoadInfo;


		long ms=-1; 

		public FlacPCM(FileDataMapPoint mapPoint, PamDataBlock dataBlock, FileInputStream fileStream, OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
			this.mapPoint = mapPoint;
			this.dataBlock = (PamRawDataBlock) dataBlock;
			this.fileStream = fileStream;
			this.loadObserver = loadObserver;
			this.offlineDataLoadInfo=offlineDataLoadInfo; 
		}

		@Override
		public void processPCM(ByteData byteData) {
			if (offlineDataLoadInfo.cancel) {

				try {
					fileStream.close();
					offlineDataLoadInfo.setLastLoadInfo(new AquisitionLoadPoint(ms, -1)); 
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}

			ms = mapPoint.getStartTime() + (long)totalSamples * 1000L / (long)sampleRate;
			//			System.out.println(String.format("PCM data at %s samps %d (load %s - %s)", PamCalendar.formatTime2(ms, 3), totalSamples, 
			//					PamCalendar.formatTime2(dataStart, 3), PamCalendar.formatTime2(dataEnd, 3)));
			if (ms > offlineDataLoadInfo.getEndMillis()) {
				try {
					fileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			int newSamples = byteData.getLen() / frameSize;
			if (ms >= offlineDataLoadInfo.getStartMillis()) {
				double[][] doubleData = new double[nChannels][newSamples];
				byteConverter.bytesToDouble(byteData.getData(), doubleData, byteData.getLen());
				RawDataUnit newDataUnit = null;
				for (int ichan = 0; ichan < nChannels; ichan++) {

					newDataUnit = new RawDataUnit(ms, 1 << ichan, totalSamples, newSamples);
					newDataUnit.setRawData(doubleData[ichan]);

					dataBlock.addPamData(newDataUnit);

					// GetOutputDataBlock().addPamData(pamDataUnit);
				}
			}
			//		System.out.println(String.format("new samps %d at %s", newSamples, PamCalendar.formatTime(ms, 3)));

			totalSamples += newSamples;

		}

		@Override
		public void processStreamInfo(StreamInfo streamInfo) {
			frameSize = streamInfo.getChannels() * streamInfo.getBitsPerSample() / 8;
			byteConverter = ByteConverter.createByteConverter(streamInfo.getBitsPerSample() / 8, false, Encoding.PCM_SIGNED);
			fileSamples = streamInfo.getTotalSamples();
			nChannels = streamInfo.getChannels();
			sampleRate = streamInfo.getSampleRate();
		}

	}

	private boolean openSoundFile(File soundFile) {

		try {
			audioInputStream = PamAudioSystem.getAudioInputStream(soundFile);
			if (audioInputStream == null) return false;
			audioFormat = audioInputStream.getFormat();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean loadData(PamDataBlock dataBlock, OfflineDataLoadInfo offlineLoadDataInfo, ViewLoadObserver loadObserver) {
		//		Debug.out.println(String.format("Request raw data from %s to %s", PamCalendar.formatDateTime(dataStart),
		//				PamCalendar.formatTime(dataEnd)));
		/*
		 * Find the data mapped files, work through them and load data as appropriate, forming into 
		 * standard sized data units and passing off into the dataBlock.
		 * 
		 * ***** Note that this function doesn't clear data units and probably isn't meant to 
		 * so that subsequent calls can be made. However, this is quite risky ! So unless you're very
		 * sure that it's going to be called for sequential block of data make sure you call clearall for the
		 * datablock in question to avoid calamity. 
		 */
		OfflineDataMap<FileDataMapPoint> dataMap = getDataMap();
		FileDataMapPoint mapPoint = findFirstMapPoint( dataMap.getListIterator(), 
				offlineLoadDataInfo.getStartMillis(), offlineLoadDataInfo.getEndMillis());
		if (mapPoint == null) {
			return false;
		}
		if (mapPoint.getSoundFile().getName().toLowerCase().endsWith(".flac")) {
			return loadFlacData(dataBlock, offlineLoadDataInfo, loadObserver);
		}
//		if (mapPoint.getSoundFile().getName().toLowerCase().endsWith(".wav")) {
		if (isStandardAudio(mapPoint.getSoundFile().getName())) {
			return loadWavData(dataBlock, offlineLoadDataInfo, loadObserver);
		}

		return true;
	}

	private String[] standardAudioTypes = {".wav", ".aif", ".aiff"};
	
	/**
	 * Is it a standard aif or wav audio file which can be opened with the standard reader ? 
	 * @param fileName
	 * @return
	 */
	private boolean isStandardAudio(String fileName) {
		if (fileName == null) {
			return false;
		}
		fileName = fileName.toLowerCase();
		for (int i = 0; i < standardAudioTypes.length; i++) {
			if (fileName.endsWith(standardAudioTypes[i])) {
				return true;
			}
		}
		return false;
	}
}
