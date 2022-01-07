package loggerForms.controls;

import java.math.RoundingMode;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JOptionPane;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class IntegerControl extends SimpleControl {
//	ControlDescription controlDescription;
//	LoggerForm loggerForm;
	
	public IntegerControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
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
//			return Integer.parseInt(textField.getText());
////			return Integer.valueOf(textField.getText());
//		}
//		catch (NumberFormatException e) {
//			dataError=controlDescription.getTitle()+": "+textField.getText()+" is not an integer";
//			return null;
//		}
	}


	@Override
	AbstractFormatter getAbstractformatter() {
		
//		MaskFormatter formatter = new MaskFormatter();
//		formatter.setValidCharacters("1234567890");
		
		NumberFormat format = NumberFormat.getIntegerInstance();
		// turn off grouping since it makes the numbers impossible to read !
		format.setGroupingUsed(false);
		/*
		 * The following two lines alters whether integer fields round up for >=5 or not, 
		 * with out them the number is rounded down for all decimals.
		 */
//		format.setParseIntegerOnly(false);
//		format.setRoundingMode(RoundingMode.HALF_UP);
		
		NumberFormatter formatter = new NumberFormatter(format){
			@Override  
		    public Object stringToValue(String text) throws ParseException {  
		        if(text.trim().isEmpty())  
		            return null;  
		        Object newValue = super.stringToValue(text);
		        return super.stringToValue(text);  
		    }
		};
		
		return formatter;
	}
	
}
