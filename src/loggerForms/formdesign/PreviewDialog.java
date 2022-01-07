package loggerForms.formdesign;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import loggerForms.FormDescription;
import loggerForms.LoggerForm;
import loggerForms.UDFErrors;

public class PreviewDialog extends PamDialog {

	private LoggerForm loggerForm;

	public PreviewDialog(Window parentFrame, FormDescription formDescription) {
		super(parentFrame, formDescription.getFormName() + " preview", false);
		JPanel mainPanel = new JPanel(new BorderLayout());
//		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		loggerForm = new LoggerForm(formDescription, LoggerForm.EditDataForm);
		
		JPanel previewPanel = new JPanel(new BorderLayout());
		previewPanel.setBorder(new TitledBorder("Form Preview"));
		previewPanel.add(BorderLayout.CENTER, loggerForm.getComponent());
		mainPanel.add(BorderLayout.CENTER, previewPanel);
		
		JPanel errorPanel = new JPanel();
		errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
		errorPanel.setBorder(new TitledBorder("Form Errors"));
		mainPanel.add(BorderLayout.SOUTH, errorPanel);
		UDFErrors formErrors = formDescription.getFormErrors();
		for (String err:formErrors.getErrors()) {
			errorPanel.add(new JLabel("Error - " + err));
		}
		formErrors = formDescription.getFormWarnings();
		for (String err:formErrors.getErrors()) {
			errorPanel.add(new JLabel("Warning - " + err));
		}
		
		
		setDialogComponent(mainPanel);
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
