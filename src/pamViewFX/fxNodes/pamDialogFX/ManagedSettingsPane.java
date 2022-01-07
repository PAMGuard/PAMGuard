package pamViewFX.fxNodes.pamDialogFX;

import PamController.SettingsPane;

/**
 * Wrapper around a SettingsPane to provide simpler 
 * functionality for including panes from one part of PAMGuard in 
 * another part. With a SettingsPane, whatever uses that SettingsPane
 * needs to have a way of putting parameters into it and using them when 
 * they come back out, which can be hard to implement if the dialog 
 * using the pane really knows nothing about it. 
 * With a ManagedSettingsPane all of the work is done within this 
 * one object, which will have to implement methods to get and store
 * the data. 
 * @author Doug Gillespie
 *
 * @param <T>
 */
abstract public class ManagedSettingsPane<T> {

	private T currentParams;
	
	/**
	 * A settings pane to incorporate into dialogs, control panels, etc.
	 * @return the settingsPane
	 */
	abstract public SettingsPane<T> getSettingsPane();
	
	/**
	 * Get parameters from the settings pane. 
	 * @return It will return the parameters, or null, but nothing 
	 * needs to happen to that object. 
	 */
	public T getParams() {
		T newParams = getSettingsPane().getParams(currentParams);
		if (newParams != null) {
			if (useParams(newParams) == false) {
				return null;
			};
		}
		return newParams;
	}

	/**
	 * Called when parameters are returned by the settings pane. 
	 * Here the parameters should be put into the right location in whatever
	 * class is using them.  
	 * <br>This doesn't get called when the parameters returned are null
	 * @param newParams Parameters returned by the SettingsPane
	 * @return false if the parameters are invalid for some reason. 
	 */
	public abstract boolean useParams(T newParams);
	
	/**
	 * Set parameters in the settings pane. This calls
	 * findParams() which should get the correct parameters
	 * from whichever object owns them. 
	 */
	public void setParams() {
		currentParams = findParams();
		getSettingsPane().setParams(currentParams);
	}

	/**
	 * Get the parameters from whichever object owns them
	 * so that they can be put into the SettingsPane.
	 * @return Parameters to put into the settings pane. 
	 */
	public abstract T findParams();
	
	
	
	
}
