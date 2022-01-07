package propagation;

/**
 * Attenuation model. Does not do anything in terms of 
 * spreading, just handles frequency dependent attenuation. 
 * @author Doug
 *
 */
public interface AttenuationModel {

	/**
	 * Attenuate a waveform using a frequency dependent attenuation model for seawater. 
	 * @param wave waveform to attenuate
	 * @param sampleRate sample rate in data
	 * @param distance distance from source to receiver. 
	 * @return attenuated waveform (can probably do it in place!)
	 */
	public double[] attenuateWaveform(double[] wave, double sampleRate, double distance);
	
}
