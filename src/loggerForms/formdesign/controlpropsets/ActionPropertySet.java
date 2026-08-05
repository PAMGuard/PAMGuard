package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.ActionTopicPanel;
import loggerForms.formdesign.itempanels.CtrlColPanel;

/**
 * Property set for logger actions. Will just provide a dropdown list of actions and
 * store the result in the Topic field. 
 */
public class ActionPropertySet extends BasePropertySet{

	public ActionPropertySet(FormDescription formDescription, ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case Topic:
			return new ActionTopicPanel(selTitle, propertyName);
			default:
				return null;
		}
	}
}
