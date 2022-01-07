package clickDetector.dialogs;


import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import Localiser.DelayMeasurementParams;
import Localiser.algorithms.timeDelayLocalisers.bearingLoc.DelayOptionsDialog;
import PamView.PamGui;
import PamView.PamSymbol;
import PamView.dialog.PamGridBagContraints;
import clickDetector.ClickControl;
import clickDetector.ClickParameters;
import clickDetector.ClickClassifiers.ClickIdentifier;

public class ClickDelayPanel {
	
	private JPanel mainPanel, typePanel;
	private ClickControl clickControl;
//	private ClickParameters clickParameters;
	private DelayMeasurementParams[] delayParams;
	private Window parentWindow;

	public ClickDelayPanel(ClickControl clickControl, Window parentWindow) {
		super();
		this.clickControl = clickControl;
		this.parentWindow = parentWindow;
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Timing Options"));
		typePanel = new JPanel(new GridBagLayout());
		mainPanel.add(BorderLayout.NORTH, typePanel);
	}

	public JComponent getDialogComponent() {
		return mainPanel;
	}

	public void setParams(ClickParameters clickParameters) {
//		this.clickParameters = clickParameters.clone();
		createPanelControls(clickParameters);
		for (int i = 0; i < typesList.length; i++) {
			useDefault[i].setSelected(delayParams[i] == null);
		}
		saySettings(0); // will do the lot !
		enableControls();
	}

	int[] typesList;
	JLabel[] typeSettings;
	JLabel[] typeNames;
	JButton[] typeChange;
	JCheckBox[] useDefault;
	private void createPanelControls(ClickParameters clickParameters) {
		ClickIdentifier clickIdentifier = clickControl.getClickIdentifier();
		int nTypes = 1; // always a default. 
		int[] codeList = null;
		if (clickIdentifier != null) {
			codeList = clickIdentifier.getCodeList();
			if (codeList != null) {
				nTypes += codeList.length;
			}
		}
		// allocate space for everything we need. 
		typesList = new int[nTypes];
		typeNames = new JLabel[nTypes];
		useDefault = new JCheckBox[nTypes];
		typeSettings = new JLabel[nTypes];
		typeChange = new JButton[nTypes];
		delayParams = new DelayMeasurementParams[nTypes];
		delayParams[0] = clickParameters.getDelayMeasurementParams(0, true);
		for (int i = 1; i < nTypes; i++) {
			typesList[i] = codeList[i-1];
			delayParams[i] = clickParameters.getDelayMeasurementParams(typesList[i], false);
		}
		typePanel.removeAll();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
		JPanel subPanel;
		// column headings. 
		
		GridBagConstraints c = new PamGridBagContraints();
		String typeName;
		PamSymbol[] typeSymbols = null;
		if (clickIdentifier != null) {
			typeSymbols = clickIdentifier.getSymbols();
		}
		for (int i = 0; i < nTypes; i++) {
			c.gridx = 0;
			c.gridwidth = 1;
			if (i == 0) {
				typeName = "Unclassified clicks / default";
			}
			else {
				typeName = clickIdentifier.getSpeciesName(typesList[i]);
			}
			subPanel = new JPanel(new GridBagLayout());
//			typePanel.add(typeNames[i] = new JLabel(typeName + " ", JLabel.RIGHT), c);
			subPanel.setBorder(new TitledBorder(typeName));
			typePanel.add(subPanel);
			c = new PamGridBagContraints();
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.NONE;
			
			useDefault[i] = new JCheckBox("Use Default");
			useDefault[i].addActionListener(new DefaultButton(i));
			if (i > 0) {
				subPanel.add(useDefault[i], c);
				Icon icon = typeSymbols[i-1];
				c.gridx++;
				subPanel.add(new JLabel(icon));
			}
			c.gridx++;
			c.anchor = GridBagConstraints.EAST;
			subPanel.add(typeChange[i] = new JButton("Change"), c);
			typeChange[i].addActionListener(new SettingsButton(i));
			c.gridx = 0;
			c.gridy++;
			c.gridwidth = 3;
			subPanel.add(typeSettings[i] = new JLabel(
					"                                                                                                                      ")
					, c);
			c.gridy++;

		}
		typePanel.validate();
		PamGui.packFrame(typePanel);
//		PamDialog.packFrame();
//		Container parent = mainPanel.getParent();
//		if (parent != null) {
//			parent.validate();
//		}
	}
	
	private DelayMeasurementParams getParamsForIndex(int index, boolean forceDefault) {
		DelayMeasurementParams dmp = delayParams[index];
		if (useDefault[index].isSelected()) {
			dmp = delayParams[0];
		}
		if (forceDefault && dmp == null) {
			dmp = delayParams[0];
		}
		return dmp;
	}

	private void saySettings(int index) {
		DelayMeasurementParams dmp = getParamsForIndex(index, true);
		if (dmp == null) {
			typeSettings[index].setText("Error - enknown delay settings");
		}
		else {
			typeSettings[index].setText(dmp.toString());
		}
		if (index == 0) {
			for (int i = 1; i < typesList.length; i++) {
				saySettings(i);
			}
		}
		PamGui.packFrame(mainPanel);
	}

	/**
	 * Copies the local list back into the click settings, removing 
	 * old entries as necessary. 
	 * @param clickParameters
	 * @return true unless there are no default params. 
	 */
	public boolean getParams(ClickParameters clickParameters) {
		if (delayParams[0] == null) {
			return false;
		}
		clickParameters.setDelayMeasurementParams(0, delayParams[0]);
		for (int i = 1; i < typesList.length; i++) {
			if (useDefault[i].isSelected()) {
				clickParameters.setDelayMeasurementParams(typesList[i], null);
			}
			else {
				clickParameters.setDelayMeasurementParams(typesList[i], delayParams[i]);
			}
		}
		return true;
	}
	
	void defaultButtonPress(int typeIndex) {

		saySettings(typeIndex);
		enableControls();
	}
	
	void settingsButtonPress(int typeIndex) {
		DelayMeasurementParams dmp = getParamsForIndex(typeIndex, true);
		dmp = DelayOptionsDialog.showDialog(parentWindow, dmp);
		if (dmp != null) {
			delayParams[typeIndex] = dmp;
			saySettings(typeIndex);
			enableControls();
		}
	}
	
	private class DefaultButton implements ActionListener {
		int typeindex;

		public DefaultButton(int typeindex) {
			super();
			this.typeindex = typeindex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			defaultButtonPress(typeindex);
		}
		
	}
	private class SettingsButton implements ActionListener {
		int typeindex;

		public SettingsButton(int typeindex) {
			super();
			this.typeindex = typeindex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			settingsButtonPress(typeindex);
		}
		
	}

	public void enableControls() {
		for (int i = 0; i < typesList.length; i++) {
			typeChange[i].setEnabled(i == 0 || useDefault[i].isSelected() == false);
		}
	}



}
