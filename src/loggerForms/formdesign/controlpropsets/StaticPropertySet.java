package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.TextCtrlColPanel;

public class StaticPropertySet extends BasePropertySet {

	public StaticPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch(propertyName) {
		case Title:
			return new TextCtrlColPanel(selTitle, propertyName, propertyName.getStringLength());
		}
		return null;
//		return super.getItemPropertyPanel(selTitle, propertyName);
	}

	@Override
	public String getPanelTitle() {
		return "Enter static Text to display on the form";
	}
	
	

}
