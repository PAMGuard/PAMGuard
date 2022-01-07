package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.controlDescriptions.ControlTypes;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.IntegerCtlrColPanel;

public class SpacePropertySet extends BasePropertySet {

	public SpacePropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case Length:
			return new IntegerCtlrColPanel(selTitle, propertyName, "Space between controls");
		}
		return null;
	}

}
