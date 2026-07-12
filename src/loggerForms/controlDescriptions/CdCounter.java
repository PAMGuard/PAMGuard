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
import loggerForms.controls.FormCounterManagement;
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
		// immediately initialise the counter so that it will get sent to any new subscribers. 
//		to do this we need a counter - annoying, but can't avoid. 
		CounterControl c = new CounterControl(this, null); 
		FormCounterManagement.getInstance().getCurrentCounterNumber(c, formDescription.getDBTABLENAME());
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
