package clickTrainDetector.tethys;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.ClickTrainControl;
import nilus.Detection;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class ClickTrainTethysProvider extends AutoTethysProvider {

	public ClickTrainTethysProvider(TethysControl tethysControl, ClickTrainControl clickTrainControl, PamDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection det = super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		
		return det;
	}
}
