package qa.generator.window;

/**
 * Window function for sounds, always operate in place on a waveform
 * @author dg50
 *
 */
public interface SoundWindow {

	/**
	 * Run the window on a sound
	 * @param wave sound to window
	 * @param soundSamples true number of samples in the sound - may be non integer. 
	 * @param sampleOffset Offset of first sample - the time the wave actually stated so the 
	 * window can be put at exactly the right start time relative to the true waveform. 
	 */
	public void runWindow(double[] wave, double soundSamples, double sampleRate, double sampleOffset);
	
}
