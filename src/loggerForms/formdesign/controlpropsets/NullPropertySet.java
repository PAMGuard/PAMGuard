package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.CtrlColPanel;

public class NullPropertySet extends BasePropertySet {

	public NullPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.controlpropsets.BasePropertySet#getPanelTitle()
	 */
	@Override
	public String getPanelTitle() {
		ControlTypes type = controlTitle.getType();
		if (type == null) {
			return "Select a control type from the drop down list and configure the control";
		}
		else {
			return "There are no configurable properties for " + controlTitle.getType().toString() + " controls";
		}
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.controlpropsets.BasePropertySet#getItemPropertyPanel(loggerForms.formdesign.ControlTitle, loggerForms.UDColName)
	 */
	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		return null;
	}

}
