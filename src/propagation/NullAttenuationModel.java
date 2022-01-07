package propagation;

/*
 * Null attenuation model that doesn't do anything at all. 
 */
public class NullAttenuationModel implements AttenuationModel {

	@Override
	public double[] attenuateWaveform(double[] wave, double sampleRate, double distance) {
		return wave;
	}

}
