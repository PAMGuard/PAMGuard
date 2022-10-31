package Acquisition.pamAudio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFormat.Encoding;

import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.metadata.StreamInfo;
import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.util.ByteData;

import Acquisition.offlineFuncs.AquisitionLoadPoint;
import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.OfflineDataMap;
import dataMap.filemaps.FileDataMapPoint;
import dataMap.filemaps.OfflineFileServer;
import pamScrollSystem.ViewLoadObserver;
import wavFiles.ByteConverter;


/**
 * Opens a Flac compressed audio files. 
 * 
 * @author Jamie Macaulay, Doug Gillespie
 *
 */
public class FlacAudioFile implements PamAudioFileLoader {
	

	/**
	 * Get the file extensions associated with loading these data. 
	 */
	private ArrayList<String> fileExtensions; 

	public FlacAudioFile() {
		fileExtensions = new ArrayList<String>(Arrays.asList(new String[]{".flac"})); 
	}

	@Override
	public ArrayList<String> getFileExtensions() {
		return fileExtensions;
	}

	@Override
	public String getName() {
		return "FLAC";
	}

	@Override
	public AudioInputStream getAudioStream(File soundFile) {
		if (soundFile != null && isFlacFile(soundFile)) {
			try {
				return new FlacAudioFileReader().getAudioInputStream(soundFile);
			}
			catch (UnsupportedAudioFileException | IOException e) {
				System.err.println("Could not open flac file: " + soundFile.getName());
				return null;
			}
		}
		return null; 
	}

	@Override
	public boolean loadAudioData(OfflineFileServer offlineFileServer, PamDataBlock dataBlock, OfflineDataLoadInfo offlineDataLoadInfo,
			ViewLoadObserver loadObserver) { 
		//		System.out.println(String.format("Load flac data from "));
		/*
		 * Only seem able to read whole flac files - so read the whole file
		 * and just output samples of interest to data units. A bit crude, 
		 * but will probably not be too slow. 
		 */
		OfflineDataMap<FileDataMapPoint> dataMap = offlineFileServer.getDataMap();
		Iterator<FileDataMapPoint> mapIt = dataMap.getListIterator();
		FileDataMapPoint mapPoint;
		File soundFile;
		mapPoint = offlineFileServer.findFirstMapPoint(mapIt, offlineDataLoadInfo.getStartMillis(), offlineDataLoadInfo.getEndMillis());
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
		private ByteConverter byteConverter; 

		public FlacPCM(FileDataMapPoint mapPoint, PamDataBlock dataBlock, FileInputStream fileStream, OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
			this.mapPoint = mapPoint;
			this.dataBlock = (PamRawDataBlock) dataBlock;
			this.fileStream = fileStream;
			this.loadObserver = loadObserver;
			this.offlineDataLoadInfo=offlineDataLoadInfo; 
			//			System.out.printf("New FLAC decoder %s working from %s to %s\n", offlineDataLoadInfo.toString(),
			//					PamCalendar.formatDBDateTime(offlineDataLoadInfo.getStartMillis()), 
			//					PamCalendar.formatDBDateTime(offlineDataLoadInfo.getEndMillis()));
		}

		@Override
		public void processPCM(ByteData byteData) {
			if (offlineDataLoadInfo.cancel) {

				try {
					fileStream.close();
					ms = Math.max(offlineDataLoadInfo.getStartMillis(), ms);
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

	private static boolean isFlacFile(File file) {
		String name = file.getName();
		if (name.length() < 5) {
			return false;
		}
		String end = name.substring(name.length()-5).toLowerCase();
		return (end.equals(".flac"));
	}

}
