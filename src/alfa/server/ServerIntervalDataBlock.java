package alfa.server;

import java.util.List;

import PamController.PamController;
import PamguardMVC.PamProcess;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataSelector.DataSelectorCreator;
import alfa.effortmonitor.IntervalDataBlock;
import pamScrollSystem.ViewLoadObserver;

public class ServerIntervalDataBlock extends IntervalDataBlock {

	private ALFAServerLoader alfaServerLoader;
	
	private SIDSCreator sidsCreator;

	public ServerIntervalDataBlock(PamProcess parentProcess, String name) {
		super(parentProcess, name);
		alfaServerLoader = new ALFAServerLoader(this);
		sidsCreator = new SIDSCreator(this);
		alfaServerLoader.createOfflineDataMap(PamController.getMainFrame());
	}

	@Override
	public boolean loadViewerData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
		this.setCurrentViewDataStart(offlineDataLoadInfo.getStartMillis());
		this.setCurrentViewDataEnd(offlineDataLoadInfo.getEndMillis());
		return loadOnlineDatabaseData(offlineDataLoadInfo, loadObserver);
	}

	private boolean loadOnlineDatabaseData(OfflineDataLoadInfo offlineDataLoadInfo, ViewLoadObserver loadObserver) {
//		if (needViewerDataLoad(offlineDataLoadInfo) == false) {
//			return true;
//		}
		clearAll();
		return alfaServerLoader.loadALFAServerData(offlineDataLoadInfo.getStartMillis(), offlineDataLoadInfo.getEndMillis());
	}
	
	/**
	 * @return the alfaServerLoader
	 */
	public ALFAServerLoader getAlfaServerLoader() {
		return alfaServerLoader;
	}
	
	public List<Long> getImeiList() {
		return alfaServerLoader.getImeiList();
	}

	@Override
	public DataSelectorCreator getDataSelectCreator() {
		return sidsCreator;
	}

}
