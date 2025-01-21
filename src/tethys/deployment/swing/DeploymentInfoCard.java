package tethys.deployment.swing;

import javax.swing.BoxLayout;

import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import nilus.Deployment;
import tethys.deployment.DeploymentExportOpts;
import tethys.swing.export.ResponsiblePartyPanel;

public class DeploymentInfoCard extends PamWizardCard<Deployment> {

	private ResponsiblePartyPanel responsiblePartyPanel;
	
	private ProjectInformationPanel projectInformationPanel;
	
	public DeploymentInfoCard(PamWizard pamWizard, String title) {
		super(pamWizard, title);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		projectInformationPanel = new ProjectInformationPanel(pamWizard, "Project Information");
		this.add(projectInformationPanel.getMainPanel());
		responsiblePartyPanel = new ResponsiblePartyPanel("Responsible Party");
		this.add(responsiblePartyPanel.getMainPanel());
	}

	@Override
	public boolean getParams(Deployment cardParams) {
		boolean ok = responsiblePartyPanel.getParams(cardParams.getMetadataInfo().getContact());
		ok &= projectInformationPanel.getParams(cardParams);
		
		return ok;
	}

	@Override
	public void setParams(Deployment cardParams) {
		projectInformationPanel.setParams(cardParams);
		responsiblePartyPanel.setParams(cardParams.getMetadataInfo().getContact());
	}

	public boolean getParams(DeploymentExportOpts exportOptions, Deployment deployment) {
		boolean ok = getParams(deployment);
		
		return ok;
	}

}
