package effortmonitor;

import java.util.ListIterator;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import dataMap.BespokeDataMapGraphic;
import dataMap.DataMapControl;
import dataMap.OfflineDataMap;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.ViewLoadObserver;

public class EffortDataBlock extends PamDataBlock<EffortDataUnit> {

	private EffortControl effortControl;

	public EffortDataBlock(EffortControl effortControl, PamProcess parentProcess) {
		super(EffortDataUnit.class, "Effort Data", parentProcess, 0);
		this.effortControl = effortControl;
		this.setNaturalLifetimeMillis(3600*24*365*10); // 10 years !
	}

	public EffortDataUnit findActiveUnit(AbstractPamScroller scroller) {
		synchronized (getSynchLock()) {
			ListIterator<EffortDataUnit> iter = this.getListIterator(PamDataBlock.ITERATOR_END);
			if (iter == null) {
				return null;
			}
			while (iter.hasPrevious()) {
				EffortDataUnit data = iter.previous();
				if (data.getScroller() == scroller && data.isActive()) {
					return data;
				}
			}
		}
		return null;
	}

	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
//		if (getUnitsCount() == 0) {
//			OfflineDataMap dataMap = getPrimaryDataMap();
			DataMapControl dmc = DataMapControl.getDataMapControl();
			if (dmc != null) {
				//want to load all data. 
				long origStart = offlineDataLoadInfo.getStartMillis();
				long origEnd = offlineDataLoadInfo.getEndMillis();
				long dataStart = Math.min(offlineDataLoadInfo.getStartMillis(), dmc.getFirstTime() - 360000000L);
				long dataEnd = Math.max(offlineDataLoadInfo.getEndMillis(), dmc.getLastTime() + 360000000L);
				System.out.printf("Load effort data from %s to %s\n", PamCalendar.formatDBDateTime(dataStart), PamCalendar.formatDBDateTime(dataEnd));
				offlineDataLoadInfo.setStartMillis(dataStart);
				offlineDataLoadInfo.setEndMillis(dataEnd);
			}
			return super.loadViewerData(offlineDataLoadInfo, loadObserver);
//		}
//		else {
////			saveViewerData();
////		}
////		return true;
	}
//	@Override
//	public void clearAll() {
//		if (PamController.getInstance()!=null &&
//				PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW) {
//			if (getUnitsCount() > 0) {
//				saveViewerData();
//			}
//		}
//	}

	@Override
	public boolean shouldNotify() {
		return true;
	}

	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		return pamDataUnit.getDatabaseIndex() == 0;
	}

	@Override
	public boolean getShouldLog() {
		return true;
	}

	@Override
	public BespokeDataMapGraphic getBespokeDataMapGraphic() {
		return effortControl.getDataMapGraph();
	}
	

}
