/**
 * 
 */
package loggerForms.controlDescriptions;

import java.sql.Types;

import org.w3c.dom.Element;

import generalDatabase.EmptyTableDefinition;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupList;
import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;
import loggerForms.controls.LookupControl;
import loggerForms.dataselect.LookupDataSelCreator;

/**
 * @author GrahamWeatherup
 *
 */
public class CdLookup extends InputControlDescription {

	/**
	 * used for lookuptypes
	 */
	private LookupList lookupList;
	
	/**
	 * @param formDescription
	 */
	public CdLookup(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
		primarySQLType=Types.CHAR;
		setDataSelectCreator(new LookupDataSelCreator(this, formDescription.getFormsDataBlock()));
	}
	
	
	/**
	 * @return the lookupList
	 */
	public LookupList getLookupList() {
		if (lookupList == null) {
			lookupList = LookUpTables.getLookUpTables().getLookupList(getTopic());
		}
		return lookupList;
	}



	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#makeControl(loggerForms.LoggerForm)
	 */
	@Override
	public LoggerControl makeControl(LoggerForm loggerForm) {
		return new LookupControl(this, loggerForm);
	}


	/* (non-Javadoc)
	 * @see loggerForms.controlDescriptions.Cd#getHint()
	 */
	@Override
	public String getHint() {
		
		if (super.getHint()==null||EmptyTableDefinition.deblankString(super.getHint()).length()==0){
			return"Type code or press F4 for dropdown list";
		}else{
			return EmptyTableDefinition.deblankString(super.getHint());
		}
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
