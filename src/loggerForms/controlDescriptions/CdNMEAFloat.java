/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;

import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;
import loggerForms.controls.NMEAFloatControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdNMEAFloat extends NumberControlDescription {

	/**
	 * @param formDescription
	 */
	public CdNMEAFloat(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.REAL;
	}
	

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new NMEAFloatControl(this,loggerForm);
	}
	
	@Override
	public Object extractXMLElementData(Element el, String value) {
		if (value == null) {
			return null;
		}
		try {
			return Float.valueOf(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Object fromString(String data) {
		if (data == null) {
			return null;
		}
		try {
			return Float.valueOf(data);
		}
		catch (Exception e) {
			System.out.println("Unable to unpack CdNMEAFloat item " + data);
			return null;
		}
	}
}
