package Acquisition.offlineFuncs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.jflac.FLACDecoder;
import org.jflac.metadata.Metadata;
import org.jflac.metadata.StreamInfo;
import org.jflac.sound.spi.FlacAudioFileReader;
import Acquisition.filedate.FileDate;
import Acquisition.pamAudio.PamAudioFileManager;
import Acquisition.pamAudio.PamAudioFileLoader;
import Acquisition.pamAudio.PamAudioFileFilter;
import PamController.OfflineFileDataStore;
import PamController.fileprocessing.StoreStatus;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.OfflineDataMap;
import dataMap.filemaps.FileDataMapPoint;
import dataMap.filemaps.OfflineFileServer;
import pamScrollSystem.ViewLoadObserver;
import wavFiles.ByteConverter;

/**
 * This has been split off from OfflineFileServer so that OfflineFileServer can be used with 
 * other file types. 
 * @author Doug Gillespie
 *
 */
public class OfflineWavFileServer extends OfflineFileServer<FileDataMapPoint> {

	private FileDate fileDate;

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
			file = mapPoint.getSoundFile();
			
			//TODO - this should be integrated into the PamAudioFileManager. 
			/*
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
					audioStream =  PamAudioFileManager.getInstance().getAudioInputStream(file);
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
		
		PamAudioFileLoader audioFile = PamAudioFileManager.getInstance().getAudioFileLoader(mapPoint.getSoundFile()); 
		
		if (audioFile==null) {
			System.err.println("OfflineWavFileServer: could not find audio loader for mapped sound file: " + mapPoint.getSoundFile()); 
			return false; 
		}
		
		return audioFile.loadAudioData(this, dataBlock, offlineLoadDataInfo, loadObserver); 
		
	}

}
