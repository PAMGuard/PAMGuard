package group3dlocaliser.grouper;

public interface DetectionGroupMonitor {

	/**
	 * The detection grouper has decided a load of detections are associated 
	 * and provides 1 or more lists of units depending on how they are 
	 * all combined - generally with far too many options ...
	 * @param detectionGroupedSet
	 */
	public void newGroupedDataSet(DetectionGroupedSet detectionGroupedSet);
	
}
