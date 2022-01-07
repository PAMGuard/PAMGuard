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
import loggerForms.controls.NMEAIntegerControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdNMEAInt extends ControlDescription {

	/**
	 * @param formDescription
	 */
	public CdNMEAInt(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.INTEGER;
	}

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new NMEAIntegerControl(this,loggerForm);
	}
	
	@Override
	public Object extractXMLElementData(Element el, String value) {
		if (value == null) {
			return null;
		}
		try {
			return Integer.valueOf(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
}
