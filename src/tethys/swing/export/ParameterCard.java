package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.panel.WestAlignedPanel;
import PamView.wizard.PamWizard;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.deployment.DeploymentHandler;
import tethys.detection.DetectionsHandler;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;

public class ParameterCard extends ExportWizardCard {

	private DetectionsHandler detectionsHandler;
	
	private JRadioButton[] optButtons;

	public ParameterCard(TethysControl tethysControl, PamWizard pamWizard, PamDataBlock dataBlock) {
		super(tethysControl, pamWizard, "Algorithm Parameters", dataBlock);
		detectionsHandler = tethysControl.getDetectionsHandler();
		String[] optionStrings = TethysExportParams.paramsOptNames;
		optButtons = new JRadioButton[optionStrings.length];
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < optButtons.length; i++) {
			optButtons[i] = new JRadioButton(optionStrings[i]);
			bg.add(optButtons[i]);
			buttonPanel.add(optButtons[i], c);
			c.gridy++;
		}
		this.setLayout(new BorderLayout());
		this.add(BorderLayout.NORTH, new WestAlignedPanel(buttonPanel));
		this.setBorder(new TitledBorder("Algorithm parameters export"));
	}

	@Override
	public boolean getParams(StreamExportParams cardParams) {
		TethysExportParams exportParams = getTethysControl().getTethysExportParams();
		for (int i = 0; i < optButtons.length; i++) {
			if (optButtons[i].isSelected()) {
				exportParams.detectorParameterOutput = i;
				return true;
			}
		}
		return getPamWizard().showWarning("Select a parameters export option");
	}

	@Override
	public void setParams(StreamExportParams cardParams) {
		TethysExportParams exportParams = getTethysControl().getTethysExportParams();
		for (int i = 0; i < optButtons.length; i++) {
			optButtons[i].setSelected(i == exportParams.detectorParameterOutput);
		}
	}

}
