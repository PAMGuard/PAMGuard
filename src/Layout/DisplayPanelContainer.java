package Layout;

/**
 * DisplayPanelContainer is an interface implemented by graphics components 
 * which have the ability to incorporate DisplayPanels from other Pamguard
 * modules. 
 * <p>
 * Each DisplayPanelProvider privides a reference to a JPanel that the 
 * DisplayPanelContainer is responsible for incorporating into a display 
 * and sizing appropriately.
 * <p>
 * The DisplayPanelContiner is responsible for all timing (along the x axis 
 * of the display) and the plug in panel will use these funtions to 
 * get appropriate time / scale information. 
 * <p>
 * DisplayPanelContainer is currently only implemented in the spectrogram
 * display
 * 
 * @author Doug Gillespie
 * @see Layout.DisplayPanel
 * @see Layout.DisplayPanelProvider
 * @see Layout.DisplayProviderList
 * @see Spectrogram.SpectrogramDisplay
 *
 */
public interface DisplayPanelContainer {

	/**
	 * get the display duration in milliseconds
	 */
	double getXDuration();
	
	/**
	 * get the time at the cursor in milliseconds
	 */
	long getCurrentXTime();
	
	/**
	 * current pixel (need not be integer ! 
	 */
	double getCurrentXPixel();
	
	/**
	 * @return true if the display wraps (rather than scrolls). 
	 */
	boolean wrapDisplay();
	
	/**
	 * notification from a display panel that it's
	 * necessary to perform some action - such as 
	 * redrawing axis. 
	 * @param noteType
	 */
	public void panelNotify(int noteType);
	
	static public final int DRAW_BORDER = 1;
	
}
