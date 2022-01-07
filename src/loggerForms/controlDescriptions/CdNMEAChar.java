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
import loggerForms.controls.NMEACharControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdNMEAChar extends InputControlDescription {

	/**
	 * @param formDescription
	 */
	public CdNMEAChar(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.CHAR;
	}

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new NMEACharControl(this, loggerForm);
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
