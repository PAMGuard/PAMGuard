package loggerForms.formdesign;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.controlpropsets.BasePropertySet;
import loggerForms.formdesign.itempanels.ButtonTopicPanel;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.SubformTopicPanel;
import loggerForms.formdesign.itempanels.TextCtrlColPanel;

public class ButtonPropertySet extends BasePropertySet {
	
	public ButtonPropertySet(FormDescription formDescription, ControlTitle controlTitle) {
		super(formDescription, controlTitle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case Topic:
			return new ButtonTopicPanel(formDescription, selTitle, propertyName);
		case PostTitle:
			return null;
//			return new TextCtrlColPanel(controlTitle, UDColName.DbTitle, UDColName.DbTitle.getStringLength());
		case Autoclear:
		case ReadOnly:
		case DbTitle:
		case Length:
		case Required:
		case Plot:
			return null;
		
//		case Hint:
			
//		case Send_Control_Name:
//			return new SubFormCrossRefPanel(formDescription, selTitle, propertyNa)
		}
		return super.getItemPropertyPanel(selTitle, propertyName);
	}
}
