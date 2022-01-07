package Acquisition.offlineFuncs;

import PamController.OfflineDataStore;
import PamguardMVC.PamDataBlock;
import dataMap.OfflineDataMap;
import dataMap.filemaps.FileDataMapPoint;

public class WavFileDataMap extends OfflineDataMap<FileDataMapPoint> {

	public WavFileDataMap(OfflineDataStore offlineDataStore,
			PamDataBlock parentDataBlock) {
		super(offlineDataStore, parentDataBlock);
		
	}

}
