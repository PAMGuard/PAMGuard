/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;

import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.DoubleControl;
import loggerForms.controls.LoggerControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdDouble extends NumberControlDescription {

	/**
	 * @param formDescription
	 */
	public CdDouble(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.DOUBLE;
	}

	

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		// TODO Auto-generated method stub
		return new DoubleControl(this,loggerForm);
	}

	@Override
	public Object fromString(String data) {
		if (data == null) {
			return null;
		}
		try {
			return Double.valueOf(data);
		}
		catch (Exception e) {
			System.out.println("Unable to unpack cbDoubleBox item " + data);
			return null;
		}
	}



	@Override
	public Object extractXMLElementData(Element el, String value) {
		if (value == null) {
			return null;
		}
		try {
			return Double.valueOf(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

}
