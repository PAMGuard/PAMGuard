package PamController;

/**
 * Realising that we often just need to pass around the name of a module or group of settings, have
 * split this off from the main PamSettings interface so that it can be passed on it's own.
 * @author Doug Gillespie
 *
 */
public interface SettingsNameProvider {
	
	/**
	 * @return A Name specific to this instance of the particular class, e.g.
	 *         Sperm whale detector, Beaked whale detector, etc.
	 */
	public String getUnitName();
	
}
