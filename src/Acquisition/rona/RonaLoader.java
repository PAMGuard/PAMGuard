package Acquisition.rona;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

//import org.kc7bfi.jflac.FLACDecoder;
import org.jflac.FLACDecoder;
import Acquisition.RonaInputSystem;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.filemaps.FileDataMapPoint;

/**
 * Functions to automatically bring together and load sections of 
 * raw data from the single channel Rona FLAC files. 
 * @author Doug Gillespie
 *
 */
public class RonaLoader {

	private RonaInputSystem ronaInputSystem;
	
	private List<FileDataMapPoint> mapPoints;
	
	private long startTime, endTime;
	
	private PamRawDataBlock rawDataBlock;
	
	private volatile OfflineDataLoadInfo offlineLoadDataInfo;
	
	private RonaGatherer ronaGatherer;

	public RonaLoader(RonaInputSystem ronaInputSystem,
			List<FileDataMapPoint> mapPoints,
			PamRawDataBlock rawDataBlock,
			OfflineDataLoadInfo offlineLoadDataInfo) {
		super();
		this.ronaInputSystem = ronaInputSystem;
		this.mapPoints = mapPoints;
		this.rawDataBlock = rawDataBlock;
		this.offlineLoadDataInfo=offlineLoadDataInfo; 
	}
	
	public boolean loadData(long startTime, long endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
		int nChan = ronaInputSystem.getChannels();
		ronaGatherer = new RonaGatherer(rawDataBlock, nChan, offlineLoadDataInfo);
		Thread[] chanThreads = new Thread[nChan];
		for (int i = 0; i < nChan; i++) {
			ChannelThread chanThread = new ChannelThread(i);
			chanThreads[i] = new Thread(chanThread);
			chanThreads[i].start();
		}
		
		/*
		 * Have to move the data units on in this thread, from the gatherer or 
		 * we get a synch problem since this call is currently stuck inside a 
		 * block synchronised on  this thread. 
		 */
		while (chanThreads[0].isAlive()) {
			if (shouldCancel()) break;
			if (ronaGatherer.readQueue() == 0) {
				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
//					e.printStackTrace();
				}
			}
		}
		
		for (int i = 0; i < nChan; i++) {
			try {
				chanThreads[i].join();
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}
		}
		return true;
	}
	
	private boolean shouldCancel() {
		return (offlineLoadDataInfo.cancel);
	}
	
	private class ChannelThread implements Runnable {

		int channelIndex;
		private Iterator<FileDataMapPoint> mapIterator;
		private FileInputStream fileStream;
		private FLACDecoder flacDecoder;
		
		public ChannelThread(int channelIndex) {
			super();
			this.channelIndex = channelIndex;
		}


		@Override
		public void run() {
			// make an iterator over the mappoints
			mapIterator = mapPoints.iterator();
			while (mapIterator.hasNext()) {
				if (shouldCancel()) break;
				FileDataMapPoint mapPoint = mapIterator.next();
				File baseFile = mapPoint.getSoundFile();
				File channelFile = ronaInputSystem.findChannelFile(baseFile, channelIndex, 2);
				if (channelFile == null ) {
					offlineLoadDataInfo.cancel = true;
					break;
				}
				try {
					fileStream = new FileInputStream(channelFile);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
						offlineLoadDataInfo.cancel = true;
					break;
				}

				flacDecoder = new FLACDecoder(fileStream);
				RonaFlacProcessor pcmProcessor;
				flacDecoder.addPCMProcessor(pcmProcessor = new RonaFlacProcessor(fileStream, mapPoint.getStartTime(), 
						ronaInputSystem.getSampleRate(), channelIndex, ronaGatherer));
				pcmProcessor.setStartAndEnd(startTime, endTime);
//				System.out.println(String.format("Start decode chan %d from %s to %s", channelIndex, 
//						PamCalendar.formatDateTime(startTime), PamCalendar.formatDateTime(endTime)));
//				long t1 = System.currentTimeMillis();
				try {
					flacDecoder.decode();
				}
				catch (IOException e) {
					// ends here if the decoder decides we've enough data
					// and closes the file to force it to crap out.
					break;
				}
//				System.out.println(String.format("End decode chan %d after %d millis", channelIndex, 
//						System.currentTimeMillis() - t1));
				
			}
			
		}
	}

}
