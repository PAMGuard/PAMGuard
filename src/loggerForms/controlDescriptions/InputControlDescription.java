package loggerForms.controlDescriptions;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.LoggerForm;
import loggerForms.controls.LoggerControl;
import loggerForms.dataselect.ControlDataSelCreator;

public abstract class InputControlDescription extends ControlDescription {
	
	private ControlDataSelCreator dataSelectCreator;

	protected InputControlDescription(FormDescription formDescription, ItemInformation itemInformation) {
		super(formDescription, itemInformation);
	}

	/**
	 * Convert data in a string back into a valid object of the
	 * correct type for this form.  
	 * @param data string data, e.g. from a Json string. 
	 * @return Obect in correct format
	 */
	abstract public Object fromString(String data);
	
	/**
	 * Convert a data object to a string. Default just calls
	 * the objects .toString() function.
	 * @param dataObject data object, e.g. from database. 
	 * @return String
	 */
	public String toString(Object dataObject) {
		if (dataObject == null) {
			return null;
		}
		else {
			return dataObject.toString();
		}
	}
//	{
//		return data;
//	}

	/**
	 * @return the dataSelectCreator
	 */
	public ControlDataSelCreator getDataSelectCreator() {
		return dataSelectCreator;
	}

	/**
	 * @param dataSelectCreator the dataSelectCreator to set
	 */
	public void setDataSelectCreator(ControlDataSelCreator dataSelectCreator) {
		this.dataSelectCreator = dataSelectCreator;
	}


}
