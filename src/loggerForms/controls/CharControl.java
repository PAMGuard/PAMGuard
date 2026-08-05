package loggerForms.controls;


import java.text.ParseException;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class CharControl extends SimpleControl {
	
	public CharControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
				
	}
	
	@Override
	public Object getData() {
		String text = textField.getText();
		if (text==null){
			return null;
		}
		Integer leng = controlDescription.getLength();
		Integer acLeng =text.length();
		if (acLeng==0){
			return null;
		}
		if (acLeng<=leng){
			return textField.getText();
		}else{
			try {
				dataWarning=controlDescription.getTitle()+": Only the first "+leng+" character will be saved.";
//				System.out.println(textField.getText(0,leng));
				return textField.getText(0,leng);
			} catch (BadLocationException e) {
				dataError=controlDescription.getTitle()+": field not saved, data entry too long and could not truncate";
				e.printStackTrace();
				return null;
			}
		}
	}
	

	@Override
	AbstractFormatter getAbstractformatter() {
		
		DefaultFormatter formatter = new DefaultFormatter(){
			@Override  
		    public Object stringToValue(String text) throws ParseException {  
		        if(text.trim().isEmpty())  
		            return null;  
		        return super.stringToValue(text);  
		    }
		};
		formatter.setValueClass(String.class);
		return formatter;
		
	}

	
	
	
	
}

