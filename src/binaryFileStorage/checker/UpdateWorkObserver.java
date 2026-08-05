package binaryFileStorage.checker;

/**
 * Get progress updates from worker. 
 * @author dg50
 *
 */
public interface UpdateWorkObserver {

	/**
	 * Get progres updates from worker
	 * @param updateWorkProgress
	 */
	public void update(UpdateWorkProgress updateWorkProgress);
	
	public void done();
	
}
