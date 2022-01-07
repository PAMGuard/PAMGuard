package pamScrollSystem;

public interface PamScrollObserver {

	/**
	 * Notification sent when a scroller or slider changes it's position
	 * @param abstractPamScroller reference to moved scroller
	 */
	public void scrollValueChanged(AbstractPamScroller abstractPamScroller);
	
	/**
	 * Notification sent when a scroller or slider changes it's data range
	 * <p>
	 * If data were loaded in a worker thread, this notification is sent after
	 * the new data have loaded. 
	 * 
	 * @param pamScroller reference to moved scroller. 
	 */
	public void scrollRangeChanged(AbstractPamScroller pamScroller);
	
}
