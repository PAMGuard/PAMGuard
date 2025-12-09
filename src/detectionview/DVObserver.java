package detectionview;

/**
 * Observer of changes to data to be displayed.  
 * @author dg50
 *
 */
public interface DVObserver {

	/**
	 * updated data
	 * @param updateType
	 */
	public void updateData(int updateType);
	
	/**
	 * Update load progress. 
	 * @param loadProgress
	 */
	public void loadProgress(LoadProgress loadProgress);
	
	/**
	 * Updated configuration
	 */
	public void updateConfig();
	
}
