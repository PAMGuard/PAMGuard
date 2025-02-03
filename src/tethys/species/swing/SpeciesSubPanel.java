package tethys.species.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import PamController.PamConfiguration;
import PamController.PamController;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.species.ITISFunctions;
import tethys.species.SpeciesMapItem;
import tethys.species.TethysITISResult;

public class SpeciesSubPanel {
	
	private JPanel mainPanel;
	
	private JLabel pamguardName;
	private JTextField itisCode, callType, latinName, commonName;
	private JButton searchButton;

	private PamDataBlock dataBlock;

	public SpeciesSubPanel(PamDataBlock dataBlock, String aSpecies) {
		
		this.dataBlock = dataBlock;
		
		callType = new JTextField(15);
		pamguardName = new JLabel(aSpecies);
		itisCode = new JTextField(6);
		searchButton = new JButton("Find");
		latinName = new JTextField(15);
		commonName = new JTextField(15);
		
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Name ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(pamguardName, c);
		c.gridx++;
		mainPanel.add(new JLabel(" ITIS code ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(itisCode, c);
		c.gridx ++;
		mainPanel.add(searchButton, c);
		
		c.gridx ++;
		c.gridwidth = 1;
		mainPanel.add(latinName);
		
		int w1 = 2;
		int w2 = 3;
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = w1;
		mainPanel.add(new JLabel("Call / sound type ", JLabel.RIGHT), c);
		c.gridx+= c.gridwidth;
		c.gridwidth = w2;
		mainPanel.add(callType, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		mainPanel.add(commonName, c);
//		c.gridx = 0;
//		c.gridy++;
//		c.gridwidth = w1;
//		mainPanel.add(new JLabel("Scientific name ", JLabel.RIGHT), c);
//		c.gridx+= c.gridwidth;
//		c.gridwidth = w2;
//		mainPanel.add(latinName, c);
//		c.gridx = 0;
//		c.gridy++;
//		c.gridwidth = w1;
//		mainPanel.add(new JLabel("Common name ", JLabel.RIGHT), c);
//		c.gridx+= c.gridwidth;
//		c.gridwidth = w2;
//		mainPanel.add(commonName = new JTextField(15), c);
		
		callType.setText(aSpecies); // will get overwritten if the user choses something else. 
		
		pamguardName.setToolTipText("Internal name within PAMGuard module");
		itisCode.setToolTipText("ITIS species code");
		searchButton.setToolTipText("<html>Either enter a code manually and press \"Find\" for additional information,<br>"
				+ "or leave the code empty and press \"Find\" to search the Tethys database using common or scientific names.</html>" );
		callType.setToolTipText("Descriptive name for call type or measurement");
		latinName.setToolTipText("Scientific name");
		commonName.setToolTipText("Common name");
		commonName.setEditable(false);
//		commonName.setEnabled(false);
		latinName.setEditable(false);
		
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchSpecies(e);
			}
		});
		
	}

	private PamConfiguration getConfiguration() {
		if (dataBlock == null) {
			return PamController.getInstance().getPamConfiguration();
		}
		return dataBlock.getPamConfiguration();
	}
	/**
	 * Action when 'Find' button is pressed.
	 * @param e
	 */
	protected void searchSpecies(ActionEvent e) {
		TethysControl tethysControl = (TethysControl) getConfiguration().findControlledUnit(TethysControl.unitType);
		if (tethysControl == null) {
			return;
		}
		ITISFunctions itisFunctions = tethysControl.getItisFunctions();
		String itisString = this.itisCode.getText();
//		if (itisString == null || itisString.length() == 0) {
			searchForCode(tethysControl, itisFunctions);
//		}
//		else {
//			getCodeInformation(tethysControl, itisFunctions, itisString);
//		}
//		System.out.println(itisInfo);
	}

	private void getCodeInformation(TethysControl tethysControl, ITISFunctions itisFunctions, String itisString) {
		int itisCode = 0;
		try {
			itisCode = Integer.valueOf(this.itisCode.getText());
		}
		catch (NumberFormatException ex) {
			PamDialog.showWarning(PamController.getMainFrame(), "No Code", "Enter  avalid ITIS code");
			return;
		}
		TethysITISResult itisInfo = itisFunctions.getITISInformation(itisCode);
		if (itisInfo != null) {
			if (itisInfo.getLatin() != null) {
				latinName.setText(itisInfo.getLatin());
			}
			if (itisInfo.getVernacular() != null) {
				commonName.setText(itisInfo.getVernacular());
			}
		}		
	}

	private void searchForCode(TethysControl tethysControl, ITISFunctions itisFunctions) {
		Integer currentCode = null;
		try {
			currentCode = Integer.valueOf(itisCode.getText());
		}
		catch (NumberFormatException e) {
			
		}
		
		SpeciesMapItem speciesItem = SpeciesSearchDialog.showDialog(tethysControl.getGuiFrame(), tethysControl, currentCode);
		if (speciesItem != null) {
			itisCode.setText(String.format("%d", speciesItem.getItisCode()));
			latinName.setText(speciesItem.getLatinName());
			commonName.setText(speciesItem.getCommonName());
		}
	}

	public JComponent getDialogComponent() {
		return mainPanel;
	}

	public void setParams(SpeciesMapItem speciesMapItem) {
		if (speciesMapItem == null) {
			itisCode.setText(null);
//			callType.setText(null);
			latinName.setText(null);
			commonName.setText(null);
			return;
		}
		pamguardName.setText("\"" + speciesMapItem.getPamguardName() + "\"");
		itisCode.setText(String.format("%d", speciesMapItem.getItisCode()));
		String callT = speciesMapItem.getCallType();
		if (callT != null && callT.length()>0) {
			callType.setText(speciesMapItem.getCallType());
		}
		else {
			callType.setText(speciesMapItem.getPamguardName());
		}
		latinName.setText(speciesMapItem.getLatinName());
		commonName.setText(speciesMapItem.getCommonName());
	}

	public SpeciesMapItem getParams() {
		Integer tsn = null;
		String vernacular = null;
		String latin = null;
		try {
			tsn = Integer.valueOf(itisCode.getText());
		}
		catch (NumberFormatException e) {
			PamDialog.showWarning(PamController.getMainFrame(), pamguardName.getText(), "You must specify an ITIS taxanomic code");
			return null;
		}
		latin = latinName.getText();
		vernacular = commonName.getText();
		String callType = this.callType.getText();
		if (callType == null || callType.length() == 0) {
			PamDialog.showWarning(PamController.getMainFrame(), pamguardName.getText(), "You must specified a call type");
			return null;			
		}
		String pamName = pamguardName.getText().replace("\"", "");
		return new SpeciesMapItem(tsn, callType, pamName, latin, vernacular);
	}

}
