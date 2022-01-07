package Array;

/**
 * Class for handling different hydrophone locators. 
 * There have got a lot more complicated than previously and need
 * to be able to show their own dialogs and stuff before making 
 * an actual locator. some may also want to use the setting manager
 * to store persistent data.
 *  
 * @author Doug Gillespie
 *
 */
abstract public class HydrophoneLocatorSystem {

	private String name;

	public HydrophoneLocatorSystem(String name) {
		this.name = name;
	}
	
	abstract public HydrophoneLocator getLocator(PamArray array, Streamer streamer);
	
	abstract public LocatorDialogPanel getDialogPanel();

	abstract public Class getLocatorClass();
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getName();
	}
	
}
