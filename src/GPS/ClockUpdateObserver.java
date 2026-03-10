package GPS;

/**
 * Get an asynchronous callback when the GPSClockUpdate has successfully updated the system clock
 */
public interface ClockUpdateObserver {

	/**
	 * Message sent when the clock has been updated. 
	 * @param success true of false
	 * @param timeMillis the time that was set
	 * @param message any other message. 
	 */
	public void clockUpdated(boolean success, long timeMillis, String message);
	
	/**
	 * A new time has been unpacked in the GPS data.
	 * @param timeMillis
	 */
	public void newTime(long timeMillis);
	
}
