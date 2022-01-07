package qa;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.superdet.SuperDetDataBlock;
import pamScrollSystem.ViewLoadObserver;

public class QASequenceDataBlock extends SuperDetDataBlock<QASequenceDataUnit, QASoundDataUnit> {

	private QADataProcess qaDataProcess;

	public QASequenceDataBlock(QADataProcess qaDataProcess, boolean isGenerator) {
		super(QASequenceDataUnit.class, "QA Sequences", qaDataProcess, 0, !isGenerator, SuperDetDataBlock.ViewerLoadPolicy.LOAD_OVERLAPTIME);
		this.qaDataProcess = qaDataProcess;
	}	
	
	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		boolean loadOk = super.loadViewerData(offlineDataLoadInfo, loadObserver);
		qaDataProcess.findOpsDataUnits(this);
		return loadOk;
	}

	@Override
	public PamDataBlock findSubDetDataBlock(String dataLongName) {
		QASoundDataBlock block = qaDataProcess.getSoundsDataBlock();
		if (block != null && block.getLongDataName().equalsIgnoreCase(dataLongName)) {
			return block;
		}
		else {
			return super.findSubDetDataBlock(dataLongName);
		}
	}

}
