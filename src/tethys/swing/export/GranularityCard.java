package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorChangeListener;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.localization.TethysLocalisationInfo;
import tethys.niluswraps.PGranularityType;
import tethys.output.StreamExportParams;
import tethys.pamdata.TethysDataProvider;

public class GranularityCard extends ExportWizardCard {

	private JRadioButton[] granularities;

	private JTextArea dataSelectionText;

	private JTextField binLength, minBinnedCalls, encounterGap, minEncounterCalls;
	
	private JCheckBox exportDetections, exportLocalisations;
	
	private JLabel localisationTypes;
	
	private JRadioButton groupChannels, separateChannels;

	private DataSelector dataSelector;

	private DetectionsExportWizard detectionsExportWizard;

	private int callIndex, encounterIndex, binnedIndex;

	private GranularityEnumType[] allowedGranularities;

	private TethysDataProvider tethysDataProvider;
	
	public GranularityCard(DetectionsExportWizard detectionsExportWizard, TethysControl tethysControl, PamDataBlock dataBlock) {
		super(tethysControl, detectionsExportWizard, "Granularity", dataBlock);
		this.detectionsExportWizard = detectionsExportWizard;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		tethysDataProvider = dataBlock.getTethysDataProvider(tethysControl);

		GranularityChange gc = new GranularityChange();
		
		exportDetections = new JCheckBox("Export Detections");
		exportLocalisations = new JCheckBox("Export Localisations");
		exportDetections.addActionListener(gc);
		exportLocalisations.addActionListener(gc);
		JPanel whatPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cw = new PamGridBagContraints();
		cw.gridwidth = 2;
		whatPanel.add(exportDetections, cw);
		cw.gridy++;
		whatPanel.add(exportLocalisations, cw);
		cw.gridwidth = 1;
		cw.gridy++;
		whatPanel.add(new JLabel("Loclisation types: ", JLabel.RIGHT), cw);
		cw.gridx++;
		whatPanel.add(localisationTypes = new JLabel("none"), cw);
		JPanel walPanel = new WestAlignedPanel(whatPanel);
		walPanel.setBorder(new TitledBorder("What to export"));
		this.add(walPanel);
		localisationTypes.setToolTipText("Not all listed localisation types may be present in actual data");
		
		// granularity
		allowedGranularities = tethysDataProvider.getAllowedGranularities();
		granularities = new JRadioButton[allowedGranularities.length];
		JPanel granPanel = new WestAlignedPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		granPanel.setBorder(new TitledBorder("Granularity"));
		ButtonGroup granGroup = new ButtonGroup();
		binLength = new JTextField(5);
		minBinnedCalls = new JTextField(5);
		encounterGap = new JTextField(5);
		minEncounterCalls = new JTextField(5);
		for (int i = 0; i < allowedGranularities.length; i++) {
			c.gridx = 0;
			granularities[i] = new JRadioButton(PGranularityType.prettyString(allowedGranularities[i]));
			granularities[i].setToolTipText(PGranularityType.toolTip(allowedGranularities[i]));
			granularities[i].addActionListener(gc);
			granPanel.add(granularities[i], c);
			granGroup.add(granularities[i]);
			if (allowedGranularities[i] == GranularityEnumType.CALL) {
				callIndex = i;
			}
			if (allowedGranularities[i] == GranularityEnumType.BINNED) {
				binnedIndex = i;
				c.gridx++;
				granPanel.add(new JLabel(" Bin duration ", JLabel.RIGHT), c);
				c.gridx++;
				granPanel.add(binLength, c);
				c.gridx++;
				granPanel.add(new JLabel("(s), Min Calls", JLabel.LEFT), c);
				c.gridx++;
				granPanel.add(minBinnedCalls, c);			
				binLength.setToolTipText("Time bin duration in seconds");
				minBinnedCalls.setToolTipText("Minimum number of calls for a bin to be output");	
			}
			if (allowedGranularities[i] == GranularityEnumType.ENCOUNTER) {
				encounterIndex = i;
				c.gridx++;
				granPanel.add(new JLabel(" Minimum gap ", JLabel.RIGHT), c);
				c.gridx++;
				granPanel.add(encounterGap, c);
				c.gridx++;
				granPanel.add(new JLabel("(s), Min Calls", JLabel.LEFT), c);
				c.gridx++;
				granPanel.add(minEncounterCalls, c);			
				encounterGap.setToolTipText("Minimum gap between separate encounters");
				minEncounterCalls.setToolTipText("Minimum number of calls for an encounter to be output");	
			}
			c.gridy++;
		}
		c.gridx = 1;
		c.gridwidth = 2;
		granPanel.add(separateChannels = new JRadioButton("Separate channels"), c);
		c.gridx += c.gridwidth;
		granPanel.add(groupChannels = new JRadioButton("Group channels"), c);
		separateChannels.setToolTipText("Use separate bins/encounters for each detection channel");
		groupChannels.setToolTipText("Combine detections from different channels into the same bins/encounters");
		ButtonGroup chanGroup = new ButtonGroup();
		chanGroup.add(separateChannels);
		chanGroup.add(groupChannels);
		
		this.add(granPanel);

		// data selection
		dataSelector = dataBlock.getDataSelector(tethysControl.getDataSelectName(), false);
		if (dataSelector != null) {
			dataSelectionText = new JTextArea(8, 40);
			JPanel dataPanel = new JPanel(new BorderLayout());
			JPanel nPanel = new JPanel(new BorderLayout());
			dataPanel.add(BorderLayout.NORTH, nPanel);
			JButton selectorButton = dataSelector.getDialogButton(tethysControl.getGuiFrame(), new DataSelectorChangeListener() {
				@Override
				public void selectorChange(DataSelector changedSelector) {
					newDataSelection();
				}
			});
			nPanel.add(BorderLayout.EAST, selectorButton);
			newDataSelection();
			nPanel.add(BorderLayout.CENTER, new JLabel("Data selection filter ", JLabel.RIGHT));
			dataPanel.setBorder(new TitledBorder("Data selection filter"));
			JScrollPane sp = new JScrollPane(dataSelectionText, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			dataPanel.add(BorderLayout.CENTER, sp);
			this.add(dataPanel);
		}

	}

	private class GranularityChange implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}

	}

	protected void newDataSelection() {
		if (dataSelector == null) {
			return;
		}
		DataSelectParams params = dataSelector.getParams();
		if (params == null) {
			return;
		}
		if (params.getCombinationFlag() == 2) {
			dataSelectionText.setText("Not enabled");
			return;
		}
		String txt = dataSelector.getDescription();
		dataSelectionText.setText(txt);
	}

	@Override
	public boolean getParams(StreamExportParams streamExportParams) {

		streamExportParams.granularity = getCurrentGranularity();

		if (streamExportParams.granularity == GranularityEnumType.BINNED) {
			try {
				streamExportParams.binDurationS = Double.valueOf(binLength.getText());
			}
			catch (NumberFormatException e) {
				return detectionsExportWizard.showWarning("Invalid bin duration parameter");
			}
			try {
				streamExportParams.minBinCount = Integer.valueOf(minBinnedCalls.getText());
			}
			catch (NumberFormatException e) {
				return detectionsExportWizard.showWarning("Invalid minimum binned call count");
			}
		}
		if (streamExportParams.granularity == GranularityEnumType.ENCOUNTER) {
			try {
				streamExportParams.encounterGapS = Double.valueOf(encounterGap.getText());
			}
			catch (NumberFormatException e) {
				return detectionsExportWizard.showWarning("Invalid encounter gap parameter");
			}
			try {
				streamExportParams.minEncounterCount = Integer.valueOf(minEncounterCalls.getText());
			}
			catch (NumberFormatException e) {
				return detectionsExportWizard.showWarning("Invalid minimum encounter call count");
			}
		}

		streamExportParams.separateChannels = separateChannels.isSelected();
		
		streamExportParams.exportDetections = exportDetections.isSelected();
		
		streamExportParams.exportLocalisations = exportLocalisations.isSelected();
		
		if (streamExportParams.exportDetections == false && streamExportParams.exportLocalisations == false) {
			return detectionsExportWizard.showWarning("You must select Detections or Localisations for export");
		}
		
		return streamExportParams.granularity != null;
	}

	private GranularityEnumType getCurrentGranularity() {
		for (int i = 0; i < allowedGranularities.length; i++) {
			if (granularities[i].isSelected()) {
				return allowedGranularities[i];
			}
		}
		return null;
	}

	@Override
		public void setParams(StreamExportParams streamExportParams) {
			for (int i = 0; i < granularities.length; i++) {
				granularities[i].setSelected(streamExportParams.granularity == allowedGranularities[i]);
			}
			binLength.setText(String.format("%3.1f", streamExportParams.binDurationS));
			minBinnedCalls.setText(String.format("%d", streamExportParams.minBinCount));
			encounterGap.setText(String.format("%3.1f", streamExportParams.encounterGapS));
			minEncounterCalls.setText(String.format("%d", streamExportParams.minEncounterCount));
			separateChannels.setSelected(streamExportParams.separateChannels);
			groupChannels.setSelected(streamExportParams.separateChannels == false);
			String locStr = getLocInfString();
			if (locStr == null) {
				localisationTypes.setText("none");
	//			exportDetections.setSelected(true);
				exportLocalisations.setSelected(false);
			}
			else {
				localisationTypes.setText(locStr);
			}
			
			exportDetections.setSelected(streamExportParams.exportDetections);
			exportLocalisations.setSelected(streamExportParams.exportLocalisations);
			
			newDataSelection();
			enableControls();
		}

	private void enableControls() {
		String locStr = getLocInfString();
		boolean dets = exportDetections.isSelected();
		boolean locs = exportLocalisations.isSelected();
		granularities[binnedIndex].setEnabled(!locs);
		granularities[encounterIndex].setEnabled(!locs);
		GranularityEnumType granularity = getCurrentGranularity();
		boolean canLoc = tethysDataProvider.canExportLocalisations(granularity);
		exportLocalisations.setEnabled(canLoc);
		if (canLoc == false) {
			exportLocalisations.setSelected(false);
			exportDetections.setSelected(true);
		}
		
		if (granularities.length == 1) {
			granularities[0].setSelected(true);
		}
		
		binLength.setEnabled(granularities[binnedIndex].isSelected());
		minBinnedCalls.setEnabled(granularities[binnedIndex].isSelected());
		encounterGap.setEnabled(granularities[encounterIndex].isSelected());
		minEncounterCalls.setEnabled(granularities[encounterIndex].isSelected());
		boolean binOrencount = granularities[binnedIndex].isSelected() | granularities[encounterIndex].isSelected();
		separateChannels.setEnabled(binOrencount);
		groupChannels.setEnabled(binOrencount);
	}

	private String getLocInfString() {
		TethysLocalisationInfo locInf = getDataBlock().getTethysDataProvider(getTethysControl()).getLocalisationInfo();
		if (locInf == null) {
			return null;
		}
		return locInf.getLoclisationTypes();
	}

}
