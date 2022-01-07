package videoRangePanel.vrmethods;

/**
 * The Method to localise animals from an image. 
 * @author Doug Gillespie and Jamie Macaulay
 *
 */
public interface VRMethod {
	
	/**
	 * The name of this type of analysis method. 
	 * @return the name of the method
	 */
	public String getName();
	
//	/***
//	 * Get the overlay panel for the picture unique to this method. In general this will be a translucent panel which will be placed over the picture and 
//	 * will include the interactions for the method. Layer Ui's can be used for a variety of purposes, including adding wait animations etc.  
//	 * @return
//	 */
//	public LayerUI getJLayerOverlay();
	

	/**
	 * Clears all user interactions and resets overlay. Required for changes to settings, picture being changed and other interactions not directly associated with the VRMethod. 
	 */
	public void clearOverlay();
	
	/**
	 * Called from other parts of the module whenever a method panel may needed updated e.g. when new calibration data is manually added in the settings dialog. 
	 * @param updateType - the update flag. 
	 */
	public void update(int updateType);
	
	/**
	 * The AWT overlay. This handles all graphical interactions within AWT
	 * @return the awt overlay 
	 */
	public VROverlayAWT getOverlayAWT();
	
	/**
	 * The FX overlay. This handles all graphical interactions within JavaFX
	 * @return the awt overlay 
	 */
	public VROverlayFX getOverlayFX();
	

}