package qa.generator.sequence;

import java.util.ArrayList;

import PamUtils.LatLong;
import qa.QASequenceDataUnit;
import qa.generator.testset.QATestSet;

/**
 * Information about a sequence of sounds. This is an abstract class since the 
 * actual generator will have a number of different timing distributions on it's tail. 
 * @author dg50
 *
 */
public class SoundSequence {
	
	private long startSample, endSample;
	
	private ArrayList<SequenceData> sequenceDatas;
	private int currentPosition;
	private QATestSet qaTestSet;
	private LatLong sourceLocation; // location of the generated animal sounds. 
	private LatLong rxLocation; // location of the reference point for the receiver
	private QASequenceDataUnit sequenceDataUnit;


//	public SoundSequence(QATestSet qaTestSet, LatLong location, LatLong rxLocation, long startSample, long endSample, int nSounds) {
//		this.setQaTestSet(qaTestSet);
//		this.setSourceLocation(location);
//		this.setRxLocation(rxLocation);
//		this.startSample = startSample;
//		this.endSample = endSample;
//		this.nSounds = nSounds;		
//		currentPosition = 0;
//	}
	
//	public final void makeSoundSequence() {
//		sequenceDatas = createSequence();
//	}
//
//	abstract public ArrayList<SequenceData> createSequence();

	public SoundSequence(QATestSet qaTestSet, LatLong location, LatLong rxLocation,
			ArrayList<SequenceData> sequence) {
		this.setQaTestSet(qaTestSet);
		this.setSourceLocation(location);
		this.setRxLocation(rxLocation);
		if (sequence == null) {
			sequenceDatas = new ArrayList<>();
		}
		else {
			SequenceData aDat = sequence.get(0);
			startSample = aDat.getStartSample();
			aDat = sequence.get(sequence.size()-1);
			endSample = aDat.getStartSample();
			endSample += (endSample - startSample) / sequence.size();
			sequenceDatas = sequence;
		}
	}

	/**
	 * Get the information for the next sound in the sequence. If the next sound comes after
	 * the max time, then null will be returned, otherwise the information on amplitude and 
	 * timing of the next sound in the sequence 
	 * @param maxSample Max sample number for start of sound to be generated
	 * @return generation data. 
	 */
	public SequenceData getNext(long maxSample) {
//		if (sequenceDatas == null) {
//			makeSoundSequence();
//		}
		if (isFinished(maxSample) || currentPosition == sequenceDatas.size()) {
			return null;
		}
		SequenceData next = sequenceDatas.get(currentPosition);
		if (next.getStartSample() < maxSample) {
			currentPosition++;
			return next;
		}
		else {
			return null;
		}
	}
	
	public boolean isFinished(long currentSample) {
		return (currentSample > endSample && currentPosition >= sequenceDatas.size());
	}

	/**
	 * @return the startSample
	 */
	public long getStartSample() {
		return startSample;
	}

	/**
	 * @param startSample the startSample to set
	 */
	public void setStartSample(long startSample) {
		this.startSample = startSample;
	}

	/**
	 * @return the endSample
	 */
	public long getEndSample() {
		return endSample;
	}

	/**
	 * @param endSample the endSample to set
	 */
	public void setEndSample(long endSample) {
		this.endSample = endSample;
	}

	/**
	 * @return the nSounds
	 */
	public int getnSounds() {
		return sequenceDatas.size();
	}

	/**
	 * @return the qaTestSet
	 */
	public QATestSet getQaTestSet() {
		return qaTestSet;
	}

	/**
	 * @param qaTestSet the qaTestSet to set
	 */
	public void setQaTestSet(QATestSet qaTestSet) {
		this.qaTestSet = qaTestSet;
	}

	/**
	 * @return the location
	 */
	public LatLong getSourceLocation() {
		return sourceLocation;
	}

	/**
	 * @param location the location to set
	 */
	public void setSourceLocation(LatLong location) {
		this.sourceLocation = location;
	}

	/**
	 * @return the rxLocation
	 */
	public LatLong getRxLocation() {
		return rxLocation;
	}

	/**
	 * @param rxLocation the rxLocation to set
	 */
	public void setRxLocation(LatLong rxLocation) {
		this.rxLocation = rxLocation;
	}

	/**
	 * Set the sequence data unit. 
	 * @param sequenceDataUnit
	 */
	public void setSequenceDataUnit(QASequenceDataUnit sequenceDataUnit) {
		this.sequenceDataUnit = sequenceDataUnit;
	}

	/**
	 * @return the sequenceDataUnit
	 */
	public QASequenceDataUnit getSequenceDataUnit() {
		return sequenceDataUnit;
	}

}
