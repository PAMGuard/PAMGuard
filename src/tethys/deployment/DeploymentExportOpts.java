package tethys.deployment;

import java.io.Serializable;

/**
 * options for Deployment export collected by the export Wizard. 
 * @author dg50
 *
 */
public class DeploymentExportOpts implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	public boolean separateDeployments;
	
	/**
	 * Minimum number of seconds between GPS points in a track. 
	 */
	public double trackPointInterval;
	
	/**
	 * Max gap before recording periods are separated, potentially into 
	 * separate Deployment documents
	 */
	public int maxRecordingGapSeconds = 60;
	
	/**
	 * A recording section after joining with max gap parameter is too short
	 * to be worth keeping. 
	 */
	public int minRecordingLengthSeconds = 10;

	@Override
	protected DeploymentExportOpts clone() {
		try {
			return (DeploymentExportOpts) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
