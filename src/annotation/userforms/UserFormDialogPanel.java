package annotation.userforms;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamguardMVC.PamDataUnit;
import annotation.AnnotationDialogPanel;
import annotation.string.StringAnnotation;
import loggerForms.LoggerForm;

public class UserFormDialogPanel implements AnnotationDialogPanel {

	private UserFormAnnotationType userFormAnnotationType;
	private LoggerForm loggerForm;
	private JPanel formWrapper;

	public UserFormDialogPanel(UserFormAnnotationType userFormAnnotationType, LoggerForm loggerForm) {
		this.userFormAnnotationType = userFormAnnotationType;
		this.loggerForm = loggerForm;
		// hide the save button, but leave the clear button. 
		loggerForm.getSaveButton().setVisible(false);
		formWrapper = new JPanel(new BorderLayout());
		formWrapper.setBorder(new TitledBorder("Data Entry " + loggerForm.getFormDescription().getFormName()));
		formWrapper.add(loggerForm.getComponent(), BorderLayout.CENTER);
	}

	@Override
	public JComponent getDialogComponent() {
		if (loggerForm == null) return null;
		return formWrapper;
	}

	@Override
	public void setParams(PamDataUnit pamDataUnit) {
		UserFormAnnotation an = (UserFormAnnotation) pamDataUnit.findDataAnnotation(UserFormAnnotation.class, 
				userFormAnnotationType.getAnnotationName());
		if (an == null) {
//			loggerForm.
		}
		else {
			loggerForm.transferDataArrayToForm(an.getLoggerFormData());
		}

	}

	@Override
	public boolean getParams(PamDataUnit pamDataUnit) {
		String er = loggerForm.getFormErrors();
		if (er != null) {
			return false;
		}

		loggerForm.getFormWarnings();


		if (er==null){
			Object[] formData = loggerForm.extractFormData();
			UserFormAnnotation an = new UserFormAnnotation(userFormAnnotationType, formData);
			pamDataUnit.addDataAnnotation(an);
		}
		return true;
	}

}
