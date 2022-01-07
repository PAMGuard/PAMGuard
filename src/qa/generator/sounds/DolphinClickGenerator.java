package qa.generator.sounds;

import qa.generator.QASound;
import qa.generator.distributions.QADistribution;
import qa.generator.distributions.QAFixed;

/**
 * Generate short click like sounds which consist of an unwindowed single
 * cycle sin wave.  
 * @author dg50
 *
 */
public class DolphinClickGenerator extends StandardSoundGenerator {

	private QADistribution frequencyDistribution;
	
	private double[] freqRange;

	/**
	 * Make a single cycle click generator where the centre frequency is drawn at random 
	 * from a distribution independently for each click. 
	 * @param frequencyDistribution
	 */
	public DolphinClickGenerator(QADistribution frequencyDistribution) {
		super("Dolphin Click");
		this.frequencyDistribution = frequencyDistribution;
		freqRange = frequencyDistribution.getRange();
	}
	
	/**
	 * Make a single cycle click generator using a constant centre frequency. 
	 * @param centroidFrequency
	 */
	public DolphinClickGenerator(double centroidFrequency) {
		super("Dolphin Click");
		this.frequencyDistribution = new QAFixed(centroidFrequency);
		double[] f = {centroidFrequency/2, centroidFrequency*2};
		freqRange = f;
	}

	@Override
	public QASound generateSound(long sourceSample, double sampleRate, double[] delays, double[] amplitudes) {
		double frequency = frequencyDistribution.getValues(1)[0];
		StandardSoundParams ssp = getStandardSoundParams();
		ssp.setDurationS(1./frequency);
		ssp.setStartFrequency(frequency);
		ssp.setEndFrequency(frequency);
		return super.generateSound(sourceSample, sampleRate, delays, amplitudes);
	}

	/* (non-Javadoc)
	 * @see qa.generator.sounds.StandardSoundGenerator#getFrequencyRange()
	 */
	@Override
	public double[] getFrequencyRange() {
		return freqRange;
	}

	/* (non-Javadoc)
	 * @see qa.generator.sounds.StandardSoundGenerator#getDurationRange()
	 */
	@Override
	public double[] getDurationRange() {
		double[] range = frequencyDistribution.getRange();
		for (int i = 0; i < range.length; i++) {
			range[i] = 1./range[i];
		}
		return range;
	}
}
