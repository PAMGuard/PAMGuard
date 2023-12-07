package tethys.swing.export;

import java.awt.BorderLayout;

import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.output.StreamExportParams;

public class DescriptionCard extends ExportWizardCard {

	private DescriptionTypePanel descriptionPanel;
	
	public DescriptionCard(DetectionsExportWizard detectionsExportWizard, TethysControl tethysControl, PamDataBlock dataBlock) {
		super(tethysControl, detectionsExportWizard, "Description", dataBlock);
		this.setLayout(new BorderLayout());
		descriptionPanel = new DescriptionTypePanel("Description data", true, true, true);
		this.add(BorderLayout.CENTER, descriptionPanel.getMainPanel());
	}

	@Override
	public boolean getParams(StreamExportParams streamExportParams) {
		return descriptionPanel.getParams(streamExportParams.getDetectionDescription().getDescription());
	}

	@Override
	public void setParams(StreamExportParams streamExportParams) {
		descriptionPanel.setParams(streamExportParams.getDetectionDescription().getDescription());
	}

}
