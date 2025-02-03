package metadata;

import java.io.Serializable;

import nilus.ContactInfo;
import nilus.Deployment;
import nilus.DeploymentRecoveryDetails;
import nilus.DescriptionType;
import nilus.Helper;
import nilus.MetadataInfo;
import nilus.ResponsibleParty;
import tethys.niluswraps.NilusSettingsWrapper;

/**
 * Meta data for a PAMGuard data set. This is based around serialisable versions of 
 * nilus classes to be compliant with both Tethys and PAMGuard settings files. May only
 * need a Deployment object, but scope for adding others / other fields if it's useful. 
 * @author dg50
 *
 */
public class PamguardMetaData implements Serializable {

	public static final long serialVersionUID = 1L;
	
	private NilusSettingsWrapper<Deployment> deploymentWrapper;
	
	public boolean useAudioForDeploymentTimes = false;
	
//	/**
//	 * Deployment time (used if different 
//	 */
//	private Long deploymentMillis;
//	
//	private Long recoverMillis;
//	
//	private LatLong recoverLatLong;
	
	/**
	 * Get the deployment data. will create if needed. 
	 * @return nilus deployment
	 */
	public Deployment getDeployment() {
		if (deploymentWrapper == null) {
			deploymentWrapper = new NilusSettingsWrapper<>();
		}
		Deployment deployment = deploymentWrapper.getNilusObject(Deployment.class);
		if (deployment == null) {
			deployment = new Deployment();
			try {
				Helper.createRequiredElements(deployment);
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
				e.printStackTrace();
			}
			deploymentWrapper.setNilusObject(deployment);
		}
		// check some fields we know we'll need that the Helper may not have managed. 
		if (deployment.getDescription() == null) {
			deployment.setDescription(new DescriptionType());
		}
		if (deployment.getMetadataInfo() == null) {
			deployment.setMetadataInfo(new MetadataInfo());
		}
		if (deployment.getMetadataInfo().getContact() == null) {
			deployment.getMetadataInfo().setContact(new ResponsibleParty());
		}
		if (deployment.getMetadataInfo().getContact().getContactInfo() == null) {
			deployment.getMetadataInfo().getContact().setContactInfo(new ContactInfo());
		}
		
		if (deployment.getDeploymentDetails() == null) {
			deployment.setDeploymentDetails(new DeploymentRecoveryDetails());
		}
		if (deployment.getRecoveryDetails() == null) {
			deployment.setRecoveryDetails(new DeploymentRecoveryDetails());
		}
		return deployment;
	}
	
	/**
	 * Set the deployment data. 
	 * @param deployment nilus deployment
	 */
	public void setDeployment(Deployment deployment) {
		if (deploymentWrapper == null) {
			deploymentWrapper = new NilusSettingsWrapper<>();
		}
		deploymentWrapper.setNilusObject(deployment);
	}

	/**
	 * @return the deploymentWrapper
	 */
	public NilusSettingsWrapper<Deployment> getDeploymentWrapper() {
		if (deploymentWrapper == null) {
			deploymentWrapper = new NilusSettingsWrapper<>();
		}
		return deploymentWrapper;
	}

	public void checkSerialisation() {
		// check that all wrappers have their xml up to date. 
		if (deploymentWrapper == null) {
			deploymentWrapper = new NilusSettingsWrapper<>();
		}
		deploymentWrapper.reSerialise();
	}
	


}
