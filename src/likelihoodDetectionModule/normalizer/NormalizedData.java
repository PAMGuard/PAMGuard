package likelihoodDetectionModule.normalizer;

/**
 * This is a data type for normalizer output. It contains both the signal and the noise estimates for threshold
 * processing.
 * 
 * @author Dave Flogeras
 *
 */
public class NormalizedData {

	public double signal;
	public double noise;
	
	public NormalizedData( double signal, double noise ) {
		this.signal = signal;
		this.noise = noise;
	}
	
	/**
	 * This method returns the signal to noise ratio.  It is a linear ratio, not dB (ie. a straight divide)
	 * 
	 * @return The SNR as a linear ratio (not dB)
	 */
	public double snr() {
		return this.signal / this.noise;
	}
	
	
}
