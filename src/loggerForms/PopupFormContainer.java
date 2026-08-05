package loggerForms;

import java.awt.Window;

import PamView.dialog.PamDialog;

public class PopupFormContainer extends PamDialog {

	private FormDescription formDescription;
	private LoggerForm loggerForm;
	private boolean cancelled;

	public PopupFormContainer(Window parentFrame, FormDescription formDescription, LoggerForm loggerForm) {
		super(parentFrame, formDescription.getFormName(), false);
		this.formDescription = formDescription;
		this.loggerForm = loggerForm;
		
		setDialogComponent(loggerForm.getComponent());
		
		/*
		 * What else do we need to play with ? Modality ? Removing cancel button ? 
		 * Enabling hot keys and stuff ? 
		 */
	}
	
	public static boolean showForm(Window parentFrame, FormDescription formDescription, LoggerForm loggerForm) {
		PopupFormContainer pfc = new PopupFormContainer(parentFrame, formDescription, loggerForm);
		pfc.setVisible(true);
		return pfc.cancelled;
	}

	@Override
	public boolean getParams() {	
		String er = loggerForm.getFormErrors();

		String warns = loggerForm.getFormWarnings();
		
		if (er==null){
			loggerForm.save();
			return true;
		}
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		cancelled = true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
