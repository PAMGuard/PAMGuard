package PamUtils.time;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class GlobalTimeDialog extends PamDialog {
	
	private JComboBox<String> correctorChoice;
	private GlobalTimeManager globalTimeManager;
	
	private static GlobalTimeDialog singleInstance;
	private GlobalTimeParameters globalTimeParameters;
	private ArrayList<PCTimeCorrector> timeCorrectors;
	private JRadioButton logOnly, useAndLog;
	private JTextField smoothing, startupDelay;

	private GlobalTimeDialog(Window parentFrame, GlobalTimeManager globalTimeManager) {
		super(parentFrame, "Global time corrections", false);
		this.globalTimeManager = globalTimeManager;

		correctorChoice = new JComboBox<String>();
		correctorChoice.addItem("jdjflakjljafldjlfdfopp");
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Time Corrections"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(correctorChoice, c);
		c.gridx++;
		PamSettingsIconButton settingsButton = new PamSettingsIconButton();
		mainPanel.add(settingsButton, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 3;
		mainPanel.add(logOnly = new JRadioButton("Log changes only    "), c);
		c.gridy++;
		mainPanel.add(useAndLog = new JRadioButton("Log changes and correct UTC in all modules"), c);
		ButtonGroup bg = new ButtonGroup();
		bg.add(logOnly);
		bg.add(useAndLog);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Logging and smoothing interval ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(smoothing = new JTextField(4), c);
		c.gridx++;
		mainPanel.add(new JLabel(" s ", JLabel.LEFT), c);
		
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Startup delay ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(startupDelay = new JTextField(4), c);
		c.gridx++;
		mainPanel.add(new JLabel(" ms ", JLabel.LEFT), c);
		
		
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showCorrectorSettings();
			}
		});
		
		setDialogComponent(mainPanel);
	}
	
	private void showCorrectorSettings() {
		PCTimeCorrector tc = getCurrentSelection();
		if (tc != null) {
			tc.showDialog(getOwner());
		}
	}

	public static GlobalTimeParameters showDialog(Window parentFrame, GlobalTimeManager globalTimeManager) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new GlobalTimeDialog(parentFrame, globalTimeManager);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.globalTimeParameters;
	}

	private void setParams() {
		globalTimeParameters = globalTimeManager.getGlobalTimeParameters().clone();
		timeCorrectors = globalTimeManager.getPcTimeCorrectors();
		correctorChoice.removeAllItems();
		int selInd = 0;
		int ind = 0;
		for (PCTimeCorrector tc:timeCorrectors) {
			correctorChoice.addItem(tc.getName()+ "    ");
			if (tc.getClass().getName().equals(globalTimeParameters.getSelectedTimeSource())) {
				selInd = ind; 
			}
			ind++;
		}
		correctorChoice.setSelectedIndex(selInd);
		logOnly.setSelected(globalTimeParameters.isUpdateUTC() == false);
		useAndLog.setSelected(globalTimeParameters.isUpdateUTC() == true);
		smoothing.setText(String.format("%d", globalTimeParameters.getSmoothingTimeSeconds()));
		startupDelay.setText(String.format("%d", globalTimeParameters.getStartupDelay()));
	}

	@Override
	public boolean getParams() {
		globalTimeParameters.setUpdateUTC(useAndLog.isSelected());
		PCTimeCorrector selCorr = getCurrentSelection();
		globalTimeParameters.setSelectedTimeSource(selCorr.getClass().getName());
		try {
			globalTimeParameters.setSmoothingTimeSeconds(Integer.valueOf(smoothing.getText()));
			globalTimeParameters.setStartupDelay(Integer.valueOf(startupDelay.getText()));
		}
		catch (NumberFormatException e) {
			return showWarning("Logging and smoothing interval mus tbe a whole number");
		}
		return true;
	}
	
	PCTimeCorrector getCurrentSelection() {
		return timeCorrectors.get(correctorChoice.getSelectedIndex());
	}

	@Override
	public void cancelButtonPressed() {
		globalTimeParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
