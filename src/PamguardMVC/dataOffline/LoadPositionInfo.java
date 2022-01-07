package PamguardMVC.dataOffline;

/**
 * Keeps a record of the last load postion if a load thread is cancelled. 
 * @author Jamie Macaulay
 *
 */
public interface LoadPositionInfo {
	
	/**
	 * The time in millis the last thread progressed to. 
	 * @return the time in millis the thread progressed to
	 */
	public long lastLoadMillis(); 

}
