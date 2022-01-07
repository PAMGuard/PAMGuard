package pamViewFX.fxNodes.pamScrollers;

public interface VisibleRangeObserver {
	
	/**
	 * Changed when the VISIBLE range changes. Not the overall range of the scroll bar. 
	 * @param oldVal
	 * @param newVal
	 */
	public void  visibleRangeChanged(long oldVal, long  newVal);


}
