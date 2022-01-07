package userDisplayFX;

import javafx.scene.layout.Region;
import pamViewFX.fxNodes.internalNode.PamInternalPane;

/**
 * Any display which sits inside PAMGuard must be an instance of UserDisplayNodeFX. 
 * @author Jamie Macaulay
 *
 */
public interface UserDisplayNodeFX {
	
	/**
	 * The name of display. Used for default tabs etc.
	 * @return the name of the display
	 */
	public String getName();
	
	
	/**
	 * Get the node to add to the display. 
	 * @return The FX node to add to the display
	 */
	public Region getNode();
	
	/**
	 * Called when the component is first displayed. 
	 */
	public void openNode();
	
	/**
	 * Check whether a pane is static. If the pane is static then it cannot be removed from PAMGuard,
	 *  it cannot be resized and no other user display can be added to the tab. This is rarely used. 
	 * @return true if the pane cannot be removed from the view. 
	 */
	public boolean isStaticDisplay();
	
	/**
	 * Check whether a display can be resized within PAMGuard. 
	 * @return true of the pane can be resized. 
	 */
	public boolean isResizeableDisplay();
	
	/**
	 * Only used for automatic resizing. Minor displays are automatically made smaller 
	 * @return true if a minor display. 
	 */
	public boolean isMinorDisplay();
	
	/**
	 * Called when node settings pane is requested from another program. Note that 
	 * the design of displays is such that settings are shown in the display
	 * itself, usually with a sliding pane or something similar revealed. Hence, this function should 
	 * not be used to grab a pane to put in a dialog. 
	 * @return true if the pane can show settings. 
	 */
	public boolean requestNodeSettingsPane();
	
	/**
	 * Called when the node is removed from the display. 
	 */
	public void closeNode();

	/**
	 * Pick up model changed notifications from a parent user display. 
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType);
	
	/**
	 * Get the display provider params. 
	 * @return display params. 
	 */
	public UserDisplayNodeParams getDisplayParams();

	/**
	 * Called whenever the display is added to a frame. 
	 * @param internalFrame - the internal frame. 
	 */
	public void setFrameHolder(PamInternalPane internalFrame); 
	

}
