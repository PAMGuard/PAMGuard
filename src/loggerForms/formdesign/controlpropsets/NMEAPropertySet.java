package loggerForms.formdesign.controlpropsets;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import loggerForms.FormDescription;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.itempanels.BooleanCtrlColPanel;
import loggerForms.formdesign.itempanels.CtrlColPanel;
import loggerForms.formdesign.itempanels.IntegerCtlrColPanel;
import loggerForms.formdesign.itempanels.NMEACtrlPanel;

public class NMEAPropertySet extends BasePropertySet {

	
	public NMEAPropertySet(FormDescription formDescription,
			ControlTitle controlTitle) {
		super(formDescription, controlTitle);
	}

	@Override
	public CtrlColPanel getItemPropertyPanel(ControlTitle selTitle,	UDColName propertyName) {
		switch (propertyName) {
		case NMEA_Module:
			return new NMEACtrlPanel(selTitle, propertyName, NMEACtrlPanel.CTRL_ALL);
		case AutoUpdate:
			return new IntegerCtlrColPanel(selTitle, UDColName.AutoUpdate, "Auto update interval");
		}
		return super.getItemPropertyPanel(selTitle, propertyName);
	}

}
