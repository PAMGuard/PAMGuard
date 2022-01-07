package PamguardMVC;

/**
 * Data returned by a TFContourProvider. contains time and frequency 
 * information about a contour in units of milliseconds and Hz. 
 * @author Doug Gillespie
 *
 */
public class TFContourData {
	
	private long[] timesMillis;
	private double[] lowFrequency;
	private double[] ridgeFrequency;
	private double[] highFrequecy;

	public TFContourData(long[] timesMillis, double[] ridgeFrequency, double[] lowFrequency, double[] highFrequecy) {
		this.timesMillis = timesMillis;
		this.ridgeFrequency = ridgeFrequency;
		this.lowFrequency = lowFrequency;
		this.highFrequecy = highFrequecy;
	}

	/**
	 * @return Array of times in milliseconds which describe the contour
	 */
	public long[] getContourTimes() {
		return timesMillis;
	}

	/**
	 * @return the lower bound of the contour in Hz
	 */
	public double[] getLowFrequency() {
		return lowFrequency;
	}

	/**
	 * @return the ridge frequency of the contour in Hz
	 */
	public double[] getRidgeFrequency() {
		return ridgeFrequency;
	}

	/**
	 * @return the upper bound of the contour in Hz
	 */
	public double[] getHighFrequecy() {
		return highFrequecy;
	}
	
}
