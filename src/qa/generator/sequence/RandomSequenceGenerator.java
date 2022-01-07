package qa.generator.sequence;

import java.util.ArrayList;

import PamUtils.LatLong;
import qa.generator.distributions.QADistribution;
import qa.generator.testset.QATestSet;

public class RandomSequenceGenerator extends QASequenceGenerator {

	private QADistribution timeDistribution;
	private QADistribution amplitudeDistribution;
	private int nSounds;

	/**
	 * @param timeDistribution
	 * @param amplitudeDistribution
	 * @param nSounds
	 */
	public RandomSequenceGenerator(QADistribution timeDistribution, 
			QADistribution amplitudeDistribution, int nSounds) {
		super();
		this.timeDistribution = timeDistribution;
		this.amplitudeDistribution = amplitudeDistribution;
		this.nSounds = nSounds;
		timeDistribution.setIntegrate(true);
		timeDistribution.setSort(false);
		amplitudeDistribution.setIntegrate(false);
	}

	@Override
	public SoundSequence createSequence(QATestSet qaTestSet, LatLong location, LatLong rxLocation, 
			double sampleRate, long startSample) {
		double[] t = timeDistribution.getValues(getnSounds());
		double[] a = amplitudeDistribution.getValues(getnSounds());
		ArrayList<SequenceData> sequence = new ArrayList<>();
		SequenceData lastSequence = null;
		long endSample = startSample;
		for (int i = 0; i < getnSounds(); i++) {
			long samp = startSample + (long) (t[i]*sampleRate);
			sequence.add(lastSequence = new SequenceData(null, i, samp, a[i]));
			endSample = lastSequence.getStartSample();
		}
		endSample += timeDistribution.getRange()[1];
		endSample += (endSample - startSample) / (nSounds+1);
		SoundSequence soundSequence = new SoundSequence(qaTestSet, location, rxLocation, sequence);
		return soundSequence;
	}

	/**
	 * @return the timeDistribution
	 */
	public QADistribution getTimeDistribution() {
		return timeDistribution;
	}

	/**
	 * @param timeDistribution the timeDistribution to set
	 */
	public void setTimeDistribution(QADistribution timeDistribution) {
		this.timeDistribution = timeDistribution;
	}

	/**
	 * @return the amplitudeDistribution
	 */
	public QADistribution getAmplitudeDistribution() {
		return amplitudeDistribution;
	}

	/**
	 * @param amplitudeDistribution the amplitudeDistribution to set
	 */
	public void setAmplitudeDistribution(QADistribution amplitudeDistribution) {
		this.amplitudeDistribution = amplitudeDistribution;
	}

	/**
	 * @return the nSounds
	 */
	public int getnSounds() {
		return nSounds;
	}

	/**
	 * @param nSounds the nSounds to set
	 */
	public void setnSounds(int nSounds) {
		this.nSounds = nSounds;
	}

}
