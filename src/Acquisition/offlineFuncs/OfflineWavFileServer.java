package Acquisition.offlineFuncs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
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
import PamUtils.worker.filelist.WavFileType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import clickDetector.WindowsFile;
import dataMap.OfflineDataMap;
import dataMap.filemaps.FileDataMapPoint;
import dataMap.filemaps.FileSubSection;
import dataMap.filemaps.OfflineFileServer;
import pamScrollSystem.ViewLoadObserver;
import wavFiles.ByteConverter;
import wavFiles.WavHeader;
import wavFiles.xwav.HarpCycle;
import wavFiles.xwav.HarpHeader;

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
		// need to see if it's an xwav file from a HARP that would contain additional header data. 
		HarpHeader harpHeader = null;
		if (file.getName().toLowerCase().endsWith(".x.wav")) {
			harpHeader = getHarpHeader(file);
		}
		if (harpHeader == null || harpHeader.harpCycles == null) {
			FileDataMapPoint mapPoint = new FileDataMapPoint(file, startTime, endTime);
			/*
			 * Create a single map point as normal. 
			 */
			return mapPoint;
		}
		else {
			//make many map points and add them.
			ArrayList<HarpCycle> cycles = harpHeader.harpCycles;
			OfflineDataMap<FileDataMapPoint> map = getDataMap();
			for (int i = 0; i < cycles.size(); i++) {
				HarpCycle harpCycle = cycles.get(i);
				/**
				 * It's cycled HARP data so make lots of map points and add them to the map 
				 * here, then return null so that the functions in the main map maker (OfflineFileServer)
				 * doesn't add anything itself. 
				 */
				FileSubSection subSection = new FileSubSection(harpCycle.getByteLoc(), harpCycle.getByteLoc()+harpCycle.getByteLength());
				WavFileType harpFile = new WavFileType(file);
				harpFile.setSamplesOffset(harpCycle.getSamplesSkipped());
				harpFile.setMaxSamples(harpCycle.getDurationMillis() * harpCycle.getSampleRate() / 1000);
				FileDataMapPoint mapPoint = new FileDataMapPoint(harpFile, harpCycle.gettMillis(), harpCycle.getEndMillis());
				mapPoint.setFileSubSection(subSection);
				map.addDataPoint(mapPoint);
			}
		}
		return null;
	}

	private HarpHeader getHarpHeader(File file) {		
		WavHeader wavHeader = new WavHeader();
		WindowsFile windowsFile;
		try {
			windowsFile = new WindowsFile(file, "r");
			if (wavHeader.readHeader(windowsFile) == false) {
				return null;
			}
			windowsFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return null;
		}
		HarpHeader harpHeader = wavHeader.getHarpHeader();
		if (harpHeader == null) {
			return null;
		}
		if (harpHeader.harpCycles == null || harpHeader.harpCycles.size() == 0) {
			return null;
		}
		return harpHeader;
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
			if (mapPoint.getFileSubSection() != null) {
				// already sorted for xwav viles, so do nothing more
				continue;
			}
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
		
		/**
		 * This just gets a loader based on the types of audio file. These are a bit different
		 * for wav, flac, and SUD files. The loading itself of multiple map points is handled a 
		 * bit later in the call to loadAudioData, which will iterate again through the file map. 
		 */
		PamAudioFileLoader audioFile = PamAudioFileManager.getInstance().getAudioFileLoader(mapPoint.getSoundFile()); 
		
		if (audioFile==null) {
			System.err.println("OfflineWavFileServer: could not find audio loader for mapped sound file: " + mapPoint.getSoundFile()); 
			return false; 
		}
		
		return audioFile.loadAudioData(this, dataBlock, offlineLoadDataInfo, loadObserver); 
		
	}

}
