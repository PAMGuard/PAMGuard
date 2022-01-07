/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;

import org.w3c.dom.Element;

import loggerForms.FormDescription;
import loggerForms.FormsTableItem;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.CharAreaControl;
import loggerForms.controls.CharControl;
import loggerForms.controls.LoggerControl;

/**
 * @author GrahamWeatherup
 *
 */
public class CdChar extends InputControlDescription {

//	protected static String primarySQLType="";//Types.CHAR;
	
	/**
	 * @param formDescription
	 */
	public CdChar(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.CHAR;
		
		// TODO Auto-generated constructor stub
	}

	

	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
//		return new CharControl(this,loggerForm);
		return new CharAreaControl(this,loggerForm);
	}

	
	
	/**
	 * 
	 * @return data in Double/LatLong type.etc
	 */
	public Object getFormData(){
		return null;
	}
	
	public String getTableData(){
		return null;
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
