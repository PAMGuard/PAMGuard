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
	private int[] channelCallCount;

	public DCFilter(double sampleRate, double timeConstant, int nChannels) {
		this.sampleRate = sampleRate;
		this.timeConstant = timeConstant;
		this.nChannels = nChannels;
		background = new double[nChannels];
		setTimeContant(sampleRate, timeConstant);
		channelCallCount = new int[nChannels];
	}
	
	/**
	 * Set the time constant
	 * @param timeConstant 
	 * @param timeConstant2
	 * @param timeConstant 
	 */
	public void setTimeContant(double sampleRate, double timeConstant) {
		this.sampleRate = sampleRate;
		this.timeConstant = timeConstant;
		alpha = 1-1./(sampleRate*timeConstant);
	}

	/**
	 * Filter data in place. 
	 * @param channel
	 * @param data
	 */
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
		channelCallCount[channel] ++;
	}
	
	public int getChannelCallCount(int channel) {
		return channelCallCount[channel];
	}

	public double getSampleRate() {
		return sampleRate;
	}

	public double getTimeConstant() {
		return timeConstant;
	}

	public int getnChannels() {
		return nChannels;
	}

	public double[] getBackground() {
		return background;
	}

	public double getAlpha() {
		return alpha;
	}
}
