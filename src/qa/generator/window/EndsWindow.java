package qa.generator.window;

/**
 * Window that only scales the ends of a waveform - suitable for calls such as whistles 
 * where we want to leave most of it alone. 
 * @author dg50
 *
 */
public class EndsWindow extends HannWindow {

	private double endSecs;
	
	/**
	 * The endLengh is in seconds since at constructor time of the window functions 
	 * we might not know the sample rate. This will get added when the window actually runs. 
	 * @param endSecs
	 */
	public EndsWindow(double endSecs) {
		this.endSecs = endSecs;
	}

	/**
	 * Window the waveform using a Hann window function. 
	 * If WindowLen > the length of wav, then only the ends 
	 * of wav up to windowLen/2 from each end are windowed, the rest
	 * remaining flat. If windowLen > length of wav, then the window 
	 * length is set to wav.length, i.e. the entire wave is windowed using
	 * a symetrical function. 
	 * @param wav wave to window
	 * @param sampleOffset sample offset 
	 * @param windowLen window length. 
	 */
	public void runWindow(double[] wav, double soundSamples, double sampleRate, double sampleOffset) {
		int windowLen = (int) (2.*endSecs*sampleRate);
		if (windowLen > wav.length) {
			super.runWindow(wav, wav.length, sampleRate, sampleOffset);
			return;
		}
		int hLen = windowLen/2;
		for (int i = 0, j = wav.length-1; i < hLen; i++, j--) {
			double t = (double)(i+sampleOffset)/hLen*Math.PI/2;
			double f = Math.pow(Math.sin(t), 2);
			wav[i] *= f;
			t = (double)(i-sampleOffset)/hLen*Math.PI/2;
			f = Math.pow(Math.sin(t), 2);
			wav[j] *= f;
		}
//		for (int i = (int) soundSamples; i < wav.length; i++) {
//			wav[i] = 0;
//		}
	}
}
