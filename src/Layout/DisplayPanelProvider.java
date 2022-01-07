package Layout;

/**
 * Plug in panels for the display. <p>
 * A DisplayPanelProvider is created for each Pamguard module
 * that is capable of providing a display panel whether an actual 
 * display is created or not. If required a DisplayPanelContainer, 
 * which has the ability to use a panel, will ask the DisplayPanelProvider
 * to create a DisplayPanel. It is possible that many different parts of 
 * PamGuard will ask for similar DisplayPanels, so a DisplayPanelProvider
 * may create any number of DisplayPanels. It si therefore recommended that 
 * each DisplayPanel subscribes itslef to any data it required and generally
 * looks after itself as much as possible. 
 * <p>
 * DisplayPanelProviders should be normally be registered with the DisplayProviderList so 
 * that Pamguard has access to a list of available panels. 
 *   
 * @author Doug Gillespie
 * 
 * @see Layout.DisplayPanel
 * @see Layout.DisplayPanelContainer
 * @see Layout.DisplayProviderList
 *
 */
public interface DisplayPanelProvider {

	/**
	 * @return create the panel to be included in the display
	 */
	DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer);
	
	/**
	 * Get a name for the panel which may be used in options dialogs, etc. 
	 * @return a character string
	 */
	String getDisplayPanelName();
	
	
}
