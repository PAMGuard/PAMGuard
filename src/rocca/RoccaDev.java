package rocca;

/**
 * Global flag set at runtime for Rocca development flags. 
 * @author mo55
 *
 */
public class RoccaDev {

	
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
		RoccaDev.enabled = enabled;
	}

}
