package loggerForms.controlDescriptions;

import javax.swing.JButton;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.ButtonControl;
import loggerForms.controls.LoggerControl;

public class CdButton extends ControlDescription {
	
	private JButton button;

	protected CdButton(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		// TODO Auto-generated constructor stub
	}

	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new ButtonControl(this, loggerForm);
	}

}
