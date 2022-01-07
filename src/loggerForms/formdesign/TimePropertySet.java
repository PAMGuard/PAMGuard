package loggerForms.formdesign;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.controlpropsets.BasePropertySet;
import loggerForms.formdesign.itempanels.BooleanCtrlColPanel;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.IntegerCtlrColPanel;

public class TimePropertySet extends BasePropertySet {

	public TimePropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.controlpropsets.BasePropertySet#getItemPropertyPanel(loggerForms.formdesign.ControlTitle, loggerForms.UDColName)
	 */
	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case AutoUpdate:
//			return new BooleanCtrlColPanel(selTitle, UDColName.AutoUpdate);
			return new IntegerCtlrColPanel(selTitle, UDColName.AutoUpdate, "Interval betweem updates in seconds");
		case Length:
			return null;
		}
		return super.getItemPropertyPanel(selTitle, propertyName);
	}

}
