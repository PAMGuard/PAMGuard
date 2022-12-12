package landMarks;

import PamUtils.PamCalendar;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import autecPhones.AutecGraphics;
import pamScrollSystem.ViewLoadObserver;

public class LandmarkDataBlock extends PamDataBlock<LandmarkDataUnit> {

	LandmarkControl landmarkControl;
	
	public LandmarkDataBlock(String dataName, LandmarkControl landmarkControl, PamProcess parentProcess) {
		super(LandmarkDataUnit.class, dataName, parentProcess, 0);
		setOverlayDraw(new LandmarkGraphics(landmarkControl));
//		this.setPamSymbolManager(new StandardSymbolManager(this, LandmarkGraphics.defSymbol, true));
		this.setClearAtStart(false);
	}

	@Override
	protected int removeOldUnitsT(long currentTimeMS) {
//		return super.removeOldUnitsT(currentTimeMS);
		return 0; // don't do anything !
	}
	
	protected void createDataUnits(LandmarkDatas landmarkDatas) {
		clearAll();
		long now = PamCalendar.getTimeInMillis();
		for (int i = 0; i < landmarkDatas.size(); i++) {
			addPamData(new LandmarkDataUnit(now, landmarkDatas.get(i)));
		}
	}

	@Override
	public boolean loadViewerData(long dataStart, long dataEnd, ViewLoadObserver loadObserver) {
		return true;
	}

	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		return true;
	}

}
