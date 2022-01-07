package qa.generator.testset;

import PamUtils.LatLong;
import qa.QATestDataUnit;
import qa.generator.QASoundGenerator;
import qa.generator.clusters.QACluster;
import qa.generator.distributions.QADistribution;
import qa.generator.distributions.QAFixed;
import qa.generator.distributions.QAGaussian;
import qa.generator.location.QALocationGenerator;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sequence.SoundSequence;

public class SimpleTestSet extends QATestSet {
	
	private SoundSequence previousSequence;

	private int nNewSeq = 0;
	
	/**
	 * Will ne non zero unless the test has been reloaded from the database. 
	 */
	private int nOldSequences = 0;
	
	public SimpleTestSet(String testName, QALocationGenerator locationGenerator,
			QACluster qaCluster, double sampleRate, long currentSample) {
		super(testName, locationGenerator, qaCluster, sampleRate);
	}
	
	@Override
	public boolean isFinsihed(long currentSample) {
		if (STATUS_CANCELLED.equals(getStatus())) {
			return true;
		}
		if (previousSequence == null) {
			return getLocationGenerator().isFinished();
		}
		else {
			return getLocationGenerator().isFinished() && previousSequence.isFinished(currentSample);
		}
	}

	@Override
	public SoundSequence getNextSequence(long startSample, LatLong currentLocation) {
		if (isFinsihed(startSample)) {
			return null;
		}
		if (previousSequence == null || previousSequence.isFinished(startSample)) {
			// generate a new sequence
//			long lastSample = startSample + (long) (getSampleRate()*10);
//			double range = rangeList[rangeIndex++];
//			LatLong seqPos = currentLocation.travelDistanceMeters(Math.random()*360, range);
			LatLong seqPos = getLocationGenerator().getNextLocation(currentLocation, previousSequence);
//			System.out.println("Generate new sound sequence at sample " + startSample);
//			previousSequence = new RandomSoundSequence(this, seqPos, currentLocation, timeDistribution, amplitudeDistribution,
//					startSample, lastSample, soundsPerSequence);
			QASequenceGenerator sequenceGenerator = getQaCluster().getSequenceGenerator();
			previousSequence = sequenceGenerator.createSequence(this, seqPos, currentLocation, getSampleRate(), startSample);
			addNewSeqCount();
			return previousSequence;
		}
		else {
			return null;
		}
	}


//	@Override
//	public int getNumSequences() {
//		QATestDataUnit tdu = getTestDataUnit();
//		if (tdu == null) {
//			return 0;
//		}
//		else {
//			return tdu.getSubDetectionsCount();
//		}
//	}

	@Override
	public String getVersion() {
		return getQaCluster().getVersion();
	}

	/**
	 * @return the previousSequence
	 */
	public SoundSequence getPreviousSequence() {
		return previousSequence;
	}


	@Override
	public int getNumSequences() {
		return nNewSeq + nOldSequences;
	}

	/**
	 * Add to the count of new sequences. 
	 */
	protected void addNewSeqCount() {
		nNewSeq++;
	}
	
	/**
	 * @return the nOldSequences
	 */
	public int getnOldSequences() {
		return nOldSequences;
	}

	/**
	 * @param nOldSequences the nOldSequences to set
	 */
	public void setnOldSequences(int nOldSequences) {
		this.nOldSequences = nOldSequences;
	}

}
