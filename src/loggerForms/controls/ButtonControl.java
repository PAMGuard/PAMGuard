package loggerForms.controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import NMEA.NMEADataUnit;
import loggerForms.FormDescription;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class ButtonControl extends LoggerControl {
	
	private JButton button;

	public ButtonControl(ControlDescription controlDescription, LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		button = new JButton(controlDescription.getTitle());
		component = new JPanel();
		component.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				buttonAction(e);
			}
		});
	}

	protected void buttonAction(ActionEvent e) {
		// find the other form, that we're to open. 
		String formName = controlDescription.getTopic();
		FormDescription fd = loggerForm.getFormDescription().getFormsControl().findFormDescription(formName);
		
		if (fd == null) {
			System.out.printf("Button form %s - %s unable to find form %s to open\n", loggerForm.getFormDescription().getFormName(),
					controlDescription.getTitle(), formName);
			return;
		}
		
		/*
		 *  open the other form - depending on it's type if it's subtabs, or popup, then open a new instance, 
		 *  otherwise, if it's normal, just go to that form. 
		 */
		LoggerForm form = fd.activateForm();
		//then set the appropriate field within the opened form. 
		String toSetData = controlDescription.getPostTitle();
		LoggerControl toSetCtrl = form.findInputControl(controlDescription.getControlOnSubform());
		if (form != null && toSetCtrl != null) {
			toSetCtrl.setData(toSetData);
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
