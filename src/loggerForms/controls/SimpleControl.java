package loggerForms.controls;

import generalDatabase.EmptyTableDefinition;
import generalDatabase.SQLTypes;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.Format;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.spi.DateFormatProvider;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.NumberFormatter;
import javax.swing.text.Position;
import javax.swing.text.Segment;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;


/**
 * class to extend- to be used for any control with only one data input field
 * @author GrahamWeatherup
 *
 */
public abstract class SimpleControl extends LoggerControl {
	
	
	protected JFormattedTextField textField;

	public SimpleControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		
		makeComponent();
	}
	
	abstract AbstractFormatter getAbstractformatter(); //protected?
	
	
	private void makeComponent(){
		
		component.add(new LoggerFormLabel(loggerForm, controlDescription.getTitle()));
		
		component.add(textField = new JFormattedTextField(getAbstractformatter()));

		Font font = getControlDescription().getFormDescription().getFONT();
		textField.setFont(font);
		Dimension d = textField.getPreferredSize();
		Integer l=controlDescription.getLength();
		double charwid = textField.getFontMetrics(font).getMaxAdvance()/2;
		if (l==null||l==0){
			d.width = (int) (3 * charwid);
		}else{
			d.width = (int) (l * charwid);
		}
		textField.setPreferredSize(new Dimension(d.width, d.height));
		
		component.add(new LoggerFormLabel(loggerForm, controlDescription.getPostTitle()));
		setDefault();
		addF1KeyListener(textField);
		textField.addFocusListener(new ComponentFocusListener());
//		textField.addPropertyChangeListener("value",new CommitListener());
		setToolTipToAllSubJComponants(component);
		
		
	}
//	class CommitListener implements PropertyChangeListener{
//
//		/* (non-Javadoc)
//		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
//		 */
//		@Override
//		public void propertyChange(PropertyChangeEvent evt) {
//			if (evt.getPropertyName()=="value"){
//				try {
//					
//					textField.commitEdit();
//				} catch (ParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			
//		}
//		
//	}
	

	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#getData()
	 */
//	@Override
//	public Object getData() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#setData(java.lang.Object)
	 */
	@Override
	public void setData(Object data) {
		if (data == null) {
			textField.setText("");
			
		}
		else {
			textField.setText(data.toString());
			
		}
		try {
			textField.commitEdit();
		} catch (ParseException e) {
			
//			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see loggerForms.controls.LoggerControl#setDefault()
	 */
	@Override
	public void setDefault() {
		if (getControlDescription().getDefaultValue()!=null && getControlDescription().getDefaultValue().length() > 0){
			setData(getControlDescription().getDefaultValue());
		}
	}
	
	
	@Override
	public void clear() {
		super.clear();
		/**
		 * IF using formatters, then need to commit the edit. 
		 * Normally this would have happened when the control lost the 
		 * focus, but the control didn't have the focus when the form 
		 * was cleared. This results in the underlying model still contains
		 * the last saved value, which it may suddenly revert to if an invalid
		 * value is then entered. 
		 */
		try {
			textField.commitEdit();
		} catch (ParseException e) {
//			e.printStackTrace();
		}
	}

	public String getDataError(){
		if (getData()==null){
			String text = textField.getText();
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

	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		String subStr = NMEADataBlock.getSubString(dataUnit.getCharData(), 
				controlDescription.getNmeaPosition());
		if (subStr == null) {
			clear();
			return AUTO_UPDATE_FAIL;
		}

		textField.setText(subStr);
		try {
			textField.commitEdit();
		} catch (ParseException e) {
			e.printStackTrace();
			return AUTO_UPDATE_FAIL;
		}
		//		textField.setValue(subStr);

		return AUTO_UPDATE_SUCCESS; 
	}
	
}
