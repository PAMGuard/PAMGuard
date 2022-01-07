package soundPlayback;

import PamUtils.FrequencyFormat;

/**
 * Single class to hold everything we need to know about sample 
 * rates. 
 * Output sample rate will be the rate that data are being played back at at the device. 
 * This will be the same as the data rate after decimation for a real time system
 * but may be different for a non-real time system that is playing faster or slower than real time. 
 * @author dg50
 *
 */
public class PBSampleRateData {
	
	private double speedFactor = 1.;
	
	private double inputSampleRate;
	
	private double outputSampleRate;

	/**
	 * @param inputSampleRate Input sample rate
	 * @param outputSampleRate Output sample rate
	 * @param speedFactor Speed factor (how fast output will be compared to input)
	 */
	public PBSampleRateData(double inputSampleRate, double outputSampleRate, double speedFactor) {
		super();
		this.inputSampleRate = inputSampleRate;
		this.outputSampleRate = outputSampleRate;
		this.speedFactor = speedFactor;
	}
	
	/**
	 * @param inputSampleRate Input sample rate
	 * @param outputSampleRate Output sample rate. <br>
	 *  Decimation factor will be set to inputSampleRat / outputSampleRate
	 */
	public PBSampleRateData(double inputSampleRate, double outputSampleRate) {
		super();
		this.inputSampleRate = inputSampleRate;
		this.outputSampleRate = outputSampleRate;
		this.speedFactor = 1.;
	}

	/**
	 * @return the inputSampleRate
	 */
	public double getInputSampleRate() {
		return inputSampleRate;
	}

	/**
	 * @return the outputSampleRate
	 */
	public double getOutputSampleRate() {
		return outputSampleRate;
	}

	/**
	 * @return the speedFactor
	 */
	public double getSpeedFactor() {
		return speedFactor;
	}

	/**
	 * @return the decimationFactor
	 */
	public double getDecimationFactor() {
		return inputSampleRate / outputSampleRate * speedFactor;
	}

	@Override
	public String toString() {
		return String.format("Input %s, Output %s, Decimation %3.2f, play speed x%3.2f", 
				FrequencyFormat.formatFrequency(inputSampleRate, true),
				FrequencyFormat.formatFrequency(outputSampleRate, true),
				getDecimationFactor(), speedFactor);
	}

	

}
