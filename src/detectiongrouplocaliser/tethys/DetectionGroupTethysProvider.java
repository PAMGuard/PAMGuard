package detectiongrouplocaliser.tethys;

import Localiser.LocalisationAlgorithm;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import detectiongrouplocaliser.DetectionGroupControl;
import detectiongrouplocaliser.DetectionGroupDataUnit;
import nilus.Detection;
import nilus.GranularityEnumType;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class DetectionGroupTethysProvider extends AutoTethysProvider {

	private DetectionGroupControl detectionGroupControl;

	public DetectionGroupTethysProvider(TethysControl tethysControl, PamDataBlock pamDataBlock, DetectionGroupControl detectionGroupControl) {
		super(tethysControl, pamDataBlock);
		this.detectionGroupControl = detectionGroupControl;
	}

	@Override
	public GranularityEnumType[] getAllowedGranularities() {
		GranularityEnumType[] allowed = {GranularityEnumType.GROUPED};
		return allowed;
	}

	@Override
	public LocalisationAlgorithm getLocalisationAlgorithm() {
		return detectionGroupControl.getLocalisationAlgorithm();
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection det = super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		// set the duration in seconds, since that's useful for a group detection. 
		Double durMillis = dataUnit.getDurationInMilliseconds();
		if (durMillis != null) {
			det.getParameters().setDurationS(durMillis/1000.);
		}
		DetectionGroupDataUnit dgu = (DetectionGroupDataUnit) dataUnit;
		return det;
	}
	
	
}
