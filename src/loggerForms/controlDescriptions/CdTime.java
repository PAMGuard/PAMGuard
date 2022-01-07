/**
 * 
 */
package loggerForms.controlDescriptions;

import generalDatabase.DBControlUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.SQLTypes;

import java.sql.Types;

import org.w3c.dom.Element;

import PamUtils.PamCalendar;
import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;
import loggerForms.controls.TimeControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdTime extends InputControlDescription {

	/**
	 * @param formDescription
	 */
	public CdTime(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.TIME;
	}

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new TimeControl(this, loggerForm);
	}
	
	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#getHint()
	 */
	@Override
	public String getHint() {
		
		if (super.getHint()==null||EmptyTableDefinition.deblankString(super.getHint()).length()==0){
			return"<html>Press F1 to update time automatically";
		}else{
			return EmptyTableDefinition.deblankString(super.getHint());
		}
	}
	@Override
	public Object extractXMLElementData(Element el, String value) {
		/*
		 * Need to check whether this is stored as a string or as a number. 
		 */
		return value;
	}

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.ControlDescription#formatDataItem(java.lang.Object)
	 */
	@Override
	public String formatDataItem(Object data) {
		if (data == null) {
			return null;
		}
		SQLTypes sqlTypes = DBControlUnit.findConnection().getSqlTypes();
		Long tms = sqlTypes.millisFromTimeStamp(data);
		if (tms != null) {
			return PamCalendar.formatTime(tms);
		}
		else {
			return null;
		}
		
//		if (data.getClass() != Timestamp.class) {
//			return data.toString();
//		}
//		
//		long millis = PamCalendar.millisFromTimeStamp((Timestamp) data);
//		return PamCalendar.formatTime(millis);
	}

	@Override
	public Object fromString(String data) {
		return data;
	}

}
