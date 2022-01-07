package qa;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import pamScrollSystem.ViewLoadObserver;

public class QASoundDataBlock extends PamDataBlock<QASoundDataUnit> {

	private QADataProcess qaDataProcess;

	public QASoundDataBlock(QADataProcess qaDataProcess, boolean isGenerator) {
		super(QASoundDataUnit.class, "Generated Sounds", qaDataProcess, 0, !isGenerator);
		this.qaDataProcess = qaDataProcess;
	}
	
	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		boolean loadOk = super.loadViewerData(offlineDataLoadInfo, loadObserver);
		qaDataProcess.findOpsDataUnits(this);
		return loadOk;
	}

//	@Override
//	protected synchronized int removeOldUnitsT(long currentTimeMS) {
//		if (getUnitsCount() == 0) {
//			return 0;
//		}
//		int n = super.removeOldUnitsT(currentTimeMS);
//		if (n > 0)
//		Debug.out.printf("%d dataunits removed in removeOldUnitsS\n", n);
//		return n;
//	}
//
//	@Override
//	protected synchronized int removeOldUnitsS(long mastrClockSample) {
//		if (getUnitsCount() == 0) {
//			return 0;
//		}
//		int n = super.removeOldUnitsS(mastrClockSample);
//		if (n > 0)
//		Debug.out.printf("%d dataunits removed in removeOldUnitsS\n", n);
//		return n;
//	}

//	@Override
//	public void clearAll() {
//		super.clearAll();
//	}
//
//	@Override
//	public void addPamData(QASoundDataUnit pamDataUnit) {
//		Debug.out.printf("Adding data to block %s currently %d in system\n", getDataName(), getUnitsCount());
//		super.addPamData(pamDataUnit);
//		Debug.out.printf("Added data to block %s now have %d in system\n", getDataName(), getUnitsCount());
//	}
//
//	@Override
//	public void addPamData(QASoundDataUnit pamDataUnit, Long uid) {
//		// TODO Auto-generated method stub
//		Debug.out.printf("Adding data and uid to block %s currently %d in system\n", getDataName(), getUnitsCount());
//		super.addPamData(pamDataUnit, uid);
//		Debug.out.printf("Added data and uid to block %s now have %d in system\n", getDataName(), getUnitsCount());
//	}
//
//	@Override
//	public boolean isdebug() {
//		return true;
//	}


}
