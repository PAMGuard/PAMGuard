package metadata.deployment;

import java.io.Serializable;

import PamModel.parametermanager.FieldNotFoundException;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Class to hold Deployment data in a form consistent with the ANSI PAM
 * Standard. This has been keep separate from the Tethys Interface to keep it
 * easy to benefit from these data without using Tethys itself.
 * 
 * @author dg50
 *
 */
public class DeploymentData implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * String that uniquely identifies this deployment.
	 */
	private String id;

	/**
	 * Name of project associated with this deployment. Can be related to a
	 * geographic region, funding source, etc
	 */
	private String project;

	/**
	 * Deployment identifier, a number related to either the Nth deployment
	 * operation in a series of deployments or the Nth deployment at a specific
	 * site. This is different from Id which is unique across all deployments
	 */
	private int deploymentId;

	/**
	 * Alternative deployment description.
	 */
	private String deploymentAlias;

	/**
	 * Name for current location.
	 */
	private String site;

	/**
	 * Alternative names for the deployment location
	 */
	private String siteAliases;

	/**
	 * Name of deployment cruise.
	 */
	private String cruise;

//	/**
//	 * On what platform is the instrument deployed? (e.g. mooring, tag)
//	 */
//	private String platform = "Unknown";

	/**
	 * Name of geographic region.
	 */
	private String region;

//	/**
//	 * Instrument type, e.g. HARP, EAR, Popup, DMON, etc.
//	 */
//	private String instrumentType;
//
//	/**
//	 * Instrument identifier, e.g. serial number
//	 */
//	private String instrumentId;

	public DeploymentData() {
	}

	@Override
	protected DeploymentData clone() {
		try {
			return (DeploymentData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			ps.findParameterData("id").setInfo("Unique Id", null, "String that uniquely identifies this deployment", 128);
//			ps.setOrder("id", 0);
			ps.findParameterData("project").setInfo("Project Name", null, "Name of project associated with this deployment. Can be related to a geographic region, funding source, etc", 200);
			ps.findParameterData("deploymentId").setInfo("Deployment Identifier", null, "Deployment identifier, a number related to either the Nth deployment operation in a series of deployments or the Nth deployment at a specific site.  This is different from Id which is unique across all deployments");
			ps.findParameterData("deploymentAlias").setInfo("Alternative deployment description", null, "Alternative deployment description", 20);
			ps.findParameterData("site").setInfo("Site name", null, "Name for current location", 40);
			ps.findParameterData("siteAliases").setInfo("Alternative site name", null, "Alternative site description", 40);
			ps.findParameterData("cruise").setInfo("Deployment cruise", null, "Name of deployment cruise");
			ps.findParameterData("platform").setInfo("Platform type", null, "On what platform is the instrument deployed? (e.g. mooring, tag)", 20);
			ps.findParameterData("region").setInfo("Geographic Region", "", "Name of geographic region", 40);
		} catch (FieldNotFoundException e) {
			e.printStackTrace();
		}
		return ps;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @param project the project to set
	 */
	public void setProject(String project) {
		this.project = project;
	}

	/**
	 * @return the deploymentId
	 */
	public int getDeploymentId() {
		return deploymentId;
	}

	/**
	 * @param deploymentId the deploymentId to set
	 */
	public void setDeploymentId(int deploymentId) {
		this.deploymentId = deploymentId;
	}

	/**
	 * @return the deplomentAlias
	 */
	public String getDeploymentAlias() {
		return deploymentAlias;
	}

	/**
	 * @param deplomentAlias the deplomentAlias to set
	 */
	public void setDeploymentAlias(String deplomentAlias) {
		this.deploymentAlias = deplomentAlias;
	}

	/**
	 * @return the site
	 */
	public String getSite() {
		return site;
	}

	/**
	 * @param site the site to set
	 */
	public void setSite(String site) {
		this.site = site;
	}

	/**
	 * @return the siteAliases
	 */
	public String getSiteAliases() {
		return siteAliases;
	}

	/**
	 * @param siteAliases the siteAliases to set
	 */
	public void setSiteAliases(String siteAliases) {
		this.siteAliases = siteAliases;
	}

	/**
	 * @return the cruise
	 */
	public String getCruise() {
		return cruise;
	}

	/**
	 * @param cruise the cruise to set
	 */
	public void setCruise(String cruise) {
		this.cruise = cruise;
	}

//	/**
//	 * @return the platform
//	 */
//	public String getPlatform() {
//		return platform;
//	}
//
//	/**
//	 * @param platform the platform to set
//	 */
//	public void setPlatform(String platform) {
//		this.platform = platform;
//	}

	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

//	/**
//	 * @return the instrumentType
//	 */
//	public String getInstrumentType() {
//		return instrumentType;
//	}
//
//	/**
//	 * @param instrumentType the instrumentType to set
//	 */
//	public void setInstrumentType(String instrumentType) {
//		this.instrumentType = instrumentType;
//	}
//
//	/**
//	 * @return the instrumentId
//	 */
//	public String getInstrumentId() {
//		return instrumentId;
//	}
//
//	/**
//	 * @param instrumentId the instrumentId to set
//	 */
//	public void setInstrumentId(String instrumentId) {
//		this.instrumentId = instrumentId;
//	}

}
