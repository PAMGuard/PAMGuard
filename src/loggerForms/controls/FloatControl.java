package loggerForms.controls;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.NumberFormatter;

import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class FloatControl extends SimpleControl{
//	ControlDescription controlDescription;
//	LoggerForm loggerForm;
	
	public FloatControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
	}
	
	@Override
	public Object getData() {
		try {
			return getAbstractformatter().stringToValue(textField.getText());
		} catch (ParseException e) {
			dataError=controlDescription.getTitle()+": "+textField.getText()+" is not an float/single";
			return null;
		}
//		try {
//			return Double.valueOf(textField.getText());
//		}
//		catch (NumberFormatException e) {
//			dataError=controlDescription.getTitle()+": "+textField.getText()+" is not a float/single";
//			return null;
//		}
	}

	@Override
	AbstractFormatter getAbstractformatter() {
		NumberFormat format = NumberFormat.getNumberInstance();
		// turn off grouping since it makes the numbers impossible to read !
		format.setGroupingUsed(false);
		NumberFormatter formatter = new NumberFormatter(format){
			@Override  
		    public Object stringToValue(String text) throws ParseException {  
		        if(text.trim().isEmpty())  
		            return null;  
		        return super.stringToValue(text);  
		    }
		};
		// turn off grouping since it makes the numbers impossible to read !
		formatter.setValueClass(Float.class);
		return formatter;
	}

	
	
}
