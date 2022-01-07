package Array;

import PamController.SettingsObject;

/** 
 * Class for settings to be optionally held by hydrophone locators<p>
 * Before settings are saved, these will be put into an array of data
 * which will be added to the main PamArray object.
 * @author dg50
 *
 */
public class LocatorSettings implements SettingsObject, Cloneable {

	private static final long serialVersionUID = 1L;

	public LocatorSettings(Class locatorClass) {
		super();
		this.locatorClass = locatorClass;
	}

	/**
	 * Hydrophone locator class - now specific to each streamer. 
	 */
	public Class locatorClass;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public LocatorSettings clone() {
		try {
			return (LocatorSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
