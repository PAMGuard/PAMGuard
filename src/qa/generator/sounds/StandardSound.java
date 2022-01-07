package qa.generator.sounds;

import PamUtils.LatLong;
import qa.generator.QASound;

/**
 * A standard sound that has a duration and sweeps between two 
 * frequencies. 
 * @author Doug
 *
 */

public class StandardSound implements QASound {
	
	private long[] firstSample;
	private double[][] wave;
	private String signalType;
	private double[] frequencyRange;
	
	/**
	 * Create a standard sound. 
	 * @param firstSample
	 * @param sampleOffsets
	 * @param standardSoundParams
	 */
	public StandardSound(String signalType, long[] firstSample, double[][] wave, double[] frequencyRange) {
		this.firstSample = firstSample;
		this.wave = wave;
		this.signalType = signalType;
		this.frequencyRange = frequencyRange;
	}

	/**
	 * @return the firstSample
	 */
	public long[] getFirstSamples() {
		return firstSample;
	}

	/**
	 * @return the wave
	 */
	public double[][] getWave() {
		return wave;
	}

	@Override
	public String getSignalType() {
		return signalType;
	}

	@Override
	public double[] getFrequencyRange() {
		return frequencyRange;
	}

	@Override
	public void clearWave() {
		wave = null;
	}



}
