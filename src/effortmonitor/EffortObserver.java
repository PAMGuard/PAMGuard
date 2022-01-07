package effortmonitor;

public interface EffortObserver {

	/**
	 * Notification sent when status changes or when effor
	 * changes (or anything changes)
	 */
	public void statusChange();
	
}
