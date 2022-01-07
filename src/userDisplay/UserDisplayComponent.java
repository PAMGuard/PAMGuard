package userDisplay;

import java.awt.Component;

/**
 * Class to hold information about and send notifications to open user display components. 
 * @author dg50
 *
 */
public interface UserDisplayComponent {

	/**
	 * 
	 * @return The Swing component to add to the frame
	 */
	public Component getComponent();
	
	/**
	 * Called when the component is first displayed. 
	 */
	public void openComponent();
	
	/**
	 * Called when the component is removed from the display. 
	 */
	public void closeComponent();

	/**
	 * Pick up model changed notifications from a parent user display. 
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType);
	
	/**
	 * 
	 * @return a unique name (across all of PAMGuard) which will be
	 * used for mark overlays and settings storage. 
	 */
	public String getUniqueName();
	
	/**
	 * Set a unique name for every display. This is important for 
	 * saving settings and for managing things that need a unique name
	 * such as overlay marks and markers. 
	 */
	public void setUniqueName(String uniqueName);

	/**
	 * Get a title for the frame the component will be added to. 
	 * @return a title for the components frame. 
	 */
	public String getFrameTitle();
	
}
