package PamController.status;

/**
 * Actions that can be attached to a status message. These may attempt to 
 * fix a problem directly, or might just take you to a dialog to sort it 
 * out. 
 * @author dg50
 *
 */
public interface RemedialAction {

	/**
	 * Text based information about the action to take - might be displayed
	 * on screen, in a tooltip, etc. 
	 * @return text information about the action. 
	 */
	public String getInfo();
	
	/**
	 * Take remedial action - open a dialog, fix it directly, etc. 
	 * @param currentStatus current status - may contain information needed for the action
	 * @return new status following the attempted action. 
	 */
	public ModuleStatus takeAction(ModuleStatus currentStatus);
}
