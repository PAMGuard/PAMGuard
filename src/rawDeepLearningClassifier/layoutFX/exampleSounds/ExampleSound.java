package rawDeepLearningClassifier.layoutFX.exampleSounds;


/**
 * Create an example sound. 
 * @author Jamie Macaulay 
 *
 */
public interface ExampleSound {
	
	/**
	 * Get the wave . 
	 * @return the wave
	 */
	public double[] getWave(); 
	
	/**
	 * Gte the sample rate . 
	 * @return get the sample rate. 
	 */
	public float getSampleRate(); 
	
}