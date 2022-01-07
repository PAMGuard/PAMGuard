package qa.generator;

/**
 * Interface for all QA module sounds. 
 * @author Doug
 *
 */
public interface QASound {

	/**
	 * 
	 * @return The numbers of the first samples on each hydrophone
	 */
	public long[] getFirstSamples();
	
	/**
	 * Get the waveform for each hydrophone.Note that these may be time aligned
	 * to different start samples and that each channels data may not be exactly the same length
	 * @return waveform for each channel. 
	 */
	public double[][] getWave();
	
	/**
	 * 
	 * @return The generated frequency range for each sound
	 */
	public double[] getFrequencyRange();

	/**
	 * @return The name of the signal type.
	 */
	public String getSignalType();

	/**
	 * Clears the waveform. Can be called once the sound insersion is complete 
	 * to free up memory. 
	 */
	public void clearWave();
}
