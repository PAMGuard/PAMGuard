package loggerForms.controls;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.MaskFormatter;

import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class NMEACharControl extends NMEAControl {

	public NMEACharControl(ControlDescription controlDescription, LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
	}
	
	@Override
	public Object getData() {
		String text = textField.getText();
		if (text==null){
			return null;
		}
		Integer leng = controlDescription.getLength();
		if (leng == null) {
			return null;
		}
		Integer acLeng =text.length();
		if (acLeng==0){
			return null;
		}
		if (acLeng<=leng){
			return textField.getText();
		}else{
			try {
//				System.out.print("Only the first "+leng+" character will be saved:");
//				System.out.println(textField.getText(0,leng));
				if (dataWarning==null)dataWarning="";
				dataWarning+="\n Only the first "+leng+" character will be saved:"+textField.getText(0,leng);
				return textField.getText(0,leng);
			} catch (BadLocationException e) {
				System.out.println("field not saved");
				e.printStackTrace();
				return null;
			}
		}
	}
	
	@Override
	AbstractFormatter getAbstractformatter() {
//		DefaultFormatter formatter = new DefaultFormatter();
//		return formatter;
		return null;
	}
	
}
