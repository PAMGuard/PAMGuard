package videoRangePanel.pamImage;

import java.io.Serializable;

/**
 * Time offset for PamImages. Simple constant offset which is added ot the image time. 
 * @author Jamie Macaulay
 *
 */
public class ImageTimeOffset implements TimeOffset, Serializable, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long timeOffset=0; 
	
	public ImageTimeOffset(long timeOffset) {
		this.timeOffset=timeOffset; 
	}
	
	
	@Override
	public long getOffsetTime(long originalTime) {
		return originalTime+timeOffset;
	}
	
	/**
	 * Get the time offset
	 * @return the timeOffset
	 */
	public long getTimeOffset() {
		return timeOffset;
	}



	/**
	 * Set the time offset
	 * @param timeOffset the timeOffset to set
	 */
	public void setTimeOffset(long timeOffset) {
		this.timeOffset = timeOffset;
	}
	
	
	/**
	 * Set the time offset. 
	 * @param days - the number of full days of offset
	 * @param hours - the number of full hours of time offset 
	 * @param minutes = the number of full minutes of time offset
	 * @param seconds - the number of full seconds 
	 */
	public void setTimeOffset(int days, int hours, int minutes, int seconds, int millis) {
		
		long millisOffset = (long) days*24*60*60*1000 +
				hours*60*60*1000+
				minutes*60*1000+
				seconds*1000+
				millis; 
		
		this.timeOffset=millisOffset; 
	}
	
	@Override
	public ImageTimeOffset clone() {
		try {
			return (ImageTimeOffset) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	
	

}
