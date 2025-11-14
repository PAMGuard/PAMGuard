package PamUtils.clock;

/**
 * Functions to set the OS clock for a particular operating system. 
 */
public abstract class OSClock {

	private String osName;
	
	public OSClock(String osName) {
		this.osName = osName;
	}
	
	/**
	 * Set the system clock to the given time in milliseconds. 
	 * @param timeMilliseconds time in millis, UTC, probably from GPS, but might be from internet time. 
	 * @return true if sucessful. 
	 */
	public abstract boolean setSystemTime(long timeMilliseconds);

	/**
	 * Get the process CPU elapsed time. 
	 * @return
	 */
	protected abstract long getProcessCPUTime();
	
}
