package dataGram;

/**
 * Small interface handed to {@link DatagramManager#processDataMapPoint} so that
 * datagram creation can report progress and check for cancellation without
 * knowing anything about the worker thread that's driving it.
 * <p>
 * This exists so that subclasses of {@link DatagramManager} can override the
 * creation of a datagram for a single data map point (e.g. to read sound files
 * directly rather than going through PamDataUnits) without being exposed to the
 * SwingWorker.
 * 
 * @author Jamie Macaulay
 *
 */
public interface DatagramWorkMonitor {

	/**
	 * Publish a progress update. May be called from a background thread; the
	 * implementation is responsible for getting it to the GUI safely.
	 * @param datagramProgress progress information
	 */
	public void publishProgress(DatagramProgress datagramProgress);

	/**
	 * @return true if the user has cancelled, in which case work should stop
	 * as soon as is convenient.
	 */
	public boolean isWorkCancelled();

}
