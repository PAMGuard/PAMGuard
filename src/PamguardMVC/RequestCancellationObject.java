package PamguardMVC;

/**
 * Used to cancel data requests when calling to load data in multiple threads.
 * Normally cancel is false, but it it's set true, whatever process
 * is actually loading data should exit asap. 
 * @author Doug Gillespie
 *
 */
public class RequestCancellationObject {

	public volatile boolean cancel = false;
	
}
