package Localiser.algorithms;

/**
 * Class to pass round time delay data. Can contain 
 * correlation scores and error estimates as well as the actual delays.  
 * @author Doug Gillespie
 *
 */
public class TimeDelayData {
	
	/**
	 * The time delay in seconds.
	 */
	private double delay;
	
	private double delayError;
	
	private double delayScore;

	/**
	 * @param delaySeconds Time delay in seconds
	 * @param delayError Time delay error in seconds
	 * @param delayScore Delay score (correlation value, scale 0 - 1)
	 */
	public TimeDelayData(double delay, double delayError, double delayScore) {
		super();
		this.delay = delay;
		this.delayError = delayError;
		this.delayScore = delayScore;
	}

	/**
	 * @param delaySeconds Time delay in seconds
	 * @param delayScore Delay score (correlation value, scale 0 - 1)
	 */
	public TimeDelayData(double delay, double delayScore) {
		this(delay, Double.NaN, delayScore);
	}

	/**
	 * @param delaySeconds Time delay in seconds
	 * @param delayScore Delay score (correlation value, scale 0 - 1)
	 */
	public TimeDelayData(double delay) {
		this(delay, Double.NaN, Double.NaN);
	}


	/**
	 * @return the delayError
	 */
	public double getDelayError() {
		return delayError;
	}

	/**
	 * @param delayError set the delayError
	 */
	public void setDelayError(double delayError) {
		this.delayError = delayError;
	}

	/**
	 * @return the delayScore
	 */
	public double getDelayScore() {
		return delayScore;
	}

	/**
	 * @param delayScore the delayScore to set
	 */
	public void setDelayScore(double delayScore) {
		this.delayScore = delayScore;
	}

	/**
	 * @return the delay in seconds
	 */
	public double getDelay() {
		return delay;
	}

	/**
	 * @param delay the delay to set in seconds
	 */
	public void setDelay(double delay) {
		this.delay = delay;
	}

	/**
	 * Multiply the delay by a constant
	 * @param scaleFactor
	 * @return updated delay value
	 */
	public double scaleDelay(double scaleFactor) {
		delay *= scaleFactor;
		return delay;
	}
	
	/**
	 * Add a constant offset to the time delay
	 * @param delayOffset in same units as the time delay. 
	 * @return updated delay value
	 */
	public double addDelayOffset(double delayOffset) {
		delay += delayOffset;
		return delay;
	}
	
	/**
	 * Extract just the delay data from a set of timeDelayDatas.<br>
	 * In an ideal world no one would use this since it's better to also retain the 
	 * correlation height and error data, but this method does allow for easy 
	 * reprogramming of existing localisers  
	 * @param timeDelayDatas array of timeDelayDatas
	 * @return array of time delays. 
	 */
	public static double[] extractDelays(TimeDelayData[] timeDelayDatas) {
		if (timeDelayDatas == null) {
			return null;
		}
		double[] delays = new double[timeDelayDatas.length];
		for (int i = 0; i < timeDelayDatas.length; i++) {
			delays[i] = timeDelayDatas[i].delay;
		}
		return delays;
	}

}
