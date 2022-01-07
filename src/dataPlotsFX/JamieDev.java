package dataPlotsFX;

/**
 * Global flag set at runtime for Jamies development flags. 
 * @author dg50
 *
 */
public class JamieDev {
	
	private static boolean enabled = false;

	/**
	 * @return the enabled
	 */
	public static boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public static void setEnabled(boolean enabled) {
		JamieDev.enabled = enabled;
	}
	
}
