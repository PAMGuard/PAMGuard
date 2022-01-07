package loggerForms.controls;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import loggerForms.FormDescription;
import loggerForms.LoggerForm;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlDescription;
import NMEA.NMEADataUnit;

public class SubFormControl extends LoggerControl {

	private JButton button;

	private JTextField crossRef;

	private FormDescription subFormDescription;

	private ControlDescription subFormControlDescription;

	private ControlDescription thisFormControlDescription;

	public SubFormControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		makeButton();
	}

	private void makeButton() {
		button = new JButton(controlDescription.getTitle());
		component = new JPanel(new FlowLayout(FlowLayout.LEFT));
		component.add(button);
		button.addActionListener(new ButtonListener());
		// find the cross reference item on the other form.
		subFormDescription = null;
		subFormControlDescription = null;
		thisFormControlDescription = null;
		String otherForm = controlDescription.getTopic();
		String otherItem = controlDescription.getItemInformation().getStringProperty(UDColName.Control_on_Subform.toString());
		if (otherForm != null) {
			subFormDescription = controlDescription.getFormDescription().getFormsControl().findFormDescription(otherForm);
			if (subFormDescription != null) {
				int otherFormControl = subFormDescription.findInputControlByName(otherItem);
				if (otherFormControl >= 0) {
					subFormControlDescription = subFormDescription.getInputControlDescriptions().get(otherFormControl);
				}
			}
		}
		// find the control for this form
		String thisItem = controlDescription.getItemInformation().getStringProperty(UDColName.Send_Control_Name.toString());
		if (thisItem != null) {
			int thisInd = controlDescription.getFormDescription().findInputControlByName(thisItem);
			if (thisInd >= 0) {
				thisFormControlDescription = controlDescription.getFormDescription().getInputControlDescriptions().get(thisInd);
			}
		}
		int len = 4;
		if (subFormControlDescription != null) {
			Integer otherLen = subFormControlDescription.getLength();
			if (otherLen != null) {
				len = Math.max(len, otherLen);
			}
		}
		component.add(crossRef = new JTextField(len));


	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			showSubForm();
		}
	}

	public void showSubForm() {
		System.out.println("Subform action is no tyet implemented");
		if (subFormDescription == null) {
			System.out.println("Subform cannot be found. Check your form definitions ...");
		}
		/*
		 * Probably better to make the form once so that it can be re-used, not clearing all
		 * controls, etc. 
		 */
		LoggerForm subForm = subFormDescription.createForm();
		// throw the sub form into a new window - similar to editing from the table views.  
		
	}

	@Override
	public String getDataError() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setData(Object data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDefault() {
		// TODO Auto-generated method stub

	}

	@Override
	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		// TODO Auto-generated method stub
		return 0;
	}

}
