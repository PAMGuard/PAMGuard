package clickDetector.alarm;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import listening.SpeciesItem;

import weka.core.SingleIndex;

import clickDetector.BTDisplayParameters;
import clickDetector.ClickControl;
import clickDetector.ClickClassifiers.ClickIdentifier;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ClickAlarmDialog extends PamDialog {

	private static ClickAlarmDialog singleInstance;
	private ClickAlarmParameters clickAlarmParameters;
	private ClickControl clickControl;
	private SpeciesPanel speciesPanel;
	public ClickAlarmDialog(Window parentFrame, ClickControl clickControl) {
		super(parentFrame, clickControl.getUnitName() + " alarm", false);
		this.clickControl = clickControl;
		speciesPanel = new SpeciesPanel();
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, speciesPanel);
		
		setDialogComponent(mainPanel);
	}

	public static ClickAlarmParameters showDialog(Window parentFrame, ClickControl clickControl, ClickAlarmParameters clickAlarmParameters) {
		if (singleInstance == null || singleInstance.clickControl != clickControl || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ClickAlarmDialog(parentFrame, clickControl);
		}
		singleInstance.clickAlarmParameters = clickAlarmParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.clickAlarmParameters;
	}
	
	private void setParams() {
		speciesPanel.setParams();
	}

	@Override
	public void cancelButtonPressed() {
		clickAlarmParameters = null;
	}

	@Override
	public boolean getParams() {
		return speciesPanel.getParams();
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}


	class SpeciesPanel extends JPanel {
		JCheckBox useAll;
		JTextField allWeight;
		JCheckBox[] species;
		JTextField[] weights;
		ClickIdentifier clickIdentifier;
		String[] speciesList;
		JPanel centralPanel = new JPanel();
		JPanel centralEastPanel = new JPanel();
		JPanel northPanel = new JPanel();
		JButton selectAll, clearAll;
//		JRadioButton andEvents, orEvents;
//		JRadioButton anyEvents, onlyEvents;
		private JCheckBox useEchoes;
		private JCheckBox scoreByAmplitude;
		private JTextField minICI;
		
		SpeciesPanel () {
			super();
			setLayout(new BorderLayout());
			northPanel = new JPanel();
			northPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			northPanel.setBorder(new TitledBorder("Options"));
			c.gridwidth = 3;
			addComponent(northPanel, useEchoes = new JCheckBox("Use Echoes"), c);
			c.gridwidth = 1;
			c.gridy++;
			c.gridx = 0;
			addComponent(northPanel, new JLabel("Min ICI ", JLabel.RIGHT), c);
			c.gridx++;
			addComponent(northPanel, minICI = new JTextField(4), c);
			c.gridx++;
			addComponent(northPanel, new JLabel(" ms", JLabel.LEFT), c);
			c.gridwidth = 3;
			c.gridy++;
			c.gridx = 0;
			addComponent(northPanel, scoreByAmplitude = new JCheckBox("Score by amplitude"), c);
			scoreByAmplitude.addActionListener(new AllSpeciesListener());
			add(BorderLayout.NORTH, northPanel);
			
			JPanel centralOuterPanel = new JPanel(new BorderLayout());
			centralPanel.setLayout(new GridBagLayout());
			centralOuterPanel.setBorder(new TitledBorder("Species Selection"));
			
			add(BorderLayout.CENTER, centralOuterPanel);
			centralOuterPanel.add(BorderLayout.CENTER, centralPanel);
			
			centralEastPanel.setLayout(new GridBagLayout());
			c = new PamGridBagContraints();
			addComponent(centralEastPanel, selectAll = new JButton("Select All"), c);
			c.gridx++;
			addComponent(centralEastPanel, clearAll = new JButton("Clear All"), c);
			selectAll.addActionListener(new AutoSelect(true));
			clearAll.addActionListener(new AutoSelect(false));
			centralOuterPanel.add(BorderLayout.SOUTH, centralEastPanel);
			
		}
		void setParams() {
			centralPanel.removeAll();
			species = null;
			weights = null;
			speciesList = null;
			GridBagConstraints c = new PamGridBagContraints();
			useAll = new JCheckBox("Unclassified clicks");
			useAll.addActionListener(new AllSpeciesListener());
			addComponent(centralPanel, new JLabel("Species", JLabel.CENTER), c);
			c.gridx++;
			addComponent(centralPanel, new JLabel("Score", JLabel.CENTER), c);
			c.gridx = 0;
			c.gridy++;
			addComponent(centralPanel, useAll, c);
			c.gridx++;
			addComponent(centralPanel, allWeight = new JTextField(4), c);
			clickIdentifier = clickControl.getClickIdentifier();
			if (clickIdentifier != null && clickIdentifier.getSpeciesList() != null) {
				speciesList = clickIdentifier.getSpeciesList();
				species = new JCheckBox[speciesList.length];
				weights = new JTextField[speciesList.length];
				if (speciesList != null) {
					for (int i = 0; i < speciesList.length; i++) {
						c.gridx = 0;
						c.gridy++;
						addComponent(centralPanel, species[i] = new JCheckBox(speciesList[i]), c);
						c.gridx++;
						addComponent(centralPanel, weights[i] = new JTextField(4), c);
					}
				}
			}
			useEchoes.setSelected(clickAlarmParameters.useEchoes);
			minICI.setText(String.format("%d", clickAlarmParameters.minICIMillis));
			scoreByAmplitude.setSelected(clickAlarmParameters.scoreByAmplitude);
			allWeight.setText(String.format("%3.1f", clickAlarmParameters.getSpeciesWeight(0)));
			if (species == null) {
				useAll.setSelected(true);
			}
			else {
				useAll.setSelected(clickAlarmParameters.getUseSpecies(0));
				for (int i = 0; i < species.length; i++) {
						species[i].setSelected(clickAlarmParameters.getUseSpecies(i+1));
						weights[i].setText(String.format("%3.1f", clickAlarmParameters.getSpeciesWeight(i+1)));
				}
//				if (btDisplayParameters.showSpeciesList != null) {
//					for (int i = 0; i < Math.min(species.length, btDisplayParameters.showSpeciesList.length);i++) {
//						species[i].setSelected(btDisplayParameters.showSpeciesList[i]);
//					}
//				}
			}
			pack();
			enableButtons();
		}
		boolean getParams() {
			clickAlarmParameters.useEchoes = useEchoes.isSelected();
			try {
				clickAlarmParameters.minICIMillis = Integer.valueOf(minICI.getText());
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid ICI value - must be integer");
			}
			clickAlarmParameters.scoreByAmplitude = scoreByAmplitude.isSelected();
			clickAlarmParameters.setUseSpecies(0, useAll.isSelected());
			double w;
			try {
				w = Double.valueOf(allWeight.getText());
				clickAlarmParameters.setSpeciesWeighting(0, w);
			}
			catch (NumberFormatException e) {
				return showWarning("Invalid weight for unclassified species ");
			}
			if (species != null) {
				for (int i = 0; i < species.length; i++) {
					clickAlarmParameters.setUseSpecies(i+1, species[i].isSelected());
					if (species[i].isSelected() && !scoreByAmplitude.isSelected()) {
						try {
							w = Double.valueOf(weights[i].getText());
							clickAlarmParameters.setSpeciesWeighting(i+1, w);
						}
						catch (NumberFormatException e) {
							return showWarning("Invalid weight for species: " + species[i].getText());
						}
					}
				}
				
			}

//			btDisplayParameters.showANDEvents = andEvents.isSelected();
//			btDisplayParameters.showEventsOnly = onlyEvents.isSelected();
			return true;
		}
		class AllSpeciesListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				enableButtons();				
			}
			
		}
		void enableButtons() {
			boolean someSpecies = (species != null && species.length > 0);
			useAll.setEnabled(someSpecies);
			boolean scoreAmp = scoreByAmplitude.isSelected();
			allWeight.setEnabled(!scoreAmp);
			if (weights != null) {
				for (int i = 0; i < weights.length; i++) {
					weights[i].setEnabled(!scoreAmp);
				}
			}
			if (!someSpecies) {
				useAll.setSelected(true);
			}
			selectAll.setEnabled(someSpecies);
			clearAll.setEnabled(someSpecies);
		}
		class AutoSelect implements ActionListener {

			boolean select;
			public AutoSelect(boolean select) {
				super();
				this.select = select;
			}
			public void actionPerformed(ActionEvent e) {
				useAll.setSelected(select);
				if (species == null) return;
				for (int i = 0; i < species.length; i++) {
					species[i].setSelected(select);
				}
				
			}
			
		}
	}
}
