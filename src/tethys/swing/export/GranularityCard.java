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

public class GranularityCard extends ExportWizardCard {

	private JRadioButton[] granularities;

	private JTextArea dataSelectionText;

	private JTextField binLength, minCalls, encounterGap;

	private DataSelector dataSelector;

	private DetectionsExportWizard detectionsExportWizard;

	private int encounterIndex, binnedIndex;

	public GranularityCard(DetectionsExportWizard detectionsExportWizard, TethysControl tethysControl, PamDataBlock dataBlock) {
		super(tethysControl, "Granularity", dataBlock);
		this.detectionsExportWizard = detectionsExportWizard;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// granularity
		GranularityEnumType[] grans = GranularityEnumType.values();
		granularities = new JRadioButton[grans.length];
		JPanel granPanel = new WestAlignedPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		granPanel.setBorder(new TitledBorder("Granularity"));
		ButtonGroup granGroup = new ButtonGroup();
		GranularityChange gc = new GranularityChange();
		for (int i = 0; i < grans.length; i++) {
			c.gridx = 0;
			granularities[i] = new JRadioButton(PGranularityType.prettyString(grans[i]));
			granularities[i].setToolTipText(PGranularityType.toolTip(grans[i]));
			granularities[i].addActionListener(gc);
			granPanel.add(granularities[i], c);
			granGroup.add(granularities[i]);
			if (grans[i] == GranularityEnumType.BINNED) {
				binnedIndex = i;
				c.gridx++;
				granPanel.add(new JLabel(" bin duration ", JLabel.RIGHT), c);
				c.gridx++;
				granPanel.add(binLength = new JTextField(5), c);
				c.gridx++;
				granPanel.add(new JLabel("(s), min Calls", JLabel.LEFT), c);
				c.gridx++;
				granPanel.add(minCalls = new JTextField(5), c);			
				binLength.setToolTipText("Time bin duration in seconds");
				minCalls.setToolTipText("Minimum number of calls for a bin to be output");	
			}
			if (grans[i] == GranularityEnumType.ENCOUNTER) {
				encounterIndex = i;
				c.gridx++;
				granPanel.add(new JLabel(" min gap ", JLabel.RIGHT), c);
				c.gridx++;
				granPanel.add(encounterGap = new JTextField(5), c);
				c.gridx++;
				granPanel.add(new JLabel("(s) ", JLabel.LEFT), c);
				encounterGap.setToolTipText("Minimum gap between separate encounters");
			}
			c.gridy++;
		}
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
		minCalls.setEnabled(granularities[binnedIndex].isSelected());
		encounterGap.setEnabled(granularities[encounterIndex].isSelected());
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
		GranularityEnumType[] grans = GranularityEnumType.values();
		for (int i = 0; i < grans.length; i++) {
			if (granularities[i].isSelected()) {
				streamExportParams.granularity = grans[i];
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
				streamExportParams.minBinCount = Integer.valueOf(minCalls.getText());
			}
			catch (NumberFormatException e) {
				return detectionsExportWizard.showWarning("Invalid minimum call count");
			}
		}
		if (streamExportParams.granularity == GranularityEnumType.ENCOUNTER) {
			try {
				streamExportParams.encounterGapS = Double.valueOf(encounterGap.getText());
			}
			catch (NumberFormatException e) {
				return detectionsExportWizard.showWarning("Invalid encounter gap parameter");
			}
		}

		return streamExportParams.granularity != null;
	}

	@Override
	public void setParams(StreamExportParams streamExportParams) {
		GranularityEnumType[] grans = GranularityEnumType.values();
		for (int i = 0; i < grans.length; i++) {
			granularities[i].setSelected(streamExportParams.granularity == grans[i]);
		}
		binLength.setText(String.format("%3.1f", streamExportParams.binDurationS));
		minCalls.setText(String.format("%d", streamExportParams.minBinCount));
		encounterGap.setText(String.format("%3.1f", streamExportParams.encounterGapS));
		newDataSelection();
		enableControls();
	}

}
