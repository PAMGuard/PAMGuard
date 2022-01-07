package PamguardMVC.dataOffline;

/**
 * Stores the last load point in millis. 
 * @author Jamie Macaulay	
 *
 */
public class SimpleLoadPosition implements LoadPositionInfo {
	
	private long lastLoadPoint;
	
	public SimpleLoadPosition(long lastLoadPoint){
		this.lastLoadPoint=lastLoadPoint; 
	}

	public void setLastLoadMillis(long lastLoadPoint) {
		this.lastLoadPoint = lastLoadPoint;
	}

	@Override
	public long lastLoadMillis() {
		return lastLoadPoint;
	}

}
