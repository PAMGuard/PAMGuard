package tethys.deployment;

import java.io.Serializable;

import nilus.DeploymentRecoveryDetails;

/**
 * Wrapper and functions associated with the Tethys Deployment object which can
 * exchange these with the PAMGuard database and display Everything is just held
 * within a list, in no particular order, which getContent public
 * 
 * This is from the Deployment JavaDoc. TBH it's pretty horrible, since it's possible to have
 * more than one of each type of object in the list, so not sure what to do about that. Would be nicer
 * as a HashTable. however we are where we're are for now. Start by focussing on getting the 
 * content saved for each type 
 * 
 * List<Serializable> getContent()
 * 
 * Gets the value of the content property. This accessor method returns a
 * reference to the live list,not a snapshot. Therefore any modification you
 * make to the returned list will be present inside the JAXB object.This is why
 * there is not a set method for the content property.
 * 
 * For example, to add a new item, do as follows: getContent().add(newItem);
 * 
 * 
 * Objects of the following type(s) are allowed in the list JAXBElement<String>
 * JAXBElement<String> 
 * JAXBElement<Integer>
 * JAXBElement<DeploymentRecoveryDetails> 
 * JAXBElement<String>
 * JAXBElement<Deployment.SamplingDetails> 
 * JAXBElement<Deployment.Sensors>
 * JAXBElement<Deployment.Instrument> 
 * JAXBElement<String>
 * JAXBElement<Deployment.Data> 
 * JAXBElement<String> String
 * JAXBElement<AcousticDataQAType> 
 * JAXBElement<Deployment.SiteAliases>
 * JAXBElement<String> 
 * JAXBElement<DeploymentRecoveryDetails>
 * 
 * 
 * @author dg50
 *
 */
public class PamDeployment {

	public PamDeployment() {

	}
	
	public DeploymentRecoveryDetails getDeploymentRecoveryDetails() {
		DeploymentRecoveryDetails drd = new DeploymentRecoveryDetails();
		
		return null;
	}
	
	private void ripApart(Serializable object) {
		Class cls = object.getClass();
//		cls.get
	}

}
