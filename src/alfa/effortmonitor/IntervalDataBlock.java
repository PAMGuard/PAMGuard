package alfa.effortmonitor;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import alfa.server.ALFAServerLoader;
import pamScrollSystem.ViewLoadObserver;

public class IntervalDataBlock extends PamDataBlock<IntervalDataUnit> {
	

	public IntervalDataBlock(PamProcess parentProcess, String name) {
		super(IntervalDataUnit.class, name, parentProcess, 0);
		setNaturalLifetime(3600*10); // 10 hours worth. 
//		setClearAtStart(PamController.getInstance().getRunMode() == PamController.RUN_NORMAL);
		/**
		 * Will call explicitly from start() if we want to depending on delected options. 
		 */
		setClearAtStart(false);
	}

	@Override
	public int getNumRequiredBeforeLoadTime() {
		return 10;
	}

	@Override
	public boolean getShouldLog(PamDataUnit pamDataUnit) {
		IntervalDataUnit idu = (IntervalDataUnit) pamDataUnit;
		return super.getShouldLog(pamDataUnit) & idu.getDurationInMilliseconds() > 0;
	}



}
