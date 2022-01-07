package userDisplay;

/**
 * Provider of displays for the main display panel. 
 * Can be implemented by anything anywhere in PAMGUard
 * and will add the appropriate menu command to 
 * the user display menu, etc. 
 * @author Doug Gillespie
 *
 */
public interface UserDisplayProvider {

	/**
	 * 
	 * @return the name of the display to be shown in the user display menu
	 */
	public String getName();
	
	/**
	 * 
	 * @param userDisplayControl the user display that this component is being created within. 
	 * @param uniqueDisplayName unique name for the display. This may need to be passed early to 
	 * a display so that it can find settings based on this name. 
	 * @return The swing component to add into a frame within the user display. 
	 */
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName);
	
	/**
	 * 
	 * @return the class of the swing component. 
	 */
	public Class getComponentClass();
	
	
	/**
	 * 
	 * @return the max mumber of displays that can be created. 0 indicates no limit.
	 */
	public int getMaxDisplays();
	
	/**
	 * 
	 * @return true if another display can be created (i.e. number of displays is < the max number
	 * or there is no max number. 
	 */
	public boolean canCreate();
	
	/**
	 * Called when a display is removed from a user display. 
	 * @param component removed component. 
	 */
	public void removeDisplay(UserDisplayComponent component);
		
}
