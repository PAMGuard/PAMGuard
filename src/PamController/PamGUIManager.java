package PamController;

import PamView.GuiFrameManager;
import pamViewFX.PamGuiManagerFX;

/**
 * Manages references which indicate which GUI PAMGuard is using. 
 * @author Jamie Macaulay
 *
 */
public class PamGUIManager {

	/**
	 * Flag for the swing GUI. 
	 */
	public final static int NOGUI = 0;

	/**
	 * Flag for the JavaFX GUI. 
	 */
	public final static int SWING = 1;

	/**
	 * Flag for the swing GUI. 
	 */
	public final static int FX = 2;

	/**
	 * GUI types
	 */
	private static int guiType = SWING; 


	/**
	 * Create the GUI (or not)
	 * @param - reference to the PAMController. 
	 * @param - object containing other relevant information. 
	 */
	public static PAMControllerGUI createGUI(PamController pamController, Object object) {
		switch (guiType) {
		case NOGUI: 
			return null; 
		case SWING: 
			return new GuiFrameManager(pamController); 
		case FX: 
			return new PamGuiManagerFX(pamController, object);
		default:
			return new GuiFrameManager(pamController); 
		}
	}

	/**
	 * Set the type. 
	 * @param guiType - the gui type flag. 
	 */
	public static void setType(int guiType) {
		PamGUIManager.guiType=guiType; 
	}

	/**
	 * Get the GUI type flag. 
	 * @return
	 */
	public static int getGUIType() {
		return guiType;
	}

	/**
	 * Convenience function to test whether the swing GUI is being used. 
	 * @return true if the swing GUI is being used. 
	 */
	public static boolean isSwing() {
		return guiType==SWING;
	}

	/**
	 * Convenience function to check whether the swing is the FX gui. 
	 * @return true if the FX gui is being used. 
	 */
	public static boolean isFX() {
		 return guiType==FX;
	}


}
