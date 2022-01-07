package userDisplay;

import java.util.Hashtable;

/**
 * Singleton class to manage display names and make sure they are unique 
 * across the entirety of PAMGuard .
 * @author dg50
 *
 */
public class DisplayNameManager {

	private static DisplayNameManager singleInstance;
	
	private Hashtable<String, String> usedNames;
	
	private DisplayNameManager() {
		usedNames = new Hashtable<>();
	}
	
	public static DisplayNameManager getInstance() {
		if (singleInstance == null) {
			singleInstance = new DisplayNameManager();
		}
		return singleInstance;
	}
	
	public String getUniqueName(String defaultName, DisplayProviderParameters dpp) {
		String currentName = null;
		if (dpp != null) {
			 currentName = dpp.getDisplayName();
		}
		return getUniqueName(defaultName, currentName);
	}
	
	/**
	 * Get a unique name (using the current one if possible)
	 * @param defaultName default name
	 * @param currentName attempted chosen name
	 * @return unique name (generally the default + a number 1,2,3, etc.
	 */
	public String getUniqueName(String defaultName, String currentName) {
		if (currentName == null) {
			currentName = defaultName;
		}
		int nameTries = 1; // first unique name will be #2 unless the non numbered one is ok
		String chosenName = currentName;
		while (true) {
			if (isUnique(chosenName)) {
				rememberName(chosenName);
				break;
			}
			chosenName = String.format("%s %d", defaultName, ++nameTries);
		}	
		
		return chosenName;
	}
	
	private boolean isUnique(String aName) {
		String ans = usedNames.get(aName);
		return (ans == null);
	}
	
	private void rememberName(String name) {
		usedNames.put(name, name);
	}
	
	/**
	 * Forget a name. Can be called when a display is removed. 
	 * @param name
	 */
	public void forgetName(String name) {
		usedNames.remove(name);
	}
}
