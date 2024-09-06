package IshmaelDetector.tethys;

import IshmaelDetector.IshDetection;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class IshmaelTethysProvider extends AutoTethysProvider {

	public IshmaelTethysProvider(TethysControl tethysControl, PamDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection detection =  super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		if (detection == null) {
			return null;
		}
		IshDetection ishData = (IshDetection) dataUnit;
		detection.getParameters().setScore(roundSignificantFigures(ishData.getPeakHeight(), 4));
		
		return detection;
	}
}
