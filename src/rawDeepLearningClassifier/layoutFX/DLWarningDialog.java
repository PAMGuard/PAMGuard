package rawDeepLearningClassifier.layoutFX;

import java.util.ArrayList;

import javax.swing.SwingUtilities;

import PamView.dialog.warn.WarnOnce;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import rawDeepLearningClassifier.DLStatus;
import warnings.PamWarning;

public class DLWarningDialog {

	private volatile boolean warningShow = false;
	private DLSettingsPane rawDLSettingsPane;

	public DLWarningDialog(DLSettingsPane rawDLSettingsPane) {
	this.rawDLSettingsPane=rawDLSettingsPane;
	}

	/**
	 * Show a warning on top of the window that holds the settings pane. When the pane is
	 * hosted in a Swing dialog there is no FX owner, so an FX alert opens unowned (often
	 * behind the modal Swing dialog) and blocks the FX thread in a nested loop. In that case
	 * show a Swing warning owned by the Swing dialog instead.
	 */
	private void showOwnedWarning(String title, String message, AlertType alertType, String helpPoint) {
		java.awt.Window awtOwner = rawDLSettingsPane.getAWTWindow();
		if (awtOwner != null) {
			SwingUtilities.invokeLater(()->{
				WarnOnce.showWarning(awtOwner, title, message.replace("\n", "<br>"), WarnOnce.WARNING_MESSAGE, helpPoint, null, null, null);
				warningShow = false;
			});
		}
		else {
			Platform.runLater(()->{
				WarnOnce.showWarningFX(rawDLSettingsPane.getFXWindow(), title, message, alertType, helpPoint, null, null, null, true);
				warningShow = false;
			});
		}
	}

	/**
	 * Show a warning dialog for the status
	 * @param the status to show
	 */
	public void showWarningDialog(DLStatus dlWarning) {
		switch(dlWarning) {
		case MODEL_ENGINE_FAIL:
			//special warning dialog 
			// show the warning
			if (warningShow) return;
			warningShow=true;
			
			String helpURL = "http://www.pamguard.org/deeplearning/deep_learning_troubleshoot.html";
			showOwnedWarning(dlWarning.getName(), dlWarning.getDescription(), AlertType.ERROR, helpURL);

			
//			try {
//				SwingUtilities.invokeAndWait(()->{
//				WarnOnce.showWarning(dlWarning.getName(), dlWarning.getDescription(), WarnOnce.WARNING_MESSAGE, 
//						"https://www.pamguard.org/deeplearning/deep_lerning_troubleshoot.html", null, null, null, true);
//				warningShow=false;
//				});
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
			break;
		default: 
			//default for most warnings. 
			ArrayList<PamWarning> dlWarnings = new ArrayList<PamWarning>();
			dlWarnings.add(DLSettingsPane.statusToWarnings(dlWarning)); 
			showWarning(dlWarnings);

		}	
	}


	/**
	 * Show a warning dialog. 
	 * @param the warning to show.
	 */
	public void showWarning(String dlWarning) {
		this.warningShow = true;
		showOwnedWarning("Deep Learning Warning", dlWarning, AlertType.ERROR, null);
//		try {
//			SwingUtilities.invokeAndWait(()->{
//		WarnOnce.showWarning("Deep Learning Warning", dlWarning, WarnOnce.WARNING_MESSAGE, 
//				null, null, null, null, true);
//			});
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		warningShow = false;
		//			WarnOnce.showWarning( "Deep Learning Settings Warning",  warningsF , WarnOnce.WARNING_MESSAGE);

	}

	/**
	 * Show a warning dialog. 
	 * @param the warning to show.
	 */
	public void showWarning(PamWarning dlWarning) {
		ArrayList<PamWarning> dlWarnings = new ArrayList<PamWarning>();
		dlWarnings.add(dlWarning); 
		showWarning(dlWarnings); 
	}


	/**
	 * Show a warning dialog. 
	 * @param dlWarnings - list of warnings - the most important will be shown. 
	 */
	private void showWarning(ArrayList<PamWarning> dlWarnings) {

		if (warningShow) return; //not the best but don't show multiple dialogs on top of each other. 

		if (dlWarnings==null || dlWarnings.size()<1) return; 
		String warnings ="";


		boolean error = false; 
		for (int i=0; i<dlWarnings.size(); i++) {
			warnings += dlWarnings.get(i).getWarningMessage() + "\n\n";
			if (dlWarnings.get(i).getWarnignLevel()>1) {
				error=true; 
			}
		}

		this.warningShow = true; 
		final String warningsF = warnings; 
		final boolean errorF = error;


		showOwnedWarning("Deep Learning Settings Warning", warningsF, errorF ? AlertType.ERROR : AlertType.WARNING, null);

		//user presses OK - these warnings are just a message - they do not prevent running the module.
	}

}
