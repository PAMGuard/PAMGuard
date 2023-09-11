package tethys.swing.export;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamControlledUnit;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.detection.DetectionsHandler;
import tethys.output.StreamExportParams;

public class AlgorithmCard extends ExportWizardCard {
	
	private JTextField method, software, version, supportSoftware;

	public AlgorithmCard(DetectionsExportWizard detectionsExportWizard, TethysControl tethysControl, PamDataBlock dataBlock) {
		super(tethysControl, "Algorithm", dataBlock);
		setBorder(new TitledBorder("Algorithm details"));
		method = new JTextField(40);
		software = new JTextField(40);
		version = new JTextField(40);
		supportSoftware = new JTextField(40);
		JPanel nPanel = new JPanel(new GridBagLayout());
		this.setLayout(new BorderLayout());
		this.add(BorderLayout.NORTH, nPanel);
		GridBagConstraints c = new PamGridBagContraints();
		nPanel.add(new JLabel("Method ", JLabel.LEFT), c);
		c.gridy++;
		nPanel.add(method, c);
//		c.gridx = 0;
		c.gridy++;
		nPanel.add(new JLabel("Software ", JLabel.LEFT), c);
		c.gridy++;
		nPanel.add(software, c);
		c.gridx = 0;
		c.gridy++;
		nPanel.add(new JLabel("Version ", JLabel.LEFT), c);
		c.gridy++;
		nPanel.add(version, c);
		c.gridx = 0;
		c.gridy++;
		nPanel.add(new JLabel("Support Software ", JLabel.LEFT), c);
		c.gridy++;
		nPanel.add(supportSoftware, c);
		c.gridx = 0;
		c.gridy++;
		method.setEditable(false);
		version.setEditable(false);
		software.setEditable(false);
		supportSoftware.setEditable(false);
		
	}

	@Override
	public boolean getParams(StreamExportParams streamExportParams) {
		// Nothing to actually enter here. 
		return true;
	}

	@Override
	public void setParams(StreamExportParams streamExportParams) {
		PamDataBlock dataBlock = getDataBlock();
		DetectionsHandler detHandler = getTethysControl().getDetectionsHandler();
		method.setText(detHandler.getMethodString(dataBlock));
		software.setText(detHandler.getSoftwareString(dataBlock));
		version.setText(detHandler.getVersionString(dataBlock));
		supportSoftware.setText(detHandler.getSupportSoftware(dataBlock) + " V" + detHandler.getSupportSoftwareVersion(dataBlock));
	}

}
