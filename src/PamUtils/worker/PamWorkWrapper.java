package PamUtils.worker;

/**
 * Wrapper with two callback functions to use with a PamWorker
 * @author Doug
 *
 * @param <T>
 */
public interface PamWorkWrapper<T> {

	/**
	 * Run the background task which is in it's own thread
	 * @param pamWorker reference to the main worker manager. Make repeated 
	 * calls to pamWorker.update to update progress in the dialog
	 * @return the value which will be passed to taskFinished
	 */
	public T runBackgroundTask(PamWorker<T> pamWorker);
	
	/**
	 * Called when the background task completes. 
	 * @param result result returned by runBackgroundTask or null if an 
	 * exception occurred.
	 */
	public void taskFinished(T result);
	
}
