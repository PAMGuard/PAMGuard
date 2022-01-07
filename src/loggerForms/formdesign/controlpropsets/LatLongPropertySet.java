package loggerForms.formdesign.controlpropsets;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.BooleanCtrlColPanel;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.IntegerCtlrColPanel;
import loggerForms.formdesign.itempanels.NMEACtrlPanel;

public class LatLongPropertySet extends BasePropertySet {

	public LatLongPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,
			UDColName propertyName) {
		switch (propertyName) {
		case Length:
			return null;
		case NMEA_Module:
			return new NMEACtrlPanel(selTitle, propertyName, NMEACtrlPanel.CTRL_MODULE | NMEACtrlPanel.CTRL_STRING);
		case AutoUpdate:
			return new IntegerCtlrColPanel(selTitle, propertyName, "Auto update interval");
		default:
			return super.getItemPropertyPanel(selTitle, propertyName);
		}
	}

}
