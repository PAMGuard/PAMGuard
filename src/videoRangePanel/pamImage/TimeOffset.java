package videoRangePanel.pamImage;

/**
 * Converts a tiome to another time. 
 * @author Jamie Macaulay
 *
 */
public interface TimeOffset {
	
	/**
	 * Get the time offset. 
	 * @param originalTime - the orginal time
	 * @return the time with time offset.
	 */
	public long getOffsetTime(long originalTime);

}
