package annotation.timestamp;

import PamUtils.PamCalendar;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;

public class TimestampAnnotation<T extends DataAnnotationType<?>> extends DataAnnotation<T> {

	private long timestamp; 
	
	public TimestampAnnotation(T dataAnnotationType) {
		super(dataAnnotationType);
	}

	/**
	 * @return the string
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param string the string to set
	 */
	public void setTimestamp(long string) {
		this.timestamp = string;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return PamCalendar.formatDateTime(timestamp);
	}

}
