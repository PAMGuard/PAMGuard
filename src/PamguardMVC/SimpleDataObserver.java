package PamguardMVC;

/**
 * Simple data observer which gets a notification when a 
 * data unit is selected
 * @author dg50
 *
 */
public interface SimpleDataObserver {

	/**
	 * Data unit is updated / selected, etc. 
	 * @param dataUnit Data unit, can be null
	 */
	public void update(PamDataUnit dataUnit);
	
}
