package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.LUTTopicPanel;

/**
 * Form properties that can be set for a particular control type. 
 */
public class LookupPropertySet extends BasePropertySet {

	public LookupPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.controlpropsets.BasePropertySet#getItemPropertyPanel(loggerForms.formdesign.ControlTitle, loggerForms.UDColName)
	 */
	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case Topic:
			return new LUTTopicPanel(selTitle, propertyName);
		case Length:
			// all LUT lengths are automatically set to LookUpTables.CODE_LENGTH = 12
			return null;//LookUpTables.CODE_LENGTH;
		}
		return super.getItemPropertyPanel(selTitle, propertyName);
	}

}
