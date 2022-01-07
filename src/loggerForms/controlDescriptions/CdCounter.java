/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;

import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.CounterControl;
import loggerForms.controls.LoggerControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdCounter extends InputControlDescription {

	/**
	 * @param formDescription
	 */
	public CdCounter(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		// force minimum length of five characters. 
		if(getLength()==null || getLength()<5){
			setLength(5);
		}
		primarySQLType=Types.CHAR;
	}


	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new CounterControl(this, loggerForm);
	}


	@Override
	public Object extractXMLElementData(Element el, String value) {
		return value;
	}

	@Override
	public Object fromString(String data) {
		return data;
	}
	

}
