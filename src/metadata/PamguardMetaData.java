package metadata;

import java.io.Serializable;

import PamUtils.LatLong;
import nilus.Deployment;
import nilus.Helper;
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
	
//	/**
//	 * Deployment time (used if different 
//	 */
//	private Long deploymentMillis;
//	
//	private Long recoverMillis;
//	
//	private LatLong recoverLatLong;
	
	/**
	 * Get the deployment data
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			deploymentWrapper.setNilusObject(deployment);
		}
		return deploymentWrapper.getNilusObject(Deployment.class);
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
		return deploymentWrapper;
	}

	public void checkSerialisation() {
		// check that all wrappers have their xml up to date. 
		deploymentWrapper.reSerialise();
	}
	


}
