package qa.operations;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import pamScrollSystem.ViewLoadObserver;

public class QAOpsDataBlock extends PamDataBlock<QAOpsDataUnit> {

	public QAOpsDataBlock(PamProcess parentProcess) {
		super(QAOpsDataUnit.class, "QA Ops Data", parentProcess, 0);
		SetLogging(new QAOpsLogging(this));
		setNaturalLifetime(3600*24);
	}


	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		OfflineDataLoadInfo loadInfo = offlineDataLoadInfo.clone();
		loadInfo.setStartMillis(0);
		loadInfo.setEndMillis(Long.MAX_VALUE);
		return super.loadViewerData(loadInfo, loadObserver);
	}

}
