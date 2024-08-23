package tethys.niluswraps;

import PamUtils.PamCalendar;
import nilus.Deployment;
import nilus.DeploymentRecoveryDetails;
import nilus.GranularityType;
import tethys.TethysTimeFuncs;
import tethys.deployment.RecordingPeriod;
/**
 * Wrapper around a nilus Deployment object to provide a bit of extra bookkeeping
 * and functionality for PAMGuard. 
 * @author dg50
 *
 */
public class PDeployment extends NilusDocumentWrapper<Deployment>{
	
	private RecordingPeriod matchedPAMGaurdPeriod;

	public PDeployment(Deployment deployment) {
		super(deployment);
	}
	
	public Long getAudioStart() {
		DeploymentRecoveryDetails detail = nilusObject.getDeploymentDetails();
		if (detail == null || detail.getAudioTimeStamp() == null) {
			return null;
		}
		return TethysTimeFuncs.millisFromGregorianXML(detail.getAudioTimeStamp());
	}
	
	public Long getAudioEnd() {
		DeploymentRecoveryDetails detail = nilusObject.getRecoveryDetails();
		if (detail == null || detail.getAudioTimeStamp() == null) {
			return null;
		}
		return TethysTimeFuncs.millisFromGregorianXML(detail.getAudioTimeStamp());
	}

	@Override
	public String toString() {
		return String.format("%s:%d; %s - %s", nilusObject.getId(), nilusObject.getDeploymentId(), 
				PamCalendar.formatDBDateTime(getAudioStart()), PamCalendar.formatDBDateTime(getAudioEnd()));
	}

	public RecordingPeriod getMatchedPAMGaurdPeriod() {
		return matchedPAMGaurdPeriod;
	}

	public void setMatchedPAMGaurdPeriod(RecordingPeriod matchedPAMGaurdPeriod) {
		this.matchedPAMGaurdPeriod = matchedPAMGaurdPeriod;
	}

	public String getShortDescription() {
		Long audioStart = getAudioStart();
		if (audioStart == null) {
			return String.format("%s %s", nilusObject.getId(), "unknown start");
		}
		else {
			return String.format("%s %s", nilusObject.getId(), PamCalendar.formatDBDate(getAudioStart()));
		}
	}
	
	public static String formatGranularity(GranularityType granularity) {
		if (granularity == null) {
			return null;
		}
		String str = String.format("%s", granularity.getValue());
		Double bin = granularity.getBinSizeMin();
		if (bin != null) {
			str += String.format(" (%3.1f s)", bin*60);
		}
		Double gap = granularity.getEncounterGapMin();
		if (gap != null) {
			str += String.format( " (%3.1f s)", gap*60.);
		}
		return str;
	}
	
	
}
