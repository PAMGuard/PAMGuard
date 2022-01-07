package loggerForms.controls;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class DoubleControl extends SimpleControl{
	ControlDescription controlDescription;
	LoggerForm loggerForm;

	public DoubleControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
	}



	AbstractFormatter getAbstractformatter() {

		//		MaskFormatter formatter = new MaskFormatter();
		//		formatter.setValidCharacters("1234567890.");

		//		NumberFormat format = NumberFormat.getNumberInstance();
		//		NumberFormatter formatter = new NumberFormatter(format);
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
		formatter.setValueClass(Double.class);
		return formatter;
	}


	@Override
	public Object getData() {
		try {
			return getAbstractformatter().stringToValue(textField.getText());
		} catch (ParseException e) {
			
			String classN = ((DefaultFormatter)getAbstractformatter()).getValueClass().getSimpleName();
						
			dataError=controlDescription.getTitle()+": "+textField.getText()+" is not a "+classN;
			return null;
		}
//				try {
//					return textField.getValue();
//					
//		//			return (Double) textField.getValue();
//					
////					return Double.valueOf(textField.getText());
//				}
//				catch (NumberFormatException e) {
//		//			dataError=controlDescription.getTitle()+": "+textField.getText()+" is not a double";
//					return null;
//				}
	}



}
