package PamController;


/**
 * The GUI  for a PAMControlled unit. Each PAMControlled Unit can have no, or multiple GUI's programmed in 
 * Swing JavaFX, any other GUI frameWork. All GUI's must subcless this class.
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class PamControlledUnitGUI {
	
	/**
	 * Get the flag type for the GUI e.g. PAMGuiManager.SWING;
	 * @return flag descibing the type of GUI. 
	 */
	public abstract int getGUIFlag();
	
	/**
	 * Allows the GUI to be notified of changes, e.g. in the PAMControlle.r 
	 * @param flag - the change flag. 
	 */
	public void notifyGUIChange(int flag) {
		
	}
}
