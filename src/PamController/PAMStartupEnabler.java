package PamController;

import PamView.TopToolBar;

/**
 * Class to handle some temporary disabling of the PAMGuard start button during 
 * creating of objects at startup. this is particulalry needed to handle 
 * objects being crated on the FX thread. 
 * @author dg50
 *
 */
public class PAMStartupEnabler {

	static int disableCount = 0;
	
	/**
	 * Call to disable the GUI. If you call this, you MUST
	 * then call dropDisableCount() or PAMGuard will remain disabled. 
	 */
	public static synchronized void addDisableCount() {
		if (disableCount == 0) {
			disableStartButton();
		}
		disableCount++;
	}

	/**
	 * Call to enable the GUI. This shold only be called AFTER a call 
	 * to addDiableCount().
	 */
	public static synchronized void dropDisableCount() {
		disableCount--;
		if (disableCount < 0) { // should never happen. 
			disableCount = 0;
		}
		if (disableCount == 0) {
			enableStartButton();
		}
	}

	/**
	 * Disable the GUI and main start button. 
	 */
	private static void disableStartButton() {
		PamController.getInstance().enableGUIControl(false);
		TopToolBar.enableStartButton(false);
	}

	/**
	 * Enable the GUI and main start button. 
	 */
	private static void enableStartButton() {
		PamController.getInstance().enableGUIControl(true);
		TopToolBar.enableStartButton(true);
	}
	

}
