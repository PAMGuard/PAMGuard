package tethys.deployment.swing;

import java.awt.Dimension;
import java.awt.Window;

import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import metadata.MetaDataContol;
import nilus.Deployment;
import tethys.TethysControl;
import tethys.deployment.DeploymentExportOpts;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.TrackInformation;
import tethys.swing.export.DescriptionCard;
import tethys.swing.export.ResponsiblePartyCard;

public class DeploymentWizard extends PamWizard {

	private static final long serialVersionUID = 1L;

	private Deployment deployment;
	
	private DeploymentExportOpts exportOptions;
	
	private DescriptionCard descriptionCard;
	
	private DeploymentInfoCard deploymentInfoCard;
	
	private DeploymentDataCard deploymentDataCard;
	
	private DeploymentTrackCard deploymentTrackCard;
//	private 
	
	private DeploymentWizard(Window parentFrame, TethysControl tethysControl, Deployment deployment, DeploymentExportOpts exportOptions) {
		super(parentFrame, "Deployment Export");
		this.deployment = deployment;
		this.exportOptions = exportOptions;
		DeploymentHandler deploymentHandler = tethysControl.getDeploymentHandler();
		TrackInformation trackInfo = deploymentHandler.getTrackInformation();
		
		addCard(deploymentInfoCard = new DeploymentInfoCard(this, "Responsible Party"));
		addCard(deploymentDataCard = new DeploymentDataCard(this, tethysControl));
		addCard(descriptionCard = new DescriptionCard(this, tethysControl));
		boolean haveGPS = trackInfo.haveGPSTrack();
		if (haveGPS) {
			deploymentTrackCard = new DeploymentTrackCard(this, tethysControl, trackInfo);
			addCard(deploymentTrackCard);
		}
		descriptionCard.setPreferredSize(new Dimension(10, 300));
	}
	
	public static DeploymentExportOpts showWizard(Window parentFrame, TethysControl tethysControl, Deployment deployment, DeploymentExportOpts exportOptions) {
		if (deployment == null) {
			deployment = MetaDataContol.getMetaDataControl().getMetaData().getDeployment();
		}
		DeploymentWizard wiz = new DeploymentWizard(parentFrame, tethysControl, deployment, exportOptions);
		wiz.setParams();
		wiz.setVisible(true);
		return wiz.exportOptions;
	}

	@Override
	public void setCardParams(PamWizardCard wizardCard) {
		if (wizardCard == descriptionCard) {
			descriptionCard.setParams(deployment.getDescription());
		}
		if (wizardCard == deploymentInfoCard) {
			deploymentInfoCard.setParams(deployment);
		}
		if (wizardCard == deploymentDataCard) {
			deploymentDataCard.setParams(exportOptions, deployment);
		}
		if (wizardCard == deploymentTrackCard) {
			deploymentTrackCard.setParams(exportOptions);
		}
	}

	@Override
	public boolean getCardParams(PamWizardCard wizardCard) {
		if (wizardCard == descriptionCard) {
			return descriptionCard.getParams(deployment.getDescription());
		}
		if (wizardCard == deploymentInfoCard) {
			return deploymentInfoCard.getParams(exportOptions, deployment);
		}
		if (wizardCard == deploymentDataCard) {
			return deploymentDataCard.getParams(exportOptions, deployment);
		}
		if (wizardCard == deploymentTrackCard) {
			return deploymentTrackCard.getParams(exportOptions);
		}
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		this.exportOptions = null;
	}

}
