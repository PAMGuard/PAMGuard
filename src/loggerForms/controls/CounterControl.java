package loggerForms.controls;


import java.awt.Component;
import java.text.ParseException;

import javax.swing.*;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.MaskFormatter;

import loggerForms.FormDescription;
import loggerForms.LoggerForm;
import loggerForms.PropertyTypes;
import loggerForms.controlDescriptions.ControlDescription;

public class CounterControl extends SimpleControl {
//	ControlDescription controlDescription;
//	LoggerForm loggerForm;
	
	Character suffix;
	
	
	public CounterControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		
		suffix = controlDescription.getFormDescription().getFormsControl().getOutputTableNameCounterSuffix(controlDescription.getFormDescription());
		
		updateCounter();
		
		//append counter numberto tab name
		loggerForm.setHasCounter(this);
		textField.setFocusable(false);
		
	}
	
	
	
	



	private String calculateCounter() {
		int num = FormCounterManagement.getInstance().getCounterNumber(this,controlDescription.getFormDescription().getDBTABLENAME());
		
		String numSt = Integer.toString(num);
		int nZeros = 3-numSt.length();
		
		String tSt = "";
		for (int i=0;i<nZeros;i++){
			tSt+="0";
//			System.out.println(tSt);
		}
		
		numSt = tSt+numSt;
//		System.out.println(tSt);
//		System.out.println(numSt);
		
		if (suffix!=null){
			numSt+= suffix.toString();
		}else{
//			System.out.println("SUFFIX NULL");
		}
		
//		System.out.println("numSt"+numSt);
//
//		System.out.println("numStS"+suffix);
		return numSt;
	
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
	public void setDefault() {
		
	}
	
	@Override
	public void clear(){
		
	}
	
	
	
	

	@Override
	AbstractFormatter getAbstractformatter() {
		
		DefaultFormatter formatter = new DefaultFormatter();
		return formatter;
		
		
		
	}



	public void updateCounter() {
		if (loggerForm.getNewOrEdit()==LoggerForm.EditDataForm) return;
//		System.out.println("updateCounter");
		setData(calculateCounter());
	}

	
	
	
	
}

