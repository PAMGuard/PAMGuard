package difar.offline;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import difar.DifarDataUnit;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

/**
 * Clears saved triangulations.
 * <p>
 * Used after a buoy is changed, where the triangulations worked out from the
 * old calibration no longer describe anything. The bearings are left alone,
 * since they follow the buoy record. The counterpart to UpdateCrossingTask,
 * which works them out again instead.
 */
public class ClearCrossingTask<T extends PamDataUnit> extends OfflineTask<T> {

	private final PamDataBlock<T> pamDataBlock;

	public ClearCrossingTask(PamDataBlock<T> pamDataBlock) {
		super(pamDataBlock);
		this.pamDataBlock = pamDataBlock;
		addAffectedDataBlock(pamDataBlock);
	}

	@Override
	public String getName() {
		return "Clear triangulations in " + pamDataBlock.getDataName();
	}

	@Override
	public boolean processDataUnit(T dataUnit) {
		DifarDataUnit difarDataUnit = (DifarDataUnit) dataUnit;
		if (difarDataUnit.getDifarCrossing() == null) {
			return false;
		}
		difarDataUnit.setDifarCrossing(null);
		return true;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
	}

	@Override
	public void loadedDataComplete() {
	}
}
