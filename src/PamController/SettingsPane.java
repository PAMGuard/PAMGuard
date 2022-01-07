package PamController;



import javafx.scene.Node;

/**
 * Functions needed for module settings pane 
 * @author Jamie Macaulay
 *
 */
public abstract class SettingsPane<T> {

	/**
	 * Owner window. May be FX or AWT !
	 */
	private Object ownerWindow;
	
	public SettingsPane(Object ownerWindow) {
		super();
		this.ownerWindow = ownerWindow;
	}

	/**
	 * Get settings from the pane.
	 * @return settings class
	 */
	public abstract T getParams(T currParams); 
	
	/**
	 * Called whenever the pane is first shown/open to set pane to show current settings.
	 * @param input- current settings class.
	 */
	public abstract void setParams(T input);

	/**
	 * Get the name of the pane.
	 * @return name of the pane
	 */
	public abstract String getName();
	
	/**
	 * Get node for GUI chnage of settings. 
	*/
	public abstract Node getContentNode(); 

	/**
	 * Called when settings pane is first initialised. This can be used if for example, a the size of a pane is needed for a param. 
	 * (Sizes are only initialised when Nodes are shown); 
	 */
	public abstract void paneInitialized();
	
	/**
	 * General function which allows panes to be notified of some change]
	 * @param - usually used as a notification flag
	 * @param - any corresponding data to pass through the notification prcoess. 
	 * @param - a false negetive return.
	 */
	public void notifyChange(int flag, Object object) {
	}
	

	/**
	 * Get a help point address to use in help buttons in dialogs. 
	 * @return help point address within PAMGuard help system. 
	 */
	public String getHelpPoint() {
		return null;
	}
	
	/**
	 * Set default settings in the settings pane. 
	 */
	public void setDefaults() {
		
	}
	/**
	 * @return the ownerWindow
	 */
	public Object getOwnerWindow() {
		return ownerWindow;
	} 
	
	/**
	 * @param ownerWindow the ownerWindow to set
	 */
	public void setOwnerWindow(Object ownerWindow) {
		this.ownerWindow = ownerWindow;
	}

	/**
	 * Get the Owner window as an AWT object, or just return null. 
	 * @return owner AWT window 
	 */
	public java.awt.Window getAWTWindow() {
		if (ownerWindow instanceof java.awt.Window) {
			return (java.awt.Window) ownerWindow;
		}
		else {
			return null;
		}
	}

	/**
	 * Get the Owner window as an FX object, or just return null. 
	 * @return owner FX window 
	 */
	public javafx.stage.Window getFXWindow() {
		if (ownerWindow instanceof javafx.stage.Window) {
			return (javafx.stage.Window) ownerWindow;
		}
		else {
			return null;
		}
	}

	/**
	 * An opportunity to re-pack settings pane contents. 
	 */
	public void repackContents() {
		
	}


}
