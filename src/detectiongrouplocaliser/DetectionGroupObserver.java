package detectiongrouplocaliser;

public interface DetectionGroupObserver {

	/**
	 * Data have changed - added, removed or edited. 
	 */
	public void dataChanged();
	
	/**
	 * Model has changed - generally this means a change in annotations. 
	 */
	public void modelChanged();
	
}
