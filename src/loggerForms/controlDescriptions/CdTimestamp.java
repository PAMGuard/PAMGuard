/**
 * 
 */
package loggerForms.controlDescriptions;

import generalDatabase.EmptyTableDefinition;

import java.sql.Types;




import org.w3c.dom.Element;

import PamUtils.PamCalendar;
import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;
import loggerForms.controls.TimestampControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdTimestamp extends InputControlDescription {

	/**
	 * @param formDescription
	 */
	public CdTimestamp(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.TIMESTAMP;
	}
	

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new TimestampControl(this, loggerForm);
		
	}
	
	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#getHint()
	 */
	@Override
	public String getHint() {
		
		if (super.getHint()==null||EmptyTableDefinition.deblankString(super.getHint()).length()==0){
			return"<html>Press F1 to update time and date automatically";
		}else{
			return EmptyTableDefinition.deblankString(super.getHint());
		}
	}
	
	@Override
	public Object extractXMLElementData(Element el, String value) {
		/*
		 * Need to check whether this is returned as a string or as a number. 
		 */
		return value;
	}	
	
	@Override
	public Object fromString(String data) {
		return data;
	}

}
