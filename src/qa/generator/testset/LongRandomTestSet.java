package qa.generator.testset;

import PamUtils.LatLong;
import qa.QAControl;
import qa.generator.clusters.QACluster;
import qa.generator.location.QALocationGenerator;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sequence.SoundSequence;

public class LongRandomTestSet extends SimpleTestSet {

	private long nextGenerateSample;
	
	private SoundSequence previousSequence;

	private QAControl qaControl;

	/**
	 * Generate a long term test set, which will basically go on for ever, repeating
	 * if it's sequenced, or continuing with random or smart generated sequences. . 
	 * @param testName
	 * @param locationGenerator
	 * @param qaCluster
	 * @param sampleRate
	 * @param randomInterval Random interval between sequences in seconds. 
	 */
	public LongRandomTestSet(QAControl qaControl, String testName, QALocationGenerator locationGenerator, QACluster qaCluster,
			double sampleRate, long currentSample) {
		super(testName, locationGenerator, qaCluster, sampleRate, currentSample);
		this.qaControl = qaControl;
		nextGenerateSample = nextSample(currentSample, sampleRate);
	}
	
	public void resetNextSample(long currentSample, double sampleRate) {
		nextGenerateSample = nextSample(currentSample, sampleRate);		
	}

	private long nextSample(long currentSample, double sampleRate) {
		double meanSamps = qaControl.getQaParameters().getRandomTestIntervalSeconds() * sampleRate;
		// generate a sample number between .5 and 1.5* the mean interval
		long iSam = (long) (currentSample + (Math.random()+0.5)*meanSamps);
		return iSam;
	}

	@Override
	public boolean isFinsihed(long currentSample) {
		return false;
	}

	@Override
	public SoundSequence getNextSequence(long startSample, LatLong currentLocation) {
		if (nextGenerateSample > startSample) {
			return null;
		}
		if (QATestSet.STATUS_ACTIVE.equals(getStatus()) == false) {
			return null;
		}
		LatLong seqPos = getLocationGenerator().getNextLocation(currentLocation, previousSequence);

		QASequenceGenerator sequenceGenerator = getQaCluster().getSequenceGenerator();
		previousSequence = sequenceGenerator.createSequence(this, seqPos, currentLocation, getSampleRate(), startSample);
		
		nextGenerateSample = nextSample(nextGenerateSample, getSampleRate());
		addNewSeqCount();
		return previousSequence;
	}

	@Override
	public String getVersion() {
		return getQaCluster().getVersion();
	}


}
