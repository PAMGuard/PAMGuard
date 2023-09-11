package tethys.detection;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import nilus.Detection;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;

public class GroupedGranularityHandler extends GranularityHandler {

	public GroupedGranularityHandler(TethysControl tethysControl, PamDataBlock dataBlock,
			TethysExportParams tethysExportParams, StreamExportParams streamExportParams) {
		super(tethysControl, dataBlock, tethysExportParams, streamExportParams);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void prepare(long timeMillis) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Detection[] addDataUnit(PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Detection[] cleanup(long timeMillis) {
		// TODO Auto-generated method stub
		return null;
	}

}
