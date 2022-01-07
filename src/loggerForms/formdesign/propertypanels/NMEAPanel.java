package loggerForms.formdesign.propertypanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import NMEA.NMEAControl;
import NMEA.NMEAStringSelector;
import PamController.PamControlledUnit;
import PamController.PamController;
import loggerForms.PropertyTypes;
import loggerForms.formdesign.FormEditor;

public class NMEAPanel extends PropertyPanel {

	private JComboBox<String> nmeaModule;
	
	private NMEAStringSelector nmeaStringSelector;
	
	public NMEAPanel(FormEditor formEditor, PropertyTypes propertyType) {
		super(formEditor, propertyType);
		nmeaStringSelector = new NMEAStringSelector();
		
		addItem(new JLabel("Save whenever the "));
		addItem(nmeaStringSelector.getComponent());
		addItem(new JLabel(" string is read from the "));
		addItem(nmeaModule = new JComboBox<String>());
		addItem(new JLabel(" module"));
		
		nmeaModule.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nmeaModuleSelected();
			}
		});
		 ArrayList<PamControlledUnit> nmeaModules = PamController.getInstance().findControlledUnits(NMEAControl.nmeaUnitType);
		 if (nmeaModules != null) {
			 for (PamControlledUnit aUnit:nmeaModules) {
				 nmeaModule.addItem(aUnit.getUnitName());
			 }
			 if (nmeaModules.size() > 0) {
				 nmeaModule.setSelectedIndex(0);
			 }
		 }
	}

	protected void nmeaModuleSelected() {
		nmeaStringSelector.setNMEAModule((String) nmeaModule.getSelectedItem()); 
	}

	/* (non-Javadoc)
	 * @see loggerForms.formdesign.propertypanels.PropertyPanel#propertyEnable(boolean)
	 */
	@Override
	public void propertyEnable(boolean enabled) {
		nmeaStringSelector.setEnabled(enabled);
		nmeaModule.setEnabled(enabled);
	}

	
	
}
