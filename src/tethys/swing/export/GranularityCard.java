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
	
	private JTextField binLength, encounterGap;

	private DataSelector dataSelector;
	
	public GranularityCard(TethysControl tethysControl, PamDataBlock dataBlock) {
		super(tethysControl, "Granularity", dataBlock);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// granularity
		GranularityEnumType[] grans = GranularityEnumType.values();
		granularities = new JRadioButton[grans.length];
		JPanel granPanel = new WestAlignedPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		granPanel.setBorder(new TitledBorder("Granularity"));
		ButtonGroup granGroup = new ButtonGroup();
		for (int i = 0; i < grans.length; i++) {
			c.gridx = 0;
			granularities[i] = new JRadioButton(PGranularityType.prettyString(grans[i]));
			granularities[i].setToolTipText(PGranularityType.toolTip(grans[i]));
			granPanel.add(granularities[i], c);
			granGroup.add(granularities[i]);
			if (grans[i] == GranularityEnumType.BINNED) {
				c.gridx++;
				granPanel.add(new JLabel(" bin duration ", JLabel.RIGHT), c);
				c.gridx++;
				granPanel.add(binLength = new JTextField(5), c);
				c.gridx++;
				granPanel.add(new JLabel(" (s) ", JLabel.LEFT), c);
				
			}
			if (grans[i] == GranularityEnumType.ENCOUNTER) {
				c.gridx++;
				granPanel.add(new JLabel(" min gap ", JLabel.RIGHT), c);
				c.gridx++;
				granPanel.add(encounterGap = new JTextField(5), c);
				c.gridx++;
				granPanel.add(new JLabel(" (s) ", JLabel.LEFT), c);
				
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
		
		return streamExportParams.granularity != null;
	}

	@Override
	public void setParams(StreamExportParams streamExportParams) {
		GranularityEnumType[] grans = GranularityEnumType.values();
		for (int i = 0; i < grans.length; i++) {
			granularities[i].setSelected(streamExportParams.granularity == grans[i]);
		}
		newDataSelection();
	}

}
