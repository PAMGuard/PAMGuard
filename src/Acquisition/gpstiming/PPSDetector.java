package Acquisition.gpstiming;

import Acquisition.AcquisitionParameters;
import Acquisition.AcquisitionProcess;
import Acquisition.DaqStatusDataUnit;
import GPS.GPSParameters;
import GPS.GpsData;
import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.time.ntp.PamNTPTime;
import PamUtils.time.ntp.PamNTPTimeException;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;

/**
 * Class to detect and manage PPS information from GPS. 
 * @author Doug
 *
 */
public class PPSDetector {

	private AcquisitionProcess daqProcess;
	private PPSParameters ppsParams;
	private int ppsChannelMap;
	private boolean pulseState = false;
//	private long lastEdgeSample;
	long lastEdgeTime;
	long lastEdgeBufferedSamples;
//	private NMEAObserver nmeaObserver = new NMEAObserver();
	private NMEADataBlock nmeaDataBlock;
	private long nextStorageTime = 0;
	private static final int NSAVED = 5;
	private SavedSet[] savedSets = new SavedSet[NSAVED];
	private int savePos = 0;

	public PPSDetector(AcquisitionProcess daqProcess) {
		super();
		this.daqProcess = daqProcess;
	}

	public void prepare(AcquisitionParameters daqParams) {
		ppsParams = daqParams.getPpsParameters();
		if (ppsParams == null || ppsParams.useGpsPPS == false) {
			return;
		}
		ppsChannelMap = 1<<ppsParams.gpsPPSChannel;
//		lastEdgeSample = 0;
		nextStorageTime = 0;

		/**
		 * Reset the save store. 
		 */
		for (int i = 0; i < NSAVED; i++) {
			saveSet(null);
		}
		savePos = 0;
		
		//attach the NMEAObserver to the appropriate source. 
//		if (nmeaDataBlock != null) {
////			nmeaDataBlock.deleteObserver(nmeaObserver);
//			nmeaDataBlock = null;
//		}
		try {
			nmeaDataBlock = (NMEADataBlock) PamController.getInstance().getDataBlock(NMEADataUnit.class, ppsParams.gpsPPSNmeaSource);
		}
		catch (Exception e) {

		}
//		if (nmeaDataBlock != null) {
//			nmeaDataBlock.addObserver(nmeaObserver, false);
//		}
	}

	public void newData(RawDataUnit rawDataUnit) {
		if (ppsParams == null || ppsParams.useGpsPPS == false) {
			return;
		}
		if (rawDataUnit.getChannelBitmap() != ppsChannelMap) {
			return;
		}
		if (rawDataUnit.getMeasuredAmplitude() < Math.abs(ppsParams.gpsPPSThreshold)) {
			pulseState = false;
			return;
		}
		if (rawDataUnit.getTimeMilliseconds() < nextStorageTime) {
			return;
		}
		//		System.out.println("Measured Amplitude = " + rawDataUnit.getMeasuredAmplitude());
		if (pulseState == true) {
			return;
		}
		double[] rawData = rawDataUnit.getRawData();
		long edgeSample = -1;
		if (ppsParams.gpsPPSThreshold > 0) {
			for (int i = 0; i < rawData.length; i++) {
				if (rawData[i] > ppsParams.gpsPPSThreshold) {
					edgeSample = i+rawDataUnit.getStartSample();
//					System.out.println(String.format("%s Edge at sample %d (%d from previous)", 
//							PamCalendar.formatTime(PamCalendar.getTimeInMillis(), true),
//							edgeSample, edgeSample-lastEdgeSample));
					pulseState = true;
					if (findEdgeUTC(rawDataUnit, edgeSample)) {
						nextStorageTime = rawDataUnit.getTimeMilliseconds() + (ppsParams.storageInterval)*1000-500;
					}
					break;
				}
			}
		}
		else {
			for (int i = 0; i < rawData.length; i++) {
				if (rawData[i] < ppsParams.gpsPPSThreshold) {
					edgeSample = i+rawDataUnit.getStartSample();
//					System.out.println(String.format("%s Edge at sample %d (%d from previous)", 
//							PamCalendar.formatTime(PamCalendar.getTimeInMillis(), true),
//							edgeSample, edgeSample-lastEdgeSample));
//					lastEdgeSample = edgeSample;
					pulseState = true;
					if (findEdgeUTC(rawDataUnit, edgeSample)) {
						nextStorageTime = rawDataUnit.getTimeMilliseconds() + (ppsParams.storageInterval)*1000-500;
					}
					break;
				}			
			}
		}
	}

	private boolean findEdgeUTC(RawDataUnit rawDataUnit, long edgeSample) {
		long now = PamCalendar.getTimeInMillis(); 
		long bufferedSamples = daqProcess.getNewDataQueue().getQueuedSamples(ppsParams.gpsPPSChannel);
		long unusedSamples = rawDataUnit.getLastSample()-edgeSample;
		long bufferedMillis = (long) ((bufferedSamples+unusedSamples) * 1000 / daqProcess.getSampleRate());
		NMEADataUnit nmeaDataUnit = nmeaDataBlock.findNMEADataUnit("GPRMC");
		if (nmeaDataUnit == null || nmeaDataUnit.getTimeMilliseconds() < now - 1500) {
			return false;
		}
		long edgeTime = now - bufferedMillis; // true start time of the edge re current PC clock. 
		/*
		 * Unpack GPS data
		 */
		GpsData gpsData = new GpsData(nmeaDataUnit.getCharData(), GPSParameters.READ_RMC);
		long gpsUTC = gpsData.getTimeInMillis(); // this is the time read from the GPS
		long gpsPCTime = nmeaDataUnit.getTimeMilliseconds(); // this is the corrected UTC time (e.g. from NMEA or from NTP)
		/*
		 * Need GPS to be on a second boundary - so if it's got millis, remove from both the above
		 * rounding down the GPS actual time and the PC time to the nearest second. 
		 */
		long gpsMillis = gpsUTC%1000;
		gpsUTC -= gpsMillis;  
		gpsPCTime -= gpsMillis;
		/*
		 * Now the fun part. If there was no buffer delay on the audio data, then the most likely scenario is that
		 * the GPS time will be from the second before the PPS edge. If on the other hand, the audio data were 
		 * delayed in the buffer, then the PPS data could refer to a time several seconds earlier than the latest GPS
		 * data. 
		 * edgeTime refers to the actual time of the PC clock at the time the edge would have arrived. Ideally the gpsPCTime time 
		 * should be somewhere between that time and about .6 seconds later. 
		 * Note that since the PC clock will be wrong, there is no reason at all to assume that it's aligned on a second, though 
		 * of course the gpsUTC and ultimately the PPS time will be. 
		 * So while gpsPCTime < edgeTime, add a second to it. 
		 * while gpsPCTime > edgeTime + 1s, subtract a second from it
		 * then give up if it's still ambiguous as to where it should lie. 
		 */
//		System.out.printf("GPS - ADC time is %d millis\n", gpsPCTime - edgeTime);
		long gpsOffset = 0;
		while (gpsPCTime+gpsOffset < edgeTime) {
			gpsOffset += 1000;
		}
		while (gpsPCTime + gpsOffset >= edgeTime + 1000) {
			gpsOffset -= 1000;
		}
		//give up if it's too far away
		if (gpsOffset > 1000) {
//			System.out.println("GPS time is more than a second earlier than the latest edge time so cannot be used");
			return false;
		}
		if (gpsOffset < -bufferedMillis){
//			System.out.println("GPS time further off than the length of the audio buffer which doens't make sense !");
			return false;
		}
		// so far as I can work out, these are the true UTC and PC times of the GPS signal. 
		gpsUTC += gpsOffset;
		gpsPCTime += gpsOffset;
		// gpsPCTime should come just after the PPS. 
		if (gpsPCTime < edgeTime || gpsPCTime >edgeTime + 600) {
//			System.out.printf("Edge time %d and gpsTime %d (diff %d, offs %d) can't match up.\n", 
//					edgeTime%100000, gpsPCTime%100000, gpsPCTime-edgeTime, gpsOffset);
			return false;
		}
		/*
		 *  if we survive to here then I think that the edge time for the number of samples acquired
		 *  should be exactly the gpsUTC time, lying on a 1s boundary. 
		 *  
		 *  however, we will do a couple of final checks to see how this matches up with 
		 *  previous measurements. 
		 */
		int ok = 0;
		for (int i = 0; i < 2; i++) {
			SavedSet prevSet = getSavedSet(-i);
			if (prevSet == null) {
				continue;
			}
			long msDiff = gpsUTC-prevSet.gpsTime;
			long sampleDiff = edgeSample-prevSet.sampleNo;
			long sampleMSDiff = (long) (sampleDiff * 1000 / daqProcess.getSampleRate());
			if (Math.abs(sampleMSDiff-msDiff) < 100) {
				ok++;
			}
			else {
				System.out.printf("PPS Mismatch at %s msDiff=%d, sampleMSDiff=%d\n", PamCalendar.formatDateTime(gpsUTC), msDiff, sampleMSDiff);
			}
		}
		saveSet(new SavedSet(edgeSample, gpsUTC));
		if (ok < 2) {
			return false;
		}
		
		Long serverTime = null;
//		try {
//			serverTime = PamNTPTime.getInstance().getNTPTime();
//			// and correct for the remaining sample that are in buffers or come after the edgeTime
//			serverTime -= bufferedMillis;
//		} catch (PamNTPTimeException e) {
//			serverTime = null;
//		}
		
//		System.out.printf("GPS PPS Edge time set at %s, gpsOffset %d, buffMillis %d\n", PamCalendar.formatDateTime(gpsUTC), gpsOffset, bufferedMillis);
		daqProcess.recordPPSOffset(edgeTime, edgeSample, gpsUTC, serverTime);

		return true;
	}
	
	/**
	 * Retrieve a saved set of times. relPos 0 is always invalid
	 * relPos = -1 will give the last set saved, etc.  
	 * @param relPos relative position
	 * @return a saved set.
	 */
	private SavedSet getSavedSet(int relPos){
		int pos = savePos+relPos;
		if (pos < 0) pos += NSAVED;
		return savedSets[pos];
	}
	
	private void saveSet(SavedSet savedSet) {
		savedSets[savePos++] = savedSet;
		if (savePos >= NSAVED) {
			savePos -= NSAVED;
		}
	}
	
	private class SavedSet {
		long sampleNo;
		public SavedSet(long sampleNo, long gpsTime) {
			super();
			this.sampleNo = sampleNo;
			this.gpsTime = gpsTime;
		}
		long gpsTime;
		
	}
//	class NMEAObserver implements PamObserver {
//
//		@Override
//		public long getRequiredDataHistory(PamObservable o, Object arg) {
//			return 0;
//		}
//
//		@Override
//		public void update(PamObservable o, PamDataUnit arg) {
//			NMEADataUnit nmeaDataUnit = (NMEADataUnit) arg;
//			String nmeaStringId = nmeaDataUnit.getStringId();
//			if (nmeaStringId.contains("RMC")) {
//				//				System.out.println(PamCalendar.formatTime(PamCalendar.getTimeInMillis(), true) + " " + nmeaDataUnit.getCharData());
//				GpsData gpsData = new GpsData(nmeaDataUnit.getCharData(), GPSParameters.READ_RMC);
//			}
//		}
//
//		@Override
//		public void removeObservable(PamObservable o) {
//		}
//
//		@Override
//		public void setSampleRate(float sampleRate, boolean notify) {
//		}
//
//		@Override
//		public void noteNewSettings() {
//		}
//
//		@Override
//		public String getObserverName() {
//			return null;
//		}
//
//		@Override
//		public void masterClockUpdate(long milliSeconds, long sampleNumber) {			
//		}
//
//		@Override
//		public PamObserver getObserverObject() {
//			return this;
//		}
//
//	}

}
