package RightWhaleEdgeDetector.species;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import RightWhaleEdgeDetector.RWEDataUnit;
import nilus.Detection;
import nilus.Detection.Parameters;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class RWTethysDataProvider extends AutoTethysProvider {

	public RWTethysDataProvider(TethysControl tethysControl, PamDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection detection =  super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		if (detection == null) {
			return null;
		}
		
		RWEDataUnit rweDataUnit = (RWEDataUnit) dataUnit;

		Parameters parameters = detection.getParameters();
		parameters.setScore((double) rweDataUnit.rweSound.soundType);
		double snr = 20.*Math.log10(rweDataUnit.rweSound.signal/rweDataUnit.rweSound.noise);
		parameters.setSNRDB(snr);
		
		return detection;
	}

}
