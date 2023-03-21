package tethys.niluswraps;

import PamUtils.PamCalendar;
import nilus.Deployment;
import nilus.DeploymentRecoveryDetails;
import tethys.TethysTimeFuncs;
import tethys.deployment.RecordingPeriod;
/**
 * Wrapper around a nilus Deployment object to provide a bit of extra bookkeeping
 * and functionality for PAMGuard. 
 * @author dg50
 *
 */
public class PDeployment {

	public Deployment deployment;
	private RecordingPeriod matchedPAMGaurdPeriod;

	public PDeployment(Deployment deployment) {
		super();
		this.deployment = deployment;
	}
	
	public Long getAudioStart() {
		DeploymentRecoveryDetails detail = deployment.getDeploymentDetails();
		if (detail == null) {
			return null;
		}
		return TethysTimeFuncs.millisFromGregorianXML(detail.getAudioTimeStamp());
	}
	
	public Long getAudioEnd() {
		DeploymentRecoveryDetails detail = deployment.getRecoveryDetails();
		if (detail == null) {
			return null;
		}
		return TethysTimeFuncs.millisFromGregorianXML(detail.getAudioTimeStamp());
	}

	@Override
	public String toString() {
		return String.format("%s:%d; %s - %s", deployment.getId(), deployment.getDeploymentId(), 
				PamCalendar.formatDBDateTime(getAudioStart()), PamCalendar.formatDBDateTime(getAudioEnd()));
	}

	public RecordingPeriod getMatchedPAMGaurdPeriod() {
		return matchedPAMGaurdPeriod;
	}

	public void setMatchedPAMGaurdPeriod(RecordingPeriod matchedPAMGaurdPeriod) {
		this.matchedPAMGaurdPeriod = matchedPAMGaurdPeriod;
	}
	
	
}
