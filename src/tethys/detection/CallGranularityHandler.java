package tethys.detection;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import nilus.Detections;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.TethysDataProvider;

public class CallGranularityHandler extends GranularityHandler {

	private TethysDataProvider dataProvider;

	public CallGranularityHandler(TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		super(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		
		dataProvider = dataBlock.getTethysDataProvider(tethysControl);
		
	}

	@Override
	public void prepare(long timeMillis) {
		// never anything to do here for call level granularity. 
	}

	@Override
	public Detection[] addDataUnit(PamDataUnit dataUnit) {
		Detection det = dataProvider.createDetection(dataUnit, tethysExportParams, streamExportParams);
		return toDetectionArray(det);
	}

	@Override
	public Detection[] cleanup(long timeMillis) {
		// never anything to do here for call level granularity. 
		return null;
	}

	@Override
	protected boolean autoEffortFix(Detections detections, Detection det) {
		return expandEffort(detections, det);
	}

}
