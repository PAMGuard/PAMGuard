package clickDetector.dataSelector;

import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupList;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import clickDetector.ClickControl;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.alarm.ClickAlarmParameters;
import clickDetector.offlineFuncs.ClicksOffline;
import PamView.dialog.DialogScrollPane;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamAlignmentPanel;

public class ClickSelectPanel implements PamDialogPanel {
	
	private SpeciesPanel speciesPanel;
	private ClickControl clickControl;
	private EventTypePanel eventTypePanel;
	private boolean allowScores;
	private ClickDataSelector clickDataSelector;
	private JPanel mainPanel;
	private boolean isViewer;

	public ClickSelectPanel(ClickDataSelector clickDataSelector, boolean allowScores, boolean useEventTypes) {
		this.clickDataSelector = clickDataSelector;
		this.allowScores = allowScores;
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		speciesPanel = new SpeciesPanel();
		eventTypePanel = new EventTypePanel();
		clickControl = clickDataSelector.getClickControl();
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(speciesPanel);
		if (useEventTypes) {
			mainPanel.add(eventTypePanel);
		}
	}
	

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		eventTypePanel.setParams();
		speciesPanel.setParams();
	}

	@Override
	public boolean getParams() {
		return (speciesPanel.getParams() & eventTypePanel.getParams());
	}
	
	class EventTypePanel extends JPanel {
		private JCheckBox useUnassigned, onlineAuto, onlineManual;
		private JCheckBox[] useType;
		private LookupList lutList;
		
		public EventTypePanel() {
			setBorder(new TitledBorder("Event Type Selection"));
		}
		
		void setParams() {
			ClickAlarmParameters clickAlarmParameters = clickDataSelector.getClickAlarmParameters();
			removeAll();
			GridBagConstraints c = new PamGridBagContraints();
			c.insets = new Insets(0,0,0,0);
			c.ipady = 0;
			JPanel boxPanel = new JPanel(new GridBagLayout());
			this.setLayout(new BorderLayout());
			JScrollPane scrollPane = new DialogScrollPane(boxPanel, 10);
			this.add(scrollPane, BorderLayout.CENTER);
			c.gridx = c.gridy = 0;
			boxPanel.add(useUnassigned = new JCheckBox("Unassigned events"), c);
			useUnassigned.setSelected(clickAlarmParameters.unassignedEvents);
			c.gridy++;
			boxPanel.add(onlineManual = new JCheckBox("Manually detected click trains"), c);
			onlineManual.setSelected(clickAlarmParameters.onlineManualEvents);
			c.gridy++;
			boxPanel.add(onlineAuto = new JCheckBox("Automatically detected click trains"), c);
			onlineAuto.setSelected(clickAlarmParameters.onlineAutoEvents);
			
			
			lutList = LookUpTables.getLookUpTables().getLookupList(ClicksOffline.ClickTypeLookupName);
			if (lutList == null) {
				return;
			}
			useType = new JCheckBox[lutList.getList().size()];
			for (int i = 0; i < useType.length; i++) {
				c.gridy++;
				boxPanel.add(useType[i] = new JCheckBox(lutList.getList().get(i).getText()), c);
				useType[i].setSelected(clickAlarmParameters.isUseEventType(lutList.getList().get(i).getCode()));
			}
		}
		
		boolean getParams() {
			ClickAlarmParameters clickAlarmParameters = clickDataSelector.getClickAlarmParameters();
			try {
				clickAlarmParameters.unassignedEvents = useUnassigned.isSelected();
				clickAlarmParameters.onlineAutoEvents = onlineAuto.isSelected();
				clickAlarmParameters.onlineManualEvents = onlineManual.isSelected();
				
				if (useType != null) {
					for (int i = 0; i < useType.length; i++) {
						clickAlarmParameters.setUseEventType(lutList.getList().get(i).getCode(), useType[i].isSelected());
					}
				}
			}
			catch (Exception e) {
				return false;
			}
			return true;
		}
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
			northPanel.setBorder(new TitledBorder("Echoes"));
			c.gridwidth = 3;
			c.anchor = GridBagConstraints.WEST;
			northPanel.add(useEchoes = new JCheckBox("Use Echoes"), c);
			c.gridwidth = 1;
			c.gridy++;
			c.gridx = 0;
//			northPanel.add(new JLabel("Min ICI ", JLabel.RIGHT), c);
			c.gridx++;
			minICI = new JTextField(4);
//			northPanel.add(minICI, c);
//			minICI.setToolTipText("Minimum ICI in milliseconds");
//			c.gridx++;
//			northPanel.add(new JLabel(" ms", JLabel.LEFT), c);
			c.gridwidth = 3;
			c.gridy++;
			c.gridx = 0;
			northPanel.add(scoreByAmplitude = new JCheckBox("Score by amplitude"), c);
			scoreByAmplitude.setVisible(allowScores);
			scoreByAmplitude.addActionListener(new AllSpeciesListener());
			add(BorderLayout.NORTH, northPanel);
			
			JPanel centralOuterPanel = new JPanel(new BorderLayout());
			centralPanel.setLayout(new GridBagLayout());
			centralOuterPanel.setBorder(new TitledBorder("Click Type Selection"));
			
			add(BorderLayout.CENTER, centralOuterPanel);
			JScrollPane scrollPane = new DialogScrollPane(new PamAlignmentPanel(centralPanel, BorderLayout.WEST), 10);
			centralOuterPanel.add(BorderLayout.CENTER, scrollPane);
			
			centralEastPanel.setLayout(new GridBagLayout());
			c = new PamGridBagContraints();
			centralEastPanel.add(selectAll = new JButton("Select All"), c);
			c.gridx++;
			centralEastPanel.add(clearAll = new JButton("Clear All"), c);
			selectAll.addActionListener(new AutoSelect(true));
			clearAll.addActionListener(new AutoSelect(false));
			centralOuterPanel.add(BorderLayout.SOUTH, new PamAlignmentPanel(centralEastPanel, BorderLayout.WEST));
			
		}
		
		void setParams() {
			ClickAlarmParameters clickAlarmParameters = clickDataSelector.getClickAlarmParameters();
			centralPanel.removeAll();
			species = null;
			weights = null;
			speciesList = null;
			GridBagConstraints c = new PamGridBagContraints(null, 0, 0);
			useAll = new JCheckBox("Unclassified clicks");
			useAll.addActionListener(new AllSpeciesListener());
//			centralPanel.add(new JLabel("Species", JLabel.CENTER), c);
			if (allowScores) {
				c.gridx++;
				centralPanel.add(new JLabel("Score", JLabel.CENTER), c);
			}
			c.gridx = 0;
			c.gridy++;
			centralPanel.add(useAll, c);
			c.gridx++;
			centralPanel.add(allWeight = new JTextField(4), c);
			allWeight.setVisible(allowScores);
			clickIdentifier = clickControl.getClickIdentifier();
			if (clickIdentifier != null && clickIdentifier.getSpeciesList() != null) {
				speciesList = clickIdentifier.getSpeciesList();
				species = new JCheckBox[speciesList.length];
				weights = new JTextField[speciesList.length];
				if (speciesList != null) {
					for (int i = 0; i < speciesList.length; i++) {
						c.gridx = 0;
						c.gridy++;
						centralPanel.add(species[i] = new JCheckBox(speciesList[i]), c);
						c.gridx++;
						centralPanel.add(weights[i] = new JTextField(4), c);
						weights[i].setVisible(allowScores);
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
			
			enableButtons();
		}
		boolean getParams() {

			ClickAlarmParameters clickAlarmParameters = clickDataSelector.getClickAlarmParameters().clone();
			clickAlarmParameters.useEchoes = useEchoes.isSelected();
			try {
				clickAlarmParameters.minICIMillis = Integer.valueOf(minICI.getText());
			}
			catch (NumberFormatException e) {
				return PamDialog.showWarning(null, "ICI Value", "Invalid ICI value - must be integer number of milliseconds");
			}
			clickAlarmParameters.setUseSpecies(0, useAll.isSelected());
			double w;
			if (allowScores) {
			clickAlarmParameters.scoreByAmplitude = scoreByAmplitude.isSelected();
			try {
				w = Double.valueOf(allWeight.getText());
				clickAlarmParameters.setSpeciesWeighting(0, w);
			}
			catch (NumberFormatException e) {
				return false; //showWarning("Invalid weight for unclassified species ");
			}
			}
			if (species != null) {
				for (int i = 0; i < species.length; i++) {
					clickAlarmParameters.setUseSpecies(i+1, species[i].isSelected());
					if (species[i].isSelected() && !scoreByAmplitude.isSelected() && allowScores) {
						try {
							w = Double.valueOf(weights[i].getText());
							clickAlarmParameters.setSpeciesWeighting(i+1, w);
						}
						catch (NumberFormatException e) {
							return false; //showWarning("Invalid weight for species: " + species[i].getText());
						}
					}
				}
				
			}

//			btDisplayParameters.showANDEvents = andEvents.isSelected();
//			btDisplayParameters.showEventsOnly = onlyEvents.isSelected();
			clickDataSelector.setClickAlarmParameters(clickAlarmParameters);
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
