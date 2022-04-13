package pamguard;

import java.util.HashMap;

/**
 * Global parameter pairs set at startup time.<br>
 * These are the arguments passed to the command line. <br>Basically all going into a static hash map
 * @author dg50
 *
 */
public class GlobalArguments {

	static HashMap<String, String> globalFlags = new HashMap<>();
	
	/**
	 * Set a global parameter value
	 * @param name value name
	 * @param value parameter value
	 */
	public static void setParam(String name, String value) {
		globalFlags.put(name, value);
	}
	
	/**
	 * Get a global parameter value
	 * @param name value name
	 * @return value
	 */
	public static String getParam(String name) {
		return globalFlags.get(name);
	}
	
}
