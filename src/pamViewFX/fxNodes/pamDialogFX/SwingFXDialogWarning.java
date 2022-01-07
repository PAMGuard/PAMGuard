package pamViewFX.fxNodes.pamDialogFX;

import java.awt.Window;

import PamView.dialog.PamDialog;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Set of dialog warnings which can be called on either the AWT of the FX thread. 
 * @author Doug Gillespie
 *
 */
public class SwingFXDialogWarning {

	/**
	 * Show a dialog warning message in either the AWT of the FX application thread. 
	 * @param owner Swing Window OR FX Stage object. 
	 * @param title Title for warning message
	 * @param content content of warning message
	 * @return always false
	 */
	public static boolean showWarning(Object owner, String title, String content) {
		if (isFXThread()) {
			if (owner instanceof Stage) {
				return PamDialogFX.showWarning((Stage) owner, title, content);
			}
			else {
				return PamDialogFX.showWarning(null, title, content);
			}
		}
		else {
			if (owner instanceof Window) {
				return PamDialog.showWarning((Window) owner, title, content);
			}
			else {
				return PamDialog.showWarning(null, title, content);
			}
		}
	}
	/**
	 * Show a dialog warning message in either the AWT of the FX application thread. 
	 * @param owner Swing Window OR FX Stage object. 
	 * @param content content of warning message
	 * @return always false
	 */
	public static boolean showWarning(Object owner, String content) {
		return showWarning(owner, "Warning", content);
	}

	/**
	 * Show a dialog warning message in either the AWT of the FX application thread. 
	 * @param content content of warning message
	 * @return always false
	 */
	public static boolean showWarning(String content) {
		return showWarning(null, "Warning", content);
	}

	/**
	 * 
	 * @return true if this is the FX Applicatoin Thread. False otherwise. 
	 */
	private static boolean isFXThread() {
//		String tName = Thread.currentThread().getName();
//		if (tName == null) return false;
//		return tName.contains("JavaFX Application Thread");
		return Platform.isFxApplicationThread();
	}
}
