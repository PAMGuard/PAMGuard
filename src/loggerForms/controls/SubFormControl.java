package loggerForms.controls;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import loggerForms.FormDescription;
import loggerForms.FormsControl;
import loggerForms.LoggerForm;
import loggerForms.PopupFormContainer;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlDescription;
import NMEA.NMEADataUnit;
import PamView.dialog.warn.WarnOnce;

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

		button.setToolTipText("Open sub form " + otherForm + " in popup window");

	}
	
	private FormDescription findSubForm() {
		String otherForm = controlDescription.getTopic();
		if (otherForm == null) {
			String tit = "Form " + loggerForm.getFormDescription().getFormName();
			String msg = "No sub form set in subform configuration ";
			WarnOnce.showWarning(tit, msg, WarnOnce.WARNING_MESSAGE);
			return null;
		}
		FormsControl formCtrl = loggerForm.getFormDescription().getFormsControl();
		FormDescription subForm = formCtrl.findFormDescription(otherForm);
		if (subForm == null) {
			String tit = "Form " + loggerForm.getFormDescription().getFormName();
			String msg = "Unable to find sub-form named " + otherForm;
			WarnOnce.showWarning(tit, msg, WarnOnce.WARNING_MESSAGE);
		}
		
		return subForm;
	}
	
	/**
	 * Find the appropriate control on the other form, after it's been generated. 
	 * @param otherForm
	 * @return
	 */
	private LoggerControl findOtherXRef(LoggerForm otherForm) {
		String otherItem = controlDescription.getItemInformation().getStringProperty(UDColName.Control_on_Subform.toString());
		if (otherItem == null) {
			return null;
		}
		return otherForm.findInputControl(otherItem);
//		ArrayList<LoggerControl> inputs = otherForm.getInputControls();
//		for (LoggerControl lc : inputs) {
//			if (lc.getControlDescription().getTitle().equalsIgnoreCase(otherItem)) {
//				return lc;
//			}
//		}
//		return null;
	}
	
	/**
	 * Find the cross reference item within this parent form. 
	 * @return
	 */
	private LoggerControl findThisXRef() {
		String xItem = controlDescription.getItemInformation().getStringProperty(UDColName.Send_Control_Name.toString());
		LoggerControl xRefControl = null;
		try {
			int xRefInd = loggerForm.getFormDescription().findInputControlByName(xItem);
			xRefControl = loggerForm.getInputControls().get(xRefInd);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
		return xRefControl;
	}

	private class ButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			showSubForm();
		}
	}

	public void showSubForm() {
//		System.out.println("Subform action is no tyet implemented");
//		if (subFormDescription == null) {
//			System.out.println("Subform cannot be found. Check your form definitions ...");
//		}
		subFormDescription = findSubForm();
		if (subFormDescription == null) {
			System.out.println("Subform cannot be found. Check your form definitions ...");
		}
		LoggerControl xRefControl = findThisXRef();
//		Object xRefValue = null;
//		if (xRefControl != null) {
//			xRefValue = xRefControl.getData();
//		}
		/*
		 * Probably better to make the form once so that it can be re-used, not clearing all
		 * controls, etc. 
		 */
		LoggerForm subForm = subFormDescription.createForm();
		subForm.getSaveButton().setVisible(false);
		if (subForm.getCancelButton() != null) {
			subForm.getCancelButton().setVisible(false);
		}
		LoggerControl otheXRef = findOtherXRef(subForm);
		if (otheXRef != null && xRefControl != null) {
			Object xRefVal = xRefControl.getData();
			try {
				otheXRef.setData(xRefVal);
			}
			catch (Exception e) {
				System.out.println("Incompatible data types setting cross reference in sub form to " + xRefVal);
			}
		}
		// throw the sub form into a new window - similar to editing from the table views.  
		boolean ok = PopupFormContainer.showForm(null, subFormDescription, subForm);
		if (ok) {
			// copy the cross reference the other way. ???
		}
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
