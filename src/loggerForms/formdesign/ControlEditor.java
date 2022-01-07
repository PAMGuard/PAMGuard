package loggerForms.formdesign;

import java.sql.Types;

import loggerForms.FormDescription;
import loggerForms.ItemInformation;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlDescription;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.controlpropsets.BasePropertySet;
import loggerForms.formdesign.controlpropsets.CharPropertySet;
import loggerForms.formdesign.controlpropsets.CheckBoxPropertySet;
import loggerForms.formdesign.controlpropsets.CounterPropertySet;
import loggerForms.formdesign.controlpropsets.LatLongPropertySet;
import loggerForms.formdesign.controlpropsets.LookupPropertySet;
import loggerForms.formdesign.controlpropsets.NMEAPropertySet;
import loggerForms.formdesign.controlpropsets.NullPropertySet;
import loggerForms.formdesign.controlpropsets.NumberPropertySet;
import loggerForms.formdesign.controlpropsets.SpacePropertySet;
import loggerForms.formdesign.controlpropsets.StaticPropertySet;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.TextCtrlColPanel;

/**
 * Main editor for a particular control. Not 100% sure how it will be displayed yet, but
 * this is basically a controller that does the work, while the graphics components to 
 * go into a dialog panel are separate classes. 
 * @author Doug
 *
 */
public class ControlEditor {
	
	private FormDescription formDescription;
	
	private ItemInformation itemDescription;
	
	private ControlEditor(FormDescription formDescription, ItemInformation itemDescription) {
		super();
		this.formDescription = formDescription;
		this.itemDescription = itemDescription;
	}
	
	public static BasePropertySet createControlPropertySet(FormDescription formDescription, ControlTitle controlTitle) {
		ControlTypes controlType = controlTitle.getType();
		if (controlType == null) {
			return new NullPropertySet(formDescription, controlTitle);
		}
		switch (controlType) {
		case CHAR:
			return new CharPropertySet(formDescription, controlTitle);
		case CHECKBOX:
			return new CheckBoxPropertySet(formDescription, controlTitle);
		case COUNTER:
			return new CounterPropertySet(formDescription, controlTitle);
		case DOUBLE:
			return new NumberPropertySet(formDescription, controlTitle);
		case HSPACE:
			return new SpacePropertySet(formDescription, controlTitle);
		case INTEGER:
			return new NumberPropertySet(formDescription, controlTitle);
		case LATLONG:
			return new LatLongPropertySet(formDescription, controlTitle);
		case LOOKUP:
			return new LookupPropertySet(formDescription, controlTitle);
		case NEWLINE:
			return new NullPropertySet(formDescription, controlTitle);
		case NMEACHAR:
		case NMEAFLOAT:
		case NMEAINT:
			return new NMEAPropertySet(formDescription, controlTitle);
		case SHORT:
			return new NumberPropertySet(formDescription, controlTitle);
		case SINGLE:
			return new NumberPropertySet(formDescription, controlTitle);
		case STATIC:
			return new StaticPropertySet(formDescription, controlTitle);
		case SUBFORM:
			return new SubformPropertySet(formDescription, controlTitle);
		case TIME:
			return new TimePropertySet(formDescription, controlTitle);
		case TIMESTAMP:
			return new TimePropertySet(formDescription, controlTitle);
		case VSPACE:
			return new SpacePropertySet(formDescription, controlTitle);
		default:
			return null;
		}
	}

	public FormDescription getFormDescription() {
		return formDescription;
	}

	public ItemInformation getItemDescription() {
		return itemDescription;
	}
	
//	/**
//	 * This basically needs to be a massive 2D switch statement looking at the
//	 * type of control and also the name of the individual property.
//	 * Since this would be utter chaos, we need to create a set of classes
//	 * which can handle the second level of the switch statement. 
//	 * @param propertyName
//	 * @return
//	 */
//	public ItemPropertyPanel getItemPropertyPanel(ControlTitle controlTitle, UDColName propertyName) {
//		switch(propertyName.getSqlType()) {
//		case Types.CHAR:
//			return new TextPropertyPanel(controlTitle, propertyName, 20);
//		}
//		return null;
//	}

	
}
