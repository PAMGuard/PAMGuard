package tethys.swing.export;

import java.awt.BorderLayout;

import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import PamguardMVC.PamDataBlock;
import nilus.DescriptionType;
import tethys.TethysControl;
import tethys.output.StreamExportParams;

public class DescriptionCard extends PamWizardCard<DescriptionType> {

	private DescriptionTypePanel descriptionPanel;
	
	public DescriptionCard(PamWizard detectionsExportWizard, TethysControl tethysControl) {
		super(detectionsExportWizard, "Description");
		this.setLayout(new BorderLayout());
		descriptionPanel = new DescriptionTypePanel("Description data", true, true, true);
		this.add(BorderLayout.CENTER, descriptionPanel.getMainPanel());
	}

	@Override
	public boolean getParams(DescriptionType description) {
		return descriptionPanel.getParams(description);
	}
	
	public boolean getParams(StreamExportParams streamExportParams) {
		return descriptionPanel.getParams(streamExportParams.getDescription());
	}

	@Override
	public void setParams(DescriptionType description) {
		descriptionPanel.setParams(description);
	}
	
	public void setParams(StreamExportParams streamExportParams) {
		descriptionPanel.setParams(streamExportParams.getDescription());
	}

}
