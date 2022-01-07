package PamUtils.time;

/**
 * Class to hold time correction information
 * @author Doug Gillespie
 *
 */
public class TimeCorrection {

	private long systemTime;
	
	private long correctedTime;
	
	private String source;

	public TimeCorrection(long systemTime, long correctedTime, String source) {
		super();
		this.systemTime = systemTime;
		this.correctedTime = correctedTime;
		this.source = source;
	}

	/**
	 * @return the systemTime
	 */
	public long getSystemTime() {
		return systemTime;
	}

	/**
	 * @return the correctedTime
	 */
	public long getCorrectedTime() {
		return correctedTime;
	}
	
	/**
	 * Get the correction to be applied to the system time in milliseconds. 
	 * @return correction in milliseconds. 
	 */
	public long getCorrection() {
		return correctedTime - systemTime;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
}
