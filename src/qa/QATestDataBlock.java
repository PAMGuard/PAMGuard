package qa;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.superdet.SuperDetDataBlock;
import pamScrollSystem.ViewLoadObserver;

public class QATestDataBlock extends SuperDetDataBlock<QATestDataUnit, QASequenceDataUnit> {

	private QADataProcess qaDataProcess;

	public QATestDataBlock(String dataName, QADataProcess qaDataProcess, boolean isGenerator) {
		super(QATestDataUnit.class, dataName, qaDataProcess, 0, !isGenerator, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.qaDataProcess = qaDataProcess;
		setClearAtStart(false);
		setNaturalLifetime(3600*24*365);
	}
	
	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		boolean loadOk = super.loadViewerData(offlineDataLoadInfo, loadObserver);
		qaDataProcess.findOpsDataUnits(this);
		return loadOk;
	}

	@Override
	public PamDataBlock findSubDetDataBlock(String dataLongName) {
		QASequenceDataBlock block = qaDataProcess.getSequenceDataBlock();
		if (block != null && block.getLongDataName().equalsIgnoreCase(dataLongName)) {
			return block;
		}
		else {
			return super.findSubDetDataBlock(dataLongName);
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsT(long)
	 */
	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#removeOldUnitsS(long)
	 */
	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		// this one doesn't get called since it's not acoustic data. 
		return 0;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamDataBlock#getNumRequiredBeforeLoadTime()
	 */
	@Override
	public int getNumRequiredBeforeLoadTime() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
			return 0;
		}
		else {
			if (isOffline()) {
				return 0;
			}
			else {
				return Integer.MAX_VALUE;
			}
		}
	}
	
	

}
