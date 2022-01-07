package qa;

import java.util.Hashtable;

import PamUtils.LatLong;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import qa.generator.QASound;
import qa.generator.sequence.SequenceData;
import qa.generator.sequence.SoundSequence;

public class QASoundDataUnit extends QADataUnit implements AcousticDataUnit {

	private QASound qaSound;
	
	/*
	 * Flag to say this is multipath - 0 = first arrival, 1 = echo, etc.
	 */
	private int multiPath;
	
	/**
	 * First and last of all samples in sound. 
	 */
	private long firstSample, lastSample;

	/**
	 * Arrival times of sound in milliseconds on all hydrophones. 
	 */
	private long arrivalStartMillis, arrivalEndMillis;
	
	private Hashtable<PamDataBlock, Integer> detectorsList;
	
	private double receivedLevel;
	
	/**
	 * State of the sound during generation - used to speed up skipping units not in current time window
	 */
	private int state = SOUND_NOT_STARTED;

	/**
	 * Coordinates for the source and for the receiver reference at the time 
	 * the sound was generated. 
	 */
	private LatLong referenceLatLong;
	private SoundSequence soundSequence;
	private SequenceData sequenceData;

	/**
	 * Distance to nearest airgun
	 */
	private Double distanceToAirgun;
	/**
	 * distance to nearest hydrophone
	 */
	private Double distanceToHydrophone;
	
	public static final int SOUND_NOT_STARTED = 0;
	public static final int SOUND_GENERATING = 1;
	public static final int SOUND_COMPLETE = 2;

	public QASoundDataUnit(SoundSequence soundSequence, SequenceData sequenceData, long timeMilliseconds, 
			int channelBitmap, long startSample, QASound qaSound, double receivedLevel) {
		super(timeMilliseconds, channelBitmap, startSample, 0);
		this.soundSequence = soundSequence;
		this.sequenceData = sequenceData;
		this.qaSound = qaSound;
		this.receivedLevel = receivedLevel;
		if (qaSound != null) { // null in viewer
			this.setFrequency(qaSound.getFrequencyRange());
			long[] fs = qaSound.getFirstSamples();
			double[][] wave = qaSound.getWave();
			firstSample = fs[0];
			lastSample = fs[0] + wave[0].length;
			for (int i = 1; i < fs.length; i++) {
				firstSample = Math.min(fs[i], firstSample);
				lastSample = Math.max(lastSample, fs[i] + wave[i].length);
			}
		}
	}

	/**
	 * @return the standardSound
	 */
	public QASound getStandardSound() {
		return qaSound;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
		if (state == QASoundDataUnit.SOUND_COMPLETE) {
			qaSound.clearWave();
		}
	}

	/**
	 * @return the multiPath
	 */
	public int getMultiPath() {
		return multiPath;
	}

	/**
	 * @param multiPath the multiPath to set
	 */
	public void setMultiPath(int multiPath) {
		this.multiPath = multiPath;
	}

	/**
	 * @return the firstSample
	 */
	public long getFirstSample() {
		return firstSample;
	}

	/**
	 * @return the lastSample
	 */
	@Override
	public long getLastSample() {
		return lastSample;
	}

	public void setArrivalMillis(long arrivalStartMillis, long arrivalEndMillis) {
		this.arrivalStartMillis = arrivalStartMillis;
		this.arrivalEndMillis = arrivalEndMillis;
	}

	/**
	 * @return the arrivalStartMillis
	 */
	public long getArrivalStartMillis() {
		return arrivalStartMillis;
	}

	/**
	 * @param arrivalStartMillis the arrivalStartMillis to set
	 */
	public void setArrivalStartMillis(long arrivalStartMillis) {
		this.arrivalStartMillis = arrivalStartMillis;
	}

	/**
	 * @return the arrivalEndMillis
	 */
	public long getArrivalEndMillis() {
		return arrivalEndMillis;
	}

	/**
	 * @param arrivalEndMillis the arrivalEndMillis to set
	 */
	public void setArrivalEndMillis(long arrivalEndMillis) {
		this.arrivalEndMillis = arrivalEndMillis;
	}

	/**
	 * Set 'hit' status for a detector. 
	 * @param dataBlock datablock containing the detection
	 * @param detection detection data unit. 
	 * @return true if the underlying data were updated. false if they remained the same. 
	 */
	public boolean setDetectorHit(PamDataBlock dataBlock, PamDataUnit detection) {
		return setDetectorHit(dataBlock, detection.getChannelBitmap());
	}
	/**
	 * Set 'hit' status for a detector. 
	 * @param dataBlock datablock containing the detection
	 * @param channelMap detection channel map. 
	 * @return true if the underlying data were updated. false if they remained the same. 
	 */
	public boolean setDetectorHit(PamDataBlock dataBlock, int channelMap) {
		if (detectorsList == null) {
			detectorsList = new Hashtable<>();
		}
		if (channelMap == 0) channelMap = 1; // should never be the case - apart from manual entries which might have 0 map. 
		int existing = getDetectorHit(dataBlock);
		if (existing == 0) {
			detectorsList.put(dataBlock, channelMap);
			return true; // no update - new data 
		}
		else if (channelMap != existing){
			detectorsList.put(dataBlock, existing | channelMap);
			return true;
		}
		return false;
		
//		boolean hasAlready = (getDetectorHit(dataBlock) == detection);
//		if (hasAlready) {
//			return false;
//		}
//		else {
//			detectorsList.put(dataBlock, detection);
//			return true;
//		}
	}
	
	/**
	 * Get whether or not this simulated data has been detected by 
	 * a particular detector.  
	 * @param dataBlock datablock for the detector
	 * @return 0 if no detection, otherwise the UID of the detection. 
	 */
	public int getDetectorHit(PamDataBlock dataBlock) {
		if (detectorsList == null) {
			return 0;
		}
		Integer ans = detectorsList.get(dataBlock);
		if (ans == null) {
			return 0;
		}
		else {
			return ans;
		}
	}
	
	public int getNumDetectorHits() {
		if (detectorsList == null) {
			return 0;
		}
		return detectorsList.size();
	}

	/**
	 * @return the latLong
	 */
	public LatLong getSourceLatLong() {
		return soundSequence.getSourceLocation();
	}

	/**
	 * @return the referenceLatLong
	 */
	public LatLong getReferenceLatLong() {
		return referenceLatLong;
	}

	/**
	 * @return the soundSequence
	 */
	public SoundSequence getSoundSequence() {
		return soundSequence;
	}

	/**
	 * @return the sequenceData
	 */
	public SequenceData getSequenceData() {
		return sequenceData;
	}

	/**
	 * @return the receivedLevel
	 */
	public double getReceivedLevel() {
		return receivedLevel;
	}

	/**
	 * @return the distanceToAirgun
	 */
	public Double getDistanceToAirgun() {
		return distanceToAirgun;
	}

	/**
	 * @param distanceToAirgun the distanceToAirgun to set
	 */
	public void setDistanceToAirgun(Double distanceToAirgun) {
		this.distanceToAirgun = distanceToAirgun;
	}

	/**
	 * @return the distanceToHydrophone
	 */
	public Double getDistanceToHydrophone() {
		return distanceToHydrophone;
	}

	/**
	 * @param distanceToHydrophone the distanceToHydrophone to set
	 */
	public void setDistanceToHydrophone(Double distanceToHydrophone) {
		this.distanceToHydrophone = distanceToHydrophone;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("QASoundDataUnit %s UID %d", qaSound.getSignalType(), getUID());
	}


}
