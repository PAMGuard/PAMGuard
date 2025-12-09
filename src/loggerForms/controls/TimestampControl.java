package loggerForms.controls;

import generalDatabase.DBControlUnit;
import generalDatabase.SQLTypes;

import java.awt.Dimension;
import java.awt.Font;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamUtils.PamCalendar;
import PamView.component.PamFormattedTextField;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

public class TimestampControl extends LoggerControl {

	protected static int TIME_FIELD_LENGTH = 8;
	protected static int DATE_FIELD_LENGTH = 10;
	protected JFormattedTextField textFieldDate;
	protected JFormattedTextField textFieldTime;
	private TimeZone timeZone;
	
	public TimestampControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		makeComponent();

		// sort out timezone information
		String tzName = controlDescription.getTopic();
		if (tzName != null && tzName.length() > 0) {
			timeZone = TimeZone.getTimeZone(tzName);
		}
	}

	
	private void makeComponent() {

		component.add(new LoggerFormLabel(loggerForm, controlDescription.getTitle()));
		component.add(new LoggerFormLabel(loggerForm, "Date"));
		component.add(textFieldDate = new PamFormattedTextField(getDateAbstractformatter()));
		component.add(new LoggerFormLabel(loggerForm, "Time"));
		component.add(textFieldTime = new PamFormattedTextField(getTimeAbstractformatter()));
		addF1KeyListener(textFieldDate);
		addF1KeyListener(textFieldTime);
		Font font = getControlDescription().getFormDescription().getFONT();
		textFieldDate.setFont(font);
		textFieldTime.setFont(font);
		
		Dimension d = textFieldDate.getPreferredSize();
		Dimension t = textFieldTime.getPreferredSize();
		double charwid = textFieldDate.getFontMetrics(font).getMaxAdvance()/2;
		d.width = (int) (DATE_FIELD_LENGTH * charwid);
		t.width = (int) (TIME_FIELD_LENGTH * charwid);
		textFieldDate.setPreferredSize(new Dimension(d));
		textFieldTime.setPreferredSize(new Dimension(t));
		setDefault();
		component.add(new LoggerFormLabel(loggerForm, controlDescription.getPostTitle()));
		
//		textFieldDate.setToolTipText(controlDescription.getHint());
//		textFieldTime.setToolTipText(controlDescription.getHint());
		
		textFieldDate.addFocusListener(new ComponentFocusListener());
		textFieldTime.addFocusListener(new ComponentFocusListener());
		setToolTipToAllSubJComponants(component);
//		textFieldDate.addPropertyChangeListener("value", new CommitListener());

	}
	
	
//	class CommitListener implements PropertyChangeListener{
//
//		/* (non-Javadoc)
//		 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
//		 */
//		@Override
//		public void propertyChange(PropertyChangeEvent evt) {
//			try {
//				textFieldDate.commitEdit();
//				textFieldTime.commitEdit();
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//	}

	private AbstractFormatter getTimeAbstractformatter() {
		return null;
////		DateFormat format= new SimpleDateFormat("HH:mm:ss"); //("H:m:s")
////		DateFormatter formatter = new DateFormatter(format){
//		
//		/* (non-Javadoc)
//		* @see javax.swing.text.InternationalFormatter#stringToValue(java.lang.String)
//		*/
//		@Override
//		public Object stringToValue(String text) throws ParseException {
//			if (text==null||text.length()==0)return null;
//			return super.stringToValue(text);
//		}
//		
////		/* (non-Javadoc)
////		 * @see javax.swing.text.InternationalFormatter#valueToString(java.lang.Object)
////		 */
////		@Override
////		public String valueToString(Object value) throws ParseException {
////			
////			return super.valueToString(value);
////		}
////		};
////		return formatter;
	}

	private AbstractFormatter getDateAbstractformatter() {
		return null;
//		DateFormat format= new SimpleDateFormat("yyyy-MM-dd"); //("d-M-yy")
//		DateFormatter formatter = new DateFormatter(format){
//			
//			/* (non-Javadoc)
//			* @see javax.swing.text.InternationalFormatter#stringToValue(java.lang.String)
//			*/
//			@Override
//			public Object stringToValue(String text) throws ParseException {
//				if (text==null||text.length()==0)return null;
//				return super.stringToValue(text);
//			}
//			
////			/* (non-Javadoc)
////			 * @see javax.swing.text.InternationalFormatter#valueToString(java.lang.Object)
////			 */
////			@Override
////			public String valueToString(Object value) throws ParseException {
////				
////				return super.valueToString(value);
////			}
//			};
//		return formatter;
	}

	/**
	 * gets Data in Timestamp format
	 */
	@Override
	public Object getData() {
		
		String date = textFieldDate.getText();
		String time = textFieldTime.getText();
		date = date.trim();
		time = time.trim();
		if (date.length() == 0 && time.length() == 0) {
			return null;
		}
		
//		System.out.println("text: "+date+" "+time);
//		System.out.println("mili: "+PamCalendar.msFromDateString(date+" "+time));
//		System.out.println("date: "+PamCalendar.dateFromDateString(date+" "+time));
//		System.out.println("tstp: "+PamCalendar.getTimeStamp(PamCalendar.msFromDateString(date+" "+time)));

		SQLTypes sqlTypes = getLoggerForm().getSqlTypes();
		return sqlTypes.getTimeStamp(PamCalendar.msFromDateString(date+" "+time));
		
//		return textFieldDate.getValue()+textFieldTime.getValue();
		
	}
	
	@Override
	public int autoUpdate() {
		if (findNMEADataBlock() != null) {
			return updateNMEAData();
		}
		
		if (loggerForm.getNewOrEdit()==LoggerForm.EditDataForm) return super.autoUpdate();
//		setData(PamCalendar.getTimeInMillis());
		long millis = PamCalendar.getTimeInMillis();
		if (timeZone != null) {
			millis += timeZone.getOffset(millis);
		}
		setData(millis);
		return AUTO_UPDATE_SUCCESS;
	}
	
	
	/**
	 * set data in long format - TODO should change to timestamp 
	 */
	@Override
	public void setData(Object timeObject) {//long
		
		if (timeObject==null){
//			System.out.println("timestamp null");
//			return;

			/*
			 * Graham You've got to test this stuff when you change it 
			 * These bloody formatters just don't work. 
			 * Thie form will not clear when you use them 
			 * D.
			 */
			textFieldDate.setText(null);
			textFieldTime.setText(null);
			
//			textFieldDate.setValue(null);
//			textFieldTime.setValue(null);
			return;
		}

		else if (timeObject.getClass().isAssignableFrom(String.class)) {
			timeObject = PamCalendar.millisFromDateString((String) timeObject, false);
		}
		
		SQLTypes sqlTypes = DBControlUnit.findConnection().getSqlTypes();
		Long tms = sqlTypes.millisFromTimeStamp(timeObject);
		
		if (tms==null){
//			textFieldDate.setText("");
//			textFieldTime.setText("");
			textFieldDate.setValue(null);
			textFieldTime.setValue(null);
			return;
		}
		long time = (Long) tms;
//		PamCalendar.formatDateShort/(time);
//		textFieldDate.setText(arg0)
		textFieldDate.setText(PamCalendar.formatDBDate(time));
		textFieldTime.setText(PamCalendar.formatTime(time));
//		try {
//			textFieldDate.commitEdit();
//			textFieldTime.commitEdit();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
	}


	@Override
	public void setDefault() {
		setData(null);
	}


	@Override
	public String getDataError() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		String dateStr = NMEADataBlock.getSubString(dataUnit.getCharData(), 
				controlDescription.getNmeaPosition());
		String timeStr = NMEADataBlock.getSubString(dataUnit.getCharData(), 
				controlDescription.getNmeaPosition()+1);
		
		String dateFormat = "ddMMyy";
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		Date d;
//		if (timeZone != null) {
//			df.setTimeZone(timeZone);
//		}
		try {
			d = df.parse(dateStr);
		}
		catch (ParseException e) {
			return AUTO_UPDATE_FAIL;
		}
		
		String timeFormat = "HHmmss";
		SimpleDateFormat tf = new SimpleDateFormat(timeFormat);
		Date t;
//		if (timeZone != null) {
//			tf.setTimeZone(timeZone);
//		}
		try {
			 t = tf.parse(timeStr);
		}
		catch (ParseException e) {
			return AUTO_UPDATE_FAIL;
		}
		Long millis =  d.getTime() + t.getTime();
//		System.out.println("NMEA date time = " + PamCalendar.formatDateTime(millis));
		setData(millis);
		return AUTO_UPDATE_SUCCESS;
	}


}
