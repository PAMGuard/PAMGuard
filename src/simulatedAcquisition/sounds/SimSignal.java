package simulatedAcquisition.sounds;

import Spectrogram.WindowFunction;

/**
 * Class to hold information and generate a signal 
 * of a specific type
 * @author Doug Gillespie
 *
 */
public abstract class SimSignal {

	/**
	 *  
	 * @return friendly name for displaying
	 */
	public abstract String getName();


	/**
	 * @param sampleRate
	 */
	public SimSignal() {
		super();
	}

	@Override
	public String toString() {
		return getName();
	}

	/**
	 * Get a single simulated signal. 
	 * @param sampleRate sample rate of data
	 * @param sampleOffset sample offset - 0<=sampleOffset<1 <br>How far into the sound the first sample is, i.e.
	 * 0 means that the first sample point will be right on the start of the sound (value 0
	 * if it's sin wave based), value 0.5 would be half a sample later. 
	 * @return a signal for a single channel. 
	 */
	abstract public double[] getSignal(int channel, float sampleRate, double sampleOffset);

	/**
	 * Taper the ends of the signal using a hanning 
	 * window function. 
	 * <p> if the percentTaper parameter is 50%, then the 
	 * entire signal is windowed, otherwise, it will 
	 * just taper the bits at the end. 
	 * @param percentTaper
	 */
	protected void taperEnds(double[] signal, double percentTaper) {
		int len = signal.length;
		int winLen = (int) (len * percentTaper / 100);
		//		winLen /=2;
		winLen *=2;

		double[] winFunc = WindowFunction.hann(winLen);
		for (int i = 0; i < winLen/2; i++) {
			signal[i] *= winFunc[i];
			signal[len-1-i] *= winFunc[i];
		}
	}

	/**
	 * Called just before simsound loops through channels and propagation paths
	 * so that parameters of a new sound can be set up. Generally, if the sounds
	 * are deterministic and all the same, nothing has to be done here. but if 
	 * sounds contain random elements, then it will be necessary to set up the
	 * random bit here so that the generatet sound is the same on all channels. 
	 */
	public void prepareSignal() {
	}

}
