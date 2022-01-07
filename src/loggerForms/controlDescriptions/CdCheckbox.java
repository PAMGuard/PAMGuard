/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;

import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.CheckboxControl;
import loggerForms.controls.LoggerControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdCheckbox extends InputControlDescription {

	/**
	 * @param formDescription
	 */
	public CdCheckbox(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.BIT;
	}
	

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new CheckboxControl(this,loggerForm);
	}


	@Override
	public Object extractXMLElementData(Element el, String value) {
		if (value == null) {
			return null;
		}
		if (value.toLowerCase().contains("true")) {
			return Boolean.TRUE;
		}
		else {
			return Boolean.FALSE;
		}
	}


	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.ControlDescription#formatDataItem(java.lang.Object)
	 */
	@Override
	public Object formatDataItem(Object data) {
		/**
		 * This needs to be overridden so as NOT to return toString, since 
		 * when it's used in the data table, it get's put into a boolean checkbox
		 * and it has to be given a Boolean object, not a string, or it throws an exception. 
		 * 
		 */
		return data;
	}

	@Override
	public Object fromString(String data) {
		if (data == null) {
			return null;
		}
		try {
			return Boolean.valueOf(data);
		}
		catch (Exception e) {
			System.out.println("Unable to unpack cbcheckBox item " + data);
			return null;
		}
	}

}
