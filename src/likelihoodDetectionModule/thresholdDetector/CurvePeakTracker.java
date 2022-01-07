package likelihoodDetectionModule.thresholdDetector;

/**
 * This class is used to track the time (both sample and milliseconds since 01-01-1970), and the value
 * of a curve.  It is used to track energy peaks in the detector
 * 
 * @author Dave Flogeras
 *
 */
public class CurvePeakTracker {

	private long timeMilliseconds;
	private long sample;
	private double curvePeakValue;
	
	/**
	 * Constructor.
	 *  
	 * @param timeMilliseconds The start time in milliseconds
	 * @param sample The start sample number
	 * @param curveValue The starting value
	 */
	public CurvePeakTracker( long timeMilliseconds,
				  long sample,
				  double curveValue ) {
		
		this.timeMilliseconds = timeMilliseconds;
		this.sample = sample;
		this.curvePeakValue = curveValue;

	}

	/**
	 * Update the tracker with new data point. 
	 * 
	 * @param timeMilliseconds The time of the current sample
	 * @param sample The sample number of the current sample
	 * @param curveValue The current data point value
	 */
	public void update( long timeMilliseconds,
			long sample,
			double curveValue ) {
		
		if( curveValue > this.curvePeakValue ) {
			
			this.curvePeakValue = curveValue;
			this.timeMilliseconds = timeMilliseconds;
			this.sample = sample;
			
		}
	}
	
	/**
	 * 
	 * @return The time of the peak value
	 */
	public long peakTime() {
		return this.timeMilliseconds;
	}
	
	/**
	 * 
	 * @return The sample number of the peak value
	 */
	public long peakSample() {
		return this.sample;
	}
	
	/**
	 * 
	 * @return The peak value
	 */
	public double peakValue() {
		return this.curvePeakValue;
	}
	
}
