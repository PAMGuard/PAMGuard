package loggerForms.controls;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.NumberFormatter;

import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class NMEAIntegerControl extends NMEAControl {

	public NMEAIntegerControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getData() {		
		try {
			return getAbstractformatter().stringToValue(textField.getText());
		} catch (ParseException e) {
			dataError=controlDescription.getTitle()+": "+textField.getText()+" is not an integer";
			return null;
		}
		//		try {
		//			return Integer.valueOf(textField.getText());
		//		}
		//		catch (NumberFormatException e) {
		//			return null;
		//		}
	}

	@Override
	AbstractFormatter getAbstractformatter() {
		NumberFormat format = NumberFormat.getIntegerInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		// turn off grouping since it makes the numbers impossible to read !
		format.setGroupingUsed(false);
		return formatter;
	}

}
