package loggerForms.controlDescriptions;

import java.sql.Types;

import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;
import loggerForms.controls.SubFormControl;

/**
 * Sub form control is a button which sits within the layout of a normal 
 * form and creates an additional Popup form when it's pressed, with appropriate
 * cross referencing between the two form types. . 
 * @author Doug
 *
 */
public class CdSubForm extends ControlDescription {

	public CdSubForm(FormDescription formDescription,
			ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.CHAR;
	
	}

	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new SubFormControl(this, loggerForm);
	}

	@Override
	public Object extractXMLElementData(Element el, String value) {
		return value;
	}
}
