package rawDeepLearningClassifier.layoutFX;

import java.util.ArrayList;

import PamView.dialog.warn.WarnOnce;
import PamView.dialog.warn.WarnOnceDialogFX;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import rawDeepLearningClassifier.DLStatus;
import warnings.PamWarning;

public class DLWarningDialog {
	
	private boolean warningShow;

	public DLWarningDialog(DLSettingsPane rawDLSettingsPane) {
		
	}
	
	/**
	 * Show a warning dialog for the status
	 * @param the status to show
	 */
	public void showWarningDialog(DLStatus dlWarning) {
		
		switch(dlWarning) {
		case MODEL_ENGINE_FAIL ->{
			//special warning dialog 
			// show the warning
			WarnOnceDialogFX wo = new WarnOnceDialogFX(null, dlWarning.getName(), dlWarning.getDescription(), AlertType.ERROR, null, null, "Ok",  "Cancel", true);
			wo.showDialog();
		}
		default -> {
			//default for most warnings. 
			ArrayList<PamWarning> dlWarnings = new ArrayList<PamWarning>();
			dlWarnings.add(DLSettingsPane.statusToWarnings(dlWarning)); 
			showWarning(dlWarnings); 
		}
		
		}
	
	}
	

	/**
	 * Show a warning dialog. 
	 * @param the warning to show.
	 */
	public void showWarning(String dlWarning) {
		this.warningShow = true; 
		Platform.runLater(()->{
			WarnOnce.showWarningFX(null,  "Deep Learning Settings Warning",  dlWarning ,  AlertType.WARNING, true);
			warningShow = false;
			//			WarnOnce.showWarning( "Deep Learning Settings Warning",  warningsF , WarnOnce.WARNING_MESSAGE);
		});

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
		
		System.out.println("Show warning: " + dlWarnings);
		
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
		
		
		Platform.runLater(()->{
			WarnOnce.showWarningFX(null,  "Deep Learning Settings Warning",  warningsF , errorF ? AlertType.ERROR : AlertType.WARNING, true);
			warningShow = false;
			//			WarnOnce.showWarning( "Deep Learning Settings Warning",  warningsF , WarnOnce.WARNING_MESSAGE);
		});

		//user presses OK - these warnings are just a message - they do not prevent running the module.
	}

}
