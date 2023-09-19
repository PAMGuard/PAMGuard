package tethys.detection;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;

public class GroupedGranularityHandler extends CallGranularityHandler {

	public GroupedGranularityHandler(TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		super(tethysControl, dataBlock, tethysExportParams, streamExportParams);
	}

	@Override
	public void prepare(long timeMillis) {
		super.prepare(timeMillis);
	}
	
	@Override
	public Detection[] addDataUnit(PamDataUnit dataUnit) {
		return super.addDataUnit(dataUnit);
	}

	@Override
	public Detection[] cleanup(long timeMillis) {
		return super.cleanup(timeMillis);
	}

}
