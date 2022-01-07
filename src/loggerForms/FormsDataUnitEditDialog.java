package loggerForms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import PamView.PamColors;
import PamView.dialog.PamDialog;

public class FormsDataUnitEditDialog extends PamDialog{

	private static FormsDataUnitEditDialog singleInstance;
	private FormDescription formDescription;
	private LoggerForm loggerForm;
	private FormsDataUnit formDataUnit;
	
	private FormsDataUnitEditDialog(Window parentFrame, FormDescription formDescription) {
		super(parentFrame, "Edit " + formDescription.getFormNiceName(), false);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		loggerForm = new LoggerForm(formDescription, LoggerForm.EditDataForm);
		mainPanel.add(BorderLayout.CENTER, loggerForm.getComponent());
		
		JLabel warning = new JLabel("You are changing stored data from " + formDescription.getFormNiceName(), SwingConstants.CENTER);
		warning.setFont(PamColors.getInstance().getBoldFont());
		warning.setForeground(Color.RED);
		mainPanel.add(BorderLayout.NORTH, warning);
		
		setDialogComponent(mainPanel);
		
	}

	public static FormsDataUnit showDialog(Window parent, FormDescription formDescription, FormsDataUnit formsDataUnit) {
		if (singleInstance == null || singleInstance.getOwner() != parent || singleInstance.formDescription != formDescription) {
			singleInstance = new FormsDataUnitEditDialog(parent, formDescription);
		}
		singleInstance.setDataUnit(formsDataUnit);
		singleInstance.setVisible(true);
		
		
		return singleInstance.formDataUnit;
	}
	
	private void setDataUnit(FormsDataUnit formsDataUnit) {
		this.formDataUnit = formsDataUnit;
		loggerForm.restoreData(formsDataUnit);
	}

	@Override
	public void cancelButtonPressed() {
		formDataUnit = null;
	}

	@Override
	public boolean getParams() {
		String errors = loggerForm.getFormErrors();
		if (errors != null) {
			return showWarning(formDescription.getFormNiceName() + " cannot be updated since it contians the following errors", errors);
		}
		Object[] newData = loggerForm.transferControlDataToArray(loggerForm.getSqlTypes());
		formDataUnit.setFormsData(loggerForm, newData);
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
