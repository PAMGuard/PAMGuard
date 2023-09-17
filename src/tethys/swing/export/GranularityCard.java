package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.w3c.dom.Document;

import PamController.settings.output.xml.PamguardXMLWriter;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorChangeListener;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.niluswraps.PGranularityType;
import tethys.output.StreamExportParams;
import tethys.pamdata.TethysDataProvider;

public class GranularityCard extends ExportWizardCard {

	private JRadioButton[] granularities;

	private JTextArea dataSelectionText;

	private JTextField binLength, minBinnedCalls, encounterGap, minEncounterCalls;
	
	private JRadioButton groupChannels, separateChannels;

	private DataSelector dataSelector;

	private DetectionsExportWizard detectionsExportWizard;

	private int encounterIndex, binnedIndex;

	private GranularityEnumType[] allowedGranularities;

	public GranularityCard(DetectionsExportWizard detectionsExportWizard, TethysControl tethysControl, PamDataBlock dataBlock) {
		super(tethysControl, "Granularity", dataBlock);
		this.detectionsExportWizard = detectionsExportWizard;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		TethysDataProvider tethysDataProvider = dataBlock.getTethysDataProvider(tethysControl);
		// granularity
		allowedGranularities = tethysDataProvider.getAllowedGranularities();
		granularities = new JRadioButton[allowedGranularities.length];
		JPanel granPanel = new WestAlignedPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		granPanel.setBorder(new TitledBorder("Granularity"));
		ButtonGroup granGroup = new ButtonGroup();
		GranularityChange gc = new GranularityChange();
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

	private void enableControls() {
		binLength.setEnabled(granularities[binnedIndex].isSelected());
		minBinnedCalls.setEnabled(granularities[binnedIndex].isSelected());
		encounterGap.setEnabled(granularities[encounterIndex].isSelected());
		minEncounterCalls.setEnabled(granularities[encounterIndex].isSelected());
		boolean binOrencount = granularities[binnedIndex].isSelected() | granularities[encounterIndex].isSelected();
		separateChannels.setEnabled(binOrencount);
		groupChannels.setEnabled(binOrencount);
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
		for (int i = 0; i < allowedGranularities.length; i++) {
			if (granularities[i].isSelected()) {
				streamExportParams.granularity = allowedGranularities[i];
				break;
			}
		}
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
		
		return streamExportParams.granularity != null;
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
		newDataSelection();
		enableControls();
	}

}
