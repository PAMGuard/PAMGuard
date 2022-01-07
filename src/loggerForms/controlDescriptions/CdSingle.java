/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;

import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.FloatControl;
import loggerForms.controls.LoggerControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdSingle extends NumberControlDescription {

	/**
	 * @param formDescription
	 */
	public CdSingle(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.REAL;
	}
	

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new FloatControl(this,loggerForm);
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
	public String getHint() {
		// TODO Auto-generated method stub
		return super.getHint();
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
			System.out.println("Unable to unpack CdSingle item " + data);
			return null;
		}
	}
}
