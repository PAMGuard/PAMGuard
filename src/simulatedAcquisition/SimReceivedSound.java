package simulatedAcquisition;

/*
 * The simulated received sound at a individual hydrophone. 
 */
public class SimReceivedSound {

	private long startSample; 
	
	private double[] wave;
	
	public SimReceivedSound(long iStartSample, double[] wave) {
		this.startSample = iStartSample;
		this.wave = wave;
	}


	/**
	 * @return the startSample
	 */
	public long getStartSample() {
		return startSample;
	}
	
	/**
	 * 
	 * @return teh last sample. 
	 */
	public long getEndSample() {
		return startSample + wave.length;
	}


	/**
	 * @return the wave
	 */
	public double[] getWave() {
		return wave;
	}
}
