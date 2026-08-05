package loggerForms.controls;


import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.text.BadLocationException;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamView.component.PamFormattedTextField;
import PamView.dialog.PamTextArea;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class CharAreaControl extends LoggerControl {
//	ControlDescription controlDescription;
//	LoggerForm loggerForm;
	
	private JTextArea textArea;


	public CharAreaControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		//create this just to steal graphics properties so is consistent.
		JFormattedTextField textField = new PamFormattedTextField();
		
		textArea=new PamTextArea();
		textArea.setLineWrap(true);
		Font font = getControlDescription().getFormDescription().getFONT();
		if (font != null) {
			textArea.setFont(font);
		}
		textArea.addKeyListener(new KeyAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_TAB) {
//		    		System.out.println(e.getModifiers());
		    		if(e.getModifiers() > 0) textArea.transferFocusBackward();
		    		else textArea.transferFocus();	
		    		e.consume();
		    	}
			}
		});
		textArea.setBorder(textField.getBorder());
		Dimension d = textField.getPreferredSize();
//		System.out.println(d);
		Integer l=controlDescription.getLength();
		double charwid = textField.getFontMetrics(textField.getFont()).getMaxAdvance()/2;
//		System.out.println("l:"+l);
		FontMetrics fontMetrics = null;
		if (font != null) {
			fontMetrics = textArea.getFontMetrics(font);
			d.height = fontMetrics.getHeight();
		}
		int lines = 1;
		if (l==null||l==0){
			d.width = (int) (3 * charwid);
		}
		else if(l>50){
			d.width = (int) (50 * charwid);
			lines = (int) Math.ceil(controlDescription.getLength()/50.0);
			
		}else{
			d.width = (int) (l * charwid);
		}
		d.height *= lines;
		if (fontMetrics != null) {
			d.height += fontMetrics.getDescent();
		}
		
		
		Dimension dim=new Dimension(d);
		
		textArea.setPreferredSize(dim);
		textArea.setMaximumSize(dim);
		textArea.setMinimumSize(dim);
		
//		JScrollPane scrollPane= new JScrollPane(textArea);
//		scrollPane.add
		
		
		component.add(new LoggerFormLabel(loggerForm, controlDescription.getTitle()));
		component.add(textArea);
		component.add(new LoggerFormLabel(loggerForm, controlDescription.getPostTitle()));
		setDefault();
		addF1KeyListener(textArea);
		setToolTipToAllSubJComponants(component);
		addFocusListenerToAllSubComponants(new ComponentFocusListener(), component);
		addMouseListenerToAllSubComponants(new HoverListener(), component);
//		textField.addFocusListener(new ComponentFocusListener());
//		textField.addPropertyChangeListener("value",new CommitListener());
//		setToolTipToAllSubJComponants(component);
		
		
//		if (controlDescription.getLength()>50){
//			int lines = (int) Math.ceil(controlDescription.getLength()/50.0);
//			Dimension dim = textField.getPreferredSize();
//			dim.height=lines*dim.height;
//			textField.setPreferredSize(new Dimension(dim));
//			textField.set
//			
//			//textField.setAutoscrolls(true);
//		}
		
	}
	
//	class FormsTextDocument extends Doc
	
	
	@Override
	public Object getData() {
		String text = textArea.getText();
		if (text==null){
			return null;
		}
		Integer leng = controlDescription.getLength();
		Integer acLeng =text.length();
		if (acLeng==0){
			return null;
		}
		if (acLeng<=leng){
			return textArea.getText();
		}else{
			try {
				dataWarning=controlDescription.getTitle()+": Only the first "+leng+" character will be saved.";
//				System.out.println(textField.getText(0,leng));
				return textArea.getText(0,leng);
			} catch (BadLocationException e) {//shouldnt happen since leng already checked to be <=acLeng
				dataError=controlDescription.getTitle()+": field not saved, data entry too long and could not truncate";
				e.printStackTrace();
				return null;
			}
		}
	}

	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#getDataError()
	 */
	@Override
	public String getDataError() {
		if (getData()==null){
			String text = textArea.getText();
			if (text==null||text.length()==0){
				Boolean req = controlDescription.getRequired();
				if (req==true){
					return controlDescription.getTitle()+" is a required field.";
				}
				return null;
			}else{
				return dataError;
			}
			
		}
		return dataError;
	}

	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#setData(java.lang.Object)
	 */
	@Override
	public void setData(Object data) {
		if (data==null){
			textArea.setText("");
		} else {
			textArea.setText(data.toString());
		}
	}

	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#setDefault()
	 */
	@Override
	public void setDefault() {
		setData(getControlDescription().getDefaultValue());
	}

	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#fillNMEAControlData(NMEA.NMEADataUnit)
	 */
	@Override
	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		String subStr = NMEADataBlock.getSubString(dataUnit.getCharData(), 
				controlDescription.getNmeaPosition());
		if (subStr == null) {
			clear();
			return AUTO_UPDATE_FAIL;
		}

		textArea.setText(subStr);
		
		//		textField.setValue(subStr);

		return AUTO_UPDATE_SUCCESS; 
	}
	

	
	
	
	
}

