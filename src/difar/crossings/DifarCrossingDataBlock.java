package difar.crossings;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataSelector.DataSelectorCreator;
import PamguardMVC.superdet.SuperDetDataBlock;
import difar.DifarDataUnit;
import difar.DifarProcess;
import difar.dataSelector.CrossingDataSelectCreator;

/**
 * Holds DIFAR crossings. Crossings are stored in the database only, and
 * name their clips by UID in a subtable.
 * <p>
 * Follows the detection group localiser's block. In the viewer, any
 * crossing overlapping the loaded period is loaded, and crossings are
 * always reloaded so they link up with the clips loaded beside them.
 */
public class DifarCrossingDataBlock extends SuperDetDataBlock<DifarCrossing, DifarDataUnit> {

	private final DifarProcess difarProcess;

	private CrossingDataSelectCreator crossingSelectCreator;

	public DifarCrossingDataBlock(String dataName, DifarProcess difarProcess) {
		super(DifarCrossing.class, dataName, difarProcess, 0, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.difarProcess = difarProcess;
	}

	/**
	 * Only saved clips can be crossed, not clips waiting on the queue, which
	 * hold the same kind of data unit.
	 */
	@Override
	public boolean canSuperDetection(PamDataBlock subDataBlock) {
		return subDataBlock == difarProcess.getProcessedDifarData();
	}

	@Override
	public void addPamData(DifarCrossing pamDataUnit) {
		super.addPamData(pamDataUnit);
		sortData();
	}

	@Override
	public void addPamData(DifarCrossing pamDataUnit, Long uid) {
		super.addPamData(pamDataUnit, uid);
		sortData();
	}

	@Override
	public void updatePamData(DifarCrossing pamDataUnit, long updateTimeMillis) {
		super.updatePamData(pamDataUnit, updateTimeMillis);
		sortData();
	}

	/**
	 * Crossings are selected by the quality of their geometry: angle, error,
	 * and whether they lie on one of their own buoys.
	 */
	@Override
	public DataSelectorCreator getDataSelectCreator() {
		if (crossingSelectCreator == null) {
			crossingSelectCreator = new CrossingDataSelectCreator(this);
		}
		return crossingSelectCreator;
	}

	@Override
	public boolean needViewerDataLoad(OfflineDataLoadInfo offlineDataLoadInfo) {
		return true;
	}
}
