package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.IntegerCtlrColPanel;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.TextCtrlColPanel;
import loggerForms.formdesign.itempanels.TitleCtrlColPanel;

public class CharPropertySet extends BasePropertySet {

	public CharPropertySet(FormDescription formDescription,
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
		case Title:
			return new TitleCtrlColPanel(selTitle, UDColName.Title, UDColName.Title.getStringLength());
		case Length:
			return new IntegerCtlrColPanel(selTitle, propertyName, "Max characters");
		case Plot:
		case ReadOnly:
			return null;
			default:
//				return null;
		}
		return super.getItemPropertyPanel(selTitle, propertyName);
	}

}
