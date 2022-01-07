package loggerForms.controls;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class NMEAFloatControl extends NMEAControl {

	public NMEAFloatControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getData() {
//		try {
//			return getAbstractformatter().stringToValue(textField.getText());
//		} catch (ParseException e) {
//			dataError=controlDescription.getTitle()+": "+textField.getText()+" is not a float/single";
//			return null;
//		}
		try {
			return Double.valueOf(textField.getText());
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	AbstractFormatter getAbstractformatter() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
