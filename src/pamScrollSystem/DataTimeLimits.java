package pamScrollSystem;

/**
 * Interface to set  / control limits on data times. 
 * @author dg50
 *
 */
public interface DataTimeLimits {
	/**
	 * @return the minimumMillis - the minimum of loaded data
	 */
	public long getMinimumMillis();

	/**
	 * @return the maximumMillis - the maximum of loaded data
	 */
	public long getMaximumMillis();
}
