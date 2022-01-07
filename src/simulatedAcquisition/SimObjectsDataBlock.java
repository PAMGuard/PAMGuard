package simulatedAcquisition;

import Acquisition.AcquisitionProcess;
import PamguardMVC.PamDataBlock;

public class SimObjectsDataBlock extends PamDataBlock<SimObjectDataUnit> {

	public SimObjectsDataBlock(AcquisitionProcess acquisitionProcess) {
		super(SimObjectDataUnit.class, "Simulated Objects", acquisitionProcess, 0);

		
	}

	@Override
	public void clearAll() {
		// TODO Auto-generated method stub
//		super.clearAll();
	}
	
	protected void clearOldData() {
		super.clearAll();
	}

	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		return 0;
	}

	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		return 0;
	}

}
