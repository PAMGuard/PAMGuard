package qa.generator;

public abstract class QASoundGenerator {
	
	private String name;
	
	/**
	 * @param name
	 * @param latLong
	 */
	public QASoundGenerator(String name) {
		super();
		this.name = name;
	}

	/**
	 * Generate a sound
	 * @param sourceSample the sample number at which the source was generated
	 * @param sampleRate sample rate of data to receive the sound
	 * @param delays delays in samples from generation of the sound to it's reception on each hydrophone
	 * @param amplitudes amplitudes on a linear scale (notDB) which incorporates the system full scale and the spreading loss
	 * but doesn't yet contain frequency dependent attenuation. 
	 * @return a generated sound. 
	 */
	public abstract QASound generateSound(long sourceSample, double sampleRate, double[] delays, double[] amplitudes);

//	/**
//	 * Calculate the amplitude in internal units (-1 to 1 full scale) of a sound based on amplitude information 
//	 * from the system and the spreading model and from the source level.  
//	 * @param amplitudeFactor amplitude factor taking into account system gain and spreading loss
//	 * @param soundLevel source level in dB re 1uPa. 
//	 * @return amplitude on a linear scale. 
//	 */
//	public double dB2Amplitude(double amplitudeFactor, double soundLevel) {
//		return amplitudeFactor * Math.pow(10, (soundLevel)/20.);
//	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return The nominal frequency range of the sound in Hz. 
	 */
	public abstract double[] getFrequencyRange();
	
	/**
	 * 
	 * @return the nominal duration of the sound in seconds. 
	 */
	public abstract double[] getDurationRange();

}
