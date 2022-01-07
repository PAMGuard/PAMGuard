package loggerForms.formdesign.itempanels;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import NMEA.NMEAControl;
import NMEA.NMEAStringSelector;
import NMEA.NMEAStringsTable;
import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.pamBuoyGlobals;
import PamView.dialog.PamGridBagContraints;
import loggerForms.ItemInformation;
import loggerForms.PropertyTypes;
import loggerForms.UDColName;
import loggerForms.formdesign.ControlTitle;
import loggerForms.formdesign.FormEditor;
import loggerForms.formdesign.propertypanels.PropertyPanel;

public class NMEACtrlPanel extends CtrlColPanel {


	private JComboBox<String> nmeaModule;
	private NMEAStringSelector nmeaStringSelector;
	private JTextField nmeaPosition;
	private JPanel mainPanel;
	private int controlSelection;

	public static final int CTRL_MODULE = 0x1;
	public static final int CTRL_STRING = 0x2;
	public static final int CTRL_FIELD = 0x4;
	public static final int CTRL_EXAMPLE = 0x8;
	public static final int CTRL_ALL = CTRL_MODULE | CTRL_STRING | CTRL_FIELD | CTRL_EXAMPLE;

	public NMEACtrlPanel(ControlTitle controlTitle, UDColName propertyName, int ctrlSelection) {
		super(controlTitle, propertyName);
		this.controlSelection = ctrlSelection;
		mainPanel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new PamGridBagContraints();
		if ((controlSelection & CTRL_MODULE) != 0) {
			mainPanel.add(new JLabel(" Module ", JLabel.RIGHT));
			c.gridx++;
			mainPanel.add(nmeaModule = new JComboBox<String>());
			c.gridx++;
		}
		if ((controlSelection & CTRL_STRING) != 0) {
			mainPanel.add(new JLabel(", String ", JLabel.RIGHT));
			c.gridx++;
			nmeaStringSelector = new NMEAStringSelector();
			mainPanel.add(nmeaStringSelector.getComponent());
			c.gridx++;
		}
		if ((controlSelection & CTRL_FIELD) != 0) {
			mainPanel.add(new JLabel(", Index ", JLabel.RIGHT));
			c.gridx++;
			mainPanel.add(nmeaPosition = new JTextField(2));
		}
		if ((controlSelection & CTRL_EXAMPLE) != 0) {
			c.gridy++;
			c.gridx = 0;
			mainPanel.add(new JLabel(" Example ", JLabel.RIGHT), c);
			c.gridx++;
			c.gridwidth = 5;
			mainPanel.add(nmeaStringSelector.getExampleLabel(), c);
		}


		if (nmeaModule != null) {
			// fill the list on NMEA modules. 
			ArrayList<PamControlledUnit> nmeaModules = PamController.getInstance().findControlledUnits(NMEAControl.nmeaUnitType);
			if (nmeaModules != null) {
				for (PamControlledUnit aUnit:nmeaModules) {
					nmeaModule.addItem(aUnit.getUnitName());
				}
				if (nmeaModules.size() > 0) {
					nmeaModule.setSelectedIndex(0);
				}
			}

			nmeaModule.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					nmeaModuleSelection();
				}
			});
		}
	}

	protected void nmeaModuleSelection() {
		if (nmeaModule != null && nmeaStringSelector != null) {
			String sel = (String) nmeaModule.getSelectedItem();
			nmeaStringSelector.setNMEAModule(sel);
		}
	}

	@Override
	public Component getPanel() {
		return mainPanel;
	}

	@Override
	public void pushProperty(ItemInformation itemDescription) {
		String nmeaMod = itemDescription.getStringProperty(UDColName.NMEA_Module.toString());
		if (nmeaMod != null && nmeaModule != null) {
			nmeaModule.setSelectedItem(nmeaMod);
		}
		String nmeaStr = itemDescription.getStringProperty(UDColName.NMEA_String.toString());
		if (nmeaStr != null && nmeaStringSelector != null) {
			nmeaStringSelector.setSelectedString(nmeaStr);
		}
		Integer nmeaPos = itemDescription.getIntegerProperty(UDColName.NMEA_Position.toString());
		if (nmeaPos != null && nmeaPosition != null) {
			nmeaPosition.setText(String.format("%d", nmeaPos));
		}
	}

	@Override
	public boolean fetchProperty(ItemInformation itemDescription) {
		String nmeaMod = null;
		String nmeaStr = null;
		Integer nmeaPos = null;
		boolean ok = true;
		if (nmeaModule != null) {
			nmeaMod = (String) nmeaModule.getSelectedItem();
			if (nmeaMod == null) ok = false;
		}
		if (nmeaStringSelector != null) {
			nmeaStr = (String) nmeaStringSelector.getSelectedString();
			if (nmeaStr == null) ok = false;
		}
		if (nmeaPosition != null) {
			try {
				nmeaPos = Integer.valueOf(nmeaPosition.getText());
			}
			catch (NumberFormatException e) {
				ok = false;
			}
		}
		itemDescription.setProperty(UDColName.NMEA_Module.toString(), nmeaMod);
		itemDescription.setProperty(UDColName.NMEA_String.toString(), nmeaStr);
		itemDescription.setProperty(UDColName.NMEA_Position.toString(), nmeaPos);
		return ok;
	}



}
