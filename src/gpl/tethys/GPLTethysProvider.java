package gpl.tethys;

import PamguardMVC.PamDataUnit;
import gpl.GPLControlledUnit;
import gpl.GPLDetection;
import gpl.GPLDetectionBlock;
import nilus.Detection;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class GPLTethysProvider extends AutoTethysProvider {

	private GPLControlledUnit gplControlledUnit;
	
	public GPLTethysProvider(TethysControl tethysControl, GPLControlledUnit gplControlledUnit, GPLDetectionBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
		this.gplControlledUnit = gplControlledUnit;
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection det = super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		
		GPLDetection gplDetection = (GPLDetection) dataUnit;
		det.getParameters().setScore(gplDetection.getPeakValue());
		det.getParameters().setReceivedLevelDB(gplDetection.getAmplitudeDB());
		
		return det;
	}
}
