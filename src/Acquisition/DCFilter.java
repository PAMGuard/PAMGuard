package Acquisition;

/**
 * Class to remove DC offset from audio data.
 * @author Douglas Gillespie
 *
 */
public class DCFilter {

	private double sampleRate;
	private double timeConstant;
	private int nChannels;
	private double[] background;
	private double alpha;

	public DCFilter(double sampleRate, double timeConstant, int nChannels) {
		this.sampleRate = sampleRate;
		this.timeConstant = timeConstant;
		this.nChannels = nChannels;
		background = new double[nChannels];
		alpha = 1-1./(sampleRate*timeConstant);
	}

	public void filterData(int channel, double[] data) {
		double x;
		double b = background[channel];
		for (int i = 0; i < data.length; i++) {
			x = data[i]-b;
			b = data[i] - alpha*x;
			data[i] = x;
//			x = data[i] - b;
//			b = b*alpha + (1.-alpha)*data[i];
//			data[i] = x;
		}
		background[channel] = b;
	}
}
